package fr.tropimon.betterpc;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PcFilterTest {
  @Test
  void combinesAllFiltersAndBothTypes() {
    var filter = PcFilter.parse("Évoli fuite", "5", "50", "80", "2", false, 1, 2, "normal", 1);
    assertTrue(filter.matches("evoli fuite", 30, 90, 5000, 10000, true, 2, "fairy", "normal", 1));
    assertFalse(filter.matches("evoli fuite", 30, 90, 5000, 10000, false, 2, "fairy", "normal", 1));
    assertFalse(filter.matches("evoli fuite", 30, 90, 5000, 10000, true, 1, "fairy", "normal", 1));
    assertFalse(filter.matches("evoli fuite", 30, 90, 5000, 10000, true, 2, "fairy", "normal", 0));
    assertFalse(
        filter.matches("evoli anticipation", 30, 90, 5000, 10000, true, 2, "fairy", "normal", 1));
  }

  @Test
  void blankFiltersIncludeUnknownDates() {
    var filter = PcFilter.parse("", "", "", "", "", false, 0, 0, "", -1);
    assertTrue(filter.matches("", 1, 0, 0, 10000, false, 3, "normal", "", 0));
    assertTrue(filter.matches("", 130, 0, 0, 10000, false, 3, "normal", "", 0));
  }

  @Test
  void numericErrorsCannotAccidentallySelectEverything() {
    for (String bad : new String[] {"abc", "-1", "NaN", "Infinity", "100001"}) {
      var filter = PcFilter.parse("", "", "", "", bad, false, 0, 0, "", -1);
      assertFalse(filter.valid());
      assertFalse(filter.matches("", 30, 100, 999, 1000, false, 1, "normal", "", 0));
    }
    assertFalse(PcFilter.parse("", "80", "20", "", "", false, 0, 0, "", -1).valid());
  }

  @Test
  void hoursAndDaysAreExactWithDecimalComma() {
    assertEquals(5_400_000, PcFilter.parse("", "", "", "", "1,5", false, 0, 0, "", -1).duration());
    assertEquals(129_600_000, PcFilter.parse("", "", "", "", "1,5", true, 0, 0, "", -1).duration());
  }

  @Test
  void tinyPositivePeriodsNeverDisableTheFilter() {
    var filter = PcFilter.parse("", "", "", "", "0.000000001", false, 0, 0, "", -1);
    assertEquals(1, filter.duration());
    assertFalse(filter.matches("", 30, 0, 0, 10000, false, 1, "normal", "", 0));
    assertFalse(filter.matches("", 30, 0, 9998, 10000, false, 1, "normal", "", 0));
    assertTrue(filter.matches("", 30, 0, 9999, 10000, false, 1, "normal", "", 0));
  }
}
