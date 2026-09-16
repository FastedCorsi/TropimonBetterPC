package fr.tropimon.betterpc;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BetterPcClient implements ClientModInitializer {
  public static final Logger LOGGER = LoggerFactory.getLogger("tropimon_better_pc");
  private static DetectionHistory history;
  private static Path historyFile;
  private static Object network;
  private static PCGUI pending;
  private static long revision;
  private static int ticks;
  private static boolean readFailed;
  private static PcPreferences preferences = PcPreferences.load(null);

  static PcPreferences preferences() {
    return preferences;
  }

  @Override
  public void onInitializeClient() {
        TropimonSelfUpdater.start(LOGGER);
    ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ensureSession());
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
    ClientTickEvents.END_CLIENT_TICK.register(
        client -> {
          if (client.getNetworkHandler() == null) return;
          ensureSession();
          PcTeamBuilder.tick();
          if (pending != null) {
            PCGUI source = pending;
            pending = null;
            if (client.currentScreen == source
                && source.getConfiguration().getSelectOverride() == null)
              client.setScreen(new BetterPcScreen(source));
          }
          if (++ticks % 200 == 0) save();
        });
  }

  public static void opened(PCGUI screen) {
    ensureSession();
    baseline();
    pending = screen;
  }

  public static void changed() {
    revision++;
  }

  public static long revision() {
    return revision;
  }

  public static long detectedAt(UUID id) {
    return history == null ? 0 : history.time(id);
  }

  public static void arrived(Pokemon previous, Pokemon incoming) {
    ensureSession();
    if (history != null && incoming != null && !PcPokemon.ownOriginalTrainer(incoming))
      history.remember(incoming.getUuid());
    if (history != null)
      history.arrived(
          previous == null ? null : previous.getUuid(),
          incoming == null ? null : incoming.getUuid(),
          System.currentTimeMillis());
    changed();
  }

  public static void remember(Pokemon pokemon) {
    if (history != null && pokemon != null) history.remember(pokemon.getUuid());
  }

  public static void ready() {
    ensureSession();
    baseline();
    if (history != null) history.ready();
    changed();
  }

  public static void baseline() {
    ensureSession();
    var storage = CobblemonClient.INSTANCE.getStorage();
    var party = storage.getParty();
    if (party != null) for (Pokemon pokemon : party.getSlots()) remember(pokemon);
    for (var pc : storage.getPcStores().values())
      for (var box : pc.getBoxes()) for (Pokemon pokemon : box.getSlots()) remember(pokemon);
  }

  public static void reset() {
    PcTeamBuilder.reset();
    save();
    preferences.save();
    preferences = PcPreferences.load(null);
    history = null;
    historyFile = null;
    network = null;
    pending = null;
    readFailed = false;
    changed();
  }

  private static void ensureSession() {
    MinecraftClient client = MinecraftClient.getInstance();
    Object connection = client.getNetworkHandler();
    if (connection == null || connection == network) return;
    reset();
    network = connection;
    history = new DetectionHistory();
    String server = null;
    if (client.getCurrentServerEntry() != null) server = client.getCurrentServerEntry().address;
    else if (client.getServer() != null)
      server =
          client
              .getServer()
              .getSavePath(WorldSavePath.ROOT)
              .toAbsolutePath()
              .normalize()
              .toString();
    try {
      UUID account = client.getSession().getUuidOrNull();
      if (account == null) throw new IllegalStateException("Account unavailable");
      String identity = "account\n" + account;
      String hash =
          HexFormat.of()
              .formatHex(
                  MessageDigest.getInstance("SHA-256")
                      .digest(identity.getBytes(StandardCharsets.UTF_8)));
      historyFile =
          FabricLoader.getInstance()
              .getConfigDir()
              .resolve("tropimon-better-pc")
              .resolve(hash + ".json");
      Path settings = historyFile.resolveSibling(hash + "-preferences.json");
      boolean firstImport = !Files.exists(settings);
      preferences = PcPreferences.load(settings);
      Path legacy = null;
      // Server identity is used only to locate legacy files, never for current storage.
      if (server != null) {
        String legacyHash =
            HexFormat.of()
                .formatHex(
                    MessageDigest.getInstance("SHA-256")
                        .digest((server + "\n" + account).getBytes(StandardCharsets.UTF_8)));
        legacy = historyFile.resolveSibling(legacyHash + ".json");
        preferences.importLegacy(
            historyFile.resolveSibling(legacyHash + "-preferences.json"), legacyHash, firstImport);
      }
      history.restore(readHistory(historyFile), System.currentTimeMillis());
      if (legacy != null) history.mergeLegacy(readHistory(legacy), System.currentTimeMillis());
    } catch (Exception failure) {
      readFailed = true;
      LOGGER.warn("Historique local indisponible ; fichier original conserve.");
    }
  }

  private static Map<UUID, Long> readHistory(Path file) throws java.io.IOException {
    Map<UUID, Long> values = new HashMap<>();
    if (!Files.exists(file)) return values;
    if (Files.size(file) > 12_000_000) throw new IllegalStateException("History too large");
    JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
    for (var entry : json.entrySet()) {
      try {
        values.put(UUID.fromString(entry.getKey()), entry.getValue().getAsLong());
      } catch (IllegalArgumentException ignored) {
      }
      if (values.size() >= DetectionHistory.LIMIT) break;
    }
    return values;
  }

  public static void save() {
    preferences.save();
    if (history == null || !history.dirty() || historyFile == null || readFailed) return;
    try {
      Files.createDirectories(historyFile.getParent());
      JsonObject json = new JsonObject();
      history.snapshot().forEach((id, time) -> json.addProperty(id.toString(), time));
      Path temp = historyFile.resolveSibling(historyFile.getFileName() + ".tmp");
      Files.writeString(temp, json.toString());
      try {
        Files.move(
            temp, historyFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException unsupported) {
        Files.move(temp, historyFile, StandardCopyOption.REPLACE_EXISTING);
      }
      history.saved();
    } catch (Exception failure) {
      LOGGER.warn(
          "Sauvegarde de l'historique local differee ({}).", failure.getClass().getSimpleName());
    }
  }
}

