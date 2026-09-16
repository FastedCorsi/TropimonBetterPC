package fr.tropimon.betterpc;

import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/** A bounded, scrolling dropdown, also used as the box drop target list. */
final class PcMenu {
  record Entry(String label, Runnable action, int box) {
    Entry(String label, Runnable action) {
      this(label, action, -2);
    }
  }

  final int x, y, width;
  final List<Entry> entries;
  private int scroll;
  private final int rows;

  PcMenu(int x, int y, int width, List<Entry> entries) {
    this.x = x;
    this.width = width;
    this.entries = List.copyOf(entries);
    rows = Math.min(12, entries.size());
    this.y = Math.min(y, BetterPcScreen.H - 40 - rows * 24);
  }

  int index(int mx, int my) {
    if (mx < x || mx >= x + width || my < y || my >= y + rows * 24) return -1;
    int index = scroll + (my - y) / 24;
    return index < entries.size() ? index : -1;
  }

  void scroll(double amount) {
    scroll = Math.clamp(scroll - (int) Math.signum(amount), 0, Math.max(0, entries.size() - rows));
  }

  void draw(DrawContext context, TextRenderer font, int mx, int my) {
    PcTextures.panel(context, x - 3, y - 3, width + 6, rows * 24 + 6);
    for (int row = 0; row < rows; row++) {
      int index = scroll + row;
      PcTextures.button(context, x, y + row * 24, width, 23, true, index == index(mx, my), false);
      String label = entries.get(index).label();
      PcText.button(context, font, label, x, y + row * 24, width, 23, 0xFFFFFFFF);
    }
    if (entries.size() > rows)
      PcText.draw(
          context,
          font,
          (scroll + 1) + "–" + (scroll + rows) + " / " + entries.size(),
          x + 6,
          y - 13,
          width,
          0xFFB9E2ED,
          PcText.BODY,
          false);
  }
}
