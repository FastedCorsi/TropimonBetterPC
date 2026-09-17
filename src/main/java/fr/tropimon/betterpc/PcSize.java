package fr.tropimon.betterpc;

import com.cobblemon.mod.common.pokemon.Pokemon;
import java.lang.reflect.Method;
import java.util.Locale;

/** Optional Cobblemon 1.8 size API, kept reflective so 1.7.2 remains supported. */
final class PcSize {
  static final int ALL = 0, XS = 1, S = 2, M = 3, L = 4, XL = 5, ALPHA = 6;

  private static final Method GET_SIZE = method("getSizeCategory");
  private static final Method IS_ALPHA = method("isAlpha");

  private PcSize() {}

  static boolean supported() {
    return GET_SIZE != null && IS_ALPHA != null;
  }

  static int category(Pokemon pokemon) {
    if (!supported()) return -1;
    try {
      boolean alpha = Boolean.TRUE.equals(IS_ALPHA.invoke(pokemon));
      Object size = GET_SIZE.invoke(pokemon);
      return classify(alpha, size instanceof Enum<?> value ? value.name() : "");
    } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
      return -1;
    }
  }

  static boolean matches(Pokemon pokemon, int filter) {
    return filter == ALL || category(pokemon) == filter;
  }

  static int classify(boolean alpha, String category) {
    if (alpha) return ALPHA;
    return switch (category.toUpperCase(Locale.ROOT)) {
      case "XS" -> XS;
      case "S" -> S;
      case "M" -> M;
      case "L" -> L;
      case "XL" -> XL;
      default -> -1;
    };
  }

  private static Method method(String name) {
    try {
      return Pokemon.class.getMethod(name);
    } catch (NoSuchMethodException | LinkageError ignored) {
      return null;
    }
  }
}
