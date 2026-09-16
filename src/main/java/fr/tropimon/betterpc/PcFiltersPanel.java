package fr.tropimon.betterpc;

import static fr.tropimon.betterpc.BetterPcScreen.*;

import com.cobblemon.mod.common.pokemon.Pokemon;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.text.Text;

/** Filter controls and dropdowns; storage actions remain in the screen/session. */
final class PcFiltersPanel {
  private final BetterPcScreen screen;

  PcFiltersPanel(BetterPcScreen screen) {
    this.screen = screen;
  }

  void build() {
    screen.field(
        40,
        658,
        410,
        screen.searchText,
        PcLang.tr("nom_attaque"),
        value -> screen.searchText = value);
    screen.field(460, 658, 96, screen.ageText, "X", value -> screen.ageText = value);
    screen.button(
        564,
        658,
        96,
        screen.days ? PcLang.tr("jours_626d0d") : PcLang.tr("heures_763c8a"),
        () ->
            choices(
                564,
                682,
                120,
                List.of(PcLang.tr("heures"), PcLang.tr("jours")),
                i -> {
                  screen.days = i == 1;
                  screen.rebuild();
                }));
    screen.button(
        40,
        684,
        200,
        PcLang.tr("type")
            + (screen.type.isEmpty() ? PcLang.tr("tous_7df7e7") : typeLabel(screen.type))
            + " ▾",
        () ->
            choices(
                40,
                708,
                210,
                Arrays.stream(TYPES)
                    .map(t -> t.isEmpty() ? PcLang.tr("tous_les_types") : typeLabel(t))
                    .toList(),
                i -> {
                  screen.type = TYPES[i];
                  screen.rebuild();
                }));
    screen.button(
        250,
        684,
        200,
        PcLang.tr("talent")
            + (screen.ability.isEmpty()
                ? PcLang.tr("tous_7df7e7")
                : screen.ability.equals("@hidden") ? "HA" : abilityLabel(screen.ability))
            + " ▾",
        () -> {
          screen.menu = null;
          screen.setFocused(null);
          screen.abilitySearch = new PcAbilitySearch(screen);
        });
    screen.button(
        880,
        684,
        200,
        PcLang.tr("objet")
            + (screen.item.isEmpty()
                ? PcLang.tr("tous_7df7e7")
                : screen.item.equals("@none")
                    ? PcLang.tr("aucun")
                    : screen.item.equals("@any")
                        ? PcLang.tr("avec_objet_7a319b")
                        : PcLang.tr("selectionne"))
            + " ▾",
        () -> itemMenu());
    screen.button(
        460,
        684,
        200,
        new String[] {PcLang.tr("shiny_tous"), PcLang.tr("shiny_oui"), PcLang.tr("shiny_non")}
            [screen.shiny],
        () ->
            choices(
                460,
                708,
                180,
                List.of(PcLang.tr("shiny_tous"), PcLang.tr("shiny_oui"), PcLang.tr("shiny_non")),
                i -> {
                  screen.shiny = i;
                  screen.rebuild();
                }));
    screen.button(
        670,
        684,
        200,
        new String[] {
              PcLang.tr("sexe_tous"), PcLang.tr("male"), PcLang.tr("femelle"), PcLang.tr("asexue")
            }
            [screen.gender],
        () ->
            choices(
                670,
                708,
                200,
                List.of(
                    PcLang.tr("tous_les_sexes"),
                    PcLang.tr("male"),
                    PcLang.tr("femelle"),
                    PcLang.tr("asexue")),
                i -> {
                  screen.gender = i;
                  screen.rebuild();
                }));
    screen.button(
        250,
        710,
        200,
        screen.favoritesOnly ? PcLang.tr("favoris_uniquement") : PcLang.tr("favoris_tous"),
        () -> {
          screen.favoritesOnly = !screen.favoritesOnly;
          screen.rebuild();
        });
    String tagLabel =
        screen.tag == 0
            ? PcLang.tr("tags_all")
            : screen.tag == -1
                ? PcLang.tr("tags_none")
                : java.util.stream.IntStream.range(0, PcPreferences.TAG_KEYS.length)
                    .filter(i -> (screen.tag & (1 << i)) != 0)
                    .mapToObj(i -> PcLang.tr("tag_" + PcPreferences.TAG_KEYS[i]))
                    .collect(java.util.stream.Collectors.joining(", "));
    screen.button(460, 710, 200, tagLabel + " ▾", this::tagFilterMenu);
    screen.button(880, 710, 200, PcLang.tr("protections"), this::protectionMenu);
    screen.button(670, 658, 200, PcLang.tr("recherches_enregistrees"), this::presetsMenu);
    screen.button(
        880,
        658,
        200,
        PcLang.tr("enregistrer_cette_recherche"),
        () -> {
          screen.savingPreset = true;
          screen.menu = null;
          screen.presetName.setText("");
          screen.setFocused(screen.presetName);
          screen.updateButtons();
        });
    screen.deleteSearch =
        screen.button(250, 762, 200, PcLang.tr("supprimer_une_recherche"), this::deletePresetMenu);
    screen.button(
        40,
        762,
        200,
        PcLang.tr("reinitialiser_les_filtres"),
        () -> screen.loadFilters(PcPreferences.Filters.empty()));
  }

  void choices(int x, int y, int w, List<String> labels, Consumer<Integer> select) {
    List<PcMenu.Entry> entries = new ArrayList<>();
    for (int i = 0; i < labels.size(); i++) {
      int index = i;
      entries.add(new PcMenu.Entry(labels.get(i), () -> select.accept(index)));
    }

    screen.menu = new PcMenu(x, y, w, entries);
    screen.setFocused(null);
  }

  private void tagFilterMenu() {
    List<String> labels = new ArrayList<>(List.of(PcLang.tr("tags_all"), PcLang.tr("tags_none")));
    for (String key : PcPreferences.TAG_KEYS) labels.add(PcLang.tr("tag_" + key));
    choices(
        460,
        734,
        240,
        labels,
        i -> {
          screen.tag = i == 0 ? 0 : i == 1 ? -1 : 1 << (i - 2);
          screen.rebuild();
        });
  }

  void tagsMenu(List<UUID> ids) {
    if (ids.isEmpty() || screen.session.busy() || !screen.prefs.available) return;
    List<PcMenu.Entry> entries = new ArrayList<>();
    for (int i = 0; i < PcPreferences.TAG_KEYS.length; i++) {
      int mask = 1 << i;
      long count = ids.stream().filter(id -> (screen.prefs.tags(id) & mask) != 0).count();
      boolean remove = count == ids.size();
      String state = remove ? "[x] " : count == 0 ? "[ ] " : "[-] ";
      entries.add(
          new PcMenu.Entry(
              state
                  + PcLang.tr("tag_" + PcPreferences.TAG_KEYS[i])
                  + " · "
                  + PcLang.tr(remove ? "tags_remove" : "tags_add")
                  + " ("
                  + ids.size()
                  + ")",
              () -> changeTags(ids, mask, !remove)));
    }
    entries.add(
        new PcMenu.Entry(
            PcLang.tr("tags_clear") + " (" + ids.size() + ")", () -> changeTags(ids, 15, false)));
    screen.menu = new PcMenu(460, 760, 410, entries);
    screen.setFocused(null);
  }

  private void changeTags(List<UUID> ids, int mask, boolean enabled) {
    if (!screen.session.valid() || screen.session.busy()) return;
    var present = ids.stream().filter(id -> screen.session.locate(id) != null).toList();
    screen.prefs.setTags(present, mask, enabled);
    screen.applyFilters();
    screen.updateButtons();
  }

  private String abilityLabel(String id) {
    for (PcPokemon e : screen.all)
      if (e.pokemon().getAbility().getName().equals(id)) return PcPokemon.ability(e.pokemon());
    return id;
  }

  private String boxName(int index) {
    Text label = screen.session.pc.getBoxes().get(index).getName();
    String name = label == null ? "" : label.getString();
    return name.isBlank() ? PcLang.tr("boite") + (index + 1) : name;
  }

  void boxesMenu() {
    List<PcMenu.Entry> entries = new ArrayList<>();
    for (int b = 0; b < screen.session.pc.getBoxes().size(); b++) {
      var slots = screen.session.pc.getBoxes().get(b).getSlots();
      entries.add(
          new PcMenu.Entry(
              boxName(b)
                  + "   "
                  + slots.stream().filter(Objects::nonNull).count()
                  + "/"
                  + slots.size(),
              () -> {},
              b));
    }

    screen.menu = new PcMenu(40, 734, 270, entries);
    screen.setFocused(null);
  }

  String speciesLabel() {
    return PcLang.tr(
            switch (screen.species) {
              case "@duplicates" -> "species_duplicates";
              case "@legendary" -> "species_legendary";
              default -> "species_all";
            })
        + " ▾";
  }

  void speciesMenu() {
    choices(
        40,
        734,
        250,
        List.of(
            PcLang.tr("species_all"),
            PcLang.tr("species_duplicates"),
            PcLang.tr("species_legendary")),
        i -> {
          screen.species = new String[] {"", "@duplicates", "@legendary"}[i];
          screen.rebuild();
        });
  }

  private void itemMenu() {
    Map<String, String> options = new TreeMap<>();
    for (PcPokemon entry : screen.all) {
      Pokemon p = entry.pokemon();
      if (!p.heldItemNoCopy$common().isEmpty())
        options.put(PcPokemon.itemId(p), p.heldItemNoCopy$common().getName().getString());
    }
    Consumer<String> pick =
        value -> {
          screen.item = value;
          screen.rebuild();
        };
    List<PcMenu.Entry> entries = new ArrayList<>();
    entries.add(new PcMenu.Entry(PcLang.tr("tous_2ff599"), () -> pick.accept("")));
    entries.add(new PcMenu.Entry(PcLang.tr("sans_objet"), () -> pick.accept("@none")));
    entries.add(new PcMenu.Entry(PcLang.tr("avec_objet"), () -> pick.accept("@any")));
    options.entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .forEach(e -> entries.add(new PcMenu.Entry(e.getValue(), () -> pick.accept(e.getKey()))));
    screen.menu = new PcMenu(830, 708, 250, entries);
    screen.setFocused(null);
  }

  private void protectionMenu() {
    screen.menu =
        new PcMenu(
            800,
            734,
            280,
            List.of(
                new PcMenu.Entry(PcLang.tr("favoris_toujours_proteges"), () -> {}),
                new PcMenu.Entry(
                    PcLang.tr("exclure_les_shinies") + yes(screen.prefs.excludeShiny),
                    () -> {
                      screen.prefs.excludeShiny = !screen.prefs.excludeShiny;
                      screen.prefs.changed();
                    }),
                new PcMenu.Entry(
                    PcLang.tr("exclure_les_marques") + yes(screen.prefs.excludeMarked),
                    () -> {
                      screen.prefs.excludeMarked = !screen.prefs.excludeMarked;
                      screen.prefs.changed();
                    }),
                new PcMenu.Entry(
                    PcLang.tr("exclure_les_objets") + yes(screen.prefs.excludeItems),
                    () -> {
                      screen.prefs.excludeItems = !screen.prefs.excludeItems;
                      screen.prefs.changed();
                    })));
  }

  private static String yes(boolean value) {
    return value ? PcLang.tr("oui") : PcLang.tr("non");
  }

  private void presetsMenu() {
    List<PcMenu.Entry> entries = new ArrayList<>();
    screen.prefs.presets.forEach(
        (name, filters) -> entries.add(new PcMenu.Entry(name, () -> screen.loadFilters(filters))));
    if (!screen.prefs.presets.isEmpty())
      entries.add(new PcMenu.Entry(PcLang.tr("supprimer_une_recherche"), this::deletePresetMenu));
    if (entries.isEmpty()) entries.add(new PcMenu.Entry(PcLang.tr("no_saved_searches"), () -> {}));
    screen.menu = new PcMenu(670, 682, 280, entries);
  }

  private void deletePresetMenu() {
    List<PcMenu.Entry> entries = new ArrayList<>();
    for (String name : screen.prefs.presets.keySet())
      entries.add(
          new PcMenu.Entry(
              PcLang.tr("supprimer") + name,
              () -> {
                screen.prefs.deletePreset(name);
                screen.rebuild();
              }));
    screen.menu = new PcMenu(250, 786, 280, entries);
  }
}
