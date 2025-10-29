package com.rpg.dungeon.model;

import com.rpg.model.characters.Erou;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Reprezintă o sesiune activă de dungeon
 * Conține starea curentă a run-ului și run item-urile temporare
 */
public class DungeonRun implements Serializable {
    private static final long serialVersionUID = 1L;

    private DungeonMap map;
    private Erou hero;
    private List<RunItem> activeRunItems;
    private int roomsCleared;
    private long startTime;
    private boolean bossDefeated;
    private boolean isActive;

    // Progression tracking (persists across floor transitions)
    private int enemiesKilled;
    private int bossesKilled;
    private int highestDepthReached;

    // Temporary loot tracking (lost on death, kept on escape)
    private List<com.rpg.model.items.ObiectEchipament> temporaryLoot;
    private int temporaryGold;
    private int temporaryExp;
    private List<com.rpg.model.items.Jewel> temporaryJewels;
    private int temporaryShaorma;

    public DungeonRun(DungeonMap map, Erou hero) {
        this.map = map;
        this.hero = hero;
        this.activeRunItems = new ArrayList<>();
        this.roomsCleared = 0;
        this.startTime = System.currentTimeMillis();
        this.bossDefeated = false;
        this.isActive = true;
        this.enemiesKilled = 0;
        this.bossesKilled = 0;
        this.highestDepthReached = map.getDepth();
        this.temporaryLoot = new ArrayList<>();
        this.temporaryGold = 0;
        this.temporaryExp = 0;
        this.temporaryJewels = new ArrayList<>();
        this.temporaryShaorma = 0;
    }

    /**
     * Adaugă un run item la colecția activă
     */
    public void addRunItem(RunItem item) {
        // Verifică dacă itemul există deja (pentru stacking)
        for (RunItem existing : activeRunItems) {
            if (existing.getName().equals(item.getName())) {
                existing.addStack();
                return;
            }
        }
        activeRunItems.add(item);
    }

    /**
     * Aplică toate modificatorii run item-urilor pe erou
     */
    public void applyRunItemModifiers() {
        // Resetează modificatorii anteriori (dacă e necesar)
        // Apoi aplică toți modificatorii activi

        for (RunItem item : activeRunItems) {
            // Aici vom aplica modificatorii pe statisticile eroului
            // De exemplu: damage boost, defense boost, etc.
            // Implementarea detaliată va fi făcută când integrăm cu sistemul de combat
        }
    }

    /**
     * Marchează o cameră ca fiind cleared
     */
    public void markRoomCleared() {
        roomsCleared++;
        map.getCurrentRoom().markCleared();
    }

    /**
     * Termină dungeon run-ul
     */
    public void endRun(boolean victory) {
        this.isActive = false;
        if (victory) {
            this.bossDefeated = true;
        }

        // Run item-urile vor fi șterse când se întoarce în oraș
        // Equipment-ul permanent rămâne cu eroul
    }

    /**
     * Calculează un modificator total pentru un stat din toate run item-urile
     */
    public double getTotalModifier(String stat) {
        double total = 0.0;
        for (RunItem item : activeRunItems) {
            total += item.getTotalModifier(stat);
        }
        return total;
    }

    /**
     * Returnează timpul total petrecut în dungeon (în secunde)
     */
    public long getRunDuration() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }

    // Getters
    public DungeonMap getMap() { return map; }
    public Erou getHero() { return hero; }
    public List<RunItem> getActiveRunItems() { return new ArrayList<>(activeRunItems); }
    public int getRoomsCleared() { return roomsCleared; }
    public boolean isBossDefeated() { return bossDefeated; }
    public boolean isActive() { return isActive; }

    // Setter for continuing to next depth
    public void setMap(DungeonMap newMap) {
        this.map = newMap;
        // Reset rooms cleared for new depth but keep run items!
        this.roomsCleared = 0;
        // Update highest depth reached
        if (newMap.getDepth() > highestDepthReached) {
            highestDepthReached = newMap.getDepth();
        }
    }

    /**
     * Record enemy kill
     */
    public void recordEnemyKill(boolean isBoss) {
        enemiesKilled++;
        if (isBoss) {
            bossesKilled++;
        }
    }

    // Getters for progression tracking
    public int getEnemiesKilled() { return enemiesKilled; }
    public int getBossesKilled() { return bossesKilled; }
    public int getHighestDepthReached() { return highestDepthReached; }

    /**
     * Add temporary loot from battle (not saved until escape)
     */
    public void addTemporaryLoot(List<com.rpg.model.items.ObiectEchipament> loot) {
        if (loot != null) {
            temporaryLoot.addAll(loot);
        }
    }

    /**
     * Add temporary gold (not saved until escape)
     */
    public void addTemporaryGold(int gold) {
        temporaryGold += gold;
    }

    /**
     * Add temporary exp (not saved until escape)
     */
    public void addTemporaryExp(int exp) {
        temporaryExp += exp;
    }

    /**
     * Add temporary jewel (not saved until escape)
     */
    public void addTemporaryJewel(com.rpg.model.items.Jewel jewel) {
        if (jewel != null) {
            temporaryJewels.add(jewel);
        }
    }

    /**
     * Add temporary shaorma (not saved until escape)
     */
    public void addTemporaryShaorma(int shaorma) {
        temporaryShaorma += shaorma;
    }

    /**
     * Apply death penalty - lose temporary loot and 30% gold
     */
    public void applyDeathPenalty() {
        System.out.println("💀 Applying death penalty...");
        System.out.println("  Lost " + temporaryLoot.size() + " items");
        System.out.println("  Lost " + temporaryGold + " temporary gold");
        System.out.println("  Lost " + temporaryExp + " temporary exp");
        System.out.println("  Lost " + temporaryJewels.size() + " jewels");
        System.out.println("  Lost " + temporaryShaorma + " shaorma");

        // Clear temporary loot
        temporaryLoot.clear();
        temporaryGold = 0;
        temporaryExp = 0;
        temporaryJewels.clear();
        temporaryShaorma = 0;

        // Lose 30% of current gold
        int goldLoss = (int) (hero.getGold() * 0.30);
        hero.setGold(Math.max(0, hero.getGold() - goldLoss));
        System.out.println("  Lost 30% current gold: " + goldLoss);
    }

    /**
     * Escape successfully - save all temporary loot to hero
     */
    public void escapeSuccessfully() {
        System.out.println("✅ Escaping successfully!");
        System.out.println("  Saving " + temporaryLoot.size() + " items");
        System.out.println("  Saving " + temporaryGold + " gold");
        System.out.println("  Saving " + temporaryExp + " exp");
        System.out.println("  Saving " + temporaryJewels.size() + " jewels");
        System.out.println("  Saving " + temporaryShaorma + " shaorma");

        // Save temporary loot to hero's inventory
        for (com.rpg.model.items.ObiectEchipament item : temporaryLoot) {
            hero.getInventar().addItem(item);
        }

        // Save jewels
        for (com.rpg.model.items.Jewel jewel : temporaryJewels) {
            hero.addJewel(jewel);
        }

        // Save gold, exp, and shaorma
        hero.adaugaGold(temporaryGold);
        hero.adaugaXp(temporaryExp);
        hero.adaugaShaormaRevival(temporaryShaorma);

        // Clear temporary tracking
        temporaryLoot.clear();
        temporaryGold = 0;
        temporaryExp = 0;
        temporaryJewels.clear();
        temporaryShaorma = 0;
    }

    // Getters for temporary loot tracking
    public List<com.rpg.model.items.ObiectEchipament> getTemporaryLoot() {
        return new ArrayList<>(temporaryLoot);
    }

    public int getTemporaryGold() { return temporaryGold; }
    public int getTemporaryExp() { return temporaryExp; }

    /**
     * Returnează un summary al run-ului pentru display
     */
    public String getRunSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════\n");
        sb.append("     DUNGEON RUN SUMMARY\n");
        sb.append("═══════════════════════════════\n");
        sb.append(String.format("Depth: %d\n", map.getDepth()));
        sb.append(String.format("Rooms Cleared: %d\n", roomsCleared));
        sb.append(String.format("Duration: %d seconds\n", getRunDuration()));
        sb.append(String.format("Status: %s\n", bossDefeated ? "VICTORY" : "IN PROGRESS"));
        sb.append("\nActive Run Items:\n");
        if (activeRunItems.isEmpty()) {
            sb.append("  (none)\n");
        } else {
            for (RunItem item : activeRunItems) {
                sb.append("  • ").append(item.toString()).append("\n");
            }
        }
        sb.append("\n💼 Temporary Loot (not saved yet):\n");
        sb.append(String.format("  Gold: %d\n", temporaryGold));
        sb.append(String.format("  Exp: %d\n", temporaryExp));
        sb.append(String.format("  Items: %d\n", temporaryLoot.size()));
        sb.append("═══════════════════════════════\n");
        return sb.toString();
    }
}
