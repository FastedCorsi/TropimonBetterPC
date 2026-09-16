package fr.tropimon.betterpc;

import java.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Small local autocomplete: typing narrows suggestions, choosing commits the exact ability. */
final class PcAbilitySearch {
  record Option(String id, String label) {}

  static final int X = 250, Y = 418, WIDTH = 330, ROWS = 8;
  private final BetterPcScreen screen;
  final TextFieldWidget input;
  private final List<Option> options;
  List<Option> matches = List.of();
  private int selected, scroll;

  PcAbilitySearch(BetterPcScreen screen) {
    this.screen = screen;
    var font = MinecraftClient.getInstance().textRenderer;
    Map<String, String> abilities = new TreeMap<>();
    for (var entry : screen.all)
      abilities.put(entry.pokemon().getAbility().getName(), PcPokemon.ability(entry.pokemon()));
    var entries = new ArrayList<Option>();
    entries.add(new Option("", PcLang.tr("tous_les_talents")));
    entries.add(new Option("@hidden", PcLang.tr("talent_cache_uniquement")));
    abilities.entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .forEach(e -> entries.add(new Option(e.getKey(), e.getValue())));
    options = List.copyOf(entries);
    input =
        new PcInput(
            font, X + 14, Y + 32, WIDTH - 28, 14, Text.literal(PcLang.tr("ability_search")));
    input.setDrawsBackground(false);
    input.setMaxLength(80);
    input.setPlaceholder(PcText.label(PcLang.tr("ability_search")));
    input.setRenderTextProvider((value, start) -> PcText.label(value).asOrderedText());
    input.setChangedListener(this::search);
    input.setFocused(true);
    search("");
  }

  private void search(String query) {
    String normalized = DetectionHistory.normalize(query).strip();
    matches =
        options.stream()
            .filter(o -> DetectionHistory.normalize(o.label() + " " + o.id()).contains(normalized))
            .toList();
    selected = scroll = 0;
  }

  private void pick(int index) {
    if (screen.session.busy() || index < 0 || index >= matches.size()) return;
    screen.ability = matches.get(index).id();
    screen.abilitySearch = null;
    screen.rebuild();
  }

  void key(int key, int scan, int mods) {
    if (key == GLFW.GLFW_KEY_ESCAPE) screen.abilitySearch = null;
    else if (key == GLFW.GLFW_KEY_ENTER
        || key == GLFW.GLFW_KEY_KP_ENTER
        || key == GLFW.GLFW_KEY_TAB) pick(selected);
    else if (key == GLFW.GLFW_KEY_DOWN || key == GLFW.GLFW_KEY_UP) {
      selected =
          Math.clamp(
              selected + (key == GLFW.GLFW_KEY_DOWN ? 1 : -1), 0, Math.max(0, matches.size() - 1));
      scroll = Math.clamp(scroll, Math.max(0, selected - ROWS + 1), selected);
    } else input.keyPressed(key, scan, mods);
  }

  void click(int x, int y, int button) {
    if (button != 0 || x < X || x >= X + WIDTH || y < Y || y >= Y + 242) {
      screen.abilitySearch = null;
      return;
    }
    if (y >= Y + 58 && y < Y + 58 + ROWS * 22) pick(scroll + (y - Y - 58) / 22);
    else input.mouseClicked(x, y, button);
    input.setFocused(true);
  }

  void scroll(double amount) {
    scroll = Math.clamp(scroll - (int) Math.signum(amount), 0, Math.max(0, matches.size() - ROWS));
    selected =
        Math.clamp(selected, scroll, Math.min(scroll + ROWS - 1, Math.max(0, matches.size() - 1)));
  }

  void draw(DrawContext c, int mx, int my) {
    var f = MinecraftClient.getInstance().textRenderer;
    PcTextures.panel(c, X, Y, WIDTH, 242);
    PcText.draw(
        c,
        f,
        PcLang.tr("ability_search"),
        X + 10,
        Y + 10,
        WIDTH - 20,
        0xFF81E9D1,
        PcText.BODY,
        false);
    PcTextures.input(c, X + 10, Y + 28, WIDTH - 20, 22);
    input.render(c, mx, my, 0);
    for (int row = 0; row < Math.min(ROWS, matches.size() - scroll); row++) {
      int index = scroll + row, y = Y + 58 + row * 22;
      PcTextures.button(
          c,
          X + 8,
          y,
          WIDTH - 16,
          21,
          true,
          index == selected || (mx >= X && mx < X + WIDTH && my >= y && my < y + 22),
          false);
      PcText.button(c, f, matches.get(index).label(), X + 8, y, WIDTH - 16, 21, 0xFFFFFFFF);
    }
    if (matches.isEmpty())
      PcText.draw(
          c,
          f,
          PcLang.tr("no_ability"),
          X + 12,
          Y + 66,
          WIDTH - 24,
          0xFFE4ECFA,
          PcText.BODY,
          false);
  }
}
