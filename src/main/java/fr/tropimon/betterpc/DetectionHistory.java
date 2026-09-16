package fr.tropimon.betterpc;

import java.text.Normalizer;
import java.util.*;

/** First observed live arrival, never a claimed capture timestamp. Zero means unknown. */
public final class DetectionHistory {
  static final int LIMIT = 100_000;
  private final Map<UUID, Long> dates = new HashMap<>();
  private boolean ready;
  private boolean dirty;

  public void ready() {
    ready = true;
  }

  public void remember(UUID id) {
    insert(id, 0);
  }

  public void arrived(UUID previous, UUID incoming, long now) {
    remember(previous);
    insert(incoming, ready && previous == null && now > 0 ? now : 0);
  }

  private void insert(UUID id, long time) {
    if (id != null && dates.size() < LIMIT && !dates.containsKey(id)) {
      dates.put(id, time);
      dirty = true;
    }
  }

  public long time(UUID id) {
    return dates.getOrDefault(id, 0L);
  }

  public boolean dirty() {
    return dirty;
  }

  public void saved() {
    dirty = false;
  }

  public Map<UUID, Long> snapshot() {
    return Map.copyOf(dates);
  }

  public void restore(Map<UUID, Long> values, long now) {
    dates.clear();
    values.forEach(
        (id, time) -> {
          if (time != null && time >= 0 && time <= now) insert(id, time);
        });
    ready = false;
    dirty = false;
  }

  void mergeLegacy(Map<UUID, Long> values, long now) {
    values.forEach(
        (id, time) -> {
          if (time != null && time >= 0 && time <= now) insert(id, time);
        });
  }

  public static boolean within(long timestamp, long now, long duration) {
    return duration == 0 || timestamp > 0 && timestamp <= now && now - timestamp <= duration;
  }

  public static String normalize(String text) {
    return Normalizer.normalize(text, Normalizer.Form.NFD)
        .replaceAll("\\p{M}+", "")
        .toLowerCase(Locale.ROOT);
  }
}
