package fr.tropimon.betterpc;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonBlocks;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.storage.party.PartyPosition;
import com.cobblemon.mod.common.api.storage.pc.PCPosition;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.net.storage.pc.ClosePCHandler;
import com.cobblemon.mod.common.net.messages.client.storage.pc.ClosePCPacket;
import fr.tropimon.betterpc.smokemixin.SmokeMouseAccess;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.LevelInfo;

/** Uses only a newly created offline world and synthetic Pokémon. Never touches a real server. */
public final class BetterPcSmoke implements ClientModInitializer {
  private int tick, stage = -1;
  private long started;
  private BlockPos pcPos;
  private UUID movedId, untouchedId, swappedId;
  private Set<UUID> dragSelection, collectionBeforeTransfer;
  private Vec3d stationary;
  private List<ReleaseBatch.Target> targets;
  private BetterPcScreen screen;
  private PcSession session;
  private boolean integrationChecked;
  private boolean collectionChecked;

  @Override
  public void onInitializeClient() {
    if (!Boolean.getBoolean("betterpc.smoke")) return;
    started = System.nanoTime();
    ClientTickEvents.END_CLIENT_TICK.register(
        client -> {
          try {
            if (stage == 9) return;
            if (System.nanoTime() - started > 240_000_000_000L)
              throw new AssertionError("Offline smoke timeout at stage " + stage);
            if (client.getOverlay() != null || ++tick < 40) return;
            switch (stage) {
              case -1 -> {
                stage = 0;
                tick = 0;
                client.options.getViewDistance().setValue(2);
                client.options.getGuiScale().setValue(2);
                client.options.pauseOnLostFocus = false;
                client
                    .getTutorialManager()
                    .setStep(net.minecraft.client.tutorial.TutorialStep.NONE);
                client
                    .createIntegratedServerLoader()
                    .createAndStart(
                        "better-pc-smoke-" + System.currentTimeMillis(),
                        new LevelInfo(
                            "Better PC verification",
                            GameMode.CREATIVE,
                            false,
                            Difficulty.PEACEFUL,
                            true,
                            new GameRules(),
                            DataConfiguration.SAFE_MODE),
                        new GeneratorOptions(1L, false, false),
                        registries ->
                            registries
                                .get(RegistryKeys.WORLD_PRESET)
                                .get(WorldPresets.FLAT)
                                .createDimensionsRegistryHolder(),
                        client.currentScreen);
              }

              case 0 -> {
                if (client.player == null
                    || CobblemonClient.INSTANCE.getStorage().getParty() == null) return;
                pcPos = client.player.getBlockPos().add(1, 0, 0);
                UUID playerId = client.player.getUuid();
                client
                    .getServer()
                    .submit(
                        () -> {
                          var player = client.getServer().getPlayerManager().getPlayer(playerId);
                          net.minecraft.block.Block block = CobblemonBlocks.PC;
                          var blockState = block.getDefaultState();
                          player.getServerWorld().setBlockState(pcPos, blockState, 3);
                          block.onPlaced(
                              player.getServerWorld(),
                              pcPos,
                              blockState,
                              player,
                              new net.minecraft.item.ItemStack(block));
                          var party = Cobblemon.INSTANCE.getStorage().getParty(player);
                          String[] species = {
                            "pikachu",
                            "eevee",
                            "gengar",
                            "dragonite",
                            "charizard",
                            "lapras",
                            "bulbasaur",
                            "squirtle",
                            "charmander",
                            "ditto",
                            "ralts",
                            "gardevoir",
                            "lucario",
                            "garchomp",
                            "snorlax",
                            "abra"
                          };
                          for (int i = 0; i < 3; i++)
                            party.set(
                                new PartyPosition(i),
                                PokemonProperties.Companion.parse(species[i] + " level=50")
                                    .create(player));
                          var pc = Cobblemon.INSTANCE.getStorage().getPC(player);
                          pc.resize(
                              3,
                              true,
                              pokemon -> {
                                throw new AssertionError("Empty fixture cannot evict Pokémon");
                              });
                          species[10] = "cubone";
                          species[12] = "cubone";
                          for (int i = 0; i < species.length; i++) {
                            var pokemon =
                                PokemonProperties.Companion.parse(
                                        species[i]
                                            + " level="
                                            + (10 + i * 5)
                                            + (i == 1 ? " shiny=true" : " shiny=false")
                                            + (i == 0 || i == 10 ? " ability=lightningrod" : ""))
                                    .create(player);
                            pokemon.setOriginalTrainer(playerId);
                            if (i == 10) {
                              var mark = com.cobblemon.mod.common.api.mark.Marks.all().getFirst();
                              pokemon.setMarks(new java.util.HashSet<>(java.util.List.of(mark)));
                              pokemon.setActiveMark(mark);
                            }
                            if (i == 2) pokemon.setMarkings(java.util.List.of(1));
                            if (i == 0) {
                              pokemon.setHeldItem$common(
                                  new net.minecraft.item.ItemStack(
                                      net.minecraft.item.Items.DIAMOND));
                              pokemon.setGender(com.cobblemon.mod.common.pokemon.Gender.MALE);
                              pokemon.setNature(
                                  com.cobblemon.mod.common.api.pokemon.Natures.ADAMANT);
                              pokemon.setMintedNature(
                                  com.cobblemon.mod.common.api.pokemon.Natures.MODEST);
                              int[] ivs = {31, 0, 10, 11, 21, 30}, evs = {252, 0, 0, 4, 0, 252};
                              for (int stat = 0; stat < 6; stat++) {
                                pokemon.getIvs().set(PcPokemon.STATS[stat], ivs[stat]);
                                pokemon.getEvs().set(PcPokemon.STATS[stat], evs[stat]);
                              }
                            }

                            if (i == 1)
                              pokemon.setGender(com.cobblemon.mod.common.pokemon.Gender.FEMALE);
                            pc.set(new PCPosition(0, i), pokemon);
                          }

                          pc.sendTo(player);
                        })
                    .get();
                stage = 1;
                tick = 0;
              }

              case 1 -> {
                check(
                    client.world.getBlockState(pcPos).isOf(CobblemonBlocks.PC),
                    "physical PC exists");
                client.setScreen(null);
                client.interactionManager.interactBlock(
                    client.player,
                    Hand.MAIN_HAND,
                    new BlockHitResult(Vec3d.ofCenter(pcPos), Direction.UP, pcPos, false));
                stage = 2;
                tick = 0;
              }

              case 2 -> {
                if (!(client.currentScreen instanceof BetterPcScreen current)) return;
                screen = current;
                client.getToastManager().clear();
                session = (PcSession) field(screen, "session");
                screen.loadFilters(PcPreferences.Filters.empty());
                check(session.valid(), "official PC session is valid");
                check(
                    session.pc.getBoxes().getFirst().getSlots().stream()
                            .filter(Objects::nonNull)
                            .count()
                        == 16,
                    "all PC Pokémon synchronized");
                var pikachu =
                    PcPokemon.of(session.pc.getBoxes().getFirst().getSlots().getFirst(), 0, 0);
                check(
                    pikachu.matches(
                        PcFilter.parse("", "", "", "", "", false, 0, 0, "electric", -1),
                        System.currentTimeMillis()),
                    "official type names match the filter");
                System.out.println(
                    "CAPACITY_CHECK: slots="
                        + screen.capacity
                        + " used="
                        + screen.usedBoxes
                        + " boxes="
                        + session.pc.getBoxes().size());
                check(
                    screen.capacity == 90
                        && screen.usedBoxes == 1
                        && session.pc.getBoxes().size() == 3,
                    "capacity follows three server-assigned boxes");
                check(
                    PcPokemon.hidden(pikachu.pokemon()),
                    "hidden ability identified from active form");
                check(
                    !PcPokemon.hidden(screen.all.get(10).pokemon()),
                    "second normal ability is not a hidden ability");
                check(
                    PcPokemon.stat(pikachu.pokemon(), 0, false) == 31
                        && PcPokemon.stat(pikachu.pokemon(), 5, true) == 252,
                    "IV and EV values synchronized");
                check(
                    pikachu.pokemon().getEffectiveNature().getName().getPath().equals("modest")
                        && pikachu.pokemon().getNature().getName().getPath().equals("adamant")
                        && PcPokemon.natureLabel(pikachu.pokemon())
                            .equals(PcPokemon.nature(pikachu.pokemon())),
                    "effective minted nature is displayed without a suffix or overwriting original"
                        + " nature");
                check(
                    PcPokemon.natureEffect(pikachu.pokemon(), 1) == -1
                        && PcPokemon.natureEffect(pikachu.pokemon(), 3) == 1
                        && PcPokemon.natureEffect(pikachu.pokemon(), 0) == 0,
                    "nature arrows use the mint rather than the original nature");
                screen.nature = "modest";
                screen.rebuild();
                check(
                    screen.visible.stream()
                        .anyMatch(e -> e.pokemon().getUuid().equals(pikachu.pokemon().getUuid())),
                    "nature filter uses the minted effective nature");
                screen.loadFilters(PcPreferences.Filters.empty());
                var searchInput =
                    (net.minecraft.client.gui.widget.TextFieldWidget)
                        ((List<?>) field(screen, "fields")).getFirst();
                searchInput.setText("Pikachu");
                click(47, 665);
                ((net.minecraft.client.gui.screen.Screen) screen).charTyped('é', 0);
                check(
                    searchInput.getText().equals("éPikachu"),
                    "Unicode input and mouse caret agree");
                screen.loadFilters(
                    new PcPreferences.Filters(
                        "", "99", "100", "100", "", false, 0, 0, "", "", "modest", "", 6, false, 0,
                        ""));
                check(
                    screen.visible.size() == 16,
                    "obsolete IV percentage and removed nature filter ignored in saved searches");
                check(
                    BetterPcScreen.CW == 210
                        && BetterPcScreen.CH == 162
                        && BetterPcScreen.PAGE == 15
                        && BetterPcScreen.COLS == 5,
                    "original card size retained with five columns");
                check(
                    PcPokemon.ivColor(31) == 0xFFFFD84D && PcPokemon.ivColor(0) == 0xFFFF6666,
                    "IV color boundaries");
                check(
                    ((List<?>) field(screen, "fields")).size() == 2,
                    "only search and time inputs remain");
                boolean french =
                    System.getProperty("betterpc.smoke.language", "fr_fr").equals("fr_fr");
                check(
                    PcLang.tr("nom").equals(french ? "Nom" : "Name"),
                    "UI follows active game language");
                check(
                    screen
                        .all
                        .get(10)
                        .pokemon()
                        .getDisplayName(false)
                        .getString()
                        .equals(french ? "Osselait" : "Cubone"),
                    "Cobblemon species follows active game language");
                check(
                    !pikachu.pokemon().heldItemNoCopy$common().isEmpty(),
                    "held item synchronized for preview");
                verifyFilters(client);
                verifyCaptureAndAbility(client);
                verifyTags();
                verifyReleaseProtections();
                click(80, 692);
                click(screen.menu.x + 10, screen.menu.y + 2 * 24 + 10);
                check(screen.visible.size() == 2, "type dropdown applies fire filter");
                click(900, 666);
                ((net.minecraft.client.gui.widget.TextFieldWidget) field(screen, "presetName"))
                    .setText("Fire test");
                click(600, 397);
                check(screen.prefs.presets.containsKey("Fire test"), "preset saved through UI");
                click(80, 770);
                check(screen.visible.size() == 16, "reset restores collection");
                click(700, 666);
                click(screen.menu.x + 10, screen.menu.y + 10);
                check(screen.visible.size() == 2, "saved preset restores filters");
                click(300, 770);
                click(screen.menu.x + 10, screen.menu.y + 10);
                check(
                    !screen.prefs.presets.containsKey("Fire test"),
                    "direct delete search action removes chosen preset");
                check(screen.visible.size() == 2, "deleting preset does not change active filters");
                check(
                    !PcPreferences.load((java.nio.file.Path) field(screen.prefs, "file"))
                        .presets
                        .containsKey("Fire test"),
                    "deleted search does not return on reload");
                click(80, 770);
                verifyAutocomplete(client);
                click(300, 692);
                ((net.minecraft.client.gui.screen.Screen) screen)
                    .keyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN, 0, 0);
                ((net.minecraft.client.gui.screen.Screen) screen)
                    .keyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER, 0, 0);
                check(
                    screen.visible.size() == 1
                        && PcPokemon.hidden(screen.visible.getFirst().pokemon()),
                    "hidden-ability dropdown filters actual ability");
                click(80, 770);
                click(64, 150);
                hover(client, 10, 10);
                stationary = client.player.getPos();
                client.options.forwardKey.setPressed(true);
                client.options.jumpKey.setPressed(true);
                stage = 3;
                tick = 0;
              }

              case 3 -> {
                check(
                    Math.hypot(
                                client.player.getX() - stationary.x,
                                client.player.getZ() - stationary.z)
                            < 0.02
                        && client.player.input.movementForward == 0
                        && !client.player.input.jumping,
                    "movement and jump blocked while PC is open with keys held");
                client.options.forwardKey.setPressed(false);
                client.options.jumpKey.setPressed(false);
                screenshot(client, "collection.png");
                if (!collectionChecked) {
                  click(300, 692);
                  stage = 20;
                  tick = 0;
                  break;
                }

                if (PcTeamBuilder.available() && !integrationChecked) {
                  screen.searchText = "pikachu";
                  screen.rebuild();
                  click(800, 30);
                  check(
                      client.currentScreen.getClass().getName().endsWith("TeamManagerScreen"),
                      "optional TeamBuilder button opens real installed screen");
                  stationary = client.player.getPos();
                  client.options.forwardKey.setPressed(true);
                  stage = 12;
                  tick = 0;
                  break;
                }

                click(300, 744);
                check(screen.selected.size() == 15, "select this page only");
                scrollPage(-1);
                check(
                    screen.page == 1 && screen.selected.size() == 15,
                    "scroll keeps earlier selection without selecting new page");
                click(300, 744);
                check(screen.selected.size() == 16, "this page adds the final partial page");
                click(300, 744);
                check(screen.selected.size() == 16, "repeating this page keeps unique selection");
                scrollPage(1);
                check(
                    screen.page == 0 && screen.selected.size() == 16,
                    "returning to previous page preserves both pages");
                click(500, 770);
                check(screen.selected.isEmpty(), "clear selection clears all selected pages");
                click(300, 744);
                scrollPage(-1);
                click(300, 744);
                scrollPage(1);
                click(224, 122); // protect first card with its favorite heart
                check(
                    screen.prefs.favorites.contains(screen.all.getFirst().pokemon().getUuid()),
                    "favorite toggled from card");
                stage = 10;
                tick = 0;
              }

              case 10 -> {
                screenshot(client, "selection.png");
                hover(client, BetterPcScreen.GX + 126, BetterPcScreen.GY + 65);
                click(BetterPcScreen.GX + 126, BetterPcScreen.GY + 65);
                check(
                    screen.movesPreview != null && screen.selected.size() == 16,
                    "move popup pins without changing the multiple selection");
                hover(client, 10, 10);
                stage = 11;
                tick = 0;
              }

              case 11 -> {
                screenshot(client, "moves.png");
                click(10, 10);
                check(screen.movesPreview == null, "outside click dismisses move popup");
                hover(client, 10, 10);
                click(930, 770);
                check(
                    ((List<?>) field(screen, "confirmation")).size() == 12,
                    "confirmation excludes shiny, favorite, item holder and markings");
                stage = 4;
                tick = 0;
              }

              case 4 -> {
                screenshot(client, "confirmation.png");
                click(350, 547); // cancel without sending
                check(
                    field(screen, "confirmation") == null, "cancel leaves no pending confirmation");
                check(
                    session.pc.getBoxes().getFirst().getSlots().stream()
                            .filter(Objects::nonNull)
                            .count()
                        == 16,
                    "cancel preserved every Pokémon");
                var first = session.pc.getBoxes().getFirst().getSlots().getFirst();
                movedId = first.getUuid();
                collectionBeforeTransfer = new HashSet<>();
                for (var e : screen.all) collectionBeforeTransfer.add(e.pokemon().getUuid());
                for (var p : session.party.getSlots())
                  if (p != null) collectionBeforeTransfer.add(p.getUuid());
                dragSelection = Set.copyOf(screen.selected);
                startDrag(64, 152, 10, 10);
                check(
                    screen.selected.equals(dragSelection) && screen.multiple,
                    "starting drag preserves the entire multiple selection");
                releaseAt(10, 10);
                check(screen.selected.equals(dragSelection), "cancelled drag preserves selection");
                // Exercise the real drag path to the first empty party slot.
                startDrag(64, 152, 24 + 3 * 180 + 70, 82);
                hover(client, 24 + 3 * 180 + 70, 82);
                check(screen.dragging, "drag keeps the Pokemon attached to the cursor");
                check(
                    screen.selected.equals(dragSelection), "drag keeps selected cards highlighted");
                stage = 18;
                tick = 0;
              }

              case 18 -> {
                screenshot(client, "drag-to-party.png");
                releaseAt(24 + 3 * 180 + 70, 82);
                check(session.busy(), "drag from PC requests transfer to party");
                stage = 5;
                tick = 0;
              }
              case 5 -> {
                if (session.busy() && tick < 160) return;
                System.out.println(
                    "TRANSFER_CHECK: active="
                        + (client.currentScreen == screen)
                        + " pc="
                        + (session.pc.findByUUID(movedId) != null)
                        + " party="
                        + (session.party.findByUUID(movedId) != null)
                        + " status="
                        + session.message);
                check(!session.busy(), "transfer settled");
                check(session.party.findByUUID(movedId) != null, "server accepted PC to party");
                check(
                    screen.selected.size() == dragSelection.size() - 1
                        && !screen.selected.contains(movedId),
                    "transfer only removes the Pokemon that left the PC selection");
                untouchedId = screen.visible.getFirst().pokemon().getUuid();
                var occupied = session.locate(untouchedId);
                check(
                    !session.transfer(movedId, session.locate(movedId), occupied)
                        && !session.busy(),
                    "party to occupied PC cannot send a swap packet");
                dragSelection = Set.copyOf(screen.selected);
                startDrag(24 + 3 * 180 + 70, 82, 64, 152);
                check(screen.selected.equals(dragSelection), "party drag preserves PC selection");
                releaseAt(64, 152);
                check(session.busy(), "party drop over occupied PC chooses a free slot");
                stage = 23;
                tick = 0;
              }

              case 23 -> {
                if (session.busy() && tick < 160) return;
                check(
                    session.locate(movedId).equals(new PcSession.Place(0, 0)),
                    "party member deposited into first empty PC slot");
                check(
                    session.locate(untouchedId).equals(new PcSession.Place(0, 1)),
                    "occupied PC card is not swapped out by party deposit");
                check(
                    session.party.get(3) == null && screen.selected.equals(dragSelection),
                    "party deposit leaves team slot empty and keeps PC selection");
                swappedId = session.party.get(0).getUuid();
                drag(64, 152, 94, 82, false);
                check(session.busy(), "PC drop on occupied party still requests swap");
                stage = 24;
                tick = 0;
              }

              case 24 -> {
                if (session.busy() && tick < 160) return;
                check(
                    session.locate(movedId).equals(new PcSession.Place(-1, 0))
                        && session.locate(swappedId).equals(new PcSession.Place(0, 0)),
                    "server confirms PC to party swap in both directions");
                check(
                    screen.selected.equals(dragSelection), "PC swap preserves unrelated selection");
                // The explicit box drop keeps its original destination behavior.
                drag(94, 82, 60, 770, true);
                check(session.busy(), "drag from party requests transfer to chosen box");
                stage = 6;
                tick = 0;
              }

              case 6 -> {
                check(!session.busy(), "return settled");
                check(session.pc.findByUUID(movedId) != null, "server accepted party to PC");
                check(
                    session.locate(movedId).box() == 1 && screen.usedBoxes == 2,
                    "box drop and occupied-box count updated");
                screen.loadFilters(PcPreferences.Filters.empty());
                for (int page = 0; page < screen.pages(); page++) {
                  screen.page = page;
                  click(300, 744);
                }
                click(930, 770);
                check(
                    screen.confirmation != null && screen.confirmation.size() > 10,
                    "bulk release review covers multiple selected pages");
                targets = List.copyOf(screen.confirmation);
                check(
                    targets.stream()
                        .noneMatch(
                            t ->
                                screen.prefs.protectedFromRelease(
                                    t.id(),
                                    session.pc.findByUUID(t.id()).getShiny(),
                                    PcPokemon.marked(session.pc.findByUUID(t.id())),
                                    !session
                                        .pc
                                        .findByUUID(t.id())
                                        .heldItemNoCopy$common()
                                        .isEmpty())),
                    "bulk review excludes every protected Pokemon");
                click(650, 547);
                stage = 7;
                tick = 0;
              }

              case 20 -> {
                screenshot(client, "ability-search.png");
                ((net.minecraft.client.gui.screen.Screen) screen)
                    .keyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE, 0, 0);
                verifySpecies();
                click(80, 718);
                click(screen.menu.x + 10, screen.menu.y + 24 + 10);
                check(
                    screen.species.equals("@duplicates") && screen.visible.size() == 2,
                    "Species groups duplicate species and forms");
                check(
                    screen.focusId == null,
                    "hidden Pokémon cannot remain targeted by collection actions");
                screen.multiple = true;
                screen.selected.addAll(
                    screen.visible.stream().map(p -> p.pokemon().getUuid()).toList());
                click(700, 744);
                check(
                    screen.tools.dialog == PcTools.Dialog.COMPARE,
                    "two selected Pokémon open comparison");
                stage = 14;
                tick = 0;
              }

              case 14 -> {
                screenshot(client, "comparison.png");
                hover(client, 503, 289);
                stage = 19;
                tick = 0;
              }

              case 19 -> {
                screenshot(client, "comparison-marks.png");
                var marked =
                    screen.visible.stream()
                        .map(PcPokemon::pokemon)
                        .filter(p -> !p.getMarks().isEmpty())
                        .findFirst()
                        .orElseThrow();
                check(
                    PcMarks.descriptions(marked).size() == 2,
                    "owned mark name and description use translated Cobblemon accessors");
                var plain =
                    screen.visible.stream()
                        .map(PcPokemon::pokemon)
                        .filter(p -> p.getMarks().isEmpty())
                        .findFirst()
                        .orElseThrow();
                check(
                    PcMarks.descriptions(plain).isEmpty(), "markless Pokemon have no mark tooltip");
                hover(client, 503, 289);

                UUID id = screen.selected.iterator().next();
                click(528, 264);
                check(
                    screen.prefs.favorites.contains(id),
                    "comparison can protect an exemplar as favourite");
                click(528, 264);
                click(400, 606);
                click(300, 482);
                check((screen.prefs.tags(id) & 1) != 0, "comparison can add a tag to one column");
                click(400, 606);
                click(300, 482);
                check((screen.prefs.tags(id) & 1) == 0, "comparison can remove that tag");
                click(400, 606);
                ((net.minecraft.client.gui.screen.Screen) screen)
                    .keyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE, 0, 0);
                check(screen.tools.modal(), "Escape closes tag menu before comparison");
                ((net.minecraft.client.gui.screen.Screen) screen)
                    .keyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE, 0, 0);
                check(
                    screen.species.equals("@duplicates")
                        && screen.tools.dialog == PcTools.Dialog.NONE,
                    "comparison closes back to species results");
                stage = 17;
                tick = 0;
              }

              case 17 -> {
                screenshot(client, "species.png");
                hover(client, 1088, 32);
                click(80, 770);
                collectionChecked = true;
                stationary = client.player.getPos();
                client.options.forwardKey.setPressed(true);
                client.options.jumpKey.setPressed(true);
                stage = 3;
                tick = 0;
              }

              case 12 -> {
                check(
                    Math.hypot(
                            client.player.getX() - stationary.x,
                            client.player.getZ() - stationary.z)
                        < 0.02,
                    "movement stays blocked inside TeamBuilder");
                screenshot(client, "teambuilder.png");
                client.currentScreen.keyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE, 0, 0);
                check(
                    client.currentScreen == screen
                        && screen.searchText.equals("pikachu")
                        && screen.visible.size() == 1,
                    "Escape returns to the same Better PC with its filters");
                check(session.valid(), "PC session survives TeamBuilder round trip");
                click(800, 30);
                stage = 13;
                tick = 0;
              }

              case 13 -> {
                var back = client.currentScreen.getClass().getDeclaredMethod("openBoxes");
                back.setAccessible(true);
                back.invoke(client.currentScreen);
                check(
                    client.currentScreen == screen,
                    "TeamBuilder PC button returns directly to Better PC");
                screen.loadFilters(PcPreferences.Filters.empty());
                integrationChecked = true;
                stationary = client.player.getPos();
                client.options.jumpKey.setPressed(true);
                stage = 3;
                tick = 0;
              }

              case 7 -> {
                if (session.batch.running() && tick < 1000) return;
                check(
                    session.batch.result() == ReleaseBatch.Result.DONE,
                    "batch completed with real server updates");
                check(
                    session.batch.completed() == targets.size(),
                    "exactly the reviewed bulk releases confirmed");
                for (var target : targets)
                  check(session.pc.findByUUID(target.id()) == null, "released target absent");
                var remaining = new HashSet<UUID>();
                for (var e : screen.all) remaining.add(e.pokemon().getUuid());
                for (var p : session.party.getSlots()) if (p != null) remaining.add(p.getUuid());
                for (var target : targets) collectionBeforeTransfer.remove(target.id());
                check(
                    remaining.equals(collectionBeforeTransfer),
                    "every other Pokemon preserved by UUID");
                stage = 22;
                tick = 0;
              }

              case 22 -> {
                if (PcTeamBuilder.available()) {
                  click(800, 30);
                  check(
                      client.currentScreen.getClass().getName().endsWith("TeamManagerScreen"),
                      "reopen TeamBuilder after storage operations");
                  var builder = client.currentScreen;
                  ClosePCHandler.INSTANCE.handle(new ClosePCPacket(UUID.randomUUID()), client);
                  check(
                      client.currentScreen == builder,
                      "unrelated server close does not close TeamBuilder");
                  ClosePCHandler.INSTANCE.handle(new ClosePCPacket(screen.storeId()), client);
                  check(
                      client.currentScreen == null && !PcTeamBuilder.blocksMovement(),
                      "server close invalidates TeamBuilder return and movement guard");
                } else {
                  ClosePCHandler.INSTANCE.handle(new ClosePCPacket(UUID.randomUUID()), client);
                  check(client.currentScreen == screen, "unrelated close ignored");
                  ClosePCHandler.INSTANCE.handle(new ClosePCPacket(screen.storeId()), client);
                  check(client.currentScreen == null, "server close handled by replacement UI");
                }

                client.options.forwardKey.setPressed(true);
                stage = 8;
                tick = 0;
              }

              case 8 -> {
                check(
                    client.player.input.movementForward != 0, "movement restored after PC closes");
                client.options.forwardKey.setPressed(false);
                System.out.println(
                    "BETTER_PC_SMOKE_OK: physical PC interaction, UI rendered, cumulative page"
                        + " selection, cancel, bidirectional transfers, bulk server-confirmed"
                        + " releases, close isolation");
                client.scheduleStop();
                stage = 9;
              }
            }

          } catch (Throwable error) {
            error.printStackTrace();
            System.out.println("BETTER_PC_SMOKE_FAILED stage=" + stage);
            client.scheduleStop();
            stage = 9;
          }
        });
  }

  private void verifyCaptureAndAbility(MinecraftClient client) throws Exception {
    var own = screen.all.get(0).pokemon();
    var foreign = screen.all.get(1).pokemon();
    var unknown = screen.all.get(2).pokemon();
    own.setOriginalTrainer(client.player.getUuid());
    foreign.setOriginalTrainer(UUID.randomUUID());
    unknown.removeOriginalTrainer();
    long now = System.currentTimeMillis();
    var historyField = BetterPcClient.class.getDeclaredField("history");
    historyField.setAccessible(true);
    var history = (DetectionHistory) historyField.get(null);
    history.restore(
        Map.of(own.getUuid(), now, foreign.getUuid(), now, unknown.getUuid(), now), now);
    screen.ageText = "1";
    screen.days = false;
    screen.rebuild();
    check(
        screen.visible.size() == 1 && screen.visible.getFirst().pokemon() == own,
        "recent filter excludes foreign and unknown OT, even with old recorded arrivals");
    history.restore(Map.of(own.getUuid(), now - 7200000), now);
    screen.applyFilters();
    check(screen.visible.isEmpty(), "own OT does not bypass the time limit");
    history.ready();
    var traded = PokemonProperties.Companion.parse("eevee").create();
    traded.setOriginalTrainer(UUID.randomUUID());
    BetterPcClient.arrived(null, traded);
    check(
        BetterPcClient.detectedAt(traded.getUuid()) == 0,
        "foreign OT arrivals are not recorded as recent captures");
    var captured = PokemonProperties.Companion.parse("pikachu").create();
    captured.setOriginalTrainer(client.player.getUuid());
    BetterPcClient.arrived(null, captured);
    check(
        BetterPcClient.detectedAt(captured.getUuid()) > 0,
        "own OT live arrival retains its local timestamp");
    foreign.setOriginalTrainer(client.player.getUuid());
    unknown.setOriginalTrainer(client.player.getUuid());
    screen.loadFilters(PcPreferences.Filters.empty());
    check(
        PcPokemon.hidden(own) && PcPokemon.abilityColor(own) == 0xFFFFD84D,
        "active hidden ability is yellow");
    check(
        !PcPokemon.hidden(unknown) && PcPokemon.abilityColor(unknown) != 0xFFFFD84D,
        "single-ability form is never marked as hidden or yellow");
    check(
        !PcPokemon.hidden(screen.all.get(10).pokemon()),
        "second normal ability keeps normal color");
  }

  private void startDrag(int fromX, int fromY, int toX, int toY) throws Exception {
    float scale = (float) field(screen, "scale");
    double ox = (double) field(screen, "offsetX"), oy = (double) field(screen, "offsetY");
    net.minecraft.client.gui.screen.Screen target = screen;
    target.mouseClicked(ox + fromX * scale, oy + fromY * scale, 0);
    target.mouseDragged(ox + toX * scale, oy + toY * scale, 0, 20, 20);
  }

  private void releaseAt(int x, int y) throws Exception {
    float scale = (float) field(screen, "scale");
    double ox = (double) field(screen, "offsetX"), oy = (double) field(screen, "offsetY");
    ((net.minecraft.client.gui.screen.Screen) screen)
        .mouseReleased(ox + x * scale, oy + y * scale, 0);
  }

  private void verifyTags() throws Exception {
    UUID first = screen.all.getFirst().pokemon().getUuid(),
        second = screen.all.get(1).pokemon().getUuid();
    click(64, 150);
    for (int i = 0; i < 4; i++) {
      click(500, 744);
      check(screen.menu != null, "tag editor opens for focused Pokemon");
      click(screen.menu.x + 10, screen.menu.y + i * 24 + 10);
      check((screen.prefs.tags(first) & (1 << i)) != 0, "tag added through menu: " + i);
    }
    check(screen.prefs.tags(first) == 15, "all four tags coexist");
    var settings = (java.nio.file.Path) field(screen.prefs, "file");
    check(PcPreferences.load(settings).tags(first) == 15, "tags persist for reconnect");
    screen.selected.add(second);
    click(500, 744);
    click(screen.menu.x + 10, screen.menu.y + 10);
    check(
        (screen.prefs.tags(first) & 1) != 0 && (screen.prefs.tags(second) & 1) != 0,
        "mixed selection adds tag to all selected Pokemon");
    click(500, 744);
    click(screen.menu.x + 10, screen.menu.y + 10);
    check(
        screen.prefs.tags(first) == 14 && screen.prefs.tags(second) == 0,
        "removing one tag from selection preserves other tags");
    screen.prefs.setTags(List.of(first), 1, true);
    click(500, 718);
    click(screen.menu.x + 10, screen.menu.y + 2 * 24 + 10);
    check(
        screen.tag == 1
            && screen.visible.size() == 1
            && screen.visible.getFirst().pokemon().getUuid().equals(first),
        "sale filter returns tagged Pokemon only");
    screen.type = "fire";
    screen.rebuild();
    check(screen.visible.isEmpty(), "tag filter combines with native type filter");
    screen.type = "";
    screen.rebuild();
    click(900, 666);
    screen.presetName.setText("Tagged test");
    click(600, 397);
    click(80, 770);
    check(screen.tag == 0, "reset clears tag filter without removing assigned tags");
    click(700, 666);
    click(screen.menu.x + 10, screen.menu.y + 10);
    check(screen.tag == 1 && screen.visible.size() == 1, "saved search restores tag filter");
    click(300, 770);
    click(screen.menu.x + 10, screen.menu.y + 10);
    click(64, 150);
    click(500, 744);
    click(screen.menu.x + 10, screen.menu.y + 4 * 24 + 10);
    check(
        screen.prefs.tags(first) == 0
            && screen.visible.isEmpty()
            && screen.selected.isEmpty()
            && screen.focusId == null,
        "remove all tags refreshes results and clears hidden selection");
    check(PcPreferences.load(settings).tags(first) == 0, "removed tags stay removed after reload");
    click(500, 718);
    click(screen.menu.x + 10, screen.menu.y + 24 + 10);
    check(
        screen.tag == -1 && screen.visible.size() == 16,
        "untagged filter finds all remaining Pokemon");
    click(80, 770);
    screen.prefs.setTags(List.of(first), 15, true);
  }

  private void verifyReleaseProtections() throws Exception {
    var prefs = screen.prefs;
    for (String kind : List.of("favorite", "shiny", "marked", "item", "unavailable")) {
      int slot =
          kind.equals("shiny") ? 1 : kind.equals("marked") ? 2 : kind.equals("item") ? 0 : 12;
      var pokemon = session.pc.getBoxes().getFirst().getSlots().get(slot);
      var target = PcPokemon.of(pokemon, 0, slot).target();
      prefs.excludeShiny = prefs.excludeMarked = prefs.excludeItems = false;
      screen.selected.clear();
      screen.selected.add(pokemon.getUuid());
      // The specimen has been selected before its exclusion is enabled.
      check(session.matches(target), "unprotected synthetic target initially eligible: " + kind);
      protect(prefs, pokemon.getUuid(), kind);
      click(930, 770);
      check(
          screen.confirmation == null && !session.batch.running(),
          "selected protected target excluded from review: " + kind);
      check(!session.matches(target), "official session rechecks active protection: " + kind);
      unprotect(prefs, pokemon.getUuid());
      click(930, 770);
      check(
          screen.confirmation != null && screen.confirmation.size() == 1,
          "unprotected fixture can reach reviewed confirmation: " + kind);
      protect(prefs, pokemon.getUuid(), kind);
      click(650, 547);
      check(
          !session.batch.running(),
          "protection activated after review blocks confirmation: " + kind);
      screen.confirmation = null;
      unprotect(prefs, pokemon.getUuid());
      session.batch.start(List.of(target));
      protect(prefs, pokemon.getUuid(), kind);
      session.tick();
      check(
          session.batch.result() == ReleaseBatch.Result.CHANGED,
          "protection activated after queuing blocks send: " + kind);
      check(
          session.pc.findByUUID(pokemon.getUuid()) != null,
          "protected fixture remains stored: " + kind);
      unprotect(prefs, pokemon.getUuid());
    }

    prefs.excludeShiny = prefs.excludeMarked = prefs.excludeItems = true;
    prefs.changed();
    screen.selected.clear();
    session.batch.start(List.of());
    screen.rebuild();
  }

  private static void protect(PcPreferences prefs, UUID id, String kind) {
    switch (kind) {
      case "favorite" -> prefs.favorites.add(id);
      case "shiny" -> prefs.excludeShiny = true;
      case "marked" -> prefs.excludeMarked = true;
      case "item" -> prefs.excludeItems = true;
      case "unavailable" -> prefs.available = false;
    }
  }

  private static void unprotect(PcPreferences prefs, UUID id) {
    prefs.available = true;
    prefs.favorites.remove(id);
    prefs.excludeShiny = prefs.excludeMarked = prefs.excludeItems = false;
  }

  private void verifyFilters(MinecraftClient client) {
    var pikachu = screen.all.getFirst().pokemon();
    screen.type = "electric";
    screen.gender = 1;
    screen.shiny = 2;
    screen.item = "minecraft:diamond";
    screen.nature = "modest";
    screen.ability = "@hidden";
    screen.rebuild();
    check(
        screen.visible.size() == 1 && screen.visible.getFirst().pokemon() == pikachu,
        "type gender shiny item nature and hidden ability combine");
    screen.item = "@none";
    screen.rebuild();
    check(screen.visible.isEmpty(), "conflicting filters yield no matches");
    screen.loadFilters(PcPreferences.Filters.empty());
    screen.type = "ice";
    screen.rebuild();
    check(
        screen.visible.stream().anyMatch(p -> p.pokemon().getSpecies().getName().equals("Lapras")),
        "type filter finds a secondary type");
    screen.loadFilters(PcPreferences.Filters.empty());
    screen.prefs.favorites.add(pikachu.getUuid());
    screen.favoritesOnly = true;
    screen.rebuild();
    check(
        screen.visible.size() == 1 && screen.visible.getFirst().pokemon() == pikachu,
        "favourites filter matches exact UUIDs");
    screen.prefs.favorites.remove(pikachu.getUuid());
    screen.loadFilters(PcPreferences.Filters.empty());
    screen.searchText = "filtermutation";
    screen.rebuild();
    check(screen.visible.isEmpty(), "new nickname absent before update");
    var original = pikachu.getNickname();
    var handler =
        new com.cobblemon.mod.common.client.net.pokemon.update.PokemonUpdatePacketHandler<
            com.cobblemon.mod.common.net.messages.client.pokemon.update.NicknameUpdatePacket>();
    handler.handle(
        new com.cobblemon.mod.common.net.messages.client.pokemon.update.NicknameUpdatePacket(
            () -> pikachu, net.minecraft.text.Text.literal("FilterMutation")),
        client);
    ((net.minecraft.client.gui.screen.Screen) screen).tick();
    check(
        screen.visible.size() == 1, "in-place server update refreshes search without reopening PC");
    handler.handle(
        new com.cobblemon.mod.common.net.messages.client.pokemon.update.NicknameUpdatePacket(
            () -> pikachu, original),
        client);
    screen.loadFilters(PcPreferences.Filters.empty());
  }

  private void verifyAutocomplete(MinecraftClient client) throws Exception {
    click(300, 692);
    check(screen.abilitySearch != null, "ability search opens its editable input");
    var target = (net.minecraft.client.gui.screen.Screen) screen;
    String query =
        PcPokemon.ability(screen.all.getFirst().pokemon()).toUpperCase(java.util.Locale.ROOT);
    for (char c : query.toCharArray()) target.charTyped(c, 0);
    check(screen.abilitySearch.matches.size() == 1, "localized ability autocomplete ignores case");
    target.keyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_TAB, 0, 0);
    check(
        screen.abilitySearch == null
            && screen.ability.equals("lightningrod")
            && screen.visible.size()
                == screen.all.stream()
                    .filter(e -> e.pokemon().getAbility().getName().equals("lightningrod"))
                    .count(),
        "Tab commits exact ability independently of species");
    click(300, 692);
    for (char c : "zznotanability".toCharArray()) target.charTyped(c, 0);
    check(screen.abilitySearch.matches.isEmpty(), "unknown ability query has no suggestions");
    target.keyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER, 0, 0);
    check(
        screen.ability.equals("lightningrod") && screen.abilitySearch != null,
        "empty autocomplete cannot silently clear or commit a filter");
    target.keyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE, 0, 0);
    check(
        client.currentScreen == screen && screen.abilitySearch == null,
        "Escape cancels autocomplete without closing PC");
    click(300, 692);
    screen.abilitySearch.input.setText("lightning");
    click(PcAbilitySearch.X + 30, PcAbilitySearch.Y + 66);
    check(
        screen.ability.equals("lightningrod") && screen.abilitySearch == null,
        "ability suggestion supports mouse selection and internal English IDs");
    click(80, 770);
  }

  private void verifySpecies() throws Exception {
    click(80, 718);
    click(screen.menu.x + 10, screen.menu.y + 2 * 24 + 10);
    check(
        screen.species.equals("@legendary") && screen.visible.isEmpty(),
        "legendary menu excludes ordinary collection species");
    for (String name :
        List.of("lugia", "rayquaza", "marshadow", "keldeo", "mew", "dragonite", "nihilego")) {
      var original = PokemonProperties.Companion.parse(name).create();
      var codec =
          new com.cobblemon.mod.common.net.messages.client.data.SpeciesRegistrySyncPacket(
              List.of(original.getSpecies()));
      var buffer =
          new net.minecraft.network.RegistryByteBuf(
              io.netty.buffer.Unpooled.buffer(),
              net.minecraft.client.MinecraftClient.getInstance().world.getRegistryManager());
      com.cobblemon.mod.common.pokemon.Species synced;
      try {
        codec.encodeEntry(buffer, original.getSpecies());
        synced = codec.decodeEntry(buffer);
      } finally {
        buffer.release();
      }
      check(synced != null, "species survives actual network codec: " + name);
      var remote = new com.cobblemon.mod.common.pokemon.Pokemon();
      remote.setSpecies(synced);
      boolean expected = !name.equals("dragonite") && !name.equals("nihilego");
      check(
          !remote.isLegendary() && !remote.isMythical(),
          "client registry lacks category labels: " + name);
      check(PcPokemon.legendary(remote) == expected, "multiplayer legendary filter: " + name);
      for (var form : synced.getForms()) {
        remote.setForm(form);
        check(
            PcPokemon.legendary(remote) == expected,
            "classification survives alternate form: " + name);
      }
      screen.all.add(PcPokemon.of(remote, 2, screen.all.size() - 16));
    }
    screen.applyFilters();
    check(
        screen.visible.size() == 5,
        "legendary filter includes legendary and mythical client species only");
    screen.searchText = "dragonite";
    screen.applyFilters();
    check(screen.visible.isEmpty(), "legendary filter combines with search");
    screen.searchText = "";
    screen.prefs.putPreset("Legendary test", screen.snapshot());
    var savedLegendary = PcPreferences.load((Path) field(screen.prefs, "file"));
    screen.loadFilters(savedLegendary.presets.get("Legendary test"));
    check(screen.species.equals("@legendary"), "saved search restores legendary category");
    screen.prefs.save();
    check(
        PcPreferences.load((Path) field(screen.prefs, "file"))
            .lastFilters
            .species()
            .equals("@legendary"),
        "last used filters saved independently of named searches");
    String accountKey =
        HexFormat.of()
            .formatHex(
                java.security.MessageDigest.getInstance("SHA-256")
                    .digest(
                        ("account\n"
                                + net.minecraft.client.MinecraftClient.getInstance()
                                    .getSession()
                                    .getUuidOrNull())
                            .getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    check(
        ((Path) field(screen.prefs, "file"))
            .getFileName()
            .toString()
            .equals(accountKey + "-preferences.json"),
        "all server and world preferences use the same account storage key");
    var reopened = new BetterPcScreen(session.original);
    check(
        reopened.species.equals("@legendary") && reopened.selected.isEmpty(),
        "new PC screen restores filters without restoring destructive selection");
    screen.prefs.deletePreset("Legendary test");
    screen.loadFilters(PcPreferences.Filters.empty());

    click(80, 718);
    click(screen.menu.x + 10, screen.menu.y + 24 + 10);
    check(
        screen.visible.size() == 2 && screen.species.equals("@duplicates"),
        "Species hides singletons and retains the duplicate group");
    var filters = screen.snapshot();
    screen.prefs.putPreset("Species test", filters);
    var reloaded = PcPreferences.load((Path) field(screen.prefs, "file"));
    check(
        reloaded.presets.get("Species test").species().equals("@duplicates"),
        "duplicate mode persists in saved searches");
    screen.loadFilters(reloaded.presets.get("Species test"));
    screen.type = "fire";
    screen.rebuild();
    check(screen.visible.isEmpty(), "duplicate grouping uses other active criteria");
    screen.prefs.deletePreset("Species test");
    screen.loadFilters(PcPreferences.Filters.empty());
    click(700, 666);
    check(
        screen.menu.entries.size() == 1
            && screen.menu.entries.getFirst().label().equals(PcLang.tr("no_saved_searches")),
        "the three built-in searches are removed");
    click(10, 10);
    // Calculated level-10 Pikachu stats include EVs, IVs and the effective mint nature.
    var p = screen.all.getFirst().pokemon();
    check(
        Math.abs(PcPokemon.ivPercent(p) - 100.0 * 103 / 186) < 0.00001,
        "IV rating is the raw sum divided by 186");
    var rating =
        com.cobblemon.mod.common.api.pokemon.PokemonProperties.Companion.parse("pikachu").create();
    for (var stat : PcPokemon.STATS) rating.setIV(stat, 0);
    check(PcPokemon.ivPercent(rating) == 0, "zero IVs score zero percent");
    for (var stat : PcPokemon.STATS) {
      rating.setIV(stat, 31);
      rating.setEV(stat, 0);
    }
    check(PcPokemon.ivPercent(rating) == 100, "six perfect IVs score 100 percent");
    rating.setEV(PcPokemon.STATS[0], 252);
    check(PcPokemon.ivPercent(rating) == 100, "EVs do not change the IV rating");
    int[] expected = {36, 14, 14, 17, 17, 32};
    for (int i = 0; i < 6; i++)
      check(
          p.getStat(PcPokemon.STATS[i]) == expected[i],
          "real stat follows Cobblemon at index " + i);
  }

  private void scrollPage(double amount) throws Exception {
    float scale = (float) field(screen, "scale");
    double ox = (double) field(screen, "offsetX"), oy = (double) field(screen, "offsetY");
    ((net.minecraft.client.gui.screen.Screen) screen)
        .mouseScrolled(ox + 100 * scale, oy + 200 * scale, 0, amount);
  }

  private void click(int x, int y) throws Exception {
    float scale = (float) field(screen, "scale");
    double ox = (double) field(screen, "offsetX"), oy = (double) field(screen, "offsetY");
    net.minecraft.client.gui.screen.Screen target = screen;
    target.mouseClicked(ox + x * scale, oy + y * scale, 0);
    target.mouseReleased(ox + x * scale, oy + y * scale, 0);
  }

  private void hover(MinecraftClient client, int x, int y) throws Exception {
    float scale = (float) field(screen, "scale");
    double ox = (double) field(screen, "offsetX"), oy = (double) field(screen, "offsetY");
    var window = client.getWindow();
    ((SmokeMouseAccess) client.mouse)
        .betterpcSmoke$cursor(
            window.getHandle(),
            (ox + x * scale) * window.getWidth() / window.getScaledWidth(),
            (oy + y * scale) * window.getHeight() / window.getScaledHeight());
  }

  private void drag(int fromX, int fromY, int toX, int toY, boolean viaBoxes) throws Exception {
    float scale = (float) field(screen, "scale");
    double ox = (double) field(screen, "offsetX"), oy = (double) field(screen, "offsetY");
    net.minecraft.client.gui.screen.Screen target = screen;
    target.mouseClicked(ox + fromX * scale, oy + fromY * scale, 0);
    if (viaBoxes) target.mouseDragged(ox + 60 * scale, oy + 720 * scale, 0, 20, 20);
    target.mouseDragged(ox + toX * scale, oy + toY * scale, 0, 20, 20);
    target.mouseReleased(ox + toX * scale, oy + toY * scale, 0);
  }

  private static Object field(Object instance, String name) throws Exception {
    var field = instance.getClass().getDeclaredField(name);
    field.setAccessible(true);
    return field.get(instance);
  }

  private static void screenshot(MinecraftClient client, String name) throws Exception {
    Path output = client.runDirectory.toPath().resolve("verification");
    Files.createDirectories(output);
    try (var image = ScreenshotRecorder.takeScreenshot(client.getFramebuffer())) {
      image.writeTo(output.resolve(name));
    }
  }

  private static void check(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
    System.out.println("BETTER_PC_CHECK: " + message);
  }
}
