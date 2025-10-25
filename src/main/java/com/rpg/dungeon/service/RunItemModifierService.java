package com.rpg.dungeon.service;

import com.rpg.dungeon.model.DungeonRun;
import com.rpg.dungeon.model.RunItem;
import com.rpg.model.characters.Erou;

import java.util.HashMap;
import java.util.Map;

/**
 * Service pentru aplicarea modificatorilor run item-urilor pe erou
 */
public class RunItemModifierService {

    /**
     * Aplică toți modificatorii run item-urilor pe erou
     * Aceasta salvează modificatorii într-un map pentru a fi folosiți în luptă
     */
    public static void applyRunItemModifiers(Erou hero, DungeonRun run) {
        // Resetează modificatorii anteriori
        Map<String, Double> totalModifiers = new HashMap<>();

        // Agregă toți modificatorii din toate run item-urile
        for (RunItem item : run.getActiveRunItems()) {
            for (Map.Entry<String, Double> modifier : item.getStatModifiers().entrySet()) {
                String stat = modifier.getKey();
                double value = modifier.getValue() * item.getStackCount();

                totalModifiers.merge(stat, value, Double::sum);
            }
        }

        // Aplică modificatorii pe erou
        applyModifiersToHero(hero, totalModifiers);
    }

    /**
     * Aplică modificatorii efectivi pe statisticile eroului
     */
    private static void applyModifiersToHero(Erou hero, Map<String, Double> modifiers) {
        // DAMAGE MODIFIERS
        if (modifiers.containsKey("damage_percent")) {
            double damageBoost = modifiers.get("damage_percent");
            // Damage-ul va fi aplicat în calculeazaDamage() prin multiplicator
            // Salvăm în hero pentru acces în luptă
            hero.setRunItemDamageMultiplier(1.0 + damageBoost);
        }

        if (modifiers.containsKey("damage_flat")) {
            double flatDamage = modifiers.get("damage_flat");
            hero.setRunItemFlatDamage((int) flatDamage);
        }

        // DEFENSIVE MODIFIERS
        if (modifiers.containsKey("defense_percent")) {
            double defenseBoost = modifiers.get("defense_percent");
            hero.setRunItemDefenseMultiplier(1.0 + defenseBoost);
        }

        if (modifiers.containsKey("defense_flat")) {
            double flatDefense = modifiers.get("defense_flat");
            hero.setRunItemFlatDefense((int) flatDefense);
        }

        if (modifiers.containsKey("dodge_percent")) {
            double dodgeBoost = modifiers.get("dodge_percent");
            hero.setRunItemDodgeBonus(dodgeBoost);
        }

        // SPECIAL MODIFIERS
        if (modifiers.containsKey("lifesteal")) {
            double lifesteal = modifiers.get("lifesteal");
            hero.setRunItemLifesteal(lifesteal);
        }

        if (modifiers.containsKey("regen_per_turn")) {
            double regen = modifiers.get("regen_per_turn");
            hero.setRunItemRegenPerTurn((int) regen);
        }

        if (modifiers.containsKey("gold_percent")) {
            double goldBoost = modifiers.get("gold_percent");
            hero.setRunItemGoldMultiplier(1.0 + goldBoost);
        }

        if (modifiers.containsKey("crit_chance")) {
            double critBoost = modifiers.get("crit_chance");
            hero.setRunItemCritBonus(critBoost);
        }

        // ELEMENTAL DAMAGE
        if (modifiers.containsKey("fire_damage")) {
            hero.setRunItemElementalDamage("fire", (int) modifiers.get("fire_damage").doubleValue());
        }
        if (modifiers.containsKey("ice_damage")) {
            hero.setRunItemElementalDamage("ice", (int) modifiers.get("ice_damage").doubleValue());
        }
    }

    /**
     * Resetează toți modificatorii run item-urilor de pe erou
     * Apelat când eroul iese din dungeon
     */
    public static void clearRunItemModifiers(Erou hero) {
        hero.setRunItemDamageMultiplier(1.0);
        hero.setRunItemFlatDamage(0);
        hero.setRunItemDefenseMultiplier(1.0);
        hero.setRunItemFlatDefense(0);
        hero.setRunItemDodgeBonus(0.0);
        hero.setRunItemLifesteal(0.0);
        hero.setRunItemRegenPerTurn(0);
        hero.setRunItemGoldMultiplier(1.0);
        hero.setRunItemCritBonus(0.0);
        hero.clearRunItemElementalDamage();
    }

    /**
     * Calculează totalul modificatorilor pentru un stat specific
     */
    public static double getTotalModifier(DungeonRun run, String stat) {
        double total = 0.0;
        for (RunItem item : run.getActiveRunItems()) {
            total += item.getTotalModifier(stat);
        }
        return total;
    }
}
