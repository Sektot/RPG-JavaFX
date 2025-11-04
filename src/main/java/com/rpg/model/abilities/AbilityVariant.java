package com.rpg.model.abilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a variant of an ability - a different version that fundamentally changes how it works.
 * Each ability can have multiple variants (typically 3), and the player chooses one.
 *
 * Example:
 * - Fireball (default): Single target, burn effect
 * - Firestorm: AOE, no burn
 * - Inferno Bolt: High single target, cooldown
 */
public class AbilityVariant implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;                    // Unique identifier (e.g., "fireball_default")
    private String name;                  // Display name (e.g., "Fireball")
    private String description;           // What makes this variant unique
    private boolean isDefault;            // Whether this is the default variant

    // Base ability properties (override base ability stats)
    private int baseDamage;
    private int manaCost;
    private int cooldown;
    private int hitChanceBonus;

    // Variant-specific properties
    private boolean isAOE;
    private int numberOfTargets = 1;      // For AOE: how many enemies
    private int numberOfHits = 1;         // For multi-hit abilities
    private double damagePerTargetMultiplier = 1.0; // AOE usually deals less per target

    // Status effects
    private String debuffApplied;
    private int debuffDuration;
    private int debuffDamage;

    private String buffApplied;
    private int buffDuration;
    private Map<String, Double> buffModifiers = new HashMap<>();

    // Special properties
    private int healAmount;
    private double healPercent;
    private int selfDamage;
    private int resourceGenerated;

    // Visual/flavor
    private List<String> damageTypes = new ArrayList<>(); // Fire, Ice, Lightning, etc.
    private String useCase;               // Brief explanation of when to use this variant

    /**
     * Constructor for an ability variant.
     */
    public AbilityVariant(String id, String name, String description, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isDefault = isDefault;
    }

    // Builder pattern methods for easy construction
    public AbilityVariant withDamage(int damage) {
        this.baseDamage = damage;
        return this;
    }

    public AbilityVariant withManaCost(int cost) {
        this.manaCost = cost;
        return this;
    }

    public AbilityVariant withCooldown(int cooldown) {
        this.cooldown = cooldown;
        return this;
    }

    public AbilityVariant withHitChanceBonus(int bonus) {
        this.hitChanceBonus = bonus;
        return this;
    }

    public AbilityVariant asAOE(int targets, double damageMultiplier) {
        this.isAOE = true;
        this.numberOfTargets = targets;
        this.damagePerTargetMultiplier = damageMultiplier;
        return this;
    }

    public AbilityVariant withMultiHit(int hits) {
        this.numberOfHits = hits;
        return this;
    }

    public AbilityVariant withNumberOfHits(int hits) {
        return withMultiHit(hits);
    }

    public AbilityVariant withDebuff(String debuff, int duration, int damage) {
        this.debuffApplied = debuff;
        this.debuffDuration = duration;
        this.debuffDamage = damage;
        return this;
    }

    public AbilityVariant withBuff(String buff, int duration, Map<String, Double> modifiers) {
        this.buffApplied = buff;
        this.buffDuration = duration;
        this.buffModifiers = new HashMap<>(modifiers);
        return this;
    }

    public AbilityVariant withHealing(int flatHeal, double percentHeal) {
        this.healAmount = flatHeal;
        this.healPercent = percentHeal;
        return this;
    }

    public AbilityVariant withSelfDamage(int damage) {
        this.selfDamage = damage;
        return this;
    }

    public AbilityVariant withResourceGeneration(int amount) {
        this.resourceGenerated = amount;
        return this;
    }

    public AbilityVariant withDamageTypes(String... types) {
        this.damageTypes = List.of(types);
        return this;
    }

    public AbilityVariant withUseCase(String useCase) {
        this.useCase = useCase;
        return this;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isDefault() { return isDefault; }

    public int getBaseDamage() { return baseDamage; }
    public int getManaCost() { return manaCost; }
    public int getCooldown() { return cooldown; }
    public int getHitChanceBonus() { return hitChanceBonus; }

    public boolean isAOE() { return isAOE; }
    public int getNumberOfTargets() { return numberOfTargets; }
    public int getNumberOfHits() { return numberOfHits; }
    public double getDamagePerTargetMultiplier() { return damagePerTargetMultiplier; }

    public String getDebuffApplied() { return debuffApplied; }
    public int getDebuffDuration() { return debuffDuration; }
    public int getDebuffDamage() { return debuffDamage; }

    public String getBuffApplied() { return buffApplied; }
    public int getBuffDuration() { return buffDuration; }
    public Map<String, Double> getBuffModifiers() { return new HashMap<>(buffModifiers); }

    public int getHealAmount() { return healAmount; }
    public double getHealPercent() { return healPercent; }
    public int getSelfDamage() { return selfDamage; }
    public int getResourceGenerated() { return resourceGenerated; }

    public List<String> getDamageTypes() { return new ArrayList<>(damageTypes); }
    public String getUseCase() { return useCase; }

    /**
     * Returns a full description with stats for UI display.
     */
    public String getFullDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("\n");
        sb.append(description).append("\n\n");

        if (isAOE) {
            sb.append("AOE: Hits ").append(numberOfTargets).append(" enemies\n");
        }
        if (numberOfHits > 1) {
            sb.append("Multi-hit: ").append(numberOfHits).append(" strikes\n");
        }

        sb.append("Damage: ").append(baseDamage);
        if (damagePerTargetMultiplier != 1.0) {
            sb.append(" (").append((int)(damagePerTargetMultiplier * 100)).append("% per target)");
        }
        sb.append("\n");

        sb.append("Mana Cost: ").append(manaCost).append("\n");

        if (cooldown > 0) {
            sb.append("Cooldown: ").append(cooldown).append(" turns\n");
        }

        if (debuffApplied != null) {
            sb.append("Debuff: ").append(debuffApplied);
            sb.append(" (").append(debuffDamage).append(" damage/turn × ").append(debuffDuration).append(")\n");
        }

        if (healAmount > 0 || healPercent > 0) {
            sb.append("Healing: ");
            if (healAmount > 0) sb.append(healAmount).append(" HP");
            if (healPercent > 0) {
                if (healAmount > 0) sb.append(" + ");
                sb.append((int)(healPercent * 100)).append("% max HP");
            }
            sb.append("\n");
        }

        if (useCase != null && !useCase.isEmpty()) {
            sb.append("\nBest for: ").append(useCase);
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return name + " - " + description;
    }
}
