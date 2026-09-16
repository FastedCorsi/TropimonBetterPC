package fr.tropimon.betterpc;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/** Keeps native editing, selection and narration with the same readable size as the labels. */
final class PcInput extends TextFieldWidget {
  private float fontScale = PcText.BODY;
  boolean centered;
  private final TextRenderer font;

  PcInput(TextRenderer font, int x, int y, int w, int h, Text message) {
    super(font, x, y, w, h, message);
    this.font = font;
  }

  @Override
  public void renderWidget(DrawContext c, int mx, int my, float delta) {
    int x = getX(), y = getY(), w = getWidth(), h = getHeight();
    fontScale = PcText.scale(c, PcText.BODY);
    float inset =
        centered && !isFocused()
            ? Math.max(
                0, (w - font.getWidth(getText().isEmpty() ? "X" : getText()) * fontScale) / 2)
            : 0;
    float vertical = centered ? Math.max(0, (h - 9 * fontScale) / 2) : 0;
    c.getMatrices().push();
    c.getMatrices().translate(x + inset, y + vertical, 0);
    c.getMatrices().scale(fontScale, fontScale, 1);
    try {
      localBounds(w, h);
      // Keyboard edits happen in screen coordinates; scroll the native field to the scaled caret.
      setSelectionStart(getCursor());
      super.renderWidget(c, (int) ((mx - x) / fontScale), (int) ((my - y) / fontScale), delta);
    } finally {
      bounds(x, y, w, h);
      c.getMatrices().pop();
    }
  }

  @Override
  public boolean mouseClicked(double mx, double my, int button) {
    int x = getX(), y = getY(), w = getWidth(), h = getHeight();
    try {
      localBounds(w, h);
      return super.mouseClicked((mx - x) / fontScale, (my - y) / fontScale, button);
    } finally {
      bounds(x, y, w, h);
    }
  }

  private void localBounds(int w, int h) {
    bounds(0, 0, (int) (w / fontScale), Math.max(9, (int) (h / fontScale)));
  }

  private void bounds(int x, int y, int w, int h) {
    setX(x);
    setY(y);
    setWidth(w);
    setHeight(h);
  }
}
