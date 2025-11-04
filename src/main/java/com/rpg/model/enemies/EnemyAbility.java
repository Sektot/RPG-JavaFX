package com.rpg.model.enemies;

import java.io.Serializable;

/**
 * Enemy ability system - special attacks enemies can use in combat.
 * Makes combat more dynamic and tactical instead of basic attacks only.
 */
public enum EnemyAbility implements Serializable {
    // ==================== OFFENSIVE ABILITIES ====================
    FIREBALL(
            "Fireball",
            "🔥",
            "Hurls a fireball dealing 150% damage as fire",
            AbilityType.OFFENSIVE,
            3,  // 3-turn cooldown
            0.0  // No HP threshold
    ),

    POWER_STRIKE(
            "Power Strike",
            "💪",
            "Devastating blow dealing 200% damage, ignores 50% defense",
            AbilityType.OFFENSIVE,
            4,
            0.0
    ),

    LIGHTNING_BOLT(
            "Lightning Bolt",
            "⚡",
            "Chains lightning dealing 120% damage",
            AbilityType.OFFENSIVE,
            3,
            0.0
    ),

    POISON_STRIKE(
            "Poison Strike",
            "☠️",
            "Attacks and applies poison (25 damage over 3 turns)",
            AbilityType.OFFENSIVE,
            4,
            0.0
    ),

    EXECUTE(
            "Execute",
            "💀",
            "If hero below 30% HP, deal 300% damage",
            AbilityType.OFFENSIVE,
            5,
            0.3  // Only usable when hero below 30% HP
    ),

    // ==================== DEFENSIVE ABILITIES ====================
    SHIELD_WALL(
            "Shield Wall",
            "🛡️",
            "Raises shield, gaining 60% damage reduction for 2 turns",
            AbilityType.DEFENSIVE,
            5,
            0.0
    ),

    DESPERATE_HEAL(
            "Desperate Heal",
            "💚",
            "Heals for 35% max HP (only usable below 50% HP)",
            AbilityType.DEFENSIVE,
            6,
            0.5  // Only when enemy below 50% HP
    ),

    EVASION(
            "Evasion",
            "💨",
            "Becomes untargetable, 100% dodge next turn",
            AbilityType.DEFENSIVE,
            6,
            0.0
    ),

    // ==================== CROWD CONTROL ABILITIES ====================
    STUN_STRIKE(
            "Stun Strike",
            "💫",
            "Deals damage and stuns hero for 1 turn",
            AbilityType.CROWD_CONTROL,
            5,
            0.0
    ),

    WEAKENING_CURSE(
            "Weakening Curse",
            "🌀",
            "Curses hero: -50% damage dealt for 3 turns",
            AbilityType.CROWD_CONTROL,
            6,
            0.0
    ),

    CRIPPLING_BLOW(
            "Crippling Blow",
            "🎯",
            "Attacks and reduces hero's dodge/hit by 30% for 2 turns",
            AbilityType.CROWD_CONTROL,
            4,
            0.0
    ),

    // ==================== TACTICAL ABILITIES ====================
    BATTLE_CRY(
            "Battle Cry",
            "📢",
            "Roars, gaining +50% damage for 3 turns",
            AbilityType.TACTICAL,
            5,
            0.0
    ),

    DESPERATE_GAMBIT(
            "Desperate Gambit",
            "🎲",
            "When low HP: +100% damage, -50% defense for 3 turns",
            AbilityType.TACTICAL,
            7,
            0.3  // Only when below 30% HP
    ),

    ENRAGE(
            "Enrage",
            "😡",
            "Enters rage: +75% damage, +30% crit chance for 2 turns",
            AbilityType.TACTICAL,
            6,
            0.5  // Only when below 50% HP
    ),

    BLOOD_FRENZY(
            "Blood Frenzy",
            "🩸",
            "Sacrifices 20% HP to gain +150% damage next attack",
            AbilityType.TACTICAL,
            5,
            0.3  // Only when above 30% HP (to avoid suicide)
    ),

    // ==================== NEW CC ABILITIES (PHASE 2) ====================
    SILENCE(
            "Silence",
            "🔇",
            "Silences hero: cannot use abilities for 2 turns",
            AbilityType.CROWD_CONTROL,
            7,
            0.0
    ),

    SLOW(
            "Slow",
            "🐌",
            "Slows hero: -50% dodge and hit chance for 3 turns",
            AbilityType.CROWD_CONTROL,
            5,
            0.0
    ),

    ARMOR_SHATTER(
            "Armor Shatter",
            "💥",
            "Shatters armor: -60% defense for 4 turns",
            AbilityType.CROWD_CONTROL,
            6,
            0.0
    ),

    CURSE_OF_WEAKNESS(
            "Curse of Weakness",
            "👁️",
            "Curses hero: -40% all stats for 3 turns",
            AbilityType.CROWD_CONTROL,
            8,
            0.0
    ),

    FROST_NOVA(
            "Frost Nova",
            "❄️",
            "Freezes hero for 1 turn and deals 100% cold damage",
            AbilityType.CROWD_CONTROL,
            6,
            0.0
    ),

    LIFE_DRAIN(
            "Life Drain",
            "🌑",
            "Drains 20% of hero's max HP, healing enemy for that amount",
            AbilityType.OFFENSIVE,
            5,
            0.0
    ),

    CORRUPTION(
            "Corruption",
            "☣️",
            "Corrupts hero: DoT that increases each turn (starts at 10)",
            AbilityType.OFFENSIVE,
            7,
            0.0
    );

    private final String name;
    private final String icon;
    private final String description;
    private final AbilityType type;
    private final int cooldown;
    private final double hpThreshold;  // 0.0 = always usable, 0.3 = only below 30% HP, etc.

    EnemyAbility(String name, String icon, String description, AbilityType type, int cooldown, double hpThreshold) {
        this.name = name;
        this.icon = icon;
        this.description = description;
        this.type = type;
        this.cooldown = cooldown;
        this.hpThreshold = hpThreshold;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public AbilityType getType() {
        return type;
    }

    public int getCooldown() {
        return cooldown;
    }

    public double getHpThreshold() {
        return hpThreshold;
    }

    /**
     * Returns formatted ability name with icon.
     */
    public String getFormattedName() {
        return icon + " " + name;
    }

    /**
     * Checks if ability is usable based on HP threshold.
     * @param currentHpPercent Current HP as percentage (0.0 to 1.0)
     * @return true if ability can be used
     */
    public boolean isUsableAtHP(double currentHpPercent) {
        if (hpThreshold == 0.0) {
            return true;  // Always usable
        }

        // Threshold > 0 means "only when BELOW this HP"
        // Example: 0.3 threshold means only usable when HP < 30%
        return currentHpPercent <= hpThreshold;
    }

    /**
     * Ability type categories.
     */
    public enum AbilityType {
        OFFENSIVE,
        DEFENSIVE,
        CROWD_CONTROL,
        TACTICAL
    }
}
