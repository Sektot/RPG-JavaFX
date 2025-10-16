package com.rpg.service.dto;

/**
 * Statistici despre inventar
 */
public class InventoryStatsDTO {
    private final int totalItems;
    private final int maxCapacity;
    private final int equipmentCount;
    private final int potionCount;
    private final int buffPotionCount;
    private final int specialItemCount;

    public InventoryStatsDTO(int totalItems, int maxCapacity, int equipmentCount,
                             int potionCount, int buffPotionCount, int specialItemCount) {
        this.totalItems = totalItems;
        this.maxCapacity = maxCapacity;
        this.equipmentCount = equipmentCount;
        this.potionCount = potionCount;
        this.buffPotionCount = buffPotionCount;
        this.specialItemCount = specialItemCount;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public int getEquipmentCount() {
        return equipmentCount;
    }

    public int getPotionCount() {
        return potionCount;
    }

    public int getBuffPotionCount() {
        return buffPotionCount;
    }

    public int getSpecialItemCount() {
        return specialItemCount;
    }

    public boolean isFull() {
        return totalItems >= maxCapacity;
    }

    public int getFreeSlots() {
        return Math.max(0, maxCapacity - totalItems);
    }

    public double getUsagePercentage() {
        return maxCapacity > 0 ? (totalItems * 100.0) / maxCapacity : 0;
    }
}
