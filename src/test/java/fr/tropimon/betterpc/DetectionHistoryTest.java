package fr.tropimon.betterpc;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;

class DetectionHistoryTest {
  @Test
  void mergingLegacyHistoryPreservesExistingDatesAndUnknownsAcrossConnections() {
    var shared = new DetectionHistory();
    UUID known = UUID.randomUUID(), unknown = UUID.randomUUID(), imported = UUID.randomUUID();
    shared.restore(java.util.Map.of(known, 20L, unknown, 0L), 100);
    shared.mergeLegacy(java.util.Map.of(known, 60L, unknown, 50L, imported, 30L), 100);
    assertEquals(20, shared.time(known));
    assertEquals(0, shared.time(unknown));
    assertEquals(30, shared.time(imported));
    assertTrue(shared.dirty());
    var reconnected = new DetectionHistory();
    reconnected.restore(shared.snapshot(), 200);
    reconnected.ready();
    reconnected.arrived(null, imported, 200);
    assertEquals(30, reconnected.time(imported));
  }

  @Test
  void baselineAndTransferAreNeverDatedAsNew() {
    var history = new DetectionHistory();
    UUID old = UUID.randomUUID(), fresh = UUID.randomUUID();
    history.arrived(null, old, 100);
    history.ready();
    history.arrived(old, null, 200);
    history.arrived(null, old, 300);
    history.arrived(null, fresh, 400);
    assertEquals(0, history.time(old));
    assertEquals(400, history.time(fresh));
    history.arrived(fresh, null, 500);
    history.arrived(null, fresh, 600);
    assertEquals(400, history.time(fresh));
  }

  @Test
  void bulkSyncNeverFabricatesDatesAndKnownDatesSurviveIt() {
    var history = new DetectionHistory();
    history.ready();
    UUID live = UUID.randomUUID(), bulk = UUID.randomUUID();
    history.arrived(null, live, 10);
    history.remember(live);
    history.remember(bulk);
    assertEquals(10, history.time(live));
    assertEquals(0, history.time(bulk));
  }

  @Test
  void replacementIsConservativelyUnknown() {
    var history = new DetectionHistory();
    history.ready();
    UUID id = UUID.randomUUID();
    history.arrived(UUID.randomUUID(), id, 100);
    assertEquals(0, history.time(id));
  }

  @Test
  void reconnectKeepsOriginalTimestampAndRebaselines() {
    var old = new DetectionHistory();
    old.ready();
    UUID id = UUID.randomUUID();
    old.arrived(null, id, 100);
    var next = new DetectionHistory();
    next.restore(old.snapshot(), 200);
    next.arrived(null, id, 200);
    UUID baseline = UUID.randomUUID();
    next.arrived(null, baseline, 200);
    assertEquals(100, next.time(id));
    assertEquals(0, next.time(baseline));
  }

  @Test
  void sameAccountHistorySurvivesServerChange() {
    var serverA = new DetectionHistory();
    var serverB = new DetectionHistory();
    UUID id = UUID.randomUUID();
    serverA.ready();
    serverA.arrived(null, id, 20);
    serverB.restore(serverA.snapshot(), 30);
    serverB.ready();
    serverB.arrived(null, id, 30);
    assertEquals(20, serverB.time(id));
  }

  @Test
  void ageBoundaryAndUnknownAndFutureDates() {
    assertTrue(DetectionHistory.within(900, 1000, 100));
    assertFalse(DetectionHistory.within(899, 1000, 100));
    assertFalse(DetectionHistory.within(0, 1000, 100));
    assertFalse(DetectionHistory.within(1001, 1000, 100));
    assertTrue(DetectionHistory.within(0, 1000, 0));
  }

  @Test
  void restoreRejectsFutureAndNegativeTimes() {
    var history = new DetectionHistory();
    UUID future = UUID.randomUUID(), negative = UUID.randomUUID();
    history.restore(Map.of(future, 200L, negative, -1L), 100);
    assertEquals(0, history.time(future));
    assertTrue(history.snapshot().isEmpty());
  }
}
