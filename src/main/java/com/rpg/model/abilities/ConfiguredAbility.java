package com.rpg.model.abilities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an ability with its selected variant and talents applied.
 * This is what the player actually uses in combat - a fully configured ability.
 *
 * Flow:
 * 1. Base Ability (Abilitate) defines core mechanics
 * 2. Player selects a Variant (changes fundamental behavior)
 * 3. Player selects 3 Talents (one per tier, modifies stats/adds effects)
 * 4. ConfiguredAbility combines all of this into the final ability
 */
public class ConfiguredAbility implements Serializable {
    private static final long serialVersionUID = 1L;

    private String baseAbilityId;           // ID of the base ability
    private Abilitate baseAbility;          // The base ability
    private AbilityVariant selectedVariant; // Selected variant
    private AbilityTalent tier1Talent;      // Selected Tier 1 talent
    private AbilityTalent tier2Talent;      // Selected Tier 2 talent
    private AbilityTalent tier3Talent;      // Selected Tier 3 talent

    // Cached computed stats (recalculated when talents/variant change)
    private int finalDamage;
    private int finalManaCost;
    private int finalCooldown;
    private double finalCritChanceBonus;
    private boolean isDirty = true;        // Marks if stats need recalculation

    /**
     * Constructor for a configured ability.
     * Starts with default variant and no talents selected.
     */
    public ConfiguredAbility(Abilitate baseAbility, AbilityVariant defaultVariant) {
        this.baseAbility = baseAbility;
        this.baseAbilityId = baseAbility.getNume(); // Using name as ID for now
        this.selectedVariant = defaultVariant;
        this.tier1Talent = null;
        this.tier2Talent = null;
        this.tier3Talent = null;
        markDirty();
    }

    /**
     * Marks the ability as needing stat recalculation.
     */
    private void markDirty() {
        this.isDirty = true;
    }

    /**
     * Recalculates all final stats based on variant and talents.
     */
    private void recalculateStats() {
        if (!isDirty) return;

        // Start with variant stats
        finalDamage = selectedVariant.getBaseDamage();
        finalManaCost = selectedVariant.getManaCost();
        finalCooldown = selectedVariant.getCooldown();
        finalCritChanceBonus = 0.0;

        // Apply each talent's modifiers
        applyTalentModifiers(tier1Talent);
        applyTalentModifiers(tier2Talent);
        applyTalentModifiers(tier3Talent);

        // Ensure no negative values
        finalDamage = Math.max(0, finalDamage);
        finalManaCost = Math.max(0, finalManaCost);
        finalCooldown = Math.max(0, finalCooldown);

        isDirty = false;
    }

    /**
     * Applies a single talent's modifiers to the final stats.
     */
    private void applyTalentModifiers(AbilityTalent talent) {
        if (talent == null) return;

        AbilityModifier mod = talent.getModifier();

        // Apply multipliers
        finalDamage = (int) (finalDamage * mod.getDamageMultiplier());
        finalManaCost = (int) (finalManaCost * mod.getManaCostMultiplier());
        finalCooldown = (int) (finalCooldown * mod.getCooldownMultiplier());

        // Apply flat bonuses
        finalDamage += mod.getFlatDamageBonus();
        finalManaCost -= mod.getFlatManaCostReduction();
        finalCooldown -= mod.getFlatCooldownReduction();

        // Apply crit bonus
        finalCritChanceBonus += mod.getCritChanceBonus();
    }

    /**
     * Collects all modifiers from all selected talents.
     * Used by combat system to apply special effects.
     */
    public AbilityModifier getCombinedModifiers() {
        AbilityModifier combined = new AbilityModifier();

        // This would ideally combine all modifiers from all talents
        // For now, we'll need to merge them in the combat system
        // TODO: Implement proper modifier merging logic

        return combined;
    }

    // Setters that mark stats as dirty
    public void setSelectedVariant(AbilityVariant variant) {
        this.selectedVariant = variant;
        markDirty();
    }

    public void setTier1Talent(AbilityTalent talent) {
        this.tier1Talent = talent;
        markDirty();
    }

    public void setTier2Talent(AbilityTalent talent) {
        this.tier2Talent = talent;
        markDirty();
    }

    public void setTier3Talent(AbilityTalent talent) {
        this.tier3Talent = talent;
        markDirty();
    }

    // Getters
    public String getBaseAbilityId() { return baseAbilityId; }
    public Abilitate getBaseAbility() { return baseAbility; }
    public AbilityVariant getSelectedVariant() { return selectedVariant; }
    public AbilityTalent getTier1Talent() { return tier1Talent; }
    public AbilityTalent getTier2Talent() { return tier2Talent; }
    public AbilityTalent getTier3Talent() { return tier3Talent; }

    /**
     * Gets final damage after all modifiers.
     */
    public int getFinalDamage() {
        recalculateStats();
        return finalDamage;
    }

    /**
     * Gets final mana cost after all modifiers.
     */
    public int getFinalManaCost() {
        recalculateStats();
        return finalManaCost;
    }

    /**
     * Gets final cooldown after all modifiers.
     */
    public int getFinalCooldown() {
        recalculateStats();
        return finalCooldown;
    }

    /**
     * Gets final crit chance bonus from talents.
     */
    public double getFinalCritChanceBonus() {
        recalculateStats();
        return finalCritChanceBonus;
    }

    /**
     * Returns display name (variant name).
     */
    public String getDisplayName() {
        return selectedVariant.getName();
    }

    /**
     * Returns full description for UI.
     */
    public String getFullDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(selectedVariant.getFullDescription());
        sb.append("\n\n=== TALENTS ===\n");

        if (tier1Talent != null) {
            sb.append("Tier 1: ").append(tier1Talent.getDisplayText()).append("\n");
        }
        if (tier2Talent != null) {
            sb.append("Tier 2: ").append(tier2Talent.getDisplayText()).append("\n");
        }
        if (tier3Talent != null) {
            sb.append("Tier 3: ").append(tier3Talent.getDisplayText()).append("\n");
        }

        sb.append("\n=== FINAL STATS ===\n");
        sb.append("Damage: ").append(getFinalDamage()).append("\n");
        sb.append("Mana Cost: ").append(getFinalManaCost()).append("\n");
        if (getFinalCooldown() > 0) {
            sb.append("Cooldown: ").append(getFinalCooldown()).append(" turns\n");
        }
        if (finalCritChanceBonus > 0) {
            sb.append("Crit Chance: +").append((int)(finalCritChanceBonus * 100)).append("%\n");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return getDisplayName() + " [" + baseAbilityId + "]";
    }
}
