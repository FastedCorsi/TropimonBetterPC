package fr.tropimon.betterpc;

import net.minecraft.text.Text;

/** Minecraft selects the active language and falls back to the bundled English translations. */
final class PcLang {
  static String tr(String key) {
    return Text.translatable("tropimon_better_pc." + key).getString();
  }
}
