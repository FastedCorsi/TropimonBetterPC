package fr.tropimon.betterpc;

import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import java.lang.reflect.Method;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

/** Optional UI bridge. No TeamBuilder class is required to load or use Better PC. */
public final class PcTeamBuilder {
  private static BetterPcScreen source;
  private static Screen builder;
  private static Method activeMethod;
  private static boolean applying;

  static boolean available() {
    return FabricLoader.getInstance().isModLoaded("tropimon_team_saver");
  }

  static void open(BetterPcScreen pc) {
    if (!available() || !pc.session.valid() || pc.session.busy()) return;
    try {
      Class<?> type = Class.forName("fr.tropimon.teamsaver.client.TeamManagerScreen");
      var constructor = type.getDeclaredConstructor(PCGUI.class);
      constructor.setAccessible(true);
      Method active =
          Class.forName("fr.tropimon.teamsaver.client.ClientTeamApplier")
              .getDeclaredMethod("isActive", type);
      active.setAccessible(true);
      Screen next = (Screen) constructor.newInstance(pc.session.original);
      source = pc;
      builder = next;
      activeMethod = active;
      pc.keepLink = true;
      MinecraftClient.getInstance().setScreen(next);
    } catch (ReflectiveOperationException | RuntimeException failure) {
      reset();
      pc.keepLink = false;
      pc.closed = false;
      pc.session.message = PcLang.tr("teambuilder_unavailable");
      BetterPcClient.LOGGER.warn(
          "Optional TeamBuilder screen could not open ({}).", failure.getClass().getSimpleName());
    }
  }

  public static Screen returnTarget(Screen next) {
    if (source == null) return next;
    Screen current = MinecraftClient.getInstance().currentScreen;
    if (next == source.session.original || next == null && current == builder) {
      if (source.session.valid()) {
        updateApplying();
        source.keepLink = false;
        source.closed = false;
        return source;
      }
      reset();
    } else if (current == source && next != builder) reset();
    return next;
  }

  static void tick() {
    if (source == null) return;
    if (!source.session.valid()) {
      closeFromServer(source.storeId());
      return;
    }
    updateApplying();
    if (MinecraftClient.getInstance().currentScreen == source && !applying) reset();
  }

  private static void updateApplying() {
    try {
      applying = (boolean) activeMethod.invoke(null, builder);
    } catch (ReflectiveOperationException failure) {
      // Preserve action protection if the optional integration cannot report its state.
      applying = true;
    }
  }

  static boolean busy(PcSession session) {
    return source != null && source.session == session && applying;
  }

  public static boolean blocksMovement() {
    Screen current = MinecraftClient.getInstance().currentScreen;
    return source != null && (current == source || isBuilderScreen(current));
  }

  private static boolean isBuilderScreen(Screen screen) {
    return screen != null
        && screen.getClass().getName().startsWith("fr.tropimon.teamsaver.client.");
  }

  public static void closeFromServer(UUID store) {
    if (source == null || store != null && !store.equals(source.storeId())) return;
    var client = MinecraftClient.getInstance();
    boolean close =
        client.currentScreen == source
            || client.currentScreen == source.session.original
            || isBuilderScreen(client.currentScreen);
    reset();
    if (close) client.setScreen(null);
  }

  static void reset() {
    source = null;
    builder = null;
    activeMethod = null;
    applying = false;
  }
}
