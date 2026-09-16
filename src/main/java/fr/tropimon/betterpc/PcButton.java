package fr.tropimon.betterpc;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

final class PcButton extends ButtonWidget {
  boolean accent;
  boolean danger;

  PcButton(int x, int y, int width, String label, Runnable action) {
    super(x, y, width, 22, Text.literal(label), button -> action.run(), DEFAULT_NARRATION_SUPPLIER);
  }

  @Override
  protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
    PcTextures.button(
        context,
        getX(),
        getY(),
        width,
        height,
        active,
        isHovered() || isFocused() || accent,
        danger || (getMessage().getString().equals("X") && isHovered()));
    String label = getMessage().getString();
    if (label.equals("<") || label.equals(">")) {
      PcTextures.part(
          context,
          label.equals("<") ? PcTextures.PREVIOUS : PcTextures.NEXT,
          getX() + (width - 14) / 2,
          getY() + 4,
          14,
          14,
          0,
          active && isHovered() ? 14 : 0,
          14,
          14,
          14,
          28);
    } else
      PcText.button(
          context,
          MinecraftClient.getInstance().textRenderer,
          label,
          getX(),
          getY(),
          width,
          height,
          active ? 0xFFFFFFFF : 0xFFBCC6CD);
  }
}
