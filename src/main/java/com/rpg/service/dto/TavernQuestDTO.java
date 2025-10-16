package com.rpg.service.dto;

/**
 * Quest de la tavernă (opțional)
 */
public class TavernQuestDTO {
    private final String id;
    private final String name;
    private final String description;
    private final int goldReward;
    private final int expReward;
    private final boolean completed;

    public TavernQuestDTO(String id, String name, String description,
                          int goldReward, int expReward, boolean completed) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.goldReward = goldReward;
        this.expReward = expReward;
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getGoldReward() {
        return goldReward;
    }

    public int getExpReward() {
        return expReward;
    }

    public boolean isCompleted() {
        return completed;
    }
}
