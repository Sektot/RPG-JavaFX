package com.rpg.service;

import com.rpg.model.characters.Erou;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.service.dto.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TrainerSmithServiceFX - Serviciu pentru training (stats) și smith (enhancement)
 * Refactorizat pentru JavaFX
 */
public class TrainerSmithServiceFX {

    // Costuri pentru training
    private static final int STAT_POINT_COST = 100; // Cost per punct de stat
    private static final int TRAINING_BASE_COST = 50;

    // Costuri pentru enhancement (exponențial) - in scrap
    private static final int BASE_ENHANCEMENT_COST = 100;

    /**
     * Obține informații despre stat-urile eroului și punctele disponibile
     */
    public TrainingInfoDTO getTrainingInfo(Erou hero) {
        return new TrainingInfoDTO(
                hero.getStrength(),
                hero.getDexterity(),
                hero.getIntelligence(),
                hero.getStatPointsToAllocate(),
                STAT_POINT_COST,
                hero.getGold(),
                calculateTrainingCost(1)
        );
    }

    /**
     * Antrenează un stat specific (costă gold)
     */
    public TrainingResultDTO trainStat(Erou hero, StatType statType) {
        int cost = calculateTrainingCost(1);

        if (hero.getGold() < cost) {
            return new TrainingResultDTO(
                    false,
                    "Nu ai destul gold! Necesar: " + cost + " gold",
                    statType,
                    0,
                    0
            );
        }

        hero.scadeGold(cost);
        int oldValue = getStatValue(hero, statType);

        // Crește stat-ul
        switch (statType) {
            case STRENGTH -> hero.increaseStrength(1);
            case DEXTERITY -> hero.increaseDexterity(1);
            case INTELLIGENCE -> hero.increaseIntelligence(1);
        }

        int newValue = getStatValue(hero, statType);

        return new TrainingResultDTO(
                true,
                "Antrenament reușit! " + statType.getDisplayName() + " a crescut!",
                statType,
                newValue,
                cost
        );
    }

    /**
     * Alocă un punct de stat gratuit (primit la level up)
     */
    public AllocationResultDTO allocateStatPoint(Erou hero, StatType statType) {
        if (hero.getStatPointsToAllocate() <= 0) {
            return new AllocationResultDTO(
                    false,
                    "Nu ai puncte de stat disponibile!",
                    statType,
                    0
            );
        }

        int oldValue = getStatValue(hero, statType);

        // Alocă punctul
        boolean success = switch (statType) {
            case STRENGTH -> { hero.increaseStrength(1); yield true; }
            case DEXTERITY -> { hero.increaseDexterity(1); yield true; }
            case INTELLIGENCE -> { hero.increaseIntelligence(1); yield true; }
        };

        if (success) {
            hero.decreaseStatPoints(1);
            int newValue = getStatValue(hero, statType);

            return new AllocationResultDTO(
                    true,
                    statType.getDisplayName() + " a crescut!",
                    statType,
                    newValue
            );
        }

        return new AllocationResultDTO(
                false,
                "Eroare la alocare!",
                statType,
                oldValue
        );
    }

    /**
     * Obține lista de echipamente care pot fi îmbunătățite
     */
    public List<EnhancementItemDTO> getEnhanceableItems(Erou hero) {
        List<EnhancementItemDTO> items = new ArrayList<>();

        // Echipament din inventar
        for (ObiectEchipament item : hero.getInventar().getItems()) {
            items.add(createEnhancementItemDTO(item, hero));
        }

        // Echipament echipat
        if (hero.getArmaEchipata() != null) {
            items.add(createEnhancementItemDTO(hero.getArmaEchipata(), hero));
        }
        if (hero.getArmuraEchipata() != null) {
            items.add(createEnhancementItemDTO(hero.getArmuraEchipata(), hero));
        }
        if (hero.getAccesoriuEchipat() != null) {
            items.add(createEnhancementItemDTO(hero.getAccesoriuEchipat(), hero));
        }

        return items;
    }

    private EnhancementItemDTO createEnhancementItemDTO(ObiectEchipament item, Erou hero) {
        int currentLevel = item.getEnhancementLevel();
        int cost = calculateEnhancementCost(currentLevel);
        int maxAffordable = calculateMaxAffordableEnhancements(item, hero.getScrap());

        Map<String, Integer> currentBonuses = item.getTotalBonuses();
        Map<String, Integer> nextLevelBonuses = simulateEnhancement(item, 1);

        return new EnhancementItemDTO(
                item,
                item.getNume(),
                currentLevel,
                cost,
                hero.getScrap() >= cost,
                maxAffordable,
                currentBonuses,
                nextLevelBonuses
        );
    }

    /**
     * Îmbunătățește un echipament cu 1 nivel
     */
    public EnhancementResultDTO enhanceItem(Erou hero, ObiectEchipament item) {
        int cost = calculateEnhancementCost(item.getEnhancementLevel());

        if (hero.getScrap() < cost) {
            return new EnhancementResultDTO(
                    false,
                    "Nu ai destul scrap! Necesar: " + cost + " scrap",
                    item,
                    item.getEnhancementLevel(),
                    0,
                    null
            );
        }

        int oldLevel = item.getEnhancementLevel();
        Map<String, Integer> oldBonuses = new HashMap<>(item.getTotalBonuses());

        hero.consumeScrap(cost);
        item.enhanceEquipment(1);

        int newLevel = item.getEnhancementLevel();
        Map<String, Integer> newBonuses = item.getTotalBonuses();

        return new EnhancementResultDTO(
                true,
                "Enhancement reușit! " + item.getNume() + " este acum +" + newLevel + "!",
                item,
                newLevel,
                cost,
                calculateBonusIncrease(oldBonuses, newBonuses)
        );
    }

    /**
     * Îmbunătățește la maxim (cât permite scrap-ul)
     */
    public EnhancementResultDTO enhanceItemMax(Erou hero, ObiectEchipament item) {
        int maxLevels = calculateMaxAffordableEnhancements(item, hero.getScrap());

        if (maxLevels <= 0) {
            return new EnhancementResultDTO(
                    false,
                    "Nu ai destul scrap pentru niciun enhancement!",
                    item,
                    item.getEnhancementLevel(),
                    0,
                    null
            );
        }

        int oldLevel = item.getEnhancementLevel();
        Map<String, Integer> oldBonuses = new HashMap<>(item.getTotalBonuses());
        int totalCost = 0;

        for (int i = 0; i < maxLevels; i++) {
            int cost = calculateEnhancementCost(item.getEnhancementLevel());
            hero.consumeScrap(cost);
            item.enhanceEquipment(1);
            totalCost += cost;
        }

        int newLevel = item.getEnhancementLevel();
        Map<String, Integer> newBonuses = item.getTotalBonuses();

        return new EnhancementResultDTO(
                true,
                "Enhancement maxim reușit! " + item.getNume() + " este acum +" + newLevel + "!",
                item,
                newLevel,
                totalCost,
                calculateBonusIncrease(oldBonuses, newBonuses)
        );
    }

    /**
     * Folosește un Enchant Scroll pentru enhancement gratuit
     */
    public EnhancementResultDTO useEnchantScroll(Erou hero, ObiectEchipament item) {
        if (!hero.getInventar().hasEnchantScroll()) {
            return new EnhancementResultDTO(
                    false,
                    "Nu ai Enchant Scrolls!",
                    item,
                    item.getEnhancementLevel(),
                    0,
                    null
            );
        }

        int oldLevel = item.getEnhancementLevel();
        Map<String, Integer> oldBonuses = new HashMap<>(item.getTotalBonuses());

        hero.getInventar().removeEnchantScroll();
        item.enhanceEquipment(1);

        int newLevel = item.getEnhancementLevel();
        Map<String, Integer> newBonuses = item.getTotalBonuses();

        return new EnhancementResultDTO(
                true,
                "Enchant Scroll folosit! " + item.getNume() + " este acum +" + newLevel + "!",
                item,
                newLevel,
                0, // Gratuit cu scroll
                calculateBonusIncrease(oldBonuses, newBonuses)
        );
    }

    /**
     * Informații despre Smith (scrap disponibil, scrolluri, etc)
     */
    public SmithInfoDTO getSmithInfo(Erou hero) {
        return new SmithInfoDTO(
                hero.getScrap(),
                hero.getInventar().getEnchantScrolls().size(),
                getEnhanceableItems(hero).size()
        );
    }

    // ==================== HELPER METHODS ====================

    private int calculateTrainingCost(int levels) {
        return TRAINING_BASE_COST * levels;
    }

    private int calculateEnhancementCost(int currentLevel) {
        // Cost exponențial: 100, 150, 225, 337, 505, ...
        return (int) (BASE_ENHANCEMENT_COST * Math.pow(1.5, currentLevel));
    }

    private int calculateMaxAffordableEnhancements(ObiectEchipament item, int availableShards) {
        int count = 0;
        int currentLevel = item.getEnhancementLevel();
        int remainingShards = availableShards;

        while (true) {
            int cost = calculateEnhancementCost(currentLevel + count);
            if (cost > remainingShards) {
                break;
            }
            remainingShards -= cost;
            count++;

            // Limită de siguranță
            if (count > 50) break;
        }

        return count;
    }

    private Map<String, Integer> simulateEnhancement(ObiectEchipament item, int levels) {
        ObiectEchipament copy = item.createCopy();
        copy.enhanceEquipment(levels);
        return copy.getTotalBonuses();
    }

    private Map<String, Integer> calculateBonusIncrease(Map<String, Integer> oldBonuses,
                                                        Map<String, Integer> newBonuses) {
        Map<String, Integer> increase = new HashMap<>();

        for (String stat : newBonuses.keySet()) {
            int oldValue = oldBonuses.getOrDefault(stat, 0);
            int newValue = newBonuses.get(stat);
            increase.put(stat, newValue - oldValue);
        }

        return increase;
    }

    private int getStatValue(Erou hero, StatType statType) {
        return switch (statType) {
            case STRENGTH -> hero.getStrength();
            case DEXTERITY -> hero.getDexterity();
            case INTELLIGENCE -> hero.getIntelligence();
        };
    }

    // ==================== ENUMS ====================

    public enum StatType {
        STRENGTH("💪 Strength", "Crește damage-ul fizic și HP-ul"),
        DEXTERITY("🎯 Dexterity", "Crește critical chance și dodge"),
        INTELLIGENCE("🧠 Intelligence", "Crește spell power și mana");

        private final String displayName;
        private final String description;

        StatType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
}