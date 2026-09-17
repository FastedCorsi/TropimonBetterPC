package fr.tropimon.betterpc;

import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.net.messages.server.storage.pc.UnlinkPlayerFromPCPacket;
import com.cobblemon.mod.common.pokemon.Pokemon;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Owns interaction and selection; PcView only draws the collection. */
public class BetterPcScreen extends Screen {
  static final int W = 1120, H = 856, COLS = 5, PAGE = 15, GX = 39, GY = 112, CW = 210, CH = 162;
  static final String[] TYPES = {
    "",
    "normal",
    "fire",
    "water",
    "electric",
    "grass",
    "ice",
    "fighting",
    "poison",
    "ground",
    "flying",
    "psychic",
    "bug",
    "rock",
    "ghost",
    "dragon",
    "dark",
    "steel",
    "fairy"
  };

  static String[] sorts() {
    return new String[] {
      PcLang.tr("emplacement"),
      PcLang.tr("nom"),
      PcLang.tr("niveau"),
      PcLang.tr("iv"),
      PcLang.tr("recents")
    };
  }

  private final PcFiltersPanel filterMenus = new PcFiltersPanel(this);
  final PcSession session;
  final PcPreferences prefs;
  final List<PcPokemon> all = new ArrayList<>(), visible = new ArrayList<>();
  final Set<UUID> selected = new LinkedHashSet<>();
  final Map<UUID, PcPortrait> portraits = new HashMap<>();
  final PcTools tools = new PcTools(this);
  private PcButton compare, speciesButton, sizeButton, tagEditor;
  PcButton deleteSearch;
  final PcPortrait dragPortrait = new PcPortrait();
  private final List<PcButton> buttons = new ArrayList<>();
  private final List<TextFieldWidget> fields = new ArrayList<>();
  UUID focusId, anchor, moving, pressed, movesPreview;
  int movesX, movesY;
  PcSession.Place focusedPlace, moveSource, pressedSource;
  private int pressX, pressY;
  boolean dragging, multiple, reverse, keepLink, closed, savingPreset, days, favoritesOnly;
  int tag, shiny, gender, perfect, size, box = -1, sort, page, ticks, usedBoxes, capacity;
  String searchText = "", minText = "", maxText = "", ageText = "";
  String type = "", ability = "", nature = "", item = "", species = "";
  PcAbilitySearch abilitySearch;
  PcFilter filter;
  private long revision = -1;
  private float scale = 1;
  private double offsetX, offsetY;
  PcMenu menu;
  List<ReleaseBatch.Target> confirmation;
  int confirmPage, protectedCount;
  boolean confirmationValid;
  TextFieldWidget presetName;
  private PcButton confirm,
      cancel,
      confirmPrev,
      confirmNext,
      add,
      release,
      nativePc,
      multi,
      selectPage,
      clearSelection,
      savePreset,
      cancelPreset;

  public BetterPcScreen(PCGUI original) {
    super(Text.literal("Tropimon Better PC"));
    session = new PcSession(original);
    prefs = BetterPcClient.preferences();
    restoreFilters(prefs.lastFilters);
    sort = prefs.lastSort;
    reverse = prefs.lastReverse;
  }

  public UUID storeId() {
    return session.pc.getUuid();
  }

  PcPreferences.Filters snapshot() {
    return new PcPreferences.Filters(
        searchText,
        minText,
        maxText,
        "",
        ageText,
        days,
        shiny,
        gender,
        type,
        ability,
        nature,
        item,
        perfect,
        favoritesOnly,
        tag,
        species,
        size);
  }

  void loadFilters(PcPreferences.Filters f) {
    restoreFilters(f);
    rebuild();
  }

  private void restoreFilters(PcPreferences.Filters f) {
    searchText = f.search();
    minText = "";
    maxText = "";
    // Ignore the obsolete IV percentage in old saved searches.
    ageText = f.age();
    days = f.days();
    shiny = Math.clamp(f.shiny(), 0, 2);
    gender = Math.clamp(f.gender(), 0, 3);
    type = f.type();
    ability = f.ability();
    nature = "";
    species = Set.of("@duplicates", "@legendary").contains(f.species()) ? f.species() : "";
    item = f.item();
    perfect = 0;
    favoritesOnly = f.favoritesOnly();
    tag = Math.clamp(f.tag(), -1, 15);
    size = PcSize.supported() ? Math.clamp(f.size(), PcSize.ALL, PcSize.ALPHA) : PcSize.ALL;
    box = -1;
  }

  @Override
  protected void init() {
    textRenderer = client.textRenderer;
    scale = 0.82f * Math.min(1.5f, Math.min(width / (float) W, height / (float) H));
    offsetX = (width - W * scale) / 2;
    offsetY = (height - H * scale) / 2;
    buttons.clear();
    fields.clear();
    menu = null;
    abilitySearch = null;
    movesPreview = null;
    filterMenus.build();
    tagEditor =
        button(460, 736, 200, PcLang.tr("tags_edit"), () -> filterMenus.tagsMenu(tagTargets()));
    button(
        670,
        710,
        140,
        PcLang.tr("tri") + sorts()[sort] + " ▾",
        () ->
            filterMenus.choices(
                670,
                734,
                210,
                List.of(sorts()),
                i -> {
                  sort = i;
                  rebuild();
                }));
    button(
        818,
        710,
        52,
        reverse ? PcLang.tr("inverse") : PcLang.tr("ordre"),
        () -> {
          reverse = !reverse;
          rebuild();
        });
    multi =
        button(
            40,
            736,
            200,
            PcLang.tr("selection_multiple"),
            () -> {
              multiple = !multiple;
              cancelMove();
              selected.clear();
              updateButtons();
            });
    button(880, 600, 32, "<", () -> page = Math.max(0, page - 1));
    button(1048, 600, 32, ">", () -> page = Math.min(pages() - 1, page + 1));
    selectPage = button(250, 736, 200, PcLang.tr("cette_page"), this::selectPage);
    clearSelection = button(460, 762, 200, PcLang.tr("deselectionner"), selected::clear);
    release = button(670, 762, 410, PcLang.tr("relacher_la_selection"), this::reviewRelease);
    release.danger = true;
    add = button(880, 736, 200, PcLang.tr("ajouter_a_l_equipe"), this::quickTransfer);
    add.accent = true;
    nativePc = button(894, 24, 164, PcLang.tr("pc_classique_options"), this::openNative);
    speciesButton = button(40, 710, 200, filterMenus.speciesLabel(), filterMenus::speciesMenu);
    sizeButton = button(460, 658, 200, filterMenus.sizeLabel(), filterMenus::sizeMenu);
    compare =
        button(670, 736, 200, PcLang.tr("tools_compare"), () -> tools.open(PcTools.Dialog.COMPARE));
    if (PcTeamBuilder.available())
      button(716, 24, 168, "TeamBuilder", () -> PcTeamBuilder.open(this));
    button(1076, 24, 28, "X", this::close);
    confirm = button(566, 539, 235, PcLang.tr("confirmer_le_relachement"), this::confirmRelease);
    confirm.danger = true;
    cancel = button(319, 539, 180, PcLang.tr("annuler"), () -> confirmation = null);
    confirmPrev = button(319, 502, 32, "<", () -> confirmPage = Math.max(0, confirmPage - 1));
    confirmNext =
        button(
            769,
            502,
            32,
            ">",
            () -> confirmPage = Math.min((confirmation.size() - 1) / 10, confirmPage + 1));
    presetName =
        new PcInput(textRenderer, 355, 344, 410, 22, Text.literal(PcLang.tr("nom_de_recherche")));
    presetName.setMaxLength(40);
    presetName.setRenderTextProvider((value, start) -> PcText.label(value).asOrderedText());
    addDrawableChild(presetName);
    savePreset =
        button(
            568,
            389,
            197,
            PcLang.tr("enregistrer"),
            () -> {
              if (prefs.putPreset(presetName.getText(), snapshot())) closePreset();
              else session.message = PcLang.tr("nom_requis_maximum_20_recherches");
            });
    cancelPreset = button(355, 389, 197, PcLang.tr("annuler"), this::closePreset);
    refresh();
    applyFilters();
    updateButtons();
  }

  private void closePreset() {
    savingPreset = false;
    setFocused(null);
  }

  void rebuild() {
    page = 0;
    selected.clear();
    anchor = null;
    menu = null;
    setFocused(null);
    clearChildren();
    init();
  }

  TextFieldWidget field(
      int x, int y, int w, String text, String placeholder, Consumer<String> setter) {
    TextFieldWidget field =
        new PcInput(textRenderer, x + 5, y + 4, w - 10, 15, Text.literal(placeholder));
    field.setDrawsBackground(false);
    if (placeholder.equals("X")) ((PcInput) field).centered = true;
    field.setMaxLength(160);
    field.setText(text);
    field.setPlaceholder(PcText.label(placeholder));
    field.setRenderTextProvider((value, start) -> PcText.label(value).asOrderedText());
    field.setChangedListener(
        value -> {
          setter.accept(value);
          page = 0;
          selected.clear();
          anchor = null;
          applyFilters();
        });
    fields.add(field);
    return addDrawableChild(field);
  }

  PcButton button(int x, int y, int w, String label, Runnable action) {
    PcButton b = new PcButton(x, y, w, label, action);
    buttons.add(b);
    return addDrawableChild(b);
  }

  static String typeLabel(String name) {
    return Text.translatable("cobblemon.type." + name.toLowerCase(Locale.ROOT)).getString();
  }

  private void refresh() {
    all.clear();
    usedBoxes = 0;
    capacity = 0;
    for (int b = 0; b < session.pc.getBoxes().size(); b++) {
      var slots = session.pc.getBoxes().get(b).getSlots();
      capacity += slots.size();
      boolean used = false;
      for (int s = 0; s < slots.size(); s++)
        if (slots.get(s) != null) {
          all.add(PcPokemon.of(slots.get(s), b, s));
          used = true;
        }

      if (used) usedBoxes++;
    }

    if (box >= session.pc.getBoxes().size()) box = -1;
    revision = BetterPcClient.revision();
    focusedPlace = session.locate(focusId);
  }

  void applyFilters() {
    filter =
        PcFilter.parse(searchText, minText, maxText, "", ageText, days, shiny, gender, type, box);
    visible.clear();
    long now = System.currentTimeMillis();
    var advanced = snapshot();
    prefs.rememberFilters(advanced, sort, reverse);
    for (PcPokemon entry : all)
      if (entry.matches(filter, now) && entry.matchesAdvanced(advanced, prefs)) visible.add(entry);
    if (species.equals("@duplicates")) {
      Map<String, Integer> groups = new HashMap<>();
      for (var entry : visible) groups.merge(PcPokemon.group(entry.pokemon()), 1, Integer::sum);
      visible.removeIf(entry -> groups.get(PcPokemon.group(entry.pokemon())) < 2);
    }
    Comparator<PcPokemon> order =
        switch (sort) {
          case 1 ->
              Comparator.comparing(
                  e -> DetectionHistory.normalize(e.pokemon().getDisplayName(false).getString()));
          case 2 -> Comparator.comparingInt((PcPokemon e) -> e.pokemon().getLevel()).reversed();
          case 3 -> Comparator.comparingDouble(PcPokemon::iv).reversed();
          case 4 ->
              Comparator.comparingLong(
                      (PcPokemon e) -> BetterPcClient.detectedAt(e.pokemon().getUuid()))
                  .reversed();
          default -> Comparator.comparingInt(PcPokemon::box).thenComparingInt(PcPokemon::slot);
        };
    if (reverse) order = order.reversed();
    if (species.equals("@duplicates"))
      order =
          Comparator.comparing((PcPokemon e) -> PcPokemon.group(e.pokemon())).thenComparing(order);
    visible.sort(order.thenComparingInt(PcPokemon::box).thenComparingInt(PcPokemon::slot));
    Set<UUID> ids = new HashSet<>();
    for (PcPokemon entry : visible) ids.add(entry.pokemon().getUuid());
    selected.retainAll(ids);
    if (focusedPlace != null && !focusedPlace.party() && !ids.contains(focusId)) {
      focusId = null;
      focusedPlace = null;
    }

    page = Math.min(page, pages() - 1);
  }

  List<UUID> tagTargets() {
    Collection<UUID> ids =
        selected.isEmpty() ? (focusId == null ? List.of() : List.of(focusId)) : selected;
    return ids.stream().filter(id -> session.locate(id) != null).toList();
  }

  int pages() {
    return Math.max(1, (visible.size() + PAGE - 1) / PAGE);
  }

  @Override
  public void tick() {
    if (!session.valid()) {
      closeFromServer();
      return;
    }

    session.tick();
    tools.tick();
    ticks++;
    if (revision != BetterPcClient.revision()) {
      refresh();
      applyFilters();
    } else if (ticks % 20 == 0 && filter != null && filter.duration() > 0) applyFilters();
    if (confirmation != null && ticks % 5 == 0)
      confirmationValid = confirmation.stream().allMatch(session::matches);
    updateButtons();
  }

  void updateButtons() {
    boolean modal = confirmation != null || savingPreset || tools.modal();
    for (PcButton b : buttons) {
      b.visible = !modal;
      b.active = !session.busy();
    }

    for (TextFieldWidget f : fields) {
      f.setVisible(!modal);
      f.setEditable(!session.busy());
    }

    confirm.visible =
        cancel.visible = confirmPrev.visible = confirmNext.visible = confirmation != null;
    presetName.setVisible(savingPreset);
    savePreset.visible = cancelPreset.visible = savingPreset;
    if (tools.modal()) return;
    if (confirmation != null) {
      confirm.active = !session.busy() && confirmationValid;
      cancel.active = true;
      return;
    }

    if (savingPreset) {
      savePreset.active = prefs.available && !presetName.getText().isBlank();
      cancelPreset.active = true;
      return;
    }

    for (PcButton b : buttons) if (b.getMessage().getString().equals("X")) b.active = true;
    boolean available = !session.busy(), focused = session.at(focusedPlace) != null;
    deleteSearch.active = available && prefs.available && !prefs.presets.isEmpty();
    tagEditor.active = available && prefs.available && (!selected.isEmpty() || focused);
    compare.active = available && selected.size() == 2;
    compare.setMessage(Text.literal(PcLang.tr("tools_compare") + " " + selected.size() + " / 2"));
    speciesButton.accent = !species.isEmpty();
    sizeButton.accent = size != PcSize.ALL;
    sizeButton.active = available && PcSize.supported();
    add.active = available && focused && !multiple;
    multi.setMessage(
        Text.literal(
            multiple ? PcLang.tr("selection_multiple_oui") : PcLang.tr("selection_multiple")));
    release.active = available && !selected.isEmpty() && prefs.available;
    clearSelection.active = available && !selected.isEmpty();
    selectPage.active = available && !visible.isEmpty();
    add.setMessage(
        Text.literal(
            focusedPlace != null && focusedPlace.party()
                ? PcLang.tr("deposer_dans_le_pc")
                : PcLang.tr("ajouter_a_l_equipe")));
  }

  private void selectPage() {
    multiple = true;
    cancelMove();
    int start = page * PAGE, end = Math.min(visible.size(), start + PAGE);
    for (int i = start; i < end; i++) selected.add(visible.get(i).pokemon().getUuid());
    updateButtons();
  }

  private void choose(int index) {
    PcPokemon entry = visible.get(index);
    UUID id = entry.pokemon().getUuid();
    if (moving != null) {
      finishMove(new PcSession.Place(entry.box(), entry.slot()));
      return;
    }

    focusId = id;
    focusedPlace = new PcSession.Place(entry.box(), entry.slot());
    boolean shift = hasShiftDown();
    multiple =
        PcSelection.click(
            selected,
            visible.stream().map(e -> e.pokemon().getUuid()).toList(),
            anchor,
            index,
            multiple,
            hasControlDown(),
            shift);
    if (!shift || anchor == null) anchor = id;
    setFocused(null);
    updateButtons();
  }

  private void beginMove() {
    moving = focusId;
    moveSource = session.locate(moving);
    session.message = PcLang.tr("choisissez_une_cible_ou_glissez_vers_une_boite_l_equipe");
  }

  private void cancelMove() {
    moving = pressed = null;
    moveSource = pressedSource = null;
    dragging = false;
  }

  private void finishMove(PcSession.Place target) {
    if (moveSource != null && moveSource.party() && target != null && !target.party())
      target = session.emptyPc(target.box());
    if (target == null) session.message = PcLang.tr("aucune_place_libre_dans_cette_boite");
    else if (!session.transfer(moving, moveSource, target))
      session.message = PcLang.tr("deplacement_annule_cible_modifiee_ou_indisponible");
    cancelMove();
    menu = null;
  }

  private void quickTransfer() {
    PcSession.Place from = session.locate(focusId);
    if (from == null) return;
    PcSession.Place to = from.party() ? session.emptyPc(box) : session.emptyParty();
    if (to == null && !from.party()) {
      beginMove();
      session.message = PcLang.tr("equipe_pleine_choisissez_le_membre_a_echanger");
    } else if (to == null)
      session.message = PcLang.tr("aucune_place_libre_dans_les_boites_choisies");
    else session.transfer(focusId, from, to);
  }

  private void reviewRelease() {
    if (!session.valid() || session.busy() || !prefs.available) return;
    List<ReleaseBatch.Target> targets = new ArrayList<>();
    protectedCount = 0;
    for (PcPokemon entry : visible)
      if (selected.contains(entry.pokemon().getUuid())) {
        Pokemon p = entry.pokemon();
        if (prefs.protectedFromRelease(
            p.getUuid(), p.getShiny(), PcPokemon.marked(p), !p.heldItemNoCopy$common().isEmpty())) {
          protectedCount++;
          continue;
        }

        var target = entry.target();
        if (!session.matches(target)) {
          session.message = PcLang.tr("stockage_modifie_refaites_la_selection");
          return;
        }

        targets.add(target);
      }

    if (targets.isEmpty()) {
      session.message =
          PcLang.tr("aucun_relachement") + protectedCount + PcLang.tr("pokemon_protege_s");
      return;
    }

    confirmation = List.copyOf(targets);
    confirmPage = 0;
    confirmationValid = true;
    cancelMove();
    menu = null;
    setFocused(null);
    updateButtons();
  }

  private void confirmRelease() {
    if (confirmation == null || !session.valid() || session.busy()) return;
    confirmationValid = confirmation.stream().allMatch(session::matches);
    if (!confirmationValid) return;
    session.batch.start(confirmation);
    confirmation = null;
    selected.clear();
    updateButtons();
  }

  private void openNative() {
    if (session.busy()) return;
    keepLink = true;
    client.setScreen(
        new PCGUI(
            session.pc,
            session.party,
            session.original.getConfiguration(),
            box < 0 ? session.original.getOpenOnBox() : box,
            session.original.getUnseenWallpapers()));
  }

  public void closeFromServer() {
    keepLink = true;
    close();
  }

  @Override
  public void close() {
    session.batch.cancel();
    tools.close();
    client.setScreen(null);
  }

  @Override
  public void removed() {
    session.batch.cancel();
    prefs.save();
    BetterPcClient.save();
    portraits.clear();
    if (!closed && !keepLink && client.getNetworkHandler() == session.connection)
      new UnlinkPlayerFromPCPacket().sendToServer();
    closed = true;
  }

  @Override
  public boolean shouldPause() {
    return false;
  }

  @Override
  public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}

  private int mx(double x) {
    return (int) ((x - offsetX) / scale);
  }

  private int my(double y) {
    return (int) ((y - offsetY) / scale);
  }

  int cardAt(int x, int y) {
    if (x < GX
        || x >= GX + CW * COLS
        || y < GY
        || y >= GY + CH * (PAGE / COLS)
        || (x - GX) % CW >= CW - 8
        || (y - GY) % CH >= CH - 8) return -1;
    int index = page * PAGE + (y - GY) / CH * COLS + (x - GX) / CW;
    return index < visible.size() ? index : -1;
  }

  private int partyAt(int x, int y) {
    return x >= 24 && x < 1096 && y >= 58 && y < 102 && (x - 24) % 180 < 172 ? (x - 24) / 180 : -1;
  }

  @Override
  public boolean charTyped(char chr, int modifiers) {
    if (abilitySearch != null) {
      abilitySearch.input.charTyped(chr, modifiers);
      return true;
    }
    return tools.modal() || menu != null || dragging || super.charTyped(chr, modifiers);
  }

  @Override
  public boolean mouseClicked(double x, double y, int button) {
    int ux = mx(x), uy = my(y);
    updateButtons();
    if (abilitySearch != null) {
      abilitySearch.click(ux, uy, button);
      return true;
    }
    if (tools.modal()) return tools.click(ux, uy, button);
    if (confirmation != null || savingPreset) return super.mouseClicked(ux, uy, button);
    if (button == 1) {
      cancelMove();
      menu = null;
      movesPreview = null;
      return true;
    }

    if (menu != null) {
      int index = menu.index(ux, uy);
      PcMenu old = menu;
      menu = null;
      if (index >= 0 && button == 0 && !session.busy()) old.entries.get(index).action().run();
      return true;
    }

    if (movesPreview != null) {
      movesPreview = null;
      return true;
    }

    if (super.mouseClicked(ux, uy, button)) return true;
    if (button != 0 || session.busy()) return false;
    int index = cardAt(ux, uy), slot = partyAt(ux, uy);
    if (index >= 0) {
      PcPokemon entry = visible.get(index);
      int cardX = GX + (index % PAGE) % COLS * CW;
      int cardY = GY + (index % PAGE) / COLS * CH;
      if (ux >= cardX + CW - 29 && uy < GY + ((index % PAGE) / COLS) * CH + 23) {
        prefs.toggleFavorite(entry.pokemon().getUuid());
        applyFilters();
        return true;
      }

      if (moving == null
          && ux >= cardX + 108
          && ux < cardX + 165
          && uy >= cardY + 59
          && uy < cardY + 77) {
        movesPreview = entry.pokemon().getUuid();
        movesX = cardX + 100;
        movesY = cardY + 58;
        setFocused(null);
        return true;
      }

      if (moving != null || hasControlDown() || hasShiftDown()) {
        choose(index);
      } else {
        pressed = entry.pokemon().getUuid();
        pressedSource = session.locate(pressed);
        pressX = ux;
        pressY = uy;
      }

      return true;
    }

    if (slot >= 0) {
      if (moving != null) finishMove(new PcSession.Place(-1, slot));
      else {
        Pokemon p = session.at(new PcSession.Place(-1, slot));
        pressed = p == null ? null : p.getUuid();
        pressedSource = p == null ? null : new PcSession.Place(-1, slot);
        pressX = ux;
        pressY = uy;
      }

      updateButtons();
      return true;
    }

    return false;
  }

  @Override
  public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
    if (tools.modal() || abilitySearch != null) return true;
    int ux = mx(x), uy = my(y);
    if (pressed != null
        && pressedSource != null
        && button == 0
        && !session.busy()
        && confirmation == null
        && !savingPreset) {
      if (!dragging && Math.abs(ux - pressX) + Math.abs(uy - pressY) >= 5) {
        moving = pressed;
        moveSource = pressedSource;
        dragging = true;
      }

      if (dragging && ux >= 40 && ux < 240 && uy >= 710 && uy < 732 && menu == null)
        filterMenus.boxesMenu();
      return true;
    }

    return super.mouseDragged(ux, uy, button, dx / scale, dy / scale);
  }

  @Override
  public boolean mouseReleased(double x, double y, int button) {
    if (tools.modal() || abilitySearch != null) return true;
    int ux = mx(x), uy = my(y);
    if (dragging) {
      int index = cardAt(ux, uy),
          slot = partyAt(ux, uy),
          mi = menu == null ? -1 : menu.index(ux, uy);
      if (mi >= 0 && menu.entries.get(mi).box() >= 0)
        finishMove(session.emptyPc(menu.entries.get(mi).box()));
      else if (menu == null && slot >= 0) finishMove(new PcSession.Place(-1, slot));
      else if (menu == null && index >= 0) {
        var e = visible.get(index);
        finishMove(new PcSession.Place(e.box(), e.slot()));
      } else if (menu == null
          && moveSource.party()
          && ux >= GX
          && ux < GX + COLS * CW
          && uy >= GY
          && uy < GY + 3 * CH) {
        finishMove(session.emptyPc(box));
      } else {
        cancelMove();
        menu = null;
        session.message = PcLang.tr("glisser_deposer_annule");
      }

      return true;
    }

    if (pressed != null && pressedSource != null && button == 0 && !session.busy()) {
      if (pressedSource.party()) {
        focusId = pressed;
        focusedPlace = session.locate(pressed);
        selected.clear();
        multiple = false;
      } else {
        int index = cardAt(ux, uy);
        if (index >= 0 && visible.get(index).pokemon().getUuid().equals(pressed)) choose(index);
      }
      updateButtons();
    }
    pressed = null;
    pressedSource = null;
    return super.mouseReleased(ux, uy, button);
  }

  @Override
  public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
    if (abilitySearch != null) {
      abilitySearch.scroll(vertical);
      return true;
    }
    if (tools.modal()) {
      return true;
    }

    if (movesPreview != null) movesPreview = null;
    else if (menu != null) menu.scroll(vertical);
    else if (confirmation != null)
      confirmPage =
          Math.clamp(confirmPage - (int) Math.signum(vertical), 0, (confirmation.size() - 1) / 10);
    else if (!savingPreset && mx(x) >= GX && mx(x) < GX + COLS * CW)
      page = Math.clamp(page - (int) Math.signum(vertical), 0, pages() - 1);
    return true;
  }

  @Override
  public boolean keyPressed(int key, int scan, int mods) {
    if (abilitySearch != null) {
      abilitySearch.key(key, scan, mods);
      return true;
    }
    if (tools.modal()) {
      if (key == GLFW.GLFW_KEY_ESCAPE) tools.escape();
      return true;
    }

    if (key == GLFW.GLFW_KEY_ESCAPE) {
      if (movesPreview != null) movesPreview = null;
      else if (menu != null) menu = null;
      else if (confirmation != null) confirmation = null;
      else if (savingPreset) closePreset();
      else if (moving != null) cancelMove();
      else close();
      return true;
    }

    if (menu != null) return true;
    if (confirmation == null
        && !savingPreset
        && !(getFocused() instanceof TextFieldWidget)
        && client.options.inventoryKey.matchesKey(key, scan)) {
      close();
      return true;
    }

    return super.keyPressed(key, scan, mods);
  }

  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    int ux = mx(mouseX), uy = my(mouseY);
    context.getMatrices().push();
    context.getMatrices().translate(offsetX, offsetY, 0);
    context.getMatrices().scale(scale, scale, 1);
    PcView.draw(this, context, textRenderer, ux, uy);
    updateButtons();
    if (confirmation != null || savingPreset) context.getMatrices().translate(0, 0, 2000);
    super.render(context, ux, uy, delta);
    PcView.overlays(this, context, textRenderer, ux, uy);
    if (tools.modal()) tools.draw(context, textRenderer, ux, uy, delta);
    context.getMatrices().pop();
  }
}
