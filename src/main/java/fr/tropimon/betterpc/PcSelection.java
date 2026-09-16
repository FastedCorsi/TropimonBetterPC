package fr.tropimon.betterpc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

final class PcSelection {
  static boolean click(
      Set<UUID> selected,
      List<UUID> order,
      UUID anchor,
      int index,
      boolean multiple,
      boolean control,
      boolean shift) {
    UUID id = order.get(index);
    if (shift && anchor != null) {
      int from = order.indexOf(anchor);
      if (!control) selected.clear();
      if (from < 0) selected.add(id);
      else
        for (int i = Math.min(from, index); i <= Math.max(from, index); i++)
          selected.add(order.get(i));
      return true;
    }
    if (multiple || control) {
      if (!selected.remove(id)) selected.add(id);
      return true;
    }
    selected.clear();
    selected.add(id);
    return false;
  }
}
