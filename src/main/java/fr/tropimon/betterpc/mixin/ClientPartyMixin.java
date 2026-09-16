package fr.tropimon.betterpc.mixin;

import com.cobblemon.mod.common.api.storage.party.PartyPosition;
import com.cobblemon.mod.common.client.storage.ClientParty;
import com.cobblemon.mod.common.pokemon.Pokemon;
import fr.tropimon.betterpc.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientParty.class, remap = false)
abstract class ClientPartyMixin {
  @Inject(
      method =
          "set(Lcom/cobblemon/mod/common/api/storage/party/PartyPosition;Lcom/cobblemon/mod/common/pokemon/Pokemon;)V",
      at = @At("HEAD"))
  private void betterpc$observe(PartyPosition position, Pokemon pokemon, CallbackInfo ci) {
    ClientParty self = (ClientParty) (Object) this;
    BetterPcClient.arrived(self.get(position), pokemon);
  }
}
