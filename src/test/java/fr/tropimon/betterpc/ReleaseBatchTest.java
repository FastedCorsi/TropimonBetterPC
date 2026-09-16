package fr.tropimon.betterpc;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;

class ReleaseBatchTest {
  private ReleaseBatch.Target target(int slot) {
    return new ReleaseBatch.Target(UUID.randomUUID(), 0, slot, "safe", "Fixture");
  }

  @org.junit.jupiter.params.ParameterizedTest
  @org.junit.jupiter.params.provider.ValueSource(
      strings = {"favorite", "shiny", "marked", "item", "unavailable"})
  void protectionEnabledAfterConfirmationPreventsAnyRequest(String protection) {
    PcPreferences prefs = new PcPreferences();
    prefs.excludeShiny = prefs.excludeMarked = prefs.excludeItems = false;
    var target = target(0);
    var batch = new ReleaseBatch();
    batch.start(List.of(target));
    protect(prefs, target.id(), protection);
    batch.tick(
        1000,
        true,
        t -> allowed(prefs, t, protection),
        id -> false,
        t -> fail("Protected Pokémon must never be sent"));
    assertEquals(ReleaseBatch.Result.CHANGED, batch.result());
    assertEquals(0, batch.completed());
  }

  @org.junit.jupiter.params.ParameterizedTest
  @org.junit.jupiter.params.provider.ValueSource(
      strings = {"favorite", "shiny", "marked", "item", "unavailable"})
  void protectionEnabledWhilePreviousRequestIsInFlightStopsBeforeProtectedTarget(
      String protection) {
    PcPreferences prefs = new PcPreferences();
    prefs.excludeShiny = prefs.excludeMarked = prefs.excludeItems = false;
    var first = target(0);
    var protectedTarget = target(1);
    var batch = new ReleaseBatch();
    var sent = new ArrayList<ReleaseBatch.Target>();
    batch.start(List.of(first, protectedTarget, target(2)));
    batch.tick(1000, true, t -> allowed(prefs, t, protection), id -> false, sent::add);
    protect(prefs, protectedTarget.id(), protection);
    batch.tick(
        1100, true, t -> allowed(prefs, t, protection), id -> id.equals(first.id()), sent::add);
    batch.tick(1300, true, t -> allowed(prefs, t, protection), id -> false, sent::add);
    batch.tick(9000, true, t -> true, id -> false, sent::add);
    assertEquals(List.of(first), sent);
    assertEquals(1, batch.completed());
    assertEquals(ReleaseBatch.Result.CHANGED, batch.result());
  }

  private static boolean allowed(PcPreferences prefs, ReleaseBatch.Target t, String kind) {
    return !prefs.protectedFromRelease(
        t.id(), kind.equals("shiny"), kind.equals("marked"), kind.equals("item"));
  }

  private static void protect(PcPreferences prefs, UUID id, String kind) {
    switch (kind) {
      case "favorite" -> prefs.toggleFavorite(id);
      case "shiny" -> prefs.excludeShiny = true;
      case "marked" -> prefs.excludeMarked = true;
      case "item" -> prefs.excludeItems = true;
      case "unavailable" -> prefs.available = false;
      default -> throw new AssertionError(kind);
    }
  }

  @Test
  void largeBatchWaitsForEachAcknowledgementWithoutSkippingOrRepeatingTargets() {
    var targets = java.util.stream.IntStream.range(0, 120).mapToObj(this::target).toList();
    var batch = new ReleaseBatch();
    var sent = new ArrayList<ReleaseBatch.Target>();
    var removed = new HashSet<UUID>();
    batch.start(targets);
    long now = 1000;
    for (var target : targets) {
      batch.tick(now, true, t -> true, removed::contains, sent::add);
      assertEquals(target, sent.getLast());
      int count = sent.size();
      batch.tick(now + 75, true, t -> true, removed::contains, sent::add);
      assertEquals(count, sent.size());
      removed.add(target.id());
      batch.tick(now + 125, true, t -> true, removed::contains, sent::add);
      now += 325;
    }
    assertEquals(targets, sent);
    assertEquals(120, batch.completed());
    assertEquals(ReleaseBatch.Result.DONE, batch.result());
  }

  @Test
  void sendsOneThenWaitsForStorageAndPacesNext() {
    var batch = new ReleaseBatch();
    var first = target(0);
    var second = target(1);
    var sent = new ArrayList<ReleaseBatch.Target>();
    batch.start(List.of(first, second));
    batch.tick(1000, true, t -> true, id -> false, sent::add);
    batch.tick(1010, true, t -> true, id -> false, sent::add);
    assertEquals(List.of(first), sent);
    batch.tick(1020, true, t -> true, id -> id.equals(first.id()), sent::add);
    batch.tick(1219, true, t -> true, id -> false, sent::add);
    assertEquals(1, sent.size());
    batch.tick(1220, true, t -> true, id -> false, sent::add);
    assertEquals(List.of(first, second), sent);
    batch.tick(1300, true, t -> true, id -> true, sent::add);
    assertEquals(ReleaseBatch.Result.DONE, batch.result());
    assertEquals(2, batch.completed());
  }

  @Test
  void frozenSelectionDoesNotFollowFutureSelectionsOrDuplicates() {
    var list = new ArrayList<ReleaseBatch.Target>();
    var first = target(0);
    list.add(first);
    list.add(first);
    var batch = new ReleaseBatch();
    batch.start(list);
    list.add(target(1));
    assertEquals(1, batch.remaining());
  }

  @Test
  void refusesMovedOrChangedPokemonBeforeSending() {
    var batch = new ReleaseBatch();
    batch.start(List.of(target(0), target(1)));
    batch.tick(1000, true, t -> false, id -> false, t -> fail("No request allowed"));
    assertEquals(ReleaseBatch.Result.CHANGED, batch.result());
  }

  @Test
  void rejectionTimeoutNeverRetriesOrContinuesBatch() {
    var batch = new ReleaseBatch();
    batch.start(List.of(target(0), target(1)));
    var sent = new ArrayList<ReleaseBatch.Target>();
    batch.tick(1000, true, t -> true, id -> false, sent::add);
    batch.tick(5999, true, t -> true, id -> false, sent::add);
    assertTrue(batch.running());
    batch.tick(6000, true, t -> true, id -> false, sent::add);
    batch.tick(9000, true, t -> true, id -> false, sent::add);
    assertEquals(1, sent.size());
    assertEquals(0, batch.completed());
    assertEquals(ReleaseBatch.Result.TIMEOUT, batch.result());
  }

  @Test
  void sessionChangesAndCloseCancelPendingWork() {
    var batch = new ReleaseBatch();
    batch.start(List.of(target(0)));
    batch.tick(1000, false, t -> true, id -> false, t -> fail());
    assertEquals(ReleaseBatch.Result.CANCELLED, batch.result());
    batch.start(List.of(target(1)));
    batch.cancel();
    batch.tick(1000, true, t -> true, id -> false, t -> fail());
  }

  @Test
  void movedInflightPokemonIsNotTreatedAsRemoved() {
    var batch = new ReleaseBatch();
    batch.start(List.of(target(0), target(1)));
    batch.tick(1000, true, t -> true, id -> false, t -> {});
    batch.tick(1100, true, t -> false, id -> false, t -> fail());
    assertEquals(0, batch.completed());
    assertEquals(ReleaseBatch.Result.CHANGED, batch.result());
  }
}
