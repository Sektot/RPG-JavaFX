package com.rpg.model.enemies;

import java.io.Serializable;

/**
 * Affixes that can be applied to elite enemies.
 * Each affix modifies enemy behavior or stats in combat.
 */
public enum EnemyAffix implements Serializable {
    // Defensive Affixes
    SHIELDED("Shielded", "🛡️", "50% damage reduction until shield breaks", AffixType.DEFENSIVE),
    ARMORED("Armored", "🦾", "+50% defense", AffixType.DEFENSIVE),
    REGENERATING("Regenerating", "💚", "Regenerates 5% HP per turn", AffixType.DEFENSIVE),
    PHASING("Phasing", "👻", "25% chance to dodge attacks", AffixType.DEFENSIVE),

    // Offensive Affixes
    FAST("Fast", "⚡", "Attacks twice per turn", AffixType.OFFENSIVE),
    ENRAGED("Enraged", "💢", "+50% damage, +30% crit chance", AffixType.OFFENSIVE),
    BERSERKER("Berserker", "😡", "Gains damage as HP decreases", AffixType.OFFENSIVE),
    VAMPIRIC("Vampiric", "🧛", "Heals for 30% of damage dealt", AffixType.OFFENSIVE),
    CRITICAL("Critical", "💥", "+40% crit chance, crits deal 3x damage", AffixType.OFFENSIVE),

    // Elemental Affixes
    BURNING("Burning", "🔥", "Returns 30% of damage as fire", AffixType.ELEMENTAL),
    FROZEN_AURA("Frozen Aura", "❄️", "Slows attacker for 2 turns", AffixType.ELEMENTAL),
    SHOCKING("Shocking", "⚡", "Chains lightning to player on hit (20 damage)", AffixType.ELEMENTAL),
    POISONOUS("Poisonous", "☠️", "Applies poison on hit (15 dmg, 3 turns)", AffixType.ELEMENTAL),
    ARCANE("Arcane", "🌟", "Reflects 30% of magic damage", AffixType.ELEMENTAL),

    // Utility Affixes
    RADIANT("Radiant", "✨", "Buffs nearby allies (+30% damage)", AffixType.UTILITY),
    SUMMONER("Summoner", "👥", "Summons 1 minion at 50% HP", AffixType.UTILITY),
    TELEPORTING("Teleporting", "🌀", "Teleports away when below 30% HP", AffixType.UTILITY),
    EXPLOSIVE("Explosive", "💣", "Explodes on death (50 damage to player)", AffixType.UTILITY);

    private final String displayName;
    private final String icon;
    private final String description;
    private final AffixType type;

    EnemyAffix(String displayName, String icon, String description, AffixType type) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public AffixType getType() {
        return type;
    }

    /**
     * Returns formatted affix name with icon.
     */
    public String getFormattedName() {
        return icon + " " + displayName;
    }

    /**
     * Returns full tooltip text.
     */
    public String getTooltip() {
        return icon + " " + displayName + ": " + description;
    }

    /**
     * Affix type categories for balancing.
     */
    public enum AffixType {
        DEFENSIVE,   // Makes enemy harder to kill
        OFFENSIVE,   // Makes enemy hit harder
        ELEMENTAL,   // Adds elemental effects
        UTILITY      // Special mechanics
    }
}
