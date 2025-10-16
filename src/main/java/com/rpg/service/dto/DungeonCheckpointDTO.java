package com.rpg.service.dto;

/**
 * Informații despre un checkpoint individual
 */
public class DungeonCheckpointDTO {
    private final int level;
    private final String difficulty;
    private final boolean unlocked;
    private final boolean isHighest;

    public DungeonCheckpointDTO(int level, String difficulty,
                                boolean unlocked, boolean isHighest) {
        this.level = level;
        this.difficulty = difficulty;
        this.unlocked = unlocked;
        this.isHighest = isHighest;
    }

    public int getLevel() {
        return level;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public boolean isHighest() {
        return isHighest;
    }

    @Override
    public String toString() {
        String status = unlocked ? "✅" : "🔒";
        String highest = isHighest ? " (Curent)" : "";
        return status + " Nivel " + level + " - " + difficulty + highest;
    }
}
