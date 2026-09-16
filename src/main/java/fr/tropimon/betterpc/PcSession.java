package fr.tropimon.betterpc;

import com.cobblemon.mod.common.api.storage.party.PartyPosition;
import com.cobblemon.mod.common.api.storage.pc.PCPosition;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.storage.*;
import com.cobblemon.mod.common.net.messages.server.storage.SwapPCPartyPokemonPacket;
import com.cobblemon.mod.common.net.messages.server.storage.party.*;
import com.cobblemon.mod.common.net.messages.server.storage.pc.*;
import com.cobblemon.mod.common.pokemon.Pokemon;
import java.util.*;
import net.minecraft.client.MinecraftClient;

final class PcSession {
  record Place(int box, int slot) {
    boolean party() {
      return box < 0;
    }
  }

  final PCGUI original;
  final ClientPC pc;
  final ClientParty party;
  final Object world, connection;
  final ReleaseBatch batch = new ReleaseBatch();
  private UUID transferId;
  private Place destination;
  private long sentAt;
  String message = "";

  PcSession(PCGUI original) {
    this.original = original;
    pc = original.getPc();
    party = original.getParty();
    var client = MinecraftClient.getInstance();
    world = client.world;
    connection = client.getNetworkHandler();
  }

  boolean valid() {
    var client = MinecraftClient.getInstance();
    var storage = CobblemonClient.INSTANCE.getStorage();
    return client.player != null
        && client.world == world
        && client.getNetworkHandler() == connection
        && storage.getPcStores().get(pc.getUuid()) == pc
        && storage.getParty() == party;
  }

  boolean busy() {
    return transferId != null || batch.running() || PcTeamBuilder.busy(this);
  }

  Pokemon at(Place place) {
    if (place == null) return null;
    if (place.party())
      return place.slot >= 0 && place.slot < party.getSlots().size() ? party.get(place.slot) : null;
    if (place.box < 0 || place.box >= pc.getBoxes().size()) return null;
    var slots = pc.getBoxes().get(place.box).getSlots();
    return place.slot >= 0 && place.slot < slots.size() ? slots.get(place.slot) : null;
  }

  Place locate(UUID id) {
    if (id == null) return null;
    Pokemon pokemon = pc.findByUUID(id);
    if (pokemon != null) {
      var position = pc.getPosition(pokemon);
      if (position != null) return new Place(position.getBox(), position.getSlot());
    }

    pokemon = party.findByUUID(id);
    if (pokemon != null) {
      for (int slot = 0; slot < party.getSlots().size(); slot++)
        if (party.get(slot) != null && id.equals(party.get(slot).getUuid()))
          return new Place(-1, slot);
    }

    return null;
  }

  Place emptyParty() {
    for (int i = 0; i < Math.min(6, party.getSlots().size()); i++)
      if (party.get(i) == null) return new Place(-1, i);
    return null;
  }

  Place emptyPc(int box) {
    for (int b = 0; b < pc.getBoxes().size(); b++) {
      if (box >= 0 && b != box) continue;
      var slots = pc.getBoxes().get(b).getSlots();
      for (int s = 0; s < slots.size(); s++) if (slots.get(s) == null) return new Place(b, s);
    }

    return null;
  }

  boolean matches(ReleaseBatch.Target target) {
    Pokemon p = at(new Place(target.box(), target.slot()));
    return valid()
        && p != null
        && p.getUuid().equals(target.id())
        && PcPokemon.fingerprint(p).equals(target.fingerprint())
        && !BetterPcClient.preferences()
            .protectedFromRelease(
                p.getUuid(),
                p.getShiny(),
                PcPokemon.marked(p),
                !p.heldItemNoCopy$common().isEmpty())
        && original.getConfiguration().getCanSelect().invoke(p);
  }

  void tick() {
    if (!valid()) {
      batch.cancel();
      transferId = null;
      return;
    }

    if (transferId != null) {
      Pokemon p = at(destination);
      if (p != null && p.getUuid().equals(transferId)) {
        transferId = null;
        message = PcLang.tr("transfert_confirme");
      } else if (System.nanoTime() - sentAt >= 5_000_000_000L) {
        transferId = null;
        message = PcLang.tr("transfert_non_confirme_verifiez_le_stockage");
      }
    }

    batch.tick(
        System.nanoTime() / 1_000_000L,
        valid(),
        this::matches,
        id -> pc.findByUUID(id) == null && party.findByUUID(id) == null,
        target ->
            new ReleasePCPokemonPacket(target.id(), new PCPosition(target.box(), target.slot()))
                .sendToServer());
  }

  boolean transfer(UUID id, Place expectedSource, Place to) {
    if (!valid()
        || transferId != null
        || batch.running()
        || PcTeamBuilder.busy(this)
        || to == null
        || !Objects.equals(locate(id), expectedSource)) return false;
    Place from = expectedSource;
    Pokemon source = at(from);
    Pokemon target = at(to);
    if (source == null
        || from.equals(to)
        || !original.getConfiguration().getCanSelect().invoke(source)
        || target != null && !original.getConfiguration().getCanSelect().invoke(target))
      return false;
    // Depositing a party member must never displace a stored Pokémon.
    if (from.party() && !to.party() && target != null) return false;
    if (from.party()
        && !to.party()
        && target == null
        && party.getSlots().stream().filter(Objects::nonNull).count() <= 1) {
      message = PcLang.tr("gardez_au_moins_un_pokemon_dans_l_equipe");
      return false;
    }

    if (from.party() && to.party()) {
      if (target == null)
        new MovePartyPokemonPacket(id, new PartyPosition(from.slot), new PartyPosition(to.slot))
            .sendToServer();
      else
        new SwapPartyPokemonPacket(
                id, new PartyPosition(from.slot), target.getUuid(), new PartyPosition(to.slot))
            .sendToServer();
    } else if (!from.party() && !to.party()) {
      if (target == null)
        new MovePCPokemonPacket(
                id, new PCPosition(from.box, from.slot), new PCPosition(to.box, to.slot))
            .sendToServer();
      else
        new SwapPCPokemonPacket(
                id,
                new PCPosition(from.box, from.slot),
                target.getUuid(),
                new PCPosition(to.box, to.slot))
            .sendToServer();
    } else if (from.party()) {
      new MovePartyPokemonToPCPacket(
              id, new PartyPosition(from.slot), new PCPosition(to.box, to.slot))
          .sendToServer();
    } else {
      if (target == null)
        new MovePCPokemonToPartyPacket(
                id, new PCPosition(from.box, from.slot), new PartyPosition(to.slot))
            .sendToServer();
      else
        new SwapPCPartyPokemonPacket(
                target.getUuid(),
                new PartyPosition(to.slot),
                id,
                new PCPosition(from.box, from.slot))
            .sendToServer();
    }

    transferId = id;
    destination = to;
    sentAt = System.nanoTime();
    message = PcLang.tr("transfert_en_attente_du_serveur");
    return true;
  }
}
