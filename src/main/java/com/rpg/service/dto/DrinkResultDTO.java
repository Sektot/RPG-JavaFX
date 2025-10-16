package com.rpg.service.dto;

import com.rpg.service.TavernServiceFX;

/**
 * Rezultatul achiziției unei băuturi
 */
public class DrinkResultDTO {
    private final boolean success;
    private final String message;
    private final TavernServiceFX.DrinkType drinkType;
    private final int goldSpent;

    public DrinkResultDTO(boolean success, String message,
                          TavernServiceFX.DrinkType drinkType, int goldSpent) {
        this.success = success;
        this.message = message;
        this.drinkType = drinkType;
        this.goldSpent = goldSpent;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public TavernServiceFX.DrinkType getDrinkType() {
        return drinkType;
    }

    public int getGoldSpent() {
        return goldSpent;
    }
}
