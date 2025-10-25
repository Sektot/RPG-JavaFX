package com.rpg.service;

import com.rpg.model.items.Jewel;
import com.rpg.model.items.Jewel.JewelRarity;
import com.rpg.model.items.Jewel.JewelType;

import java.util.*;

/**
 * Service for generating random jewels as loot drops
 * Inspired by Path of Exile's jewel generation system
 */
public class JewelGeneratorService {

    private static final Random random = new Random();

    // Jewel name prefixes and suffixes for procedural generation
    private static final String[] PREFIXES = {
            "Shimmering", "Ancient", "Glowing", "Eternal", "Mystic",
            "Radiant", "Blessed", "Cursed", "Corrupted", "Divine",
            "Infernal", "Frozen", "Storm", "Earthen", "Celestial"
    };

    private static final String[] SUFFIXES = {
            "of Power", "of Might", "of Vigor", "of Destruction",
            "of Protection", "of Swiftness", "of Precision", "of Fortune",
            "of Wisdom", "of the Titans", "of the Gods", "of Legends",
            "of Excellence", "of Mastery", "of Perfection"
    };

    /**
     * Generates a random jewel based on hero level
     */
    public static Jewel generateRandomJewel(int heroLevel) {
        JewelRarity rarity = rollRarity();
        JewelType type = rollType();
        int jewelLevel = Math.max(1, heroLevel + random.nextInt(5) - 2); // ±2 levels

        String name = generateJewelName(type, rarity);
        Jewel jewel = new Jewel(name, type, rarity, jewelLevel);

        // Generate modifiers based on rarity
        int modifierCount = rollModifierCount(rarity);
        generateModifiers(jewel, type, rarity, modifierCount);

        jewel.updatePrice();
        return jewel;
    }

    /**
     * Generates a jewel of specific type and rarity
     */
    public static Jewel generateJewel(JewelType type, JewelRarity rarity, int level) {
        String name = generateJewelName(type, rarity);
        Jewel jewel = new Jewel(name, type, rarity, level);

        int modifierCount = rollModifierCount(rarity);
        generateModifiers(jewel, type, rarity, modifierCount);

        jewel.updatePrice();
        return jewel;
    }

    /**
     * Rolls jewel rarity based on weighted probabilities
     */
    private static JewelRarity rollRarity() {
        int roll = random.nextInt(100);

        if (roll < 50) return JewelRarity.COMMON;        // 50%
        if (roll < 75) return JewelRarity.UNCOMMON;      // 25%
        if (roll < 90) return JewelRarity.RARE;          // 15%
        if (roll < 98) return JewelRarity.EPIC;          // 8%
        return JewelRarity.LEGENDARY;                     // 2%
    }

    /**
     * Rolls jewel type (equal probability for non-unique)
     */
    private static JewelType rollType() {
        int roll = random.nextInt(100);

        if (roll < 25) return JewelType.CRIMSON;    // 25% - STR
        if (roll < 50) return JewelType.VIRIDIAN;   // 25% - DEX
        if (roll < 75) return JewelType.COBALT;     // 25% - INT
        if (roll < 95) return JewelType.PRISMATIC;  // 20% - Balanced
        return JewelType.UNIQUE;                     // 5% - Special
    }

    /**
     * Rolls the number of modifiers based on rarity
     */
    private static int rollModifierCount(JewelRarity rarity) {
        int min = rarity.getMinModifiers();
        int max = rarity.getMaxModifiers();
        return min + random.nextInt(max - min + 1);
    }

    /**
     * Generates random modifiers for a jewel
     */
    private static void generateModifiers(Jewel jewel, JewelType type, JewelRarity rarity, int count) {
        Set<String> usedModifiers = new HashSet<>();
        List<String> availableModifiers = getModifiersForType(type);

        for (int i = 0; i < count && !availableModifiers.isEmpty(); i++) {
            // Pick a random modifier that hasn't been used
            String modifier;
            int attempts = 0;
            do {
                modifier = availableModifiers.get(random.nextInt(availableModifiers.size()));
                attempts++;
            } while (usedModifiers.contains(modifier) && attempts < 20);

            if (usedModifiers.contains(modifier)) {
                continue; // Skip if we couldn't find a unique modifier
            }

            usedModifiers.add(modifier);

            // Roll value based on rarity
            double value = rollModifierValue(modifier, rarity);
            jewel.addModifier(modifier, value);
        }
    }

    /**
     * Gets available modifiers for a jewel type
     */
    private static List<String> getModifiersForType(JewelType type) {
        List<String> modifiers = new ArrayList<>();

        switch (type) {
            case CRIMSON: // STR-focused (HP, Defense, Damage)
                modifiers.addAll(Arrays.asList(
                        "hp_percent", "defense_percent", "damage_percent",
                        "str_bonus", "lifesteal", "all_stats"
                ));
                break;

            case VIRIDIAN: // DEX-focused (Crit, Dodge, Attack Speed)
                modifiers.addAll(Arrays.asList(
                        "crit_chance", "crit_multiplier", "dodge_chance",
                        "attack_speed", "dex_bonus", "all_stats"
                ));
                break;

            case COBALT: // INT-focused (Damage, Crit, Experience)
                modifiers.addAll(Arrays.asList(
                        "damage_percent", "crit_chance", "crit_multiplier",
                        "int_bonus", "exp_bonus", "all_stats"
                ));
                break;

            case PRISMATIC: // Balanced (any stat)
                modifiers.addAll(Arrays.asList(
                        "hp_percent", "defense_percent", "damage_percent",
                        "crit_chance", "dodge_chance", "attack_speed",
                        "all_stats", "gold_find", "exp_bonus"
                ));
                break;

            case UNIQUE: // Special modifiers
                modifiers.addAll(Arrays.asList(
                        "damage_percent", "hp_percent", "crit_multiplier",
                        "lifesteal", "gold_find", "exp_bonus", "all_stats"
                ));
                break;
        }

        return modifiers;
    }

    /**
     * Rolls a random value for a modifier based on rarity
     */
    private static double rollModifierValue(String modifier, JewelRarity rarity) {
        double baseMin, baseMax;

        // Define base ranges for different modifier types
        switch (modifier) {
            case "hp_percent":
                baseMin = 3.0;
                baseMax = 8.0;
                break;

            case "defense_percent":
                baseMin = 2.0;
                baseMax = 6.0;
                break;

            case "damage_percent":
                baseMin = 3.0;
                baseMax = 7.0;
                break;

            case "crit_chance":
                baseMin = 2.0;
                baseMax = 5.0;
                break;

            case "crit_multiplier":
                baseMin = 5.0;
                baseMax = 15.0;
                break;

            case "dodge_chance":
                baseMin = 1.0;
                baseMax = 4.0;
                break;

            case "attack_speed":
                baseMin = 2.0;
                baseMax = 6.0;
                break;

            case "lifesteal":
                baseMin = 1.0;
                baseMax = 3.0;
                break;

            case "str_bonus":
            case "dex_bonus":
            case "int_bonus":
                baseMin = 3.0;
                baseMax = 8.0;
                break;

            case "all_stats":
                baseMin = 2.0;
                baseMax = 5.0;
                break;

            case "gold_find":
                baseMin = 5.0;
                baseMax = 15.0;
                break;

            case "exp_bonus":
                baseMin = 3.0;
                baseMax = 10.0;
                break;

            default:
                baseMin = 1.0;
                baseMax = 5.0;
                break;
        }

        // Apply rarity multiplier
        double multiplier = rarity.getMultiplier();
        double min = baseMin * multiplier;
        double max = baseMax * multiplier;

        // Roll random value in range
        double value = min + (max - min) * random.nextDouble();

        // Round to 1 decimal place
        return Math.round(value * 10.0) / 10.0;
    }

    /**
     * Generates a name for the jewel
     */
    private static String generateJewelName(JewelType type, JewelRarity rarity) {
        // Legendary jewels get special treatment
        if (rarity == JewelRarity.LEGENDARY) {
            return generateLegendaryName(type);
        }

        // For other rarities, use prefix + type + suffix
        if (random.nextBoolean()) {
            // Prefix + Type
            String prefix = PREFIXES[random.nextInt(PREFIXES.length)];
            return prefix + " " + type.getDisplayName();
        } else {
            // Type + Suffix
            String suffix = SUFFIXES[random.nextInt(SUFFIXES.length)];
            return type.getDisplayName() + " " + suffix;
        }
    }

    /**
     * Generates unique names for legendary jewels
     */
    private static String generateLegendaryName(JewelType type) {
        String[] legendaryNames = switch (type) {
            case CRIMSON -> new String[]{
                    "Blood of the Immortal", "Heart of the Titan",
                    "Warrior's Pride", "Unyielding Resolve"
            };
            case VIRIDIAN -> new String[]{
                    "Hunter's Eye", "Shadow's Grace",
                    "Wind Walker's Gift", "Precision Incarnate"
            };
            case COBALT -> new String[]{
                    "Mind of the Archmage", "Arcane Supremacy",
                    "Scholar's Wisdom", "Mystic Revelation"
            };
            case PRISMATIC -> new String[]{
                    "Perfect Balance", "Harmony of the Spheres",
                    "Universal Mastery", "Prismatic Perfection"
            };
            case UNIQUE -> new String[]{
                    "Fate's Design", "Destiny's Path",
                    "The Eternal Jewel", "Legacy of Heroes"
            };
        };

        return legendaryNames[random.nextInt(legendaryNames.length)];
    }

    /**
     * Creates a specific legendary jewel (for quest rewards, etc.)
     */
    public static Jewel createLegendaryJewel(String name, JewelType type, int level,
                                             Map<String, Double> modifiers, String flavorText) {
        Jewel jewel = new Jewel(name, type, JewelRarity.LEGENDARY, level, modifiers, flavorText);
        jewel.updatePrice();
        return jewel;
    }

    /**
     * Generates a batch of jewels for shop inventory
     */
    public static List<Jewel> generateShopInventory(int heroLevel, int count) {
        List<Jewel> jewels = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            jewels.add(generateRandomJewel(heroLevel));
        }

        // Sort by rarity and price
        jewels.sort(Comparator
                .comparing((Jewel j) -> j.getRarity().ordinal())
                .thenComparing(Jewel::getPrice)
                .reversed()
        );

        return jewels;
    }
}
