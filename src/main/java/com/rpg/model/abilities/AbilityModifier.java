package com.rpg.model.abilities;

import com.rpg.model.characters.Erou;
import com.rpg.model.characters.Inamic;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a modification that can be applied to an ability's stats or effects.
 * Used by talents and variants to customize ability behavior.
 */
public class AbilityModifier implements Serializable {
    private static final long serialVersionUID = 1L;

    // Stat multipliers
    private double damageMultiplier = 1.0;
    private double manaCostMultiplier = 1.0;
    private double cooldownMultiplier = 1.0;
    private double critChanceBonus = 0.0;

    // Flat bonuses
    private int flatDamageBonus = 0;
    private int flatManaCostReduction = 0;
    private int flatCooldownReduction = 0;

    // Effect modifiers
    private boolean appliesBleed = false;
    private int bleedDamage = 0;
    private int bleedDuration = 0;

    private boolean appliesBurn = false;
    private int burnDamage = 0;
    private int burnDuration = 0;

    private boolean appliesPoison = false;
    private int poisonDamage = 0;
    private int poisonDuration = 0;

    private boolean appliesArmorReduction = false;
    private int armorReductionAmount = 0;
    private int armorReductionDuration = 0;

    // Special effects
    private boolean chainToAdditionalEnemy = false;
    private double chainDamagePercent = 0.25;

    private boolean healsOnKill = false;
    private int healOnKillAmount = 0;

    private boolean grantsDefenseOnCast = false;
    private int defenseGranted = 0;
    private int defenseDuration = 0;

    private boolean executeBelowHPPercent = false;
    private double executeThreshold = 0.3;
    private double executeBonusDamage = 1.0;

    private boolean refundsManaOnKill = false;
    private double manaRefundPercent = 0.5;

    private boolean generatesResource = false;
    private int resourcePerHit = 0;

    private boolean hasLifesteal = false;
    private double lifestealPercent = 0.0;

    private boolean increasesAOETargets = false;
    private int additionalTargets = 0;

    private boolean triggersExplosionOnKill = false;
    private int explosionDamage = 0;

    // Chain effects with targets
    private int chainsToTargets = 0;
    private double chainDamageMultiplier = 0.75;

    // Cooldown effects
    private boolean resetsCooldownOnKill = false;
    private int cooldownReduction = 0;

    // Special on-kill effects
    private boolean burnsAllEnemiesOnKill = false;

    // Custom properties for unique effects
    private Map<String, Object> customProperties = new HashMap<>();

    public AbilityModifier() {}

    // Builder pattern methods
    public AbilityModifier withDamageMultiplier(double multiplier) {
        this.damageMultiplier = multiplier;
        return this;
    }

    public AbilityModifier withManaCostMultiplier(double multiplier) {
        this.manaCostMultiplier = multiplier;
        return this;
    }

    public AbilityModifier withFlatDamageBonus(int bonus) {
        this.flatDamageBonus = bonus;
        return this;
    }

    public AbilityModifier withFlatManaCostReduction(int reduction) {
        this.flatManaCostReduction = reduction;
        return this;
    }

    public AbilityModifier withCritChanceBonus(double bonus) {
        this.critChanceBonus = bonus;
        return this;
    }

    public AbilityModifier withBleed(int damage, int duration) {
        this.appliesBleed = true;
        this.bleedDamage = damage;
        this.bleedDuration = duration;
        return this;
    }

    public AbilityModifier withBurn(int damage, int duration) {
        this.appliesBurn = true;
        this.burnDamage = damage;
        this.burnDuration = duration;
        return this;
    }

    public AbilityModifier withArmorReduction(int amount, int duration) {
        this.appliesArmorReduction = true;
        this.armorReductionAmount = amount;
        this.armorReductionDuration = duration;
        return this;
    }

    public AbilityModifier withChainEffect(double damagePercent) {
        this.chainToAdditionalEnemy = true;
        this.chainDamagePercent = damagePercent;
        return this;
    }

    public AbilityModifier withHealOnKill(int healAmount) {
        this.healsOnKill = true;
        this.healOnKillAmount = healAmount;
        return this;
    }

    public AbilityModifier withDefenseOnCast(int defense, int duration) {
        this.grantsDefenseOnCast = true;
        this.defenseGranted = defense;
        this.defenseDuration = duration;
        return this;
    }

    public AbilityModifier withExecute(double threshold, double bonusDamage) {
        this.executeBelowHPPercent = true;
        this.executeThreshold = threshold;
        this.executeBonusDamage = bonusDamage;
        return this;
    }

    public AbilityModifier withManaRefundOnKill(double percent) {
        this.refundsManaOnKill = true;
        this.manaRefundPercent = percent;
        return this;
    }

    public AbilityModifier withResourceGeneration(int perHit) {
        this.generatesResource = true;
        this.resourcePerHit = perHit;
        return this;
    }

    public AbilityModifier withLifesteal(double percent) {
        this.hasLifesteal = true;
        this.lifestealPercent = percent;
        return this;
    }

    public AbilityModifier withAdditionalAOETargets(int targets) {
        this.increasesAOETargets = true;
        this.additionalTargets = targets;
        return this;
    }

    public AbilityModifier withExplosionOnKill(int damage) {
        this.triggersExplosionOnKill = true;
        this.explosionDamage = damage;
        return this;
    }

    public AbilityModifier withChain(int targets, double damageMultiplier) {
        this.chainsToTargets = targets;
        this.chainDamageMultiplier = damageMultiplier;
        return this;
    }

    public AbilityModifier withCooldownResetOnKill() {
        this.resetsCooldownOnKill = true;
        return this;
    }

    public AbilityModifier withCooldownReduction(int reduction) {
        this.cooldownReduction = reduction;
        return this;
    }

    public AbilityModifier withBurnAllEnemiesOnKill() {
        this.burnsAllEnemiesOnKill = true;
        return this;
    }

    public AbilityModifier withCustomProperty(String key, Object value) {
        this.customProperties.put(key, value);
        return this;
    }

    // Getters
    public double getDamageMultiplier() { return damageMultiplier; }
    public double getManaCostMultiplier() { return manaCostMultiplier; }
    public double getCooldownMultiplier() { return cooldownMultiplier; }
    public double getCritChanceBonus() { return critChanceBonus; }
    public int getFlatDamageBonus() { return flatDamageBonus; }
    public int getFlatManaCostReduction() { return flatManaCostReduction; }
    public int getFlatCooldownReduction() { return flatCooldownReduction; }

    public boolean appliesBleed() { return appliesBleed; }
    public int getBleedDamage() { return bleedDamage; }
    public int getBleedDuration() { return bleedDuration; }

    public boolean appliesBurn() { return appliesBurn; }
    public int getBurnDamage() { return burnDamage; }
    public int getBurnDuration() { return burnDuration; }

    public boolean appliesPoison() { return appliesPoison; }
    public int getPoisonDamage() { return poisonDamage; }
    public int getPoisonDuration() { return poisonDuration; }

    public boolean appliesArmorReduction() { return appliesArmorReduction; }
    public int getArmorReductionAmount() { return armorReductionAmount; }
    public int getArmorReductionDuration() { return armorReductionDuration; }

    public boolean chainsToAdditionalEnemy() { return chainToAdditionalEnemy; }
    public double getChainDamagePercent() { return chainDamagePercent; }

    public boolean healsOnKill() { return healsOnKill; }
    public int getHealOnKillAmount() { return healOnKillAmount; }

    public boolean grantsDefenseOnCast() { return grantsDefenseOnCast; }
    public int getDefenseGranted() { return defenseGranted; }
    public int getDefenseDuration() { return defenseDuration; }

    public boolean executeBelowHPPercent() { return executeBelowHPPercent; }
    public double getExecuteThreshold() { return executeThreshold; }
    public double getExecuteBonusDamage() { return executeBonusDamage; }

    public boolean refundsManaOnKill() { return refundsManaOnKill; }
    public double getManaRefundPercent() { return manaRefundPercent; }

    public boolean generatesResource() { return generatesResource; }
    public int getResourcePerHit() { return resourcePerHit; }

    public boolean hasLifesteal() { return hasLifesteal; }
    public double getLifestealPercent() { return lifestealPercent; }

    public boolean increasesAOETargets() { return increasesAOETargets; }
    public int getAdditionalTargets() { return additionalTargets; }

    public boolean triggersExplosionOnKill() { return triggersExplosionOnKill; }
    public int getExplosionDamage() { return explosionDamage; }

    public int getChainsToTargets() { return chainsToTargets; }
    public double getChainDamageMultiplier() { return chainDamageMultiplier; }

    public boolean resetsAbilityCooldownOnKill() { return resetsCooldownOnKill; }
    public int getCooldownReduction() { return cooldownReduction; }

    public boolean burnAllEnemiesOnKill() { return burnsAllEnemiesOnKill; }

    // Aliases for compatibility with BattleServiceFX
    public int getArmorReduction() { return armorReductionAmount; }
    public boolean hasExplosionOnKill() { return triggersExplosionOnKill; }

    public Map<String, Object> getCustomProperties() { return new HashMap<>(customProperties); }
    public Object getCustomProperty(String key) { return customProperties.get(key); }
}
