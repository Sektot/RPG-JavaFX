package com.rpg.model.enemies;

import java.io.Serializable;

/**
 * Enemy archetype system - defines enemy behavior patterns and combat style.
 * Each archetype has preferred abilities and tactical priorities.
 */
public enum EnemyArchetype implements Serializable {

    // 🛡️ Defensive tank - high HP, uses Shield Wall often, prioritizes survival
    TANK(
            "Tank",
            "🛡️",
            "Defensive juggernaut with high survivability",
            1.2,    // +20% HP multiplier
            0.8,    // -20% damage multiplier
            15      // 15% ability use chance bonus
    ),

    // ⚔️ Aggressive berserker - high damage, uses offensive buffs, glass cannon
    BERSERKER(
            "Berserker",
            "⚔️",
            "Reckless attacker dealing massive damage",
            0.9,    // -10% HP multiplier
            1.3,    // +30% damage multiplier
            20      // 20% ability use chance bonus
    ),

    // 🎯 High crit assassin - evasive, targets weak heroes, uses Execute
    ASSASSIN(
            "Assassin",
            "🎯",
            "Deadly striker with high critical chance",
            0.8,    // -20% HP multiplier
            1.2,    // +20% damage multiplier
            10      // 10% ability use chance bonus
    ),

    // 🧙 Elemental caster - uses spell abilities, moderate stats
    CASTER(
            "Caster",
            "🧙",
            "Magical combatant using spell abilities",
            0.9,    // -10% HP multiplier
            1.1,    // +10% damage multiplier
            25      // 25% ability use chance bonus (loves using abilities)
    ),

    // 💚 Support healer - uses Desperate Heal frequently, buffs allies
    HEALER(
            "Healer",
            "💚",
            "Supportive enemy that heals and buffs",
            1.0,    // Normal HP
            0.7,    // -30% damage multiplier
            30      // 30% ability use chance bonus (very active)
    ),

    // 🎲 Unpredictable trickster - teleports, dodges, uses Evasion
    TRICKSTER(
            "Trickster",
            "🎲",
            "Evasive and unpredictable combatant",
            0.85,   // -15% HP multiplier
            1.0,    // Normal damage
            20      // 20% ability use chance bonus
    ),

    // ⚡ Balanced elite guard - tactical, smart, uses combos
    ELITE_GUARD(
            "Elite Guard",
            "⚡",
            "Tactical warrior with balanced approach",
            1.1,    // +10% HP multiplier
            1.1,    // +10% damage multiplier
            15      // 15% ability use chance bonus
    ),

    // 🐺 Fast swarm type - multiple attacks, lower individual power
    SWARM(
            "Swarm",
            "🐺",
            "Fast attacker with rapid strikes",
            0.7,    // -30% HP multiplier
            0.9,    // -10% damage multiplier
            5       // 5% ability use chance bonus (prefers basic attacks)
    );

    // ==================== FIELDS ====================

    private final String name;
    private final String icon;
    private final String description;
    private final double hpMultiplier;
    private final double damageMultiplier;
    private final int abilityUseBonus;  // Bonus % chance to use abilities

    // ==================== CONSTRUCTOR ====================

    EnemyArchetype(String name, String icon, String description,
                   double hpMultiplier, double damageMultiplier, int abilityUseBonus) {
        this.name = name;
        this.icon = icon;
        this.description = description;
        this.hpMultiplier = hpMultiplier;
        this.damageMultiplier = damageMultiplier;
        this.abilityUseBonus = abilityUseBonus;
    }

    // ==================== GETTERS ====================

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public double getHpMultiplier() {
        return hpMultiplier;
    }

    public double getDamageMultiplier() {
        return damageMultiplier;
    }

    public int getAbilityUseBonus() {
        return abilityUseBonus;
    }

    public String getFormattedName() {
        return icon + " " + name;
    }

    // ==================== ARCHETYPE-SPECIFIC BEHAVIOR ====================

    /**
     * Get preferred abilities for this archetype.
     * Used to assign archetype-appropriate abilities to enemies.
     */
    public EnemyAbility[] getPreferredOffensiveAbilities() {
        return switch (this) {
            case TANK -> new EnemyAbility[]{EnemyAbility.POWER_STRIKE};
            case BERSERKER -> new EnemyAbility[]{EnemyAbility.POWER_STRIKE, EnemyAbility.FIREBALL};
            case ASSASSIN -> new EnemyAbility[]{EnemyAbility.EXECUTE, EnemyAbility.POISON_STRIKE};
            case CASTER -> new EnemyAbility[]{EnemyAbility.FIREBALL, EnemyAbility.LIGHTNING_BOLT};
            case HEALER -> new EnemyAbility[]{EnemyAbility.LIGHTNING_BOLT};
            case TRICKSTER -> new EnemyAbility[]{EnemyAbility.POISON_STRIKE, EnemyAbility.LIGHTNING_BOLT};
            case ELITE_GUARD -> new EnemyAbility[]{EnemyAbility.POWER_STRIKE, EnemyAbility.LIGHTNING_BOLT};
            case SWARM -> new EnemyAbility[]{EnemyAbility.LIGHTNING_BOLT}; // Rarely uses abilities
        };
    }

    public EnemyAbility[] getPreferredDefensiveAbilities() {
        return switch (this) {
            case TANK -> new EnemyAbility[]{EnemyAbility.SHIELD_WALL, EnemyAbility.DESPERATE_HEAL};
            case BERSERKER -> new EnemyAbility[]{}; // No defensive abilities
            case ASSASSIN -> new EnemyAbility[]{EnemyAbility.EVASION};
            case CASTER -> new EnemyAbility[]{EnemyAbility.EVASION};
            case HEALER -> new EnemyAbility[]{EnemyAbility.DESPERATE_HEAL, EnemyAbility.SHIELD_WALL};
            case TRICKSTER -> new EnemyAbility[]{EnemyAbility.EVASION};
            case ELITE_GUARD -> new EnemyAbility[]{EnemyAbility.SHIELD_WALL, EnemyAbility.DESPERATE_HEAL};
            case SWARM -> new EnemyAbility[]{EnemyAbility.EVASION};
        };
    }

    public EnemyAbility[] getPreferredTacticalAbilities() {
        return switch (this) {
            case TANK -> new EnemyAbility[]{EnemyAbility.ENRAGE};
            case BERSERKER -> new EnemyAbility[]{EnemyAbility.BATTLE_CRY, EnemyAbility.BLOOD_FRENZY, EnemyAbility.DESPERATE_GAMBIT};
            case ASSASSIN -> new EnemyAbility[]{EnemyAbility.BLOOD_FRENZY};
            case CASTER -> new EnemyAbility[]{EnemyAbility.BATTLE_CRY};
            case HEALER -> new EnemyAbility[]{EnemyAbility.BATTLE_CRY};
            case TRICKSTER -> new EnemyAbility[]{EnemyAbility.DESPERATE_GAMBIT};
            case ELITE_GUARD -> new EnemyAbility[]{EnemyAbility.BATTLE_CRY, EnemyAbility.ENRAGE};
            case SWARM -> new EnemyAbility[]{EnemyAbility.ENRAGE};
        };
    }

    /**
     * Returns the AI priority for ability usage.
     * Higher priority = more likely to use abilities over basic attacks.
     */
    public int getAbilityPriority() {
        return switch (this) {
            case HEALER -> 4;      // Very high - loves using abilities
            case CASTER -> 4;      // Very high - spell-focused
            case BERSERKER -> 3;   // High - aggressive ability usage
            case TRICKSTER -> 3;   // High - tricky and unpredictable
            case ELITE_GUARD -> 2; // Medium - tactical choices
            case ASSASSIN -> 2;    // Medium - waits for opportune moments
            case TANK -> 2;        // Medium - defensive when needed
            case SWARM -> 1;       // Low - prefers basic attacks
        };
    }

    /**
     * Returns the AI healing threshold (% HP).
     * Determines when this archetype prefers to heal.
     */
    public double getHealingThreshold() {
        return switch (this) {
            case HEALER -> 0.70;    // Heals at 70% HP (very cautious)
            case TANK -> 0.50;      // Heals at 50% HP
            case ELITE_GUARD -> 0.40; // Heals at 40% HP
            case BERSERKER -> 0.20; // Rarely heals (20% HP)
            default -> 0.35;        // Default 35% HP
        };
    }

    /**
     * Returns whether this archetype should prioritize defensive abilities when low HP.
     */
    public boolean prefersDefenseWhenLow() {
        return switch (this) {
            case TANK, HEALER, ELITE_GUARD -> true;
            case BERSERKER, ASSASSIN, SWARM -> false;
            default -> false;
        };
    }

    /**
     * Returns whether this archetype should prioritize offensive abilities when enemy is low HP.
     */
    public boolean prefersExecuteWhenEnemyLow() {
        return switch (this) {
            case ASSASSIN, BERSERKER, ELITE_GUARD -> true;
            default -> false;
        };
    }
}
