package com.rpg.dungeon.model;

import java.io.Serializable;
import java.util.*;

/**
 * Progression persistentă între dungeon runs
 * Gestionează unlock-uri, upgrade-uri permanente și statistici
 */
public class DungeonProgression implements Serializable {
    private static final long serialVersionUID = 1L;

    // Currency
    private int dungeonTokens;

    // Statistics
    private int totalRuns;
    private int successfulRuns;
    private int totalDeaths;
    private int deepestDepthReached;
    private int totalEnemiesKilled;
    private int totalBossesDefeated;
    private int totalItemsCollected;

    // Unlockables
    private Set<String> unlockedRunItems;        // Item IDs that can appear in runs
    private List<String> startingLoadouts;       // Available starting loadout IDs

    // Permanent Upgrades (purchased with tokens)
    private int maxHpBonusLevel;                 // Each level = +5% max HP
    private int startingGoldBonusLevel;          // Each level = +50 starting gold
    private int treasureQualityLevel;            // Each level = +5% chance for better items
    private int extraStartingItemSlots;          // Extra items you can choose at start

    // Current loadout selection
    private List<String> selectedStartingItems;  // Items chosen for next run

    public DungeonProgression() {
        this.dungeonTokens = 0;
        this.totalRuns = 0;
        this.successfulRuns = 0;
        this.totalDeaths = 0;
        this.deepestDepthReached = 0;
        this.totalEnemiesKilled = 0;
        this.totalBossesDefeated = 0;
        this.totalItemsCollected = 0;

        this.unlockedRunItems = new HashSet<>();
        this.startingLoadouts = new ArrayList<>();
        this.selectedStartingItems = new ArrayList<>();

        this.maxHpBonusLevel = 0;
        this.startingGoldBonusLevel = 0;
        this.treasureQualityLevel = 0;
        this.extraStartingItemSlots = 0;

        // Start with basic items unlocked
        initializeDefaultUnlocks();
    }

    /**
     * Inițializează unlock-urile default
     */
    private void initializeDefaultUnlocks() {
        // Unlock basic common items by default
        unlockedRunItems.add("sharp_blade");
        unlockedRunItems.add("iron_skin");
        unlockedRunItems.add("lucky_coin");
    }

    /**
     * Called when a dungeon run is completed
     * Returns list of newly unlocked items
     */
    public java.util.List<String> recordRunCompletion(int depthReached, boolean victory, int enemiesKilled, boolean bossKilled, int itemsCollected) {
        totalRuns++;
        if (victory) {
            successfulRuns++;
        } else {
            totalDeaths++;
        }

        if (depthReached > deepestDepthReached) {
            deepestDepthReached = depthReached;
        }

        totalEnemiesKilled += enemiesKilled;
        if (bossKilled) {
            totalBossesDefeated++;
        }
        totalItemsCollected += itemsCollected;

        // Award tokens based on performance
        int tokensEarned = calculateTokensEarned(depthReached, victory, enemiesKilled);
        dungeonTokens += tokensEarned;

        // Check for unlocks and return newly unlocked items
        return checkForUnlocks(depthReached, victory);
    }

    /**
     * Calculează token-urile câștigate
     */
    private int calculateTokensEarned(int depth, boolean victory, int enemiesKilled) {
        int tokens = depth * 10; // 10 tokens per depth
        if (victory) {
            tokens += 50; // Bonus for victory
        }
        tokens += enemiesKilled * 2; // 2 tokens per enemy
        return tokens;
    }

    /**
     * Verifică și deblochează conținut nou
     * Returns list of newly unlocked items
     */
    private java.util.List<String> checkForUnlocks(int depth, boolean victory) {
        java.util.List<String> newUnlocks = new java.util.ArrayList<>();

        // Unlock items based on depth reached
        if (depth >= 2) {
            if (unlockedRunItems.add("vampiric_blade")) newUnlocks.add("Vampiric Blade");
            if (unlockedRunItems.add("crit_gem")) newUnlocks.add("Critical Gem");
        }
        if (depth >= 3) {
            if (unlockedRunItems.add("fire_enchant")) newUnlocks.add("Fire Enchant");
            if (unlockedRunItems.add("regen_amulet")) newUnlocks.add("Regeneration Amulet");
        }
        if (depth >= 4) {
            if (unlockedRunItems.add("evasion_cloak")) newUnlocks.add("Evasion Cloak");
            if (unlockedRunItems.add("ice_damage")) newUnlocks.add("Ice Damage");
        }
        if (depth >= 5 && victory) {
            if (unlockedRunItems.add("berserker_rage")) newUnlocks.add("Berserker Rage");
            if (unlockedRunItems.add("dragon_heart")) newUnlocks.add("Dragon Heart");
            if (unlockedRunItems.add("shadow_dancer")) newUnlocks.add("Shadow Dancer");
        }

        return newUnlocks;
    }

    /**
     * Purchase a permanent upgrade
     */
    public boolean purchaseUpgrade(UpgradeType type) {
        int cost = getUpgradeCost(type);
        if (dungeonTokens < cost) {
            return false;
        }

        if (!canUpgrade(type)) {
            return false;
        }

        dungeonTokens -= cost;

        switch (type) {
            case MAX_HP_BONUS:
                maxHpBonusLevel++;
                break;
            case STARTING_GOLD_BONUS:
                startingGoldBonusLevel++;
                break;
            case TREASURE_QUALITY:
                treasureQualityLevel++;
                break;
            case EXTRA_STARTING_SLOT:
                extraStartingItemSlots++;
                break;
        }

        return true;
    }

    /**
     * Check if upgrade can be purchased
     */
    public boolean canUpgrade(UpgradeType type) {
        return getCurrentLevel(type) < getMaxLevel(type);
    }

    /**
     * Get current level of an upgrade
     */
    public int getCurrentLevel(UpgradeType type) {
        return switch (type) {
            case MAX_HP_BONUS -> maxHpBonusLevel;
            case STARTING_GOLD_BONUS -> startingGoldBonusLevel;
            case TREASURE_QUALITY -> treasureQualityLevel;
            case EXTRA_STARTING_SLOT -> extraStartingItemSlots;
        };
    }

    /**
     * Get max level for an upgrade
     */
    public int getMaxLevel(UpgradeType type) {
        return switch (type) {
            case MAX_HP_BONUS, STARTING_GOLD_BONUS -> 5;
            case TREASURE_QUALITY -> 3;
            case EXTRA_STARTING_SLOT -> 2;
        };
    }

    /**
     * Get cost for next level of upgrade
     */
    public int getUpgradeCost(UpgradeType type) {
        int currentLevel = getCurrentLevel(type);
        return switch (type) {
            case MAX_HP_BONUS -> 50 + (currentLevel * 30);
            case STARTING_GOLD_BONUS -> 40 + (currentLevel * 25);
            case TREASURE_QUALITY -> 100 + (currentLevel * 50);
            case EXTRA_STARTING_SLOT -> 200 + (currentLevel * 100);
        };
    }

    /**
     * Calculate total max HP bonus percentage
     */
    public double getMaxHpBonusPercent() {
        return maxHpBonusLevel * 0.05; // 5% per level
    }

    /**
     * Calculate total starting gold bonus
     */
    public int getStartingGoldBonus() {
        return startingGoldBonusLevel * 50;
    }

    /**
     * Calculate treasure quality bonus percentage
     */
    public double getTreasureQualityBonus() {
        return treasureQualityLevel * 0.05; // 5% per level
    }

    /**
     * Get total starting item slots (1 base + upgrades)
     */
    public int getTotalStartingItemSlots() {
        return 1 + extraStartingItemSlots;
    }

    /**
     * Check if a run item is unlocked
     */
    public boolean isItemUnlocked(String itemId) {
        return unlockedRunItems.contains(itemId);
    }

    // Getters and setters
    public int getDungeonTokens() {
        return dungeonTokens;
    }

    public void addTokens(int amount) {
        this.dungeonTokens += amount;
    }

    public int getTotalRuns() {
        return totalRuns;
    }

    public int getSuccessfulRuns() {
        return successfulRuns;
    }

    public int getTotalDeaths() {
        return totalDeaths;
    }

    public int getDeepestDepthReached() {
        return deepestDepthReached;
    }

    public int getTotalEnemiesKilled() {
        return totalEnemiesKilled;
    }

    public int getTotalBossesDefeated() {
        return totalBossesDefeated;
    }

    public int getTotalItemsCollected() {
        return totalItemsCollected;
    }

    public double getSuccessRate() {
        if (totalRuns == 0) return 0.0;
        return (double) successfulRuns / totalRuns * 100;
    }

    public Set<String> getUnlockedRunItems() {
        return new HashSet<>(unlockedRunItems);
    }

    public List<String> getSelectedStartingItems() {
        return new ArrayList<>(selectedStartingItems);
    }

    public void setSelectedStartingItems(List<String> items) {
        this.selectedStartingItems = new ArrayList<>(items);
    }

    /**
     * Types of permanent upgrades
     */
    public enum UpgradeType {
        MAX_HP_BONUS("Max HP Bonus", "🩸", "+5% Max HP per level"),
        STARTING_GOLD_BONUS("Starting Gold", "💰", "+50 starting gold per level"),
        TREASURE_QUALITY("Treasure Quality", "💎", "+5% better item drops per level"),
        EXTRA_STARTING_SLOT("Extra Starting Slot", "✨", "Choose 1 more starting item");

        private final String name;
        private final String icon;
        private final String description;

        UpgradeType(String name, String icon, String description) {
            this.name = name;
            this.icon = icon;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getIcon() {
            return icon;
        }

        public String getDescription() {
            return description;
        }
    }
}
