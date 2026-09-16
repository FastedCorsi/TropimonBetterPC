package fr.tropimon.betterpc;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Language;

/** Native bitmap font with pixel-aligned strokes; no external font file. */
final class PcText {
  static final Style STYLE = Style.EMPTY;
  static final float BODY = 1.15f;

  static float scale(DrawContext c, float requested) {
    double gui = net.minecraft.client.MinecraftClient.getInstance().getWindow().getScaleFactor();
    float pixels = (float) (c.getMatrices().peek().getPositionMatrix().m00() * gui);
    return Math.max(1, Math.round(requested * pixels)) / pixels;
  }

  static Text label(String value) {
    return Text.literal(value).setStyle(STYLE);
  }

  static void draw(
      DrawContext c,
      TextRenderer font,
      String value,
      int x,
      int y,
      int width,
      int color,
      float size,
      boolean bold) {
    draw(c, font, value, x, y, width, color, size, bold, true);
  }

  private static void draw(
      DrawContext c,
      TextRenderer font,
      String value,
      int x,
      int y,
      int width,
      int color,
      float size,
      boolean bold,
      boolean snapSize) {
    if (snapSize) size = scale(c, size);
    Text full = Text.literal(value).setStyle(STYLE.withBold(bold));
    int available = Math.max(0, (int) (width / size));
    StringVisitable visible = full;
    if (font.getWidth(full) > available) {
      Text dots = Text.literal("…").setStyle(STYLE.withBold(bold));
      visible =
          StringVisitable.concat(
              font.trimToWidth(full, Math.max(0, available - font.getWidth(dots))), dots);
    }

    c.getMatrices().push();
    var matrix = c.getMatrices().peek().getPositionMatrix();
    double gui = net.minecraft.client.MinecraftClient.getInstance().getWindow().getScaleFactor();
    float sx =
        (float)
            ((Math.round((matrix.m30() + x * matrix.m00()) * gui) / gui - matrix.m30())
                / matrix.m00());
    float sy =
        (float)
            ((Math.round((matrix.m31() + y * matrix.m11()) * gui) / gui - matrix.m31())
                / matrix.m11());
    c.getMatrices().translate(sx, sy, 0);
    c.getMatrices().scale(size, size, 1);
    c.drawText(font, Language.getInstance().reorder(visible), 0, 0, color, true);
    c.getMatrices().pop();
  }

  static void centered(
      DrawContext c,
      TextRenderer font,
      String value,
      int x,
      int y,
      int width,
      int color,
      boolean bold) {
    Text label = Text.literal(value).setStyle(STYLE.withBold(bold));
    float size = Math.min(scale(c, BODY), (width - 1f) / Math.max(1, font.getWidth(label)));
    int measured = (int) Math.ceil(font.getWidth(label) * size);
    draw(c, font, value, x + (width - measured) / 2, y, width, color, size, bold, false);
  }

  static void button(
      DrawContext c,
      TextRenderer font,
      String value,
      int x,
      int y,
      int width,
      int height,
      int color) {
    float size = scale(c, BODY);
    int available = Math.max(0, (int) ((width - 8) / size));
    String visible = value;
    if (font.getWidth(label(value)) > available)
      visible = font.trimToWidth(value, Math.max(0, available - font.getWidth("…"))) + "…";
    int measured = font.getWidth(label(visible));
    draw(
        c,
        font,
        visible,
        x + Math.round((width - measured * size) / 2),
        y + Math.round((height - 7 * size) / 2),
        width - 6,
        color,
        size,
        false,
        false);
  }
}
