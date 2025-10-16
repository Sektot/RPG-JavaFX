package com.rpg.service.dto;

/**
 * Recompense estimate pentru un nivel
 */
public class DungeonRewardsDTO {
    private final int goldReward;
    private final int expReward;
    private final boolean isBoss;
    private final boolean isCheckpoint;

    public DungeonRewardsDTO(int goldReward, int expReward,
                             boolean isBoss, boolean isCheckpoint) {
        this.goldReward = goldReward;
        this.expReward = expReward;
        this.isBoss = isBoss;
        this.isCheckpoint = isCheckpoint;
    }

    public int getGoldReward() {
        return goldReward;
    }

    public int getExpReward() {
        return expReward;
    }

    public boolean isBoss() {
        return isBoss;
    }

    public boolean isCheckpoint() {
        return isCheckpoint;
    }
}
