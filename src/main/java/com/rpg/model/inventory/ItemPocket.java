package com.rpg.model.inventory;

import com.rpg.model.items.ObiectEchipament;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a custom pocket/container for organizing inventory items
 */
public class ItemPocket implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private List<ObiectEchipament> items;
    private String color; // For visual distinction

    public ItemPocket(String name) {
        this.name = name;
        this.items = new ArrayList<>();
        this.color = "#3498db"; // Default blue
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ObiectEchipament> getItems() {
        return items;
    }

    public void addItem(ObiectEchipament item) {
        if (!items.contains(item)) {
            items.add(item);
        }
    }

    public void removeItem(ObiectEchipament item) {
        items.remove(item);
    }

    public boolean contains(ObiectEchipament item) {
        return items.contains(item);
    }

    public int size() {
        return items.size();
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return name + " (" + items.size() + " items)";
    }
}
