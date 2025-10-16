package com.rpg.service.dto;

import com.rpg.service.ShopServiceFX.ShopCategory;

/**
 * Data Transfer Object pentru itemurile din shop
 * Folosit pentru a transmite date între ShopService și UI
 */
public class ShopItemDTO {
    private final String id;
    private final String name;
    private final String description;
    private final int price;
    private final ShopCategory category;
    private final int maxStackSize;

    public ShopItemDTO(String id, String name, String description,
                       int price, ShopCategory category, int maxStackSize) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.maxStackSize = maxStackSize;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPrice() { return price; }
    public ShopCategory getCategory() { return category; }
    public int getMaxStackSize() { return maxStackSize; }

    @Override
    public String toString() {
        return name + " - " + price + " gold";
    }
}