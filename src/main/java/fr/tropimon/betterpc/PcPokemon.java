package fr.tropimon.betterpc;

import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbility;
import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.text.Text;

record PcPokemon(Pokemon pokemon, int box, int slot, String search, double iv) {
  // Cobblemon 1.7.2 does not send species/form category labels in registry sync.
  // Stable official species IDs keep all forms identifiable on multiplayer clients.
  private static final Set<String> LEGENDARY_SPECIES =
      Set.of(
          "arceus",
          "articuno",
          "azelf",
          "calyrex",
          "celebi",
          "chienpao",
          "chiyu",
          "cobalion",
          "cosmoem",
          "cosmog",
          "cresselia",
          "darkrai",
          "deoxys",
          "dialga",
          "diancie",
          "enamorus",
          "entei",
          "eternatus",
          "fezandipiti",
          "genesect",
          "giratina",
          "glastrier",
          "groudon",
          "heatran",
          "hooh",
          "hoopa",
          "jirachi",
          "keldeo",
          "koraidon",
          "kubfu",
          "kyogre",
          "kyurem",
          "landorus",
          "latias",
          "latios",
          "lugia",
          "lunala",
          "magearna",
          "manaphy",
          "marshadow",
          "melmetal",
          "meloetta",
          "meltan",
          "mesprit",
          "mew",
          "mewtwo",
          "miraidon",
          "moltres",
          "munkidori",
          "necrozma",
          "ogerpon",
          "okidogi",
          "palkia",
          "pecharunt",
          "phione",
          "raikou",
          "rayquaza",
          "regice",
          "regidrago",
          "regieleki",
          "regigigas",
          "regirock",
          "registeel",
          "reshiram",
          "shaymin",
          "silvally",
          "solgaleo",
          "spectrier",
          "suicune",
          "tapubulu",
          "tapufini",
          "tapukoko",
          "tapulele",
          "terapagos",
          "terrakion",
          "thundurus",
          "tinglu",
          "tornadus",
          "typenull",
          "urshifu",
          "uxie",
          "victini",
          "virizion",
          "volcanion",
          "wochien",
          "xerneas",
          "yveltal",
          "zacian",
          "zamazenta",
          "zapdos",
          "zarude",
          "zekrom",
          "zeraora",
          "zygarde");

  static final Stats[] STATS = {
    Stats.HP, Stats.ATTACK, Stats.DEFENCE, Stats.SPECIAL_ATTACK, Stats.SPECIAL_DEFENCE, Stats.SPEED
  };

  static PcPokemon of(Pokemon p, int box, int slot) {
    String search =
        p.getDisplayName(false).getString()
            + " "
            + p.getSpecies().getName()
            + " "
            + p.getPrimaryType().getName().toLowerCase(java.util.Locale.ROOT)
            + " "
            + Text.translatable(
                    "cobblemon.type."
                        + p.getPrimaryType().getName().toLowerCase(java.util.Locale.ROOT))
                .getString()
            + " "
            + (p.getSecondaryType() == null
                ? ""
                : p.getSecondaryType().getName().toLowerCase(java.util.Locale.ROOT)
                    + " "
                    + Text.translatable(
                            "cobblemon.type."
                                + p.getSecondaryType().getName().toLowerCase(java.util.Locale.ROOT))
                        .getString())
            + " "
            + p.getEffectiveNature().getName().getPath()
            + " "
            + nature(p)
            + " "
            + p.getAbility().getName()
            + " "
            + Text.translatable(p.getAbility().getDisplayName()).getString()
            + " "
            + p.getMoveSet().getMoves().stream()
                .map(move -> move.getName() + " " + move.getDisplayName().getString())
                .collect(Collectors.joining(" "))
            + " "
            + p.heldItemNoCopy$common().getName().getString()
            + " "
            + p.getCaughtBall().getName().getPath()
            + " "
            + String.join(" ", p.getAspects());
    return new PcPokemon(p, box, slot, DetectionHistory.normalize(search), ivPercent(p));
  }

  static String nature(Pokemon p) {
    return Text.translatable("cobblemon.nature." + p.getEffectiveNature().getName().getPath())
        .getString();
  }

  static String natureLabel(Pokemon p) {
    return nature(p);
  }

  static boolean marked(Pokemon p) {
    return !p.getMarks().isEmpty() || p.getMarkings().stream().anyMatch(mark -> mark != 0);
  }

  static boolean legendary(Pokemon p) {
    if (p.isLegendary() || p.isMythical()) return true;
    var id = p.getSpecies().getResourceIdentifier();
    return id.getNamespace().equals("cobblemon") && LEGENDARY_SPECIES.contains(id.getPath());
  }

  static String group(Pokemon p) {
    return p.getSpecies().getResourceIdentifier() + "/" + p.getForm().getName();
  }

  static double ivPercent(Pokemon p) {
    int total = 0;
    for (Stats stat : STATS) {
      Integer value = p.getIvs().get(stat);
      if (value != null) total += value;
    }
    return total * 100.0 / 186;
  }

  static String ivRating(Pokemon p) {
    Locale locale =
        net.minecraft.client.MinecraftClient.getInstance().options.language.startsWith("fr")
            ? Locale.FRENCH
            : Locale.ENGLISH;
    return "IV : " + String.format(locale, "%.1f %%", ivPercent(p));
  }

  static int natureEffect(Pokemon p, int stat) {
    var nature = p.getEffectiveNature();
    if (Objects.equals(nature.getIncreasedStat(), nature.getDecreasedStat())) return 0;
    return STATS[stat] == nature.getIncreasedStat()
        ? 1
        : STATS[stat] == nature.getDecreasedStat() ? -1 : 0;
  }

  static String itemId(Pokemon p) {
    return net.minecraft.registry.Registries.ITEM
        .getId(p.heldItemNoCopy$common().getItem())
        .toString();
  }

  static String ability(Pokemon p) {
    return Text.translatable(p.getAbility().getDisplayName()).getString();
  }

  static boolean hidden(Pokemon p) {
    String first = null;
    boolean multiple = false;
    for (var a : p.getForm().getAbilities()) {
      if (!(a instanceof HiddenAbility) && !a.isSatisfiedBy(p.getAspects())) continue;
      String name = a.getTemplate().getName();
      if (first == null) first = name;
      else if (!first.equals(name)) {
        multiple = true;
        break;
      }
    }
    if (!multiple) return false;
    boolean hidden = false, normal = false;
    for (var potential : p.getForm().getAbilities()) {
      // HiddenAbility.isSatisfiedBy is always false: it controls natural rolls, not ownership.
      if (!(potential instanceof HiddenAbility) && !potential.isSatisfiedBy(p.getAspects())
          || !potential.getTemplate().getName().equals(p.getAbility().getName())) continue;
      if (potential.getPriority() == p.getAbility().getPriority())
        return potential instanceof HiddenAbility;
      if (potential instanceof HiddenAbility) hidden = true;
      else normal = true;
    }

    return hidden && !normal;
  }

  static int natureColor(int effect) {
    return effect > 0 ? 0xFF73E6A1 : effect < 0 ? 0xFFFF7777 : 0xFFE2EBF5;
  }

  static int ivColor(int value) {
    return value == 31
        ? 0xFFFFD84D
        : value >= 21 ? 0xFF73E6A1 : value >= 11 ? 0xFFFFFFFF : 0xFFFF6666;
  }

  static int stat(Pokemon p, int index, boolean ev) {
    Integer value = ev ? p.getEvs().get(STATS[index]) : p.getIvs().get(STATS[index]);
    return value == null ? 0 : value;
  }

  boolean matchesAdvanced(PcPreferences.Filters filters, PcPreferences preferences) {
    if (filters.species().equals("@legendary") && !legendary(pokemon)) return false;
    Pokemon p = pokemon;
    if (!preferences.matchesTags(p.getUuid(), filters.tag())) return false;
    if (filters.favoritesOnly() && !preferences.favorites.contains(p.getUuid())) return false;
    if (!filters.ability().isEmpty()
        && !(filters.ability().equals("@hidden")
            ? hidden(p)
            : filters.ability().equals(p.getAbility().getName()))) return false;
    if (!filters.nature().isEmpty()
        && !filters.nature().equals(p.getEffectiveNature().getName().getPath())) return false;
    if (!filters.item().isEmpty()
        && !switch (filters.item()) {
          case "@none" -> p.heldItemNoCopy$common().isEmpty();
          case "@any" -> !p.heldItemNoCopy$common().isEmpty();
          default -> filters.item().equals(itemId(p));
        }) return false;
    int perfect = 0;
    for (int i = 0; i < 6; i++) if (stat(p, i, false) == 31) perfect++;
    return perfect >= filters.perfect();
  }

  static boolean ownOriginalTrainer(Pokemon p) {
    var player = net.minecraft.client.MinecraftClient.getInstance().player;
    return player != null
        && p.getOriginalTrainerType() == com.cobblemon.mod.common.pokemon.OriginalTrainerType.PLAYER
        && player.getUuid().toString().equalsIgnoreCase(p.getOriginalTrainer());
  }

  static int abilityColor(Pokemon p) {
    return hidden(p) ? 0xFFFFD84D : 0xFFBAF6E4;
  }

  boolean matches(PcFilter filter, long now) {
    if (filter.duration() > 0 && !ownOriginalTrainer(pokemon)) return false;
    int gender =
        switch (pokemon.getGender()) {
          case MALE -> 1;
          case FEMALE -> 2;
          default -> 3;
        };
    return filter.matches(
        search,
        pokemon.getLevel(),
        iv,
        BetterPcClient.detectedAt(pokemon.getUuid()),
        now,
        pokemon.getShiny(),
        gender,
        pokemon.getPrimaryType().getName().toLowerCase(java.util.Locale.ROOT),
        pokemon.getSecondaryType() == null
            ? ""
            : pokemon.getSecondaryType().getName().toLowerCase(java.util.Locale.ROOT),
        box);
  }

  ReleaseBatch.Target target() {
    return new ReleaseBatch.Target(
        pokemon.getUuid(),
        box,
        slot,
        fingerprint(pokemon),
        pokemon.getDisplayName(false).getString());
  }

  static String fingerprint(Pokemon p) {
    return p.getSpecies().getResourceIdentifier()
        + "|"
        + new java.util.TreeSet<>(p.getAspects())
        + "|"
        + p.getLevel()
        + "|"
        + p.getShiny()
        + "|"
        + p.getMarkings()
        + "|"
        + p.getMarks()
        + "|"
        + p.heldItemNoCopy$common()
        + "|"
        + p.heldItemNoCopy$common().getComponents()
        + "|"
        + p.getState().getName()
        + "|"
        + p.getTradeable()
        + "|"
        + p.getAbility().getName()
        + "|"
        + p.getEffectiveNature().getName()
        + "|"
        + java.util.stream.IntStream.range(0, 6)
            .mapToObj(i -> stat(p, i, false) + ":" + stat(p, i, true))
            .collect(Collectors.joining(","));
  }
}
