package fr.tropimon.betterpc;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PcTagsTest {
  @TempDir Path root;

  @Test
  void multipleTagsSurviveReloadAndCanBeRemovedIndependently() {
    Path file = root.resolve("settings.json");
    var prefs = PcPreferences.load(file);
    UUID id = UUID.randomUUID();
    for (int tag : List.of(1, 2, 4, 8, 16)) assertTrue(prefs.setTags(List.of(id), tag, true));
    prefs = PcPreferences.load(file);
    assertEquals(31, prefs.tags(id));
    assertTrue(prefs.setTags(List.of(id), 2, false));
    assertEquals(29, PcPreferences.load(file).tags(id));
    assertEquals(0, PcPreferences.load(root.resolve("other-world.json")).tags(id));
    prefs.setTags(List.of(id), PcPreferences.ALL_TAGS, false);
    assertEquals(0, PcPreferences.load(file).tags(id));
  }

  @Test
  void bulkChangesPreserveOtherTagsAndOtherPokemon() {
    var prefs = PcPreferences.load(root.resolve("settings.json"));
    UUID a = UUID.randomUUID(), b = UUID.randomUUID(), c = UUID.randomUUID();
    prefs.setTags(List.of(a), 2, true);
    prefs.setTags(List.of(c), 4, true);
    prefs.setTags(List.of(a, b, a), 1, true);
    assertEquals(3, prefs.tags(a));
    assertEquals(1, prefs.tags(b));
    prefs.setTags(List.of(a, b), 1, false);
    assertEquals(2, prefs.tags(a));
    assertEquals(0, prefs.tags(b));
    assertEquals(4, prefs.tags(c));
  }

  @Test
  void allUntaggedAndEachTagFilterUseMembership() {
    var prefs = new PcPreferences();
    UUID id = UUID.randomUUID();
    assertTrue(prefs.matchesTags(id, 0));
    assertTrue(prefs.matchesTags(id, -1));
    for (int tag : List.of(1, 2, 4, 8, 16)) {
      prefs.setTags(List.of(id), PcPreferences.ALL_TAGS, false);
      prefs.setTags(List.of(id), tag, true);
      assertTrue(prefs.matchesTags(id, tag));
      assertTrue(prefs.matchesTags(id, 0));
      assertFalse(prefs.matchesTags(id, -1));
      for (int other : List.of(1, 2, 4, 8, 16))
        if (other != tag) assertFalse(prefs.matchesTags(id, other));
    }
  }

  @Test
  void tagsNeverDisableFavoritesOrNativeExclusions() {
    var prefs = new PcPreferences();
    UUID id = UUID.randomUUID();
    prefs.toggleFavorite(id);
    prefs.setTags(List.of(id), PcPreferences.ALL_TAGS, true);
    prefs.setTags(List.of(id), PcPreferences.ALL_TAGS, false);
    assertTrue(prefs.protectedFromRelease(id, false, false, false));
    prefs.toggleFavorite(id);
    prefs.setTags(List.of(id), 1, true);
    assertTrue(prefs.protectedFromRelease(id, true, false, false));
    assertTrue(prefs.protectedFromRelease(id, false, true, false));
    assertTrue(prefs.protectedFromRelease(id, false, false, true));
    assertFalse(prefs.protectedFromRelease(id, false, false, false));
  }

  @Test
  void taggedSearchSurvivesReloadAndOldSearchDefaultsToAllTags() throws Exception {
    Path file = root.resolve("settings.json");
    var prefs = PcPreferences.load(file);
    var filter =
        new PcPreferences.Filters(
            "",
            "",
            "",
            "",
            "",
            false,
            0,
            0,
            "fire",
            "",
            "",
            "",
            0,
            false,
            4,
            "cobblemon:charmander",
            PcSize.XL);
    prefs.putPreset("Training", filter);
    assertEquals(filter, PcPreferences.load(file).presets.get("Training"));
    Files.writeString(file, Files.readString(file).replace(",\"tag\":4", ""));
    assertEquals(0, PcPreferences.load(file).presets.get("Training").tag());
    Files.writeString(
        file, Files.readString(file).replace(",\"species\":\"cobblemon:charmander\"", ""));
    assertEquals("", PcPreferences.load(file).presets.get("Training").species());
    Files.writeString(file, Files.readString(file).replace(",\"size\":5", ""));
    assertEquals(PcSize.ALL, PcPreferences.load(file).presets.get("Training").size());
  }

  @Test
  void invalidTagDataFailsClosedAndOriginalIsPreserved() throws Exception {
    Path file = root.resolve("settings.json");
    String invalid = "{\"tags\":{\"" + UUID.randomUUID() + "\":32}}";
    Files.writeString(file, invalid);
    var prefs = PcPreferences.load(file);
    assertFalse(prefs.available);
    assertFalse(prefs.setTags(List.of(UUID.randomUUID()), 1, true));
    assertEquals(invalid, Files.readString(file));
    assertTrue(prefs.protectedFromRelease(UUID.randomUUID(), false, false, false));
  }

  @Test
  void raidExtendsOldTagsAndItsSearchPersists() throws Exception {
    Path file = root.resolve("settings.json");
    UUID id = UUID.randomUUID();
    Files.writeString(file, "{\"tags\":{\"" + id + "\":15}}");
    var prefs = PcPreferences.load(file);
    assertTrue(prefs.available);
    assertEquals(15, prefs.tags(id));
    assertFalse(prefs.matchesTags(id, 16));
    assertTrue(prefs.setTags(List.of(id), 16, true));
    var filter = new PcPreferences.Filters(
        "", "", "", "", "", false, 0, 0, "", "", "", "", 0, false, 16, "@alpha", PcSize.ALL);
    prefs.putPreset("Raid", filter);
    prefs.rememberFilters(filter, 0, false);
    prefs.save();
    prefs = PcPreferences.load(file);
    assertTrue(prefs.available);
    assertEquals(31, prefs.tags(id));
    assertEquals(filter, prefs.lastFilters);
    assertEquals(filter, prefs.presets.get("Raid"));
    assertTrue(prefs.matchesTags(id, 16));
    assertTrue(prefs.setTags(List.of(id), 16, false));
    assertEquals(15, PcPreferences.load(file).tags(id));
    assertFalse(prefs.matchesTags(id, 16));
  }
}
