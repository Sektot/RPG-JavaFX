package com.rpg.service.dto;



import com.rpg.service.TavernServiceFX.DrinkType;

/**
 * Informații despre o băutură
 */
public class DrinkDTO {
    private final DrinkType type;
    private final String name;
    private final String description;
    private final int price;
    private final String effect;

    public DrinkDTO(DrinkType type, String name, String description,
                    int price, String effect) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.price = price;
        this.effect = effect;
    }

    public DrinkType getType() { return type; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPrice() { return price; }
    public String getEffect() { return effect; }

    @Override
    public String toString() {
        return name + " - " + price + " gold";
    }
}


