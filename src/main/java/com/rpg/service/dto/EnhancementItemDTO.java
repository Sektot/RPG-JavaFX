package com.rpg.service.dto;

import com.rpg.model.items.ObiectEchipament;

import java.util.Map;

/**
 * Informații despre un item care poate fi îmbunătățit
 */
public class EnhancementItemDTO {
    private final ObiectEchipament item;
    private final String name;
    private final int currentLevel;
    private final int enhancementCost;
    private final boolean canAfford;
    private final int maxAffordableLevels;
    private final Map<String, Integer> currentBonuses;
    private final Map<String, Integer> nextLevelBonuses;

    public EnhancementItemDTO(ObiectEchipament item, String name, int currentLevel,
                              int enhancementCost, boolean canAfford, int maxAffordableLevels,
                              Map<String, Integer> currentBonuses, Map<String, Integer> nextLevelBonuses) {
        this.item = item;
        this.name = name;
        this.currentLevel = currentLevel;
        this.enhancementCost = enhancementCost;
        this.canAfford = canAfford;
        this.maxAffordableLevels = maxAffordableLevels;
        this.currentBonuses = currentBonuses;
        this.nextLevelBonuses = nextLevelBonuses;
    }

    public ObiectEchipament getItem() {
        return item;
    }

    public String getName() {
        return name;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getEnhancementCost() {
        return enhancementCost;
    }

    public boolean canAfford() {
        return canAfford;
    }

    public int getMaxAffordableLevels() {
        return maxAffordableLevels;
    }

    public Map<String, Integer> getCurrentBonuses() {
        return currentBonuses;
    }

    public Map<String, Integer> getNextLevelBonuses() {
        return nextLevelBonuses;
    }

    @Override
    public String toString() {
        return name + " (+" + currentLevel + ")";
    }
}
