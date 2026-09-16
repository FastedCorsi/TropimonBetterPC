package fr.tropimon.betterpc;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PcPreferencesTest {
  @TempDir Path root;

  @Test
  void serverLegacyDataMergesOnceIntoSharedAccountWithoutResurrectingRemovedTags()
      throws Exception {
    Path legacy = root.resolve("legacy-a.json"), shared = root.resolve("account.json");
    UUID id = UUID.randomUUID();
    var old = PcPreferences.load(legacy);
    old.toggleFavorite(id);
    old.setTags(java.util.List.of(id), 1, true);
    old.putPreset("Training", PcPreferences.Filters.empty());
    String original = Files.readString(legacy);
    var account = PcPreferences.load(shared);
    account.importLegacy(legacy, "a".repeat(64), true);
    assertTrue(account.favorites.contains(id));
    assertEquals(1, account.tags(id));
    account.toggleFavorite(id);
    account.setTags(java.util.List.of(id), 1, false);
    var restored = PcPreferences.load(shared);
    restored.importLegacy(legacy, "a".repeat(64), false);
    assertFalse(restored.favorites.contains(id));
    assertEquals(0, restored.tags(id));
    assertEquals(original, Files.readString(legacy));
    var filters =
        new PcPreferences.Filters(
            "lugia", "", "", "", "", false, 0, 0, "", "", "", "", 0, false, 0, "@legendary");
    restored.rememberFilters(filters, 3, true);
    Path second = root.resolve("legacy-b.json");
    var other = PcPreferences.load(second);
    other.setTags(java.util.List.of(id), 8, true);
    other.putPreset("Training", filters);
    restored.importLegacy(second, "b".repeat(64), false);
    var combined = PcPreferences.load(shared);
    assertEquals(8, combined.tags(id));
    assertEquals(2, combined.presets.size());
    assertEquals(filters, combined.presets.get("Training (2)"));
    assertEquals(filters, combined.lastFilters);
    assertEquals(3, combined.lastSort);
  }

  @Test
  void unreadableLegacyPreferencesCannotSilentlyLoseProtection() throws Exception {
    Path legacy = root.resolve("legacy.json");
    Files.writeString(legacy, "invalid fixture");
    var shared = PcPreferences.load(root.resolve("account.json"));
    shared.importLegacy(legacy, "a".repeat(64), true);
    assertFalse(shared.available);
    assertTrue(shared.protectedFromRelease(UUID.randomUUID(), false, false, false));
    assertEquals("invalid fixture", Files.readString(legacy));
  }

  @Test
  void lastFiltersAndSortSurviveReconnectWithoutCrossingAccounts() {
    Path file = root.resolve("scope-a.json");
    var prefs = PcPreferences.load(file);
    var filters =
        new PcPreferences.Filters(
            "lugia",
            "",
            "",
            "",
            "12",
            true,
            1,
            2,
            "psychic",
            "pressure",
            "",
            "@none",
            0,
            true,
            8,
            "@legendary");
    prefs.rememberFilters(filters, 3, true);
    prefs.save();
    var restored = PcPreferences.load(file);
    assertEquals(filters, restored.lastFilters);
    assertEquals(3, restored.lastSort);
    assertTrue(restored.lastReverse);
    assertTrue(restored.presets.isEmpty());
    assertEquals(
        PcPreferences.Filters.empty(),
        PcPreferences.load(root.resolve("scope-b.json")).lastFilters);
    restored.rememberFilters(PcPreferences.Filters.empty(), 0, false);
    restored.save();
    assertEquals(PcPreferences.Filters.empty(), PcPreferences.load(file).lastFilters);
  }

  @Test
  void invalidRememberedFiltersKeepFavoritesAndProtection() throws Exception {
    Path file = root.resolve("settings.json");
    UUID id = UUID.randomUUID();
    Files.writeString(file, "{\"favorites\":[\"" + id + "\"],\"lastFilters\":42}");
    var prefs = PcPreferences.load(file);
    assertTrue(prefs.available);
    assertEquals(PcPreferences.Filters.empty(), prefs.lastFilters);
    assertTrue(prefs.favorites.contains(id));
    assertTrue(prefs.protectedFromRelease(id, false, false, false));
  }

  @Test
  void oldPreferencesStartWithoutFiltersAndUnchangedStateDoesNotRewrite() throws Exception {
    Path file = root.resolve("settings.json");
    Files.writeString(file, "{}");
    var prefs = PcPreferences.load(file);
    assertEquals(PcPreferences.Filters.empty(), prefs.lastFilters);
    prefs.rememberFilters(PcPreferences.Filters.empty(), 0, false);
    prefs.save();
    assertEquals("{}", Files.readString(file));
  }

  @Test
  void favoritesAndPresetsSurviveReloadButStayAccountScoped() {
    Path file = root.resolve("scope-a.json");
    UUID id = UUID.randomUUID();
    PcPreferences prefs = PcPreferences.load(file);
    prefs.toggleFavorite(id);
    assertTrue(prefs.putPreset("Recherche", PcPreferences.Filters.empty()));
    PcPreferences loaded = PcPreferences.load(file);
    assertTrue(loaded.favorites.contains(id));
    assertEquals(PcPreferences.Filters.empty(), loaded.presets.get("Recherche"));
    assertFalse(PcPreferences.load(root.resolve("scope-b.json")).favorites.contains(id));
  }

  @Test
  void removedDisplaySettingsAreIgnoredWithoutLosingFavorites() throws Exception {
    Path file = root.resolve("settings.json");
    UUID id = UUID.randomUUID();
    Files.writeString(
        file, "{\"textScale\":1.3,\"colorblind\":true,\"favorites\":[\"" + id + "\"]}");
    var prefs = PcPreferences.load(file);
    assertTrue(prefs.available);
    assertTrue(prefs.favorites.contains(id));
    prefs.changed();
    assertFalse(Files.readString(file).contains("textScale"));
    assertFalse(Files.readString(file).contains("colorblind"));
    assertTrue(PcPreferences.load(file).favorites.contains(id));
  }

  @Test
  void deletingOneSavedSearchPersistsWithoutChangingFavoritesOrOtherSearches() {
    Path file = root.resolve("settings.json");
    PcPreferences prefs = PcPreferences.load(file);
    UUID id = UUID.randomUUID();
    prefs.toggleFavorite(id);
    prefs.putPreset("Keep", PcPreferences.Filters.empty());
    prefs.putPreset("Delete", PcPreferences.Filters.empty());
    assertTrue(prefs.deletePreset("Delete"));
    assertFalse(prefs.deletePreset("Missing"));
    PcPreferences loaded = PcPreferences.load(file);
    assertEquals(java.util.Set.of("Keep"), loaded.presets.keySet());
    assertTrue(loaded.favorites.contains(id));
    assertTrue(loaded.excludeShiny && loaded.excludeMarked && loaded.excludeItems);
    loaded.available = false;
    assertFalse(loaded.deletePreset("Keep"));
    assertTrue(loaded.presets.containsKey("Keep"));
  }

  @Test
  void favoritesCannotBeReleasedEvenWhenOtherExclusionsAreOff() {
    PcPreferences prefs = PcPreferences.load(root.resolve("settings.json"));
    UUID id = UUID.randomUUID();
    prefs.excludeShiny = prefs.excludeMarked = prefs.excludeItems = false;
    assertFalse(prefs.protectedFromRelease(id, true, true, true));
    prefs.toggleFavorite(id);
    assertTrue(prefs.protectedFromRelease(id, false, false, false));
    prefs.toggleFavorite(id);
    assertFalse(prefs.protectedFromRelease(id, false, false, false));
  }

  @Test
  void exclusionsAreIndependent() {
    PcPreferences prefs = PcPreferences.load(root.resolve("settings.json"));
    UUID id = UUID.randomUUID();
    assertTrue(prefs.protectedFromRelease(id, true, false, false));
    prefs.excludeShiny = false;
    assertFalse(prefs.protectedFromRelease(id, true, false, false));
    assertTrue(prefs.protectedFromRelease(id, false, true, false));
    assertTrue(prefs.protectedFromRelease(id, false, false, true));
  }

  @Test
  void corruptFileIsPreservedAndReleaseFailsClosed() throws Exception {
    Path file = root.resolve("settings.json");
    Files.writeString(file, "invalid fixture");
    PcPreferences prefs = PcPreferences.load(file);
    prefs.toggleFavorite(UUID.randomUUID());
    prefs.changed();
    assertFalse(prefs.available);
    assertTrue(prefs.protectedFromRelease(UUID.randomUUID(), false, false, false));
    assertEquals("invalid fixture", Files.readString(file));
  }

  @Test
  void presetNamesAndCountAreBoundedAndReplacementWorks() {
    PcPreferences prefs = PcPreferences.load(root.resolve("settings.json"));
    assertFalse(prefs.putPreset("  ", PcPreferences.Filters.empty()));
    for (int i = 0; i < 20; i++)
      assertTrue(prefs.putPreset("Preset " + i, PcPreferences.Filters.empty()));
    assertFalse(prefs.putPreset("Overflow", PcPreferences.Filters.empty()));
    assertTrue(prefs.putPreset("Preset 0", PcPreferences.Filters.empty()));
    assertEquals(20, prefs.presets.size());
  }
}
