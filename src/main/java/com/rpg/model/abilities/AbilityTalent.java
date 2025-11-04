package com.rpg.model.abilities;

import java.io.Serializable;

/**
 * Represents a talent that can be selected to modify an ability.
 * Each ability has 3 tiers of talents, with 3 options per tier.
 * Player chooses ONE talent per tier.
 *
 * Example Tier 1 Talents for Fireball:
 * - Intense Heat: +20% damage
 * - Swift Cast: -5 mana cost
 * - Mana Efficient: -30% mana cost, -30% damage
 */
public class AbilityTalent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;                    // Unique identifier (e.g., "fireball_tier1_intense_heat")
    private String name;                  // Display name (e.g., "Intense Heat")
    private String description;           // What this talent does
    private String icon;                  // Icon/emoji for visual display
    private TalentTier tier;             // Which tier (1, 2, or 3)

    private AbilityModifier modifier;     // The actual modifications this talent applies

    /**
     * Constructor for an ability talent.
     */
    public AbilityTalent(String id, String name, String description, String icon, TalentTier tier) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.tier = tier;
        this.modifier = new AbilityModifier();
    }

    /**
     * Constructor with pre-made modifier.
     */
    public AbilityTalent(String id, String name, String description, String icon, TalentTier tier, AbilityModifier modifier) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.tier = tier;
        this.modifier = modifier;
    }

    // Builder pattern for setting the modifier
    public AbilityTalent withModifier(AbilityModifier modifier) {
        this.modifier = modifier;
        return this;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public TalentTier getTier() { return tier; }
    public AbilityModifier getModifier() { return modifier; }

    /**
     * Returns formatted display text for UI.
     */
    public String getDisplayText() {
        return icon + " " + name + "\n  " + description;
    }

    @Override
    public String toString() {
        return name + " [" + tier.getDisplayName() + "]: " + description;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AbilityTalent that = (AbilityTalent) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
