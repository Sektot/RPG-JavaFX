package com.rpg.service.dto;

/**
 * Informații despre Smith
 */
public class SmithInfoDTO {
    private final int availableShards;
    private final int enchantScrollCount;
    private final int enhanceableItemsCount;

    public SmithInfoDTO(int availableShards, int enchantScrollCount, int enhanceableItemsCount) {
        this.availableShards = availableShards;
        this.enchantScrollCount = enchantScrollCount;
        this.enhanceableItemsCount = enhanceableItemsCount;
    }

    public int getAvailableShards() {
        return availableShards;
    }

    public int getEnchantScrollCount() {
        return enchantScrollCount;
    }

    public int getEnhanceableItemsCount() {
        return enhanceableItemsCount;
    }

    public boolean hasShards() {
        return availableShards > 0;
    }

    public boolean hasScrolls() {
        return enchantScrollCount > 0;
    }

    public boolean hasItems() {
        return enhanceableItemsCount > 0;
    }
}
