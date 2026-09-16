package fr.tropimon.betterpc;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;

class PcSelectionTest {
  private final List<UUID> order =
      java.util.stream.IntStream.range(0, 14).mapToObj(i -> new UUID(0, i + 1)).toList();

  @Test
  void controlTogglesWithoutLosingOtherSelectedPokemon() {
    Set<UUID> chosen = new HashSet<>(Set.of(order.get(0)));
    assertTrue(PcSelection.click(chosen, order, order.get(0), 2, false, true, false));
    assertEquals(Set.of(order.get(0), order.get(2)), chosen);
    PcSelection.click(chosen, order, order.get(0), 2, true, true, false);
    assertEquals(Set.of(order.get(0)), chosen);
  }

  @Test
  void reverseRangeCrossesPagesInCurrentDisplayOrder() {
    Set<UUID> chosen = new HashSet<>();
    PcSelection.click(chosen, order, order.get(12), 7, false, false, true);
    assertEquals(new HashSet<>(order.subList(7, 13)), chosen);
  }

  @Test
  void controlShiftAddsRangeAndPlainShiftReplacesIt() {
    Set<UUID> chosen = new HashSet<>(Set.of(order.get(0)));
    PcSelection.click(chosen, order, order.get(5), 7, false, true, true);
    assertEquals(4, chosen.size());
    PcSelection.click(chosen, order, order.get(5), 7, false, false, true);
    assertEquals(new HashSet<>(order.subList(5, 8)), chosen);
  }

  @Test
  void missingAnchorNeverSelectsHiddenPokemon() {
    Set<UUID> chosen = new HashSet<>();
    List<UUID> filtered = List.of(order.get(2), order.get(7));
    PcSelection.click(chosen, filtered, order.get(5), 1, false, false, true);
    assertEquals(Set.of(order.get(7)), chosen);
  }
}
