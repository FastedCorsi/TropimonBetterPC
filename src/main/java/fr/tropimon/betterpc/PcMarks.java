package fr.tropimon.betterpc;

import com.cobblemon.mod.common.api.mark.Mark;
import com.cobblemon.mod.common.pokemon.Pokemon;
import fr.tropimon.betterpc.mixin.MarkTextAccessor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/** Official mark icon and owned marks only; independent of Catch Preview. */
final class PcMarks {
  private static final Identifier ICON =
      Identifier.of("cobblemon", "textures/gui/summary/summary_tab_icon_marks.png");
  static final int SIZE = 12;

  static void draw(DrawContext c, Pokemon p, int x, int y) {
    if (p.getMarks().isEmpty()) return;
    PcTextures.pixelOrigin(c, x, y, 1);
    c.drawTexture(ICON, 0, 0, SIZE, SIZE, 0, 0, 17, 17, 17, 17);
    c.getMatrices().pop();
  }

  static List<Text> descriptions(Pokemon p) {
    var marks = new ArrayList<>(p.getMarks());
    marks.sort(
        Comparator.comparing((Mark m) -> m != p.getActiveMark())
            .thenComparing(m -> m.getIdentifier().toString()));
    List<Text> result = new ArrayList<>();
    for (Mark mark : marks) {
      var text = (MarkTextAccessor) (Object) mark;
      result.add(text.betterPc$name().formatted(Formatting.GOLD));
      result.add(text.betterPc$description());
    }
    return result;
  }

  static void tooltip(DrawContext c, TextRenderer f, Pokemon p, int mx, int my) {
    if (p.getMarks().isEmpty()) return;
    float scale = PcText.scale(c, PcText.BODY);
    List<OrderedText> lines = new ArrayList<>();
    for (Text text : descriptions(p)) lines.addAll(f.wrapLines(text, (int) (300 / scale)));
    int lineHeight = (int) Math.ceil(10 * scale);
    int count = Math.min(lines.size(), Math.max(1, (BetterPcScreen.H - 64) / lineHeight));
    int h = count * lineHeight + 20;
    int x = Math.clamp(mx + 14, 16, BetterPcScreen.W - 340);
    int y = Math.clamp(my + 18, 16, BetterPcScreen.H - h - 16);
    c.getMatrices().push();
    c.getMatrices().translate(0, 0, 1000);
    PcTextures.panel(c, x, y, 324, h);
    c.getMatrices().translate(x + 12, y + 10, 100);
    c.getMatrices().scale(scale, scale, 1);
    for (int i = 0; i < count; i++)
      c.drawText(f, lines.get(i), 0, (int) (i * lineHeight / scale), 0xFFFFFFFF, true);
    c.getMatrices().pop();
  }
}
