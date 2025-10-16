package com.rpg.service.dto;

/**
 * Rezultatul unei victorii în dungeon
 */
public class DungeonVictoryResult {
    private final boolean victory;
    private final boolean newCheckpoint;
    private final int nextLevel;
    private final int goldReward;
    private final int expReward;
    private final String specialMessage;

    public DungeonVictoryResult(boolean victory, boolean newCheckpoint, int nextLevel,
                                int goldReward, int expReward, String specialMessage) {
        this.victory = victory;
        this.newCheckpoint = newCheckpoint;
        this.nextLevel = nextLevel;
        this.goldReward = goldReward;
        this.expReward = expReward;
        this.specialMessage = specialMessage;
    }

    public boolean isVictory() {
        return victory;
    }

    public boolean isNewCheckpoint() {
        return newCheckpoint;
    }

    public int getNextLevel() {
        return nextLevel;
    }

    public int getGoldReward() {
        return goldReward;
    }

    public int getExpReward() {
        return expReward;
    }

    public String getSpecialMessage() {
        return specialMessage;
    }

    public boolean hasSpecialMessage() {
        return specialMessage != null;
    }
}
