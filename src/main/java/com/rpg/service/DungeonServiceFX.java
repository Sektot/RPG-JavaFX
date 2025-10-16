package com.rpg.service;

import com.rpg.model.characters.Erou;
import com.rpg.service.dto.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DungeonServiceFX - Refactorizat pentru JavaFX
 * Gestionează checkpoint-uri și progresul în dungeon fără Scanner
 */
public class DungeonServiceFX implements Serializable {

    private static final long serialVersionUID = 1L;

    // Constante
    private static final int CHECKPOINT_INTERVAL = 10;
    private static final int MAX_DUNGEON_LEVEL = 100;

    // Starea curentă
    private int currentDungeonLevel;
    private int highestCheckpoint;
    private boolean inDungeon;

    public DungeonServiceFX() {
        this.currentDungeonLevel = 1;
        this.highestCheckpoint = 0;
        this.inDungeon = false;
    }

    /**
     * Obține informații despre dungeon pentru UI
     */
    public DungeonStartInfoDTO getDungeonInfo() {
        List<DungeonCheckpointDTO> checkpoints = new ArrayList<>();

        // Adaugă checkpoint-urile deblocate
        for (int level = CHECKPOINT_INTERVAL; level <= highestCheckpoint; level += CHECKPOINT_INTERVAL) {
            checkpoints.add(new DungeonCheckpointDTO(
                    level,
                    getDifficultyDescription(level),
                    true,
                    level == highestCheckpoint
            ));
        }

        // Adaugă următorul checkpoint (locked)
        if (highestCheckpoint < MAX_DUNGEON_LEVEL) {
            int nextCheckpoint = ((highestCheckpoint / CHECKPOINT_INTERVAL) + 1) * CHECKPOINT_INTERVAL;
            if (nextCheckpoint <= MAX_DUNGEON_LEVEL) {
                checkpoints.add(new DungeonCheckpointDTO(
                        nextCheckpoint,
                        getDifficultyDescription(nextCheckpoint),
                        false,
                        false
                ));
            }
        }

        return new DungeonStartInfoDTO(
                currentDungeonLevel,
                highestCheckpoint,
                checkpoints,
                inDungeon,
                CHECKPOINT_INTERVAL,
                MAX_DUNGEON_LEVEL
        );
    }

    /**
     * Începe dungeonul de la un nivel specific
     */
    public DungeonStartResult startFromLevel(int level) {
        // Validări
        if (level < 1) {
            return new DungeonStartResult(false, "Nivelul minim este 1!", 1);
        }

        if (level > MAX_DUNGEON_LEVEL) {
            return new DungeonStartResult(false, "Nivelul maxim este " + MAX_DUNGEON_LEVEL + "!", MAX_DUNGEON_LEVEL);
        }

        // Verifică dacă nivelul este deblokcat
        if (level > 1 && level > highestCheckpoint + 1) {
            // Nu poți începe de la un nivel care nu e deblokcat
            // Excepție: checkpoint-urile deblocate
            boolean isUnlockedCheckpoint = false;
            for (int checkpoint = CHECKPOINT_INTERVAL; checkpoint <= highestCheckpoint; checkpoint += CHECKPOINT_INTERVAL) {
                if (level == checkpoint) {
                    isUnlockedCheckpoint = true;
                    break;
                }
            }

            if (!isUnlockedCheckpoint && level != 1) {
                return new DungeonStartResult(
                        false,
                        "Acest nivel nu este deblokcat! Cel mai înalt nivel disponibil: " + (highestCheckpoint + 1),
                        Math.min(level, highestCheckpoint + 1)
                );
            }
        }

        // Start dungeon
        currentDungeonLevel = level;
        inDungeon = true;

        String message = level == 1
                ? "Începi o nouă aventură de la nivelul 1!"
                : "Începi de la nivelul " + level + "!";

        return new DungeonStartResult(true, message, level);
    }

    /**
     * Procesează victoria și verifică pentru checkpoint
     */
    public DungeonVictoryResult processVictory(int battleLevel, Erou erou) {
        currentDungeonLevel = battleLevel + 1;

        boolean newCheckpoint = false;
        int goldReward = 0;
        int expReward = 0;

        // Verifică dacă e checkpoint
        if (battleLevel % CHECKPOINT_INTERVAL == 0 && battleLevel > highestCheckpoint) {
            newCheckpoint = true;
            highestCheckpoint = battleLevel;

            // Recompense checkpoint
            goldReward = battleLevel * 10;
            expReward = battleLevel * 5;

            if (erou != null) {
                erou.adaugaGold(goldReward);
                erou.adaugaXp(expReward);
            }
        }

        return new DungeonVictoryResult(
                true,
                newCheckpoint,
                currentDungeonLevel,
                goldReward,
                expReward,
                currentDungeonLevel > MAX_DUNGEON_LEVEL ? "Ai terminat dungeonul complet!" : null
        );
    }

    /**
     * Procesează înfrângerea
     */
    public DungeonDefeatResult processDefeat(int battleLevel) {
        inDungeon = false;

        int resetLevel = getLastCheckpoint() > 0 ? getLastCheckpoint() : 1;
        currentDungeonLevel = resetLevel;

        return new DungeonDefeatResult(
                battleLevel,
                resetLevel,
                highestCheckpoint > 0,
                "Ai fost învins la nivelul " + battleLevel + "!"
        );
    }

    /**
     * Resetează complet progresul
     */
    public boolean resetProgress() {
        currentDungeonLevel = 1;
        highestCheckpoint = 0;
        inDungeon = false;
        return true;
    }

    /**
     * Statistici despre progres
     */
    public DungeonStatsDTO getStats() {
        int checkpointsUnlocked = highestCheckpoint / CHECKPOINT_INTERVAL;
        int totalCheckpoints = MAX_DUNGEON_LEVEL / CHECKPOINT_INTERVAL;
        double progressPercentage = (highestCheckpoint / (double) MAX_DUNGEON_LEVEL) * 100;
        boolean completed = highestCheckpoint >= MAX_DUNGEON_LEVEL;

        return new DungeonStatsDTO(
                currentDungeonLevel,
                highestCheckpoint,
                checkpointsUnlocked,
                totalCheckpoints,
                progressPercentage,
                completed
        );
    }

    /**
     * Verifică dacă poate continua
     */
    public boolean canContinue() {
        return currentDungeonLevel <= MAX_DUNGEON_LEVEL;
    }

    /**
     * Verifică dacă un nivel este checkpoint
     */
    public boolean isCheckpointLevel(int level) {
        return level % CHECKPOINT_INTERVAL == 0;
    }

    /**
     * Obține descrierea dificultății pentru un nivel
     */
    public String getDifficultyDescription(int level) {
        if (level <= 10) {
            return "🟢 Ușor";
        } else if (level <= 30) {
            return "🟡 Mediu";
        } else if (level <= 60) {
            return "🟠 Greu";
        } else if (level <= 90) {
            return "🔴 Foarte Greu";
        } else {
            return "💀 Extrem";
        }
    }

    /**
     * Obține icoana pentru dificultate
     */
    public String getDifficultyIcon(int level) {
        if (level <= 10) return "🟢";
        else if (level <= 30) return "🟡";
        else if (level <= 60) return "🟠";
        else if (level <= 90) return "🔴";
        else return "💀";
    }

    /**
     * Calculează recompensele estimate pentru un nivel
     */
    public DungeonRewardsDTO estimateRewards(int level) {
        int baseGold = level * 5;
        int baseExp = level * 3;
        boolean isBoss = level % 5 == 0;

        if (isBoss) {
            baseGold *= 2;
            baseExp *= 2;
        }

        return new DungeonRewardsDTO(
                baseGold,
                baseExp,
                isBoss,
                isCheckpointLevel(level)
        );
    }

    // ==================== HELPER METHODS ====================

    private int getLastCheckpoint() {
        if (highestCheckpoint == 0) {
            return 0;
        }
        return (highestCheckpoint / CHECKPOINT_INTERVAL) * CHECKPOINT_INTERVAL;
    }

    // ==================== GETTERS & SETTERS ====================

    public int getCurrentDungeonLevel() {
        return currentDungeonLevel;
    }

    public void setCurrentDungeonLevel(int level) {
        this.currentDungeonLevel = Math.max(1, Math.min(level, MAX_DUNGEON_LEVEL));
    }

    public int getHighestCheckpoint() {
        return highestCheckpoint;
    }

    public void setHighestCheckpoint(int checkpoint) {
        this.highestCheckpoint = Math.max(0, checkpoint);
    }

    public boolean isInDungeon() {
        return inDungeon;
    }

    public void setInDungeon(boolean inDungeon) {
        this.inDungeon = inDungeon;
    }

    public int getCheckpointInterval() {
        return CHECKPOINT_INTERVAL;
    }

    public int getMaxDungeonLevel() {
        return MAX_DUNGEON_LEVEL;
    }
}