package fr.tropimon.betterpc;

import com.google.gson.*;
import java.nio.file.*;
import java.util.*;

/** Account-wide local preferences, shared by every server and world. */
final class PcPreferences {
  record Filters(
      String search,
      String min,
      String max,
      String iv,
      String age,
      boolean days,
      int shiny,
      int gender,
      String type,
      String ability,
      String nature,
      String item,
      int perfect,
      boolean favoritesOnly,
      int tag,
      String species,
      int size) {
    Filters {
      species = species == null ? "" : species;
    }

    static Filters empty() {
      return new Filters(
          "", "", "", "", "", false, 0, 0, "", "", "", "", 0, false, 0, "", 0);
    }
  }

  // Stable bit positions in local files; labels are localized only for display.
  static final String[] TAG_KEYS = {"sale", "moveset", "ev_train", "pvp"};
  private final Map<UUID, Integer> tags = new HashMap<>();
  final Set<UUID> favorites = new HashSet<>();
  final Map<String, Filters> presets = new LinkedHashMap<>();
  boolean excludeShiny = true, excludeMarked = true, excludeItems = true;
  private final Set<String> importedScopes = new HashSet<>();
  Filters lastFilters = Filters.empty();
  int lastSort;
  boolean lastReverse;
  boolean available = true;
  private Path file;
  private boolean dirty;
  private static final Gson GSON = new Gson();

  static PcPreferences load(Path path) {
    PcPreferences prefs = new PcPreferences();
    prefs.file = path;
    if (path == null) {
      prefs.available = false;
      return prefs;
    }

    try {
      if (!Files.exists(path)) return prefs;
      // Accommodate the bounded favorites and tag maps together.
      if (Files.size(path) > 12_000_000) throw new IllegalStateException("Preferences too large");
      JsonObject json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
      if (json.has("favorites"))
        for (JsonElement value : json.getAsJsonArray("favorites")) {
          if (prefs.favorites.size() >= 100_000)
            throw new IllegalStateException("Too many favorites");
          prefs.favorites.add(UUID.fromString(value.getAsString()));
        }

      if (json.has("tags"))
        for (var entry : json.getAsJsonObject("tags").entrySet()) {
          int value = entry.getValue().getAsInt();
          if (prefs.tags.size() >= 100_000 || value <= 0 || value > 15)
            throw new IllegalStateException("Invalid local tags");
          prefs.tags.put(UUID.fromString(entry.getKey()), value);
        }
      if (json.has("excludeShiny")) prefs.excludeShiny = json.get("excludeShiny").getAsBoolean();
      if (json.has("excludeMarked")) prefs.excludeMarked = json.get("excludeMarked").getAsBoolean();
      if (json.has("excludeItems")) prefs.excludeItems = json.get("excludeItems").getAsBoolean();
      if (json.has("presets"))
        for (var entry : json.getAsJsonObject("presets").entrySet()) {
          if (prefs.presets.size() >= 1000)
            throw new IllegalStateException("Too many imported searches");
          Filters filters = GSON.fromJson(entry.getValue(), Filters.class);
          if (!validFilters(filters)) throw new IllegalStateException("Invalid preset");
          prefs.presets.put(
              entry.getKey().substring(0, Math.min(40, entry.getKey().length())), filters);
        }

      if (json.has("importedScopes")) {
        for (var value : json.getAsJsonArray("importedScopes")) {
          String scope = value.getAsString();
          if (!scope.matches("[0-9a-f]{64}") || prefs.importedScopes.size() >= 10000)
            throw new IllegalStateException("Invalid migration history");
          prefs.importedScopes.add(scope);
        }
      }
      if (json.has("lastFilters")) {
        try {
          Filters last = GSON.fromJson(json.get("lastFilters"), Filters.class);
          if (validFilters(last)) prefs.lastFilters = last;
          if (json.has("lastSort"))
            prefs.lastSort = Math.clamp(json.get("lastSort").getAsInt(), 0, 4);
          if (json.has("lastReverse")) prefs.lastReverse = json.get("lastReverse").getAsBoolean();
        } catch (RuntimeException ignored) {
          // An invalid remembered search must not discard existing protection settings.
          prefs.lastFilters = Filters.empty();
          prefs.lastSort = 0;
          prefs.lastReverse = false;
        }
      }

    } catch (Exception error) {
      prefs.available = false;
    }

    return prefs;
  }

  void importLegacy(Path legacyFile, String scope, boolean firstImport) {
    if (!available || importedScopes.contains(scope) || !Files.exists(legacyFile)) return;
    PcPreferences old = load(legacyFile);
    if (!old.available) {
      available = false;
      return;
    }
    var combinedFavorites = new HashSet<>(favorites);
    combinedFavorites.addAll(old.favorites);
    var combinedTags = new HashMap<>(tags);
    old.tags.forEach((id, mask) -> combinedTags.merge(id, mask, (a, b) -> a | b));
    if (combinedFavorites.size() > 100000
        || combinedTags.size() > 100000
        || presets.size() + old.presets.size() > 1000
        || importedScopes.size() >= 10000) {
      available = false;
      return;
    }
    favorites.addAll(old.favorites);
    tags.putAll(combinedTags);
    old.presets.forEach(
        (name, filters) -> {
          String candidate = name;
          int suffix = 2;
          while (presets.containsKey(candidate) && !presets.get(candidate).equals(filters)) {
            String tail = " (" + suffix++ + ")";
            candidate = name.substring(0, Math.min(name.length(), 40 - tail.length())) + tail;
          }
          presets.putIfAbsent(candidate, filters);
        });
    if (firstImport) {
      excludeShiny = old.excludeShiny;
      excludeMarked = old.excludeMarked;
      excludeItems = old.excludeItems;
      lastFilters = old.lastFilters;
      lastSort = old.lastSort;
      lastReverse = old.lastReverse;
    }
    importedScopes.add(scope);
    changed();
  }

  private static boolean validFilters(Filters f) {
    return f != null
        && f.search() != null
        && f.type() != null
        && f.ability() != null
        && f.nature() != null
        && f.item() != null
        && f.min() != null
        && f.max() != null
        && f.iv() != null
        && f.age() != null
        && f.perfect() >= 0
        && f.perfect() <= 6
        && f.tag() >= -1
        && f.tag() <= 15
        && f.size() >= PcSize.ALL
        && f.size() <= PcSize.ALPHA;
  }

  void rememberFilters(Filters filters, int sort, boolean reverse) {
    if (!available || !validFilters(filters)) return;
    sort = Math.clamp(sort, 0, 4);
    if (lastFilters.equals(filters) && lastSort == sort && lastReverse == reverse) return;
    lastFilters = filters;
    lastSort = sort;
    lastReverse = reverse;
    // The existing periodic/close/disconnect save avoids writing on every keystroke.
    dirty = true;
  }

  int tags(UUID id) {
    return tags.getOrDefault(id, 0);
  }

  boolean matchesTags(UUID id, int filter) {
    int value = tags(id);
    return filter == 0 || (filter == -1 ? value == 0 : (value & filter) == filter);
  }

  boolean setTags(Collection<UUID> ids, int mask, boolean enabled) {
    if (!available || mask <= 0 || mask > 15 || ids.stream().anyMatch(Objects::isNull))
      return false;
    Set<UUID> unique = new HashSet<>(ids);
    if (enabled
        && tags.size() + unique.stream().filter(id -> !tags.containsKey(id)).count() > 100_000)
      return false;
    boolean modified = false;
    for (UUID id : unique) {
      int before = tags(id), after = enabled ? before | mask : before & ~mask;
      if (before == after) continue;
      if (after == 0) tags.remove(id);
      else tags.put(id, after);
      modified = true;
    }
    if (modified) changed();
    return true;
  }

  boolean protectedFromRelease(UUID id, boolean shiny, boolean marked, boolean item) {
    return !available
        || favorites.contains(id)
        || excludeShiny && shiny
        || excludeMarked && marked
        || excludeItems && item;
  }

  void toggleFavorite(UUID id) {
    if (!available || id == null) return;
    if (!favorites.remove(id) && favorites.size() < 100_000) favorites.add(id);
    changed();
  }

  boolean putPreset(String name, Filters filters) {
    name = name.strip();
    if (!available
        || name.isEmpty()
        || name.length() > 40
        || !presets.containsKey(name) && presets.size() >= 20) return false;
    presets.put(name, filters);
    changed();
    return true;
  }

  boolean deletePreset(String name) {
    if (!available || presets.remove(name) == null) return false;
    changed();
    return true;
  }

  void changed() {
    dirty = true;
    save();
  }

  void save() {
    if (!dirty || !available || file == null) return;
    try {
      JsonObject json = new JsonObject();
      json.add("tags", GSON.toJsonTree(tags));
      json.add("favorites", GSON.toJsonTree(favorites));
      json.add("presets", GSON.toJsonTree(presets));
      json.add("importedScopes", GSON.toJsonTree(importedScopes));
      json.add("lastFilters", GSON.toJsonTree(lastFilters));
      json.addProperty("lastSort", lastSort);
      json.addProperty("lastReverse", lastReverse);
      json.addProperty("excludeShiny", excludeShiny);
      json.addProperty("excludeMarked", excludeMarked);
      json.addProperty("excludeItems", excludeItems);
      Files.createDirectories(file.getParent());
      Path temp = file.resolveSibling(file.getFileName() + ".tmp");
      Files.writeString(temp, GSON.toJson(json));
      try {
        Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException error) {
        Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
      }

      dirty = false;
    } catch (Exception error) {
      /* Keep in-memory protection and retry on close/tick. */
    }
  }
}
