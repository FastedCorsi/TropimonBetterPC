package fr.tropimon.betterpc;

import java.util.*;

/** Parsed once per edit; invalid numeric input yields no matches. */
public record PcFilter(
    List<String> words,
    int minLevel,
    int maxLevel,
    double minIv,
    long duration,
    int shiny,
    int gender,
    String type,
    int box,
    boolean valid) {
  public static PcFilter parse(
      String search,
      String min,
      String max,
      String iv,
      String age,
      boolean days,
      int shiny,
      int gender,
      String type,
      int box) {
    try {
      int low = min.isBlank() ? 1 : Integer.parseInt(min);
      int high = max.isBlank() ? Integer.MAX_VALUE : Integer.parseInt(max);
      double percent = iv.isBlank() ? 0 : Double.parseDouble(iv.replace(',', '.'));
      double amount = age.isBlank() ? 0 : Double.parseDouble(age.replace(',', '.'));
      boolean valid =
          low >= 1
              && (max.isBlank() || high <= 100)
              && low <= high
              && Double.isFinite(percent)
              && percent >= 0
              && percent <= 100
              && Double.isFinite(amount)
              && amount >= 0
              && amount <= 36500;
      long duration = (long) Math.ceil(amount * (days ? 86400000.0 : 3600000.0));
      return new PcFilter(
          Arrays.stream(DetectionHistory.normalize(search).strip().split("\\s+"))
              .filter(word -> !word.isEmpty())
              .toList(),
          low,
          high,
          percent,
          duration,
          shiny,
          gender,
          type,
          box,
          valid);
    } catch (NumberFormatException invalid) {
      return new PcFilter(List.of(), 1, 100, 0, 0, shiny, gender, type, box, false);
    }
  }

  public boolean matches(
      String haystack,
      int level,
      double iv,
      long time,
      long now,
      boolean isShiny,
      int pokemonGender,
      String primary,
      String secondary,
      int pokemonBox) {
    if (!valid
        || level < minLevel
        || level > maxLevel
        || iv + 1e-8 < minIv
        || !DetectionHistory.within(time, now, duration)
        || box >= 0 && box != pokemonBox
        || shiny == 1 && !isShiny
        || shiny == 2 && isShiny
        || gender > 0 && gender != pokemonGender
        || !type.isEmpty() && !type.equals(primary) && !type.equals(secondary)) return false;
    for (String word : words) if (!haystack.contains(word)) return false;
    return true;
  }
}
