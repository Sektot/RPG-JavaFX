package com.rpg.service.dto;

import java.util.List;

/**
 * Informații complete despre starea dungeonului
 */
public class DungeonStartInfoDTO {
    private final int currentLevel;
    private final int highestCheckpoint;
    private final List<DungeonCheckpointDTO> checkpoints;
    private final boolean inDungeon;
    private final int checkpointInterval;
    private final int maxLevel;

    public DungeonStartInfoDTO(int currentLevel, int highestCheckpoint,
                               List<DungeonCheckpointDTO> checkpoints,
                               boolean inDungeon, int checkpointInterval, int maxLevel) {
        this.currentLevel = currentLevel;
        this.highestCheckpoint = highestCheckpoint;
        this.checkpoints = checkpoints;
        this.inDungeon = inDungeon;
        this.checkpointInterval = checkpointInterval;
        this.maxLevel = maxLevel;
    }

    public int getCurrentLevel() { return currentLevel; }
    public int getHighestCheckpoint() { return highestCheckpoint; }
    public List<DungeonCheckpointDTO> getCheckpoints() { return checkpoints; }
    public boolean isInDungeon() { return inDungeon; }
    public int getCheckpointInterval() { return checkpointInterval; }
    public int getMaxLevel() { return maxLevel; }

    public boolean hasCheckpoints() {
        return highestCheckpoint > 0;
    }

    public int getUnlockedCheckpointsCount() {
        return (int) checkpoints.stream().filter(DungeonCheckpointDTO::isUnlocked).count();
    }
}
