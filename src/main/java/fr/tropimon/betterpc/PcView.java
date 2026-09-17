package fr.tropimon.betterpc;

import static fr.tropimon.betterpc.BetterPcScreen.*;

import com.cobblemon.mod.common.pokemon.Pokemon;
import java.util.*;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/** Draws existing Cobblemon assets tinted by type; contains no storage actions. */
final class PcView {
  static String[] labels() {
    return new String[] {
      PcLang.tr("hp"),
      PcLang.tr("atk"),
      PcLang.tr("def"),
      PcLang.tr("spa"),
      PcLang.tr("spd"),
      PcLang.tr("spe")
    };
  }

  static void draw(BetterPcScreen s, DrawContext c, TextRenderer f, int mx, int my) {
    PcTextures.outerPanel(c, 0, 0, W, H);
    emphasis(c, f, "TROPIMON BETTER PC", 24, 29, 470, 0xFF81E9D1, 1.25f);
    PcText.centered(
        c,
        f,
        s.all.size()
            + " / "
            + s.capacity
            + PcLang.tr("pokemon_stockes")
            + s.usedBoxes
            + " / "
            + s.session.pc.getBoxes().size()
            + PcLang.tr("boites_utilisees"),
        220,
        607,
        680,
        0xFFE4EFFF,
        false);
    PcTextures.panel(c, 24, 626, 1072, 174);
    emphasis(c, f, PcLang.tr("recherche_filtres"), 40, 640, 200, 0xFF81E9D1, 1);
    clipped(c, f, PcLang.tr("captures_depuis"), 250, 640, 200, 0xFFCDDEEC);
    if (s.confirmation == null && !s.savingPreset) {
      PcTextures.input(c, 40, 658, 200, 21);
      PcTextures.input(c, 250, 658, 96, 21);
    }
    text(c, f, s.selected.size() + PcLang.tr("selected_count"), GX, 604, 0xFF9CE9D8);
    Set<UUID> rendered = new HashSet<>();
    for (int index = s.page * PAGE;
        index < Math.min(s.visible.size(), (s.page + 1) * PAGE);
        index++) {
      int cell = index % PAGE;
      card(
          s,
          c,
          f,
          s.visible.get(index),
          GX + cell % COLS * CW,
          GY + cell / COLS * CH,
          rendered,
          s.cardAt(mx, my) == index);
    }

    if (s.visible.isEmpty()) {
      text(
          c,
          f,
          s.filter.valid()
              ? PcLang.tr("aucun_pokemon_ne_correspond")
              : PcLang.tr("valeur_de_filtre_invalide"),
          GX + 110,
          310,
          0xFFECDBB4);
      text(
          c,
          f,
          PcLang.tr("les_dates_inconnues_sont_exclues_du_filtre_temporel"),
          GX + 65,
          330,
          0xFFB3C8D8);
    }

    text(c, f, PcLang.tr("page") + (s.page + 1) + " / " + s.pages(), 928, 607, 0xFFDCEBFA);
    for (int slot = 0; slot < 6; slot++) {
      int x = 24 + slot * 180;
      Pokemon p = s.session.at(new PcSession.Place(-1, slot));
      PcTextures.slot(c, x, 58, 172, 44, p != null && p.getUuid().equals(s.focusId));
      if (p == null) text(c, f, PcLang.tr("place_libre"), x + 44, 75, 0xFFCDDDEB);
      else {
        portrait(s, c, p, x + 3, 58, 44, rendered);
        clipped(
            c,
            f,
            p.getDisplayName(false).getString() + PcLang.tr("n") + p.getLevel(),
            x + 51,
            67,
            116,
            0xFFFFFFFF);
        clipped(c, f, PcPokemon.ability(p), x + 51, 84, 116, PcPokemon.abilityColor(p));
      }
    }

    clipped(c, f, status(s), 24, 814, 915, 0xFFD0E0ED);
    clipped(c, f, "By FastedCorsi", 973, 814, 130, 0xFFBCE5E0);
    if (s.confirmation != null || s.savingPreset) {
      c.getMatrices().push();
      c.getMatrices().translate(0, 0, 2000);
      c.fill(8, 8, W - 8, H - 8, 0xE0091323);
      if (s.confirmation != null) confirmation(s, c, f);
      else {
        PcTextures.panel(c, 329, 288, 462, 144);
        emphasis(c, f, PcLang.tr("enregistrer_la_recherche"), 355, 308, 410, 0xFF81E9D1, 1.15f);
        clipped(
            c,
            f,
            PcLang.tr("nom_40_caracteres_un_nom_existant_remplace_la_recherche"),
            355,
            329,
            410,
            0xFFD2E5F0);
      }

      c.getMatrices().pop();
    }

    s.portraits.keySet().retainAll(rendered);
  }

  static void overlays(BetterPcScreen s, DrawContext c, TextRenderer f, int mx, int my) {
    if (s.confirmation != null || s.savingPreset || s.tools.modal()) return;
    c.getMatrices().push();
    c.getMatrices().translate(0, 0, 2400);
    if (s.abilitySearch != null) {
      s.abilitySearch.draw(c, mx, my);
      c.getMatrices().pop();
      return;
    }
    if (s.dragging) {
      PcTextures.button(c, 40, 710, 200, 21, true, true, false);
      PcText.button(c, f, PcLang.tr("drop_box"), 40, 710, 200, 21, 0xFFFFFFFF);
    }
    if (s.menu != null) s.menu.draw(c, f, mx, my);
    if (s.movesPreview != null && s.menu == null) {
      Pokemon p = s.session.at(s.session.locate(s.movesPreview));
      if (p != null) tooltip(c, f, p, s.movesX, s.movesY);
    } else if (s.dragging) {
      // Render the actual form, aspects and held item under the cursor while dragging.
      Pokemon moving = s.session.at(s.session.locate(s.moving));
      if (moving != null)
        s.dragPortrait.draw(
            c, moving, Math.min(mx + 8, W - 64), Math.min(my - 24, H - 64), 64, true);
    } else if (s.menu == null) {
      int index = s.cardAt(mx, my);
      if (index >= 0 && (mx - GX) % CW < 144 && (my - GY) % CH >= 20 && (my - GY) % CH < 31) {
        int tags = s.prefs.tags(s.visible.get(index).pokemon().getUuid());
        List<String> names = new ArrayList<>();
        for (int i = 0; i < PcPreferences.TAG_KEYS.length; i++)
          if ((tags & (1 << i)) != 0) names.add(PcLang.tr("tag_" + PcPreferences.TAG_KEYS[i]));
        if (!names.isEmpty()) {
          int x = Math.min(mx, W - 240), y = my + 16;
          PcTextures.panel(c, x, y, 228, names.size() * 18 + 14);
          for (int i = 0; i < names.size(); i++)
            clipped(c, f, names.get(i), x + 8, y + 8 + i * 18, 212, 0xFFFFFFFF);
        }
      } else if (index >= 0
          && (mx - GX) % CW >= 156
          && (mx - GX) % CW < 171
          && (my - GY) % CH >= 27
          && (my - GY) % CH < 43) {
        PcMarks.tooltip(c, f, s.visible.get(index).pokemon(), mx, my);
      }
      // Only the move button opens the known move list.
      else if (index >= 0
          && (mx - GX) % CW >= 108
          && (mx - GX) % CW < 165
          && (my - GY) % CH >= 59
          && (my - GY) % CH < 77) tooltip(c, f, s.visible.get(index).pokemon(), mx, my);
      else if (index >= 0 && (mx - GX) % CW >= 171 && (my - GY) % CH >= 27 && (my - GY) % CH < 47) {
        var held = s.visible.get(index).pokemon().heldItemNoCopy$common();
        if (!held.isEmpty()) {
          PcTextures.panel(c, mx - 170, my + 18, 220, 27);
          clipped(c, f, held.getName().getString(), mx - 160, my + 25, 200, 0xFFFFFFFF);
        }

      } else if (index >= 0 && (my - GY) % CH >= 109 && (my - GY) % CH < 123) {
        Pokemon p = s.visible.get(index).pokemon();
        StringBuilder effects = new StringBuilder();
        String[] stats = labels();
        for (int i = 0; i < 6; i++) {
          int effect = PcPokemon.natureEffect(p, i);
          if (effect != 0) effects.append(stats[i]).append(effect > 0 ? " +10 %   " : " -10 %   ");
        }

        PcTextures.panel(c, Math.min(mx, W - 370), my + 18, 350, 29);
        clipped(
            c,
            f,
            effects.isEmpty() ? PcLang.tr("nature_neutral") : effects.toString(),
            Math.min(mx, W - 370) + 10,
            my + 26,
            330,
            0xFFE4ECFA);
      } else if (mx >= 460 && mx < 660 && my >= 638 && my < 680) {
        PcTextures.panel(c, 250, 546, 540, 76);
        clipped(
            c,
            f,
            PcLang.tr("heure_de_premiere_arrivee_observee_sur_ce_client"),
            262,
            558,
            516,
            0xFFEAF4FF);
        clipped(
            c,
            f,
            PcLang.tr("un_echange_peut_aussi_apparaitre_dans_ce_filtre"),
            262,
            576,
            516,
            0xFFEAF4FF);
        clipped(
            c,
            f,
            PcLang.tr("les_anciennes_captures_restent_de_date_inconnue"),
            262,
            594,
            516,
            0xFFEAF4FF);
      } else if (mx >= 24 && mx < 1096 && my >= 58 && my < 102) {
        Pokemon p = s.session.at(new PcSession.Place(-1, (mx - 24) / 180));
        if (p != null) tooltip(c, f, p, mx, my);
      }
    }

    c.getMatrices().pop();
  }

  private static String status(BetterPcScreen s) {
    if (!s.prefs.available)
      return PcLang.tr("favoris_non_charges_relachement_bloque_pour_preserver_les_protections");
    if (s.moving != null)
      return PcLang.tr(
              "glissez_vers_le_menu_boites_pour_l_ouvrir_puis_deposez_sur_une_boite_clic_droit")
          + PcLang.tr("pour_annuler");
    if (s.session.batch.running())
      return PcLang.tr("relachement")
          + s.session.batch.completed()
          + PcLang.tr("confirme_s")
          + s.session.batch.remaining()
          + PcLang.tr("restant_s_fermer_interrompt_la_suite");
    if (s.session.batch.result() == ReleaseBatch.Result.DONE)
      return s.session.batch.completed() + PcLang.tr("relachement_s_confirme_s");
    if (s.session.batch.result() == ReleaseBatch.Result.TIMEOUT
        || s.session.batch.result() == ReleaseBatch.Result.CHANGED)
      return PcLang.tr(
              "relachement_interrompu_stockage_modifie_ou_confirmation_absente_verifiez_les")
          + PcLang.tr("pokemon_restants");
    return s.session.message;
  }

  private static void card(
      BetterPcScreen s,
      DrawContext c,
      TextRenderer f,
      PcPokemon entry,
      int x,
      int y,
      Set<UUID> rendered,
      boolean hovered) {
    Pokemon p = entry.pokemon();
    boolean checked = s.selected.contains(p.getUuid());
    PcTextures.coloredSlot(
        c,
        x,
        y,
        CW - 8,
        CH - 8,
        hovered,
        s.multiple && checked,
        p.getPrimaryType().getName(),
        p.getSecondaryType() == null ? null : p.getSecondaryType().getName());
    emphasis(
        c,
        f,
        (s.multiple ? checked ? "[x] " : "[ ] " : "") + p.getDisplayName(false).getString(),
        x + 8,
        y + 9,
        164,
        0xFFFFFFFF,
        1.1f);
    PcTextures.heart(c, x + 181, y + 9, s.prefs.favorites.contains(p.getUuid()));
    int tags = s.prefs.tags(p.getUuid());
    int tagX = x + 8;
    float tagScale = PcText.scale(c, 1);
    for (int i = 0; i < PcPreferences.TAG_KEYS.length; i++) {
      if ((tags & (1 << i)) == 0) continue;
      if (i == 0) {
        PcTextures.pokedollar(c, tagX, y + 20);
        tagX += (int) Math.ceil(5 * tagScale) + 8;
      } else {
        String label = PcLang.tr("tag_short_" + PcPreferences.TAG_KEYS[i]);
        int tagWidth = (int) Math.ceil(f.getWidth(PcText.label(label)) * tagScale);
        PcText.draw(
            c,
            f,
            label,
            tagX,
            y + 20,
            tagWidth + 1,
            new int[] {0xFFFFD84D, 0xFF81E1FF, 0xFFFFB45C, 0xFFDBB9FF}[i],
            1,
            false);
        tagX += tagWidth + 8;
      }
    }
    portrait(s, c, p, x + 7, y + 31, 48, rendered);
    text(c, f, PcLang.tr("niv") + p.getLevel(), x + 68, y + 30, 0xFFFFFFFF);
    PcTextures.gender(c, p, x + 144, y + 28);
    PcMarks.draw(c, p, x + 156, y + 28);
    if (!p.heldItemNoCopy$common().isEmpty())
      c.drawItem(p.heldItemNoCopy$common(), x + 174, y + 29);
    clipped(
        c,
        f,
        typeLabel(p.getPrimaryType().getName())
            + (p.getSecondaryType() == null
                ? ""
                : " / " + typeLabel(p.getSecondaryType().getName())),
        x + 68,
        y + 46,
        123,
        0xFFE7EDFF);
    PcTextures.typeIcon(c, p.getPrimaryType(), x + 68, y + 60);
    if (p.getSecondaryType() != null) PcTextures.typeIcon(c, p.getSecondaryType(), x + 87, y + 60);
    PcTextures.ballIcon(c, p, x + 175, y + 59);
    PcTextures.button(c, x + 108, y + 59, 57, 17, true, hovered, false);
    PcText.button(c, f, PcLang.tr("att"), x + 108, y + 59, 57, 17, 0xFFE1F7FF);
    if (p.getShiny()) PcTextures.icon(c, PcTextures.SHINY, x + 45, y + 26, 12, 16);
    clipped(
        c,
        f,
        PcLang.tr("talent") + PcPokemon.ability(p),
        x + 8,
        y + 78,
        186,
        PcPokemon.abilityColor(p));
    clipped(
        c,
        f,
        PcLang.tr("nature") + PcPokemon.natureLabel(p),
        x + 8,
        y + 94,
        186,
        p.getMintedNature() == null ? 0xFFE5ECFF : 0xFFB9F7AA);
    stats(c, f, p, x + 8, y + 109);
  }

  private static void stats(DrawContext c, TextRenderer f, Pokemon p, int x, int y) {
    String[] labels = labels();
    emphasis(c, f, PcLang.tr("iv"), x, y + 14, 25, 0xFFE6EDFF, 1);
    emphasis(c, f, PcLang.tr("ev"), x, y + 28, 25, 0xFF81E1FF, 1);
    for (int i = 0; i < 6; i++) {
      int center = x + 34 + i * 27;
      int effect = PcPokemon.natureEffect(p, i);
      PcText.centered(
          c,
          f,
          labels[i],
          center - 13,
          y,
          effect == 0 ? 24 : 19,
          PcPokemon.natureColor(effect),
          false);
      if (effect != 0) natureArrow(c, center + 5, y + 2, effect);
      int iv = PcPokemon.stat(p, i, false), ev = PcPokemon.stat(p, i, true);
      PcText.centered(
          c, f, Integer.toString(iv), center - 13, y + 14, 26, PcPokemon.ivColor(iv), true);
      PcText.centered(
          c,
          f,
          Integer.toString(ev),
          center - 13,
          y + 28,
          26,
          ev == 252 ? 0xFF81E1FF : ev == 0 ? 0xFFBCC5D3 : 0xFFEBF4FF,
          true);
    }
  }

  static void natureArrow(DrawContext c, int x, int y, int effect) {
    int color = PcPokemon.natureColor(effect);
    PcTextures.pixelOrigin(c, x, y, 1);
    for (int row = 0; row < 3; row++) {
      int half = effect > 0 ? row : 2 - row;
      c.fill(2 - half, row, 3 + half, row + 1, color);
    }
    c.getMatrices().pop();
  }

  private static void tooltip(DrawContext c, TextRenderer f, Pokemon p, int mx, int my) {
    List<String> lines = new ArrayList<>();
    lines.add(p.getDisplayName(false).getString() + PcLang.tr("attaques"));
    for (var attack : p.getMoveSet().getMoves())
      lines.add("• " + attack.getDisplayName().getString());
    if (p.getMoveSet().getMoves().isEmpty()) lines.add(PcLang.tr("aucune_attaque"));
    int w = 350,
        h = lines.size() * 17 + 24,
        x = Math.min(mx + 14, W - w - 16),
        y = Math.min(my + 18, H - h - 24);
    PcTextures.panel(c, x, y, w, h);
    for (int i = 0; i < lines.size(); i++)
      clipped(
          c, f, lines.get(i), x + 12, y + 10 + i * 17, w - 24, i == 0 ? 0xFFBAF6E4 : 0xFFEDF2FF);
  }

  private static void confirmation(BetterPcScreen s, DrawContext c, TextRenderer f) {
    PcTextures.panel(c, 295, 174, 530, 402);
    emphasis(
        c,
        f,
        PcLang.tr("relacher") + s.confirmation.size() + PcLang.tr("pokemon"),
        319,
        193,
        482,
        0xFFFFC4CB,
        1.25f);
    text(
        c,
        f,
        PcLang.tr("action_definitive_seuls_les_pokemon_listes_seront_relaches"),
        319,
        219,
        0xFFE4ECFA);
    text(
        c,
        f,
        s.protectedCount + PcLang.tr("pokemon_protege_s_exclus_de_la_selection"),
        319,
        240,
        0xFFFFD84D);
    int end = Math.min(s.confirmation.size(), (s.confirmPage + 1) * 10);
    for (int i = s.confirmPage * 10; i < end; i++) {
      var t = s.confirmation.get(i);
      clipped(c, f, (i + 1) + ". " + t.label(), 319, 271 + i % 10 * 21, 335, 0xFFEAF0FA);
      text(c, f, "B" + (t.box() + 1) + " · " + (t.slot() + 1), 730, 271 + i % 10 * 21, 0xFFB4CDDD);
    }

    text(
        c,
        f,
        PcLang.tr("page") + (s.confirmPage + 1) + " / " + ((s.confirmation.size() + 9) / 10),
        519,
        509,
        0xFFD4E8F0);
    if (!s.confirmationValid)
      text(
          c,
          f,
          PcLang.tr("stockage_ou_protections_modifies_annulez_et_recommencez"),
          319,
          561,
          0xFFFFAD88);
  }

  private static void portrait(
      BetterPcScreen s, DrawContext c, Pokemon p, int x, int y, int size, Set<UUID> rendered) {
    boolean first = rendered.add(p.getUuid());
    s.portraits.computeIfAbsent(p.getUuid(), id -> new PcPortrait()).draw(c, p, x, y, size, first);
  }

  private static void text(DrawContext c, TextRenderer f, String text, int x, int y, int color) {
    PcText.draw(c, f, text, x, y, W - x - 16, color, PcText.BODY, false);
  }

  private static void emphasis(
      DrawContext c, TextRenderer f, String label, int x, int y, int width, int color, float size) {
    PcText.draw(c, f, label, x, y, width, color, Math.max(PcText.BODY, size), true);
  }

  private static void clipped(
      DrawContext c, TextRenderer f, String text, int x, int y, int width, int color) {
    PcText.draw(c, f, text, x, y, width, color, PcText.BODY, false);
  }
}
