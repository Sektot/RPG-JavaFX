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
        sb.append("═══════════════════════════════\n");
        return sb.toString();
    }
}
