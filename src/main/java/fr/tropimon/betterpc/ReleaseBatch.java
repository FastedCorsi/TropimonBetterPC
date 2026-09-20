package fr.tropimon.betterpc;

import java.util.*;
import java.util.function.*;

/** One official request in flight; no optimistic removal and no automatic retry. */
public final class ReleaseBatch {
  public record Target(UUID id, int box, int slot, String fingerprint, String label) {}

  public enum Result {
    IDLE,
    RUNNING,
    DONE,
    CHANGED,
    TIMEOUT,
    CANCELLED
  }

  private final ArrayDeque<Target> queue = new ArrayDeque<>();
  private Target waiting;
  private long sentAt;
  private long nextAt;
  private int completed;
  private int total;
  private Result result = Result.IDLE;

  public void start(List<Target> confirmed) {
    if (running()) throw new IllegalStateException("Batch already running");
    queue.clear();
    // Freeze the reviewed targets. A future filter or storage update cannot add any.
    var ids = new HashSet<UUID>();
    for (Target target : List.copyOf(confirmed)) if (ids.add(target.id())) queue.add(target);
    waiting = null;
    completed = 0;
    total = queue.size();
    nextAt = 0;
    result = queue.isEmpty() ? Result.DONE : Result.RUNNING;
  }

  public void tick(
      long now,
      boolean validSession,
      Predicate<Target> unchanged,
      Predicate<UUID> absent,
      Consumer<Target> send) {
    if (!running()) return;
    if (!validSession) {
      cancel();
      return;
    }
    if (waiting != null) {
      if (absent.test(waiting.id())) {
        completed++;
        waiting = null;
        nextAt = now + 200;
        if (queue.isEmpty()) result = Result.DONE;
      } else if (!unchanged.test(waiting)) stop(Result.CHANGED);
      else if (now - sentAt >= 5000) stop(Result.TIMEOUT);
      return;
    }
    if (now < nextAt || queue.isEmpty()) return;
    Target target = queue.removeFirst();
    if (!unchanged.test(target)) {
      stop(Result.CHANGED);
      return;
    }
    waiting = target;
    sentAt = now;
    send.accept(target);
  }

  private void stop(Result why) {
    queue.clear();
    waiting = null;
    result = why;
  }

  public void cancel() {
    if (running()) stop(Result.CANCELLED);
  }

  public boolean running() {
    return result == Result.RUNNING;
  }

  public Result result() {
    return result;
  }

  public int completed() {
    return completed;
  }

  public int total() {
    return total;
  }

  public int remaining() {
    return queue.size() + (waiting == null ? 0 : 1);
  }
}
