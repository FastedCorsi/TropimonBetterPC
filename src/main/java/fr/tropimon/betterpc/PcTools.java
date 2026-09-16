package fr.tropimon.betterpc;

import com.cobblemon.mod.common.pokemon.Pokemon;
import java.util.*;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/** Compact comparison with direct favorite hearts and per-Pokemon tag editing. */
final class PcTools {
  enum Dialog {
    NONE,
    COMPARE
  }

  private final BetterPcScreen screen;
  private final List<PcButton> buttons = new ArrayList<>();
  private final PcPortrait[] portraits = {new PcPortrait(), new PcPortrait()};
  private UUID[] pair;
  private PcMenu tagMenu;
  Dialog dialog = Dialog.NONE;

  PcTools(BetterPcScreen screen) {
    this.screen = screen;
  }

  boolean modal() {
    return dialog != Dialog.NONE;
  }

  void open(Dialog dialog) {
    if (screen.session.busy() || screen.selected.size() != 2) return;
    this.dialog = dialog;
    screen.menu = null;
    screen.movesPreview = null;
    screen.setFocused(null);
    pair = screen.selected.toArray(UUID[]::new);
    controls();
  }

  void close() {
    dialog = Dialog.NONE;
    buttons.clear();
    tagMenu = null;
    pair = null;
  }

  void escape() {
    if (tagMenu != null) tagMenu = null;
    else close();
  }

  void tick() {
    if (modal())
      for (UUID id : pair)
        if (screen.session.at(screen.session.locate(id)) == null) {
          close();
          return;
        }
  }

  private void controls() {
    buttons.clear();
    buttons.add(new PcButton(808, 224, 28, "X", this::close));
    for (int side = 0; side < 2; side++) {
      UUID id = pair[side];
      int x = 284 + side * 292;
      PcButton b = new PcButton(x, 596, 260, PcLang.tr("tags_edit"), () -> openTags(id, x));
      b.active = screen.prefs.available;
      buttons.add(b);
    }
  }

  private void openTags(UUID id, int x) {
    List<PcMenu.Entry> entries = new ArrayList<>();
    for (int i = 0; i < PcPreferences.TAG_KEYS.length; i++) {
      int mask = 1 << i;
      boolean checked = (screen.prefs.tags(id) & mask) != 0;
      entries.add(
          new PcMenu.Entry(
              (checked ? "[x] " : "[ ] ") + PcLang.tr("tag_" + PcPreferences.TAG_KEYS[i]),
              () -> setTag(id, mask, !checked)));
    }
    entries.add(new PcMenu.Entry(PcLang.tr("tags_clear"), () -> setTag(id, 15, false)));
    tagMenu = new PcMenu(Math.min(x, 544), 472, 310, entries);
  }

  private void setTag(UUID id, int mask, boolean enabled) {
    if (!screen.session.valid() || screen.session.at(screen.session.locate(id)) == null) return;
    screen.prefs.setTags(List.of(id), mask, enabled);
    screen.applyFilters();
  }

  boolean click(double x, double y, int button) {
    if (button != 0) return true;
    if (tagMenu != null) {
      int index = tagMenu.index((int) x, (int) y);
      var menu = tagMenu;
      tagMenu = null;
      if (index >= 0) menu.entries.get(index).action().run();
      return true;
    }
    for (int side = 0; side < 2; side++)
      if (x >= 518 + side * 292 && x < 544 + side * 292 && y >= 254 && y < 276) {
        screen.prefs.toggleFavorite(pair[side]);
        screen.applyFilters();
        return true;
      }
    for (var b : List.copyOf(buttons)) if (b.mouseClicked(x, y, button)) break;
    return true;
  }

  void draw(DrawContext c, TextRenderer f, int mx, int my, float delta) {
    c.getMatrices().push();
    c.getMatrices().translate(0, 0, 3000);
    c.fill(8, 8, BetterPcScreen.W - 8, BetterPcScreen.H - 8, 0xCA091323);
    PcTextures.panel(c, 268, 212, 584, 434);
    line(c, f, PcLang.tr("tools_compare"), 284, 230, 510, 0xFF81E9D1);
    Pokemon[] pokemon =
        Arrays.stream(pair)
            .map(id -> screen.session.at(screen.session.locate(id)))
            .toArray(Pokemon[]::new);
    if (pokemon[0] != null && pokemon[1] != null)
      for (int side = 0; side < 2; side++) {
        Pokemon p = pokemon[side], other = pokemon[1 - side];
        int x = 284 + side * 292;
        PcTextures.coloredSlot(
            c,
            x - 8,
            250,
            276,
            340,
            false,
            false,
            p.getPrimaryType().getName(),
            p.getSecondaryType() == null ? null : p.getSecondaryType().getName());
        line(c, f, p.getDisplayName(false).getString(), x, 260, 224, 0xFFFFFFFF);
        PcTextures.heart(c, x + 240, 260, screen.prefs.favorites.contains(p.getUuid()));
        portraits[side].draw(c, p, x, 278, 44, true);
        line(c, f, PcLang.tr("niv") + p.getLevel(), x + 64, 286, 140, 0xFFFFFFFF);
        PcTextures.gender(c, p, x + 240, 286);
        PcMarks.draw(c, p, x + 216, 286);
        PcTextures.typeIcon(c, p.getPrimaryType(), x + 64, 306);
        if (p.getSecondaryType() != null) PcTextures.typeIcon(c, p.getSecondaryType(), x + 85, 306);
        line(c, f, PcPokemon.ivRating(p), x + 108, 308, 154, 0xFF81E9D1);
        line(c, f, PcLang.tr("nature") + PcPokemon.natureLabel(p), x, 330, 260, 0xFFE5ECFF);
        line(c, f, PcPokemon.ability(p), x, 346, 260, PcPokemon.abilityColor(p));
        line(
            c,
            f,
            PcLang.tr("objet")
                + (p.heldItemNoCopy$common().isEmpty()
                    ? PcLang.tr("aucun")
                    : p.heldItemNoCopy$common().getName().getString()),
            x,
            362,
            260,
            0xFFE4ECFA);
        line(c, f, PcLang.tr("real_stats"), x + 52, 384, 62, 0xFFFFFFFF);
        line(c, f, "IV", x + 116, 384, 70, 0xFFFFFFFF);
        line(c, f, "EV", x + 190, 384, 72, 0xFF81E1FF);
        String[] labels = PcView.labels();
        for (int stat = 0; stat < 6; stat++) {
          int y = 402 + stat * 16, effect = PcPokemon.natureEffect(p, stat);
          line(c, f, labels[stat], x, y, 35, PcPokemon.natureColor(effect));
          if (effect != 0) PcView.natureArrow(c, x + 36, y + 2, effect);
          line(c, f, Integer.toString(p.getStat(PcPokemon.STATS[stat])), x + 52, y, 62, 0xFFFFFFFF);
          int iv = PcPokemon.stat(p, stat, false), ev = PcPokemon.stat(p, stat, true);
          line(
              c,
              f,
              difference(iv, PcPokemon.stat(other, stat, false)),
              x + 116,
              y,
              70,
              PcPokemon.ivColor(iv));
          line(c, f, difference(ev, PcPokemon.stat(other, stat, true)), x + 190, y, 72, 0xFF81E1FF);
        }
        int y = 502;
        for (var move : p.getMoveSet().getMoves()) {
          line(c, f, move.getDisplayName().getString(), x, y, 260, 0xFFE4ECFA);
          y += 16;
        }
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < PcPreferences.TAG_KEYS.length; i++)
          if ((screen.prefs.tags(p.getUuid()) & (1 << i)) != 0)
            tags.add(PcLang.tr("tag_short_" + PcPreferences.TAG_KEYS[i]));
        line(
            c,
            f,
            tags.isEmpty() ? PcLang.tr("tags_none") : String.join(" · ", tags),
            x,
            572,
            260,
            0xFFFFD84D);
      }
    for (var b : buttons) b.render(c, mx, my, delta);
    line(c, f, PcLang.tr("compare_difference"), 284, 628, 548, 0xFFB6CCDC);
    if (tagMenu == null && pokemon[0] != null && pokemon[1] != null) {
      for (int side = 0; side < 2; side++) {
        int x = 284 + side * 292;
        if (mx >= x + 216 && mx < x + 228 && my >= 286 && my < 298)
          PcMarks.tooltip(c, f, pokemon[side], mx, my);
      }
    }
    if (tagMenu != null) {
      c.getMatrices().translate(0, 0, 2000);
      tagMenu.draw(c, f, mx, my);
    }
    c.getMatrices().pop();
  }

  private static String difference(int value, int other) {
    int delta = value - other;
    return delta == 0 ? Integer.toString(value) : value + " " + (delta > 0 ? "+" : "") + delta;
  }

  private static void line(
      DrawContext c, TextRenderer f, String value, int x, int y, int width, int color) {
    PcText.draw(c, f, value, x, y, width, color, PcText.BODY, false);
  }
}
