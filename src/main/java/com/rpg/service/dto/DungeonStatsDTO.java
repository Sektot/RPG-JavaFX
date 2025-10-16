package com.rpg.service.dto;

/**
 * Statistici despre progresul în dungeon
 */
public class DungeonStatsDTO {
    private final int currentLevel;
    private final int highestLevel;
    private final int checkpointsUnlocked;
    private final int totalCheckpoints;
    private final double progressPercentage;
    private final boolean completed;

    public DungeonStatsDTO(int currentLevel, int highestLevel,
                           int checkpointsUnlocked, int totalCheckpoints,
                           double progressPercentage, boolean completed) {
        this.currentLevel = currentLevel;
        this.highestLevel = highestLevel;
        this.checkpointsUnlocked = checkpointsUnlocked;
        this.totalCheckpoints = totalCheckpoints;
        this.progressPercentage = progressPercentage;
        this.completed = completed;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getHighestLevel() {
        return highestLevel;
    }

    public int getCheckpointsUnlocked() {
        return checkpointsUnlocked;
    }

    public int getTotalCheckpoints() {
        return totalCheckpoints;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getFormattedProgress() {
        return String.format("%.1f%%", progressPercentage);
    }
}
