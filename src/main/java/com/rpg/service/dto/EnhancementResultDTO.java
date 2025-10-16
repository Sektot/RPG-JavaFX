package com.rpg.service.dto;

import com.rpg.model.items.ObiectEchipament;

import java.util.Map;

/**
 * Rezultatul enhancement-ului
 */
public class EnhancementResultDTO {
    private final boolean success;
    private final String message;
    private final ObiectEchipament item;
    private final int newLevel;
    private final int shardsSpent;
    private final Map<String, Integer> bonusIncrease;

    public EnhancementResultDTO(boolean success, String message, ObiectEchipament item,
                                int newLevel, int shardsSpent, Map<String, Integer> bonusIncrease) {
        this.success = success;
        this.message = message;
        this.item = item;
        this.newLevel = newLevel;
        this.shardsSpent = shardsSpent;
        this.bonusIncrease = bonusIncrease;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public ObiectEchipament getItem() {
        return item;
    }

    public int getNewLevel() {
        return newLevel;
    }

    public int getShardsSpent() {
        return shardsSpent;
    }

    public Map<String, Integer> getBonusIncrease() {
        return bonusIncrease;
    }
}
