package com.rpg.model.items;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Jewel - Represents a socketable item for the talent tree
 * Jewels provide custom bonuses when socketed into jewel socket nodes
 *
 * Inspired by Path of Exile's jewel system:
 * - Different jewel types provide different bonus categories
 * - Can have multiple modifiers with random values
 * - Rarity affects the number and strength of modifiers
 */
public class Jewel implements Serializable {
    private static final long serialVersionUID = 1L;

    // ==================== ENUMS ====================

    /**
     * Jewel type determines the primary stat focus
     */
    public enum JewelType {
        CRIMSON("Crimson Jewel", "🔴", "STR-focused bonuses"),
        VIRIDIAN("Viridian Jewel", "🟢", "DEX-focused bonuses"),
        COBALT("Cobalt Jewel", "🔵", "INT-focused bonuses"),
        PRISMATIC("Prismatic Jewel", "⚪", "Balanced bonuses"),
        UNIQUE("Unique Jewel", "🟡", "Special effects");

        private final String displayName;
        private final String icon;
        private final String description;

        JewelType(String displayName, String icon, String description) {
            this.displayName = displayName;
            this.icon = icon;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
        public String getDescription() { return description; }
    }

    /**
     * Jewel rarity determines modifier count and strength
     */
    public enum JewelRarity {
        COMMON("Common", 1.0, 1, 2),
        UNCOMMON("Uncommon", 1.3, 2, 2),
        RARE("Rare", 1.6, 2, 3),
        EPIC("Epic", 2.0, 3, 4),
        LEGENDARY("Legendary", 2.5, 4, 5);

        private final String displayName;
        private final double multiplier;
        private final int minModifiers;
        private final int maxModifiers;

        JewelRarity(String displayName, double multiplier, int minMods, int maxMods) {
            this.displayName = displayName;
            this.multiplier = multiplier;
            this.minModifiers = minMods;
            this.maxModifiers = maxMods;
        }

        public String getDisplayName() { return displayName; }
        public double getMultiplier() { return multiplier; }
        public int getMinModifiers() { return minModifiers; }
        public int getMaxModifiers() { return maxModifiers; }
    }

    // ==================== FIELDS ====================

    private String name;
    private JewelType type;
    private JewelRarity rarity;
    private int requiredLevel;
    private int price;

    /**
     * Map of stat modifiers this jewel provides
     * Key examples: "damage", "hp_percent", "defense", "crit_chance", "dodge"
     */
    private Map<String, Double> modifiers;

    /**
     * Whether this jewel is currently socketed
     */
    private boolean isSocketed;

    /**
     * Flavor text for unique jewels
     */
    private String flavorText;

    // ==================== CONSTRUCTORS ====================

    /**
     * Main constructor for creating jewels
     */
    public Jewel(String name, JewelType type, JewelRarity rarity, int requiredLevel) {
        this.name = name;
        this.type = type;
        this.rarity = rarity;
        this.requiredLevel = requiredLevel;
        this.modifiers = new HashMap<>();
        this.isSocketed = false;
        this.price = calculateBasePrice();
        this.flavorText = "";
    }

    /**
     * Constructor with pre-defined modifiers (for unique jewels)
     */
    public Jewel(String name, JewelType type, JewelRarity rarity, int requiredLevel,
                 Map<String, Double> modifiers, String flavorText) {
        this(name, type, rarity, requiredLevel);
        this.modifiers = new HashMap<>(modifiers);
        this.flavorText = flavorText;
    }

    // ==================== MODIFIER MANAGEMENT ====================

    /**
     * Adds a modifier to this jewel
     */
    public void addModifier(String stat, double value) {
        modifiers.put(stat, value);
    }

    /**
     * Removes a modifier from this jewel
     */
    public void removeModifier(String stat) {
        modifiers.remove(stat);
    }

    /**
     * Gets the value of a specific modifier
     */
    public double getModifier(String stat) {
        return modifiers.getOrDefault(stat, 0.0);
    }

    /**
     * Gets all modifiers
     */
    public Map<String, Double> getModifiers() {
        return new HashMap<>(modifiers);
    }

    /**
     * Checks if jewel has a specific modifier
     */
    public boolean hasModifier(String stat) {
        return modifiers.containsKey(stat) && modifiers.get(stat) != 0.0;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Calculates base price based on level, rarity, and modifiers
     */
    private int calculateBasePrice() {
        int basePrice = requiredLevel * 15 + 50;
        double rarityMult = rarity.getMultiplier();
        return (int)(basePrice * rarityMult);
    }

    /**
     * Updates the price based on current modifiers
     */
    public void updatePrice() {
        int basePrice = calculateBasePrice();
        // Add value for each modifier
        int modifierBonus = modifiers.size() * 20;
        this.price = basePrice + modifierBonus;
    }

    /**
     * Creates a copy of this jewel
     */
    public Jewel createCopy() {
        Jewel copy = new Jewel(name, type, rarity, requiredLevel);
        copy.modifiers = new HashMap<>(this.modifiers);
        copy.flavorText = this.flavorText;
        copy.price = this.price;
        return copy;
    }

    /**
     * Gets a formatted description of all modifiers
     */
    public String getModifiersDescription() {
        if (modifiers.isEmpty()) {
            return "No modifiers";
        }

        StringBuilder sb = new StringBuilder();
        modifiers.forEach((stat, value) -> {
            String formattedStat = formatStatName(stat);
            String formattedValue = formatStatValue(stat, value);
            sb.append("• ").append(formattedValue).append(" ").append(formattedStat).append("\n");
        });

        return sb.toString().trim();
    }

    /**
     * Formats stat name for display
     */
    private String formatStatName(String stat) {
        return switch (stat) {
            case "damage_percent" -> "Increased Damage";
            case "hp_percent" -> "Increased Maximum HP";
            case "defense_percent" -> "Increased Defense";
            case "crit_chance" -> "Critical Strike Chance";
            case "crit_multiplier" -> "Critical Strike Multiplier";
            case "dodge_chance" -> "Dodge Chance";
            case "attack_speed" -> "Increased Attack Speed";
            case "lifesteal" -> "Life Steal";
            case "all_stats" -> "to All Attributes";
            case "str_bonus" -> "to Strength";
            case "dex_bonus" -> "to Dexterity";
            case "int_bonus" -> "to Intelligence";
            case "gold_find" -> "Increased Gold Found";
            case "exp_bonus" -> "Increased Experience Gained";
            default -> stat.replace("_", " ");
        };
    }

    /**
     * Formats stat value with proper suffix
     */
    private String formatStatValue(String stat, double value) {
        String prefix = value > 0 ? "+" : "";

        // Percentage stats
        if (stat.endsWith("_percent") || stat.endsWith("_chance") ||
            stat.equals("attack_speed") || stat.equals("lifesteal") ||
            stat.equals("gold_find") || stat.equals("exp_bonus")) {
            return prefix + String.format("%.1f%%", value);
        }

        // Flat stats (multiplier uses flat too)
        if (stat.equals("crit_multiplier")) {
            return prefix + String.format("%.0f%%", value);
        }

        // Regular flat bonuses
        return prefix + String.format("%.0f", value);
    }

    // ==================== GETTERS & SETTERS ====================

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public JewelType getType() { return type; }
    public void setType(JewelType type) { this.type = type; }

    public JewelRarity getRarity() { return rarity; }
    public void setRarity(JewelRarity rarity) { this.rarity = rarity; }

    public int getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public boolean isSocketed() { return isSocketed; }
    public void setSocketed(boolean socketed) { isSocketed = socketed; }

    public String getFlavorText() { return flavorText; }
    public void setFlavorText(String flavorText) { this.flavorText = flavorText; }

    // ==================== DISPLAY ====================

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Header with icon and name
        sb.append(type.getIcon()).append(" ");
        sb.append(name);

        // Socketed status
        if (isSocketed) {
            sb.append(" [SOCKETED]");
        }

        sb.append("\n");

        // Type and rarity
        sb.append(type.getDisplayName()).append(" | ");
        sb.append(rarity.getDisplayName());
        sb.append(" | Level ").append(requiredLevel);
        sb.append("\n\n");

        // Modifiers
        sb.append(getModifiersDescription());

        // Flavor text for unique jewels
        if (!flavorText.isEmpty()) {
            sb.append("\n\n\"").append(flavorText).append("\"");
        }

        // Price
        sb.append("\n\nPrice: ").append(price).append(" gold");

        return sb.toString();
    }

    /**
     * Short one-line description for inventory lists
     */
    public String toShortString() {
        String status = isSocketed ? " [SOCKETED]" : "";
        return String.format("%s %s (%s, Lv%d, %d mods)%s - %d gold",
                type.getIcon(), name, rarity.getDisplayName(),
                requiredLevel, modifiers.size(), status, price);
    }

    // ==================== EQUALS & HASHCODE ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Jewel jewel = (Jewel) obj;
        return name.equals(jewel.name) &&
                type == jewel.type &&
                rarity == jewel.rarity &&
                requiredLevel == jewel.requiredLevel;
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31 + type.hashCode() * 7 + rarity.hashCode() + requiredLevel;
    }
}
