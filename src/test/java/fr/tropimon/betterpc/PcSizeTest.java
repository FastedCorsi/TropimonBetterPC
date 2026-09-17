package fr.tropimon.betterpc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cobblemon.mod.common.pokemon.Pokemon;
import org.junit.jupiter.api.Test;

class PcSizeTest {
  @Test
  void mapsEveryOfficialCategory() {
    assertEquals(PcSize.XS, PcSize.classify(false, "XS"));
    assertEquals(PcSize.S, PcSize.classify(false, "s"));
    assertEquals(PcSize.M, PcSize.classify(false, "M"));
    assertEquals(PcSize.L, PcSize.classify(false, "l"));
    assertEquals(PcSize.XL, PcSize.classify(false, "XL"));
  }

  @Test
  void alphaReplacesItsIntrinsicMediumCategory() {
    assertEquals(PcSize.ALPHA, PcSize.classify(true, "M"));
  }

  @Test
  void unknownFutureCategoryDoesNotMatchAKnownSize() {
    assertEquals(-1, PcSize.classify(false, "XXL"));
    assertEquals(-1, PcSize.classify(false, ""));
  }

  @Test
  void optionalAdapterMatchesTheCobblemonRuntime() {
    boolean runtimeHasApi;
    try {
      Pokemon.class.getMethod("isAlpha");
      Pokemon.class.getMethod("getSizeCategory");
      runtimeHasApi = true;
    } catch (NoSuchMethodException missing) {
      runtimeHasApi = false;
    }
    assertEquals(runtimeHasApi, PcSize.supported());
  }
}
