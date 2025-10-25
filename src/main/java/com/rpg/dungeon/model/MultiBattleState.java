package com.rpg.dungeon.model;

import com.rpg.model.characters.Inamic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Manages multi-enemy battle state with reinforcement system
 */
public class MultiBattleState implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MAX_ACTIVE_ENEMIES = 4;

    // Active enemy slots (4 max)
    private BattleSlot[] slots = new BattleSlot[MAX_ACTIVE_ENEMIES];

    // Reinforcement queue (enemies waiting to join)
    private Queue<ReinforcementEntry> reinforcementQueue = new LinkedList<>();

    // Battle state
    private int currentTurn = 0;
    private boolean battleActive = false;

    public MultiBattleState() {
        // Initialize empty slots
        for (int i = 0; i < MAX_ACTIVE_ENEMIES; i++) {
            slots[i] = new BattleSlot(i);
        }
    }

    /**
     * Add initial enemy to battle (the one player engaged)
     */
    public void addInitialEnemy(Inamic enemy) {
        slots[0].setEnemy(enemy);
        slots[0].setActive(true);
        battleActive = true;
    }

    /**
     * Add reinforcement enemy based on distance zone
     * @param enemy The enemy to add
     * @param distanceZone 1-4 (1 = closest, 4 = farthest)
     */
    public void addReinforcement(Inamic enemy, int distanceZone) {
        int turnsUntilJoin = distanceZone * 2; // Zone 1 = 2 turns, Zone 2 = 4 turns, etc.
        int joinTurn = currentTurn + turnsUntilJoin;

        ReinforcementEntry entry = new ReinforcementEntry(enemy, joinTurn, distanceZone);
        reinforcementQueue.add(entry);

        System.out.println("📢 Reinforcement added: " + enemy.getNume() + " will join in " + turnsUntilJoin + " turns (Zone " + distanceZone + ")");
    }

    /**
     * Process turn - check if reinforcements should join
     */
    public void processTurn() {
        currentTurn++;

        // Check if any reinforcements are ready to join
        while (!reinforcementQueue.isEmpty() && reinforcementQueue.peek().getJoinTurn() <= currentTurn) {
            ReinforcementEntry entry = reinforcementQueue.poll();

            // Find empty slot
            int emptySlot = findEmptySlot();
            if (emptySlot >= 0) {
                slots[emptySlot].setEnemy(entry.getEnemy());
                slots[emptySlot].setActive(true);
                System.out.println("⚔️ " + entry.getEnemy().getNume() + " has joined the battle!");
            } else {
                // All slots full, put back in queue for later
                reinforcementQueue.add(entry);
                break;
            }
        }
    }

    /**
     * Remove enemy from slot (when defeated)
     */
    public void removeEnemy(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < MAX_ACTIVE_ENEMIES) {
            slots[slotIndex].setEnemy(null);
            slots[slotIndex].setActive(false);

            // Check if battle is over
            if (getActiveEnemyCount() == 0 && reinforcementQueue.isEmpty()) {
                battleActive = false;
            }
        }
    }

    /**
     * Get number of active enemies
     */
    public int getActiveEnemyCount() {
        int count = 0;
        for (BattleSlot slot : slots) {
            if (slot.isActive() && slot.getEnemy() != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get all active enemies
     */
    public List<Inamic> getActiveEnemies() {
        List<Inamic> enemies = new ArrayList<>();
        for (BattleSlot slot : slots) {
            if (slot.isActive() && slot.getEnemy() != null) {
                enemies.add(slot.getEnemy());
            }
        }
        return enemies;
    }

    /**
     * Find first empty slot
     */
    private int findEmptySlot() {
        for (int i = 0; i < MAX_ACTIVE_ENEMIES; i++) {
            if (!slots[i].isActive() || slots[i].getEnemy() == null) {
                return i;
            }
        }
        return -1; // All slots full
    }

    /**
     * Get slot by index
     */
    public BattleSlot getSlot(int index) {
        if (index >= 0 && index < MAX_ACTIVE_ENEMIES) {
            return slots[index];
        }
        return null;
    }

    /**
     * Get all slots
     */
    public BattleSlot[] getSlots() {
        return slots;
    }

    /**
     * Get reinforcement queue size
     */
    public int getReinforcementQueueSize() {
        return reinforcementQueue.size();
    }

    /**
     * Get next reinforcement info
     */
    public ReinforcementEntry getNextReinforcement() {
        return reinforcementQueue.peek();
    }

    public int getCurrentTurn() { return currentTurn; }
    public boolean isBattleActive() { return battleActive; }
    public void setBattleActive(boolean active) { this.battleActive = active; }

    /**
     * Represents a single enemy slot in battle
     */
    public static class BattleSlot implements Serializable {
        private static final long serialVersionUID = 1L;

        private int slotIndex;
        private Inamic enemy;
        private boolean active;

        public BattleSlot(int slotIndex) {
            this.slotIndex = slotIndex;
            this.active = false;
        }

        public int getSlotIndex() { return slotIndex; }
        public Inamic getEnemy() { return enemy; }
        public void setEnemy(Inamic enemy) { this.enemy = enemy; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    /**
     * Represents an enemy waiting to join battle
     */
    public static class ReinforcementEntry implements Serializable {
        private static final long serialVersionUID = 1L;

        private Inamic enemy;
        private int joinTurn; // Turn number when enemy will join
        private int distanceZone; // 1-4

        public ReinforcementEntry(Inamic enemy, int joinTurn, int distanceZone) {
            this.enemy = enemy;
            this.joinTurn = joinTurn;
            this.distanceZone = distanceZone;
        }

        public Inamic getEnemy() { return enemy; }
        public int getJoinTurn() { return joinTurn; }
        public int getDistanceZone() { return distanceZone; }
        public int getTurnsRemaining(int currentTurn) {
            return Math.max(0, joinTurn - currentTurn);
        }
    }
}
