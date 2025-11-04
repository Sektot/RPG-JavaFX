package com.rpg.model.abilities;

import java.io.Serializable;
import java.util.*;

/**
 * Manages the player's ability loadout system.
 * Players can unlock many abilities but can only bring 6 into a dungeon run.
 *
 * Flow:
 * 1. Player unlocks abilities through leveling, quests, secrets, etc.
 * 2. Player customizes each unlocked ability (variant + talents)
 * 3. Before dungeon, player selects 6 abilities for their loadout
 * 4. In combat, only loadout abilities are available
 */
public class AbilityLoadout implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MAX_ACTIVE_ABILITIES = 6;

    // All unlocked abilities (customized)
    private Map<String, ConfiguredAbility> unlockedAbilities;

    // Currently active loadout (max 6)
    private List<String> activeLoadout;

    // Predefined loadout templates
    private Map<String, List<String>> savedLoadouts;

    /**
     * Constructor - starts with empty loadout.
     */
    public AbilityLoadout() {
        this.unlockedAbilities = new HashMap<>();
        this.activeLoadout = new ArrayList<>();
        this.savedLoadouts = new HashMap<>();

        // Initialize default templates
        savedLoadouts.put("Balanced", new ArrayList<>());
        savedLoadouts.put("Boss Rush", new ArrayList<>());
        savedLoadouts.put("AOE Farm", new ArrayList<>());
        savedLoadouts.put("Survival", new ArrayList<>());
    }

    /**
     * Unlocks a new ability (adds it to available pool).
     */
    public void unlockAbility(ConfiguredAbility ability) {
        unlockedAbilities.put(ability.getBaseAbilityId(), ability);
    }

    /**
     * Checks if player has unlocked a specific ability.
     */
    public boolean hasAbility(String abilityId) {
        return unlockedAbilities.containsKey(abilityId);
    }

    /**
     * Gets a specific configured ability.
     */
    public ConfiguredAbility getAbility(String abilityId) {
        return unlockedAbilities.get(abilityId);
    }

    /**
     * Gets all unlocked abilities.
     */
    public Collection<ConfiguredAbility> getAllUnlockedAbilities() {
        return new ArrayList<>(unlockedAbilities.values());
    }

    /**
     * Gets abilities currently in the active loadout.
     */
    public List<ConfiguredAbility> getActiveAbilities() {
        List<ConfiguredAbility> active = new ArrayList<>();
        for (String abilityId : activeLoadout) {
            ConfiguredAbility ability = unlockedAbilities.get(abilityId);
            if (ability != null) {
                active.add(ability);
            }
        }
        return active;
    }

    /**
     * Adds an ability to the active loadout.
     * Returns false if loadout is full or ability not unlocked.
     */
    public boolean addToLoadout(String abilityId) {
        if (activeLoadout.size() >= MAX_ACTIVE_ABILITIES) {
            return false; // Loadout full
        }
        if (!unlockedAbilities.containsKey(abilityId)) {
            return false; // Ability not unlocked
        }
        if (activeLoadout.contains(abilityId)) {
            return false; // Already in loadout
        }

        activeLoadout.add(abilityId);
        return true;
    }

    /**
     * Removes an ability from the active loadout.
     */
    public void removeFromLoadout(String abilityId) {
        activeLoadout.remove(abilityId);
    }

    /**
     * Clears the entire loadout.
     */
    public void clearLoadout() {
        activeLoadout.clear();
    }

    /**
     * Sets the entire loadout at once.
     * Validates that all abilities are unlocked and limits to 6.
     */
    public boolean setLoadout(List<String> abilityIds) {
        if (abilityIds.size() > MAX_ACTIVE_ABILITIES) {
            return false; // Too many abilities
        }

        // Validate all abilities are unlocked
        for (String id : abilityIds) {
            if (!unlockedAbilities.containsKey(id)) {
                return false; // Ability not unlocked
            }
        }

        activeLoadout = new ArrayList<>(abilityIds);
        return true;
    }

    /**
     * Swaps two abilities in the loadout (for reordering).
     */
    public boolean swapAbilitiesInLoadout(int index1, int index2) {
        if (index1 < 0 || index1 >= activeLoadout.size() ||
            index2 < 0 || index2 >= activeLoadout.size()) {
            return false;
        }

        Collections.swap(activeLoadout, index1, index2);
        return true;
    }

    /**
     * Checks if the loadout is valid (has at least 1 ability).
     */
    public boolean isLoadoutValid() {
        return !activeLoadout.isEmpty();
    }

    /**
     * Checks if the loadout is full.
     */
    public boolean isLoadoutFull() {
        return activeLoadout.size() >= MAX_ACTIVE_ABILITIES;
    }

    /**
     * Gets number of abilities in current loadout.
     */
    public int getLoadoutSize() {
        return activeLoadout.size();
    }

    /**
     * Gets number of unlocked abilities.
     */
    public int getUnlockedAbilityCount() {
        return unlockedAbilities.size();
    }

    /**
     * Saves current loadout as a template.
     */
    public void saveLoadoutTemplate(String name) {
        savedLoadouts.put(name, new ArrayList<>(activeLoadout));
    }

    /**
     * Loads a saved loadout template.
     */
    public boolean loadLoadoutTemplate(String name) {
        List<String> template = savedLoadouts.get(name);
        if (template == null || template.isEmpty()) {
            return false;
        }

        return setLoadout(template);
    }

    /**
     * Gets all saved loadout template names.
     */
    public Set<String> getSavedLoadoutNames() {
        return new HashSet<>(savedLoadouts.keySet());
    }

    /**
     * Deletes a saved loadout template.
     */
    public void deleteLoadoutTemplate(String name) {
        // Don't allow deletion of default templates
        if (!name.equals("Balanced") && !name.equals("Boss Rush") &&
            !name.equals("AOE Farm") && !name.equals("Survival")) {
            savedLoadouts.remove(name);
        }
    }

    /**
     * Returns a summary string for UI display.
     */
    public String getLoadoutSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Active Loadout (").append(activeLoadout.size()).append("/").append(MAX_ACTIVE_ABILITIES).append("):\n");

        int slot = 1;
        for (String abilityId : activeLoadout) {
            ConfiguredAbility ability = unlockedAbilities.get(abilityId);
            if (ability != null) {
                sb.append("  ").append(slot).append(". ").append(ability.getDisplayName()).append("\n");
            }
            slot++;
        }

        sb.append("\nUnlocked Abilities: ").append(unlockedAbilities.size());

        return sb.toString();
    }

    @Override
    public String toString() {
        return "AbilityLoadout [" + activeLoadout.size() + "/" + MAX_ACTIVE_ABILITIES +
               " active, " + unlockedAbilities.size() + " unlocked]";
    }
}
