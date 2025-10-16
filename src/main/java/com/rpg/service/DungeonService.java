package com.rpg.service;

import com.rpg.model.characters.Erou;
import com.rpg.utils.Validator;

import java.io.Serializable;
import java.util.Scanner;

/**
 * Service responsabil cu gestionarea checkpoint-urilor în sistemul de luptă.
 * Gestionează progresul prin dungeon-uri cu checkpoint-uri la fiecare 10 nivele.
 */
public class DungeonService implements Serializable {

    private static final long serialVersionUID = 1L;

    // Constante pentru dungeon
    private static final int CHECKPOINT_INTERVAL = 10;
    private static final int MAX_DUNGEON_LEVEL = 100;

    // Starea curentă a dungeon-ului
    private int currentDungeonLevel;
    private int highestCheckpoint;
    private boolean inDungeon;

    public DungeonService() {
        this.currentDungeonLevel = 1;
        this.highestCheckpoint = 0;
        this.inDungeon = false;
    }


    /**
     * Afișează meniul de dungeon și permite jucătorului să aleagă de unde să înceapă.
     */
    public int chooseDungeonStart(Erou erou, Scanner scanner) {
        System.out.println("\n🏰 === DUNGEON AVENTURA ===");
        System.out.println("Bine ai venit în Dungeon, " + erou.getNume() + "!");
        System.out.println();

        // Afișează checkpoint-urile disponibile
        showAvailableCheckpoints();

        System.out.println("De unde vrei să începi aventura?");
        System.out.println();

        // Opțiuni de start
        int optionCount = 1;
        System.out.println(optionCount + ". Începe de la nivelul 1 (Fresh Start)");

        // Afișează checkpoint-urile disponibile
        for (int checkpoint = CHECKPOINT_INTERVAL; checkpoint <= highestCheckpoint; checkpoint += CHECKPOINT_INTERVAL) {
            optionCount++;
            System.out.println(optionCount + ". Începe de la checkpoint nivelul " + checkpoint);
        }

        optionCount++;
        System.out.println(optionCount + ". Continuă de la nivelul curent (" + currentDungeonLevel + ")");

        optionCount++;
        System.out.println(optionCount + ". Înapoi la meniul principal");

        int choice = Validator.readValidChoice(scanner, 1, optionCount);

        if (choice == 1) {
            // Fresh start
            currentDungeonLevel = 1;
            inDungeon = true;
            System.out.println("\n🆕 Începi o nouă aventură de la nivelul 1!");
            return 1;
        } else if (choice == optionCount) {
            // Înapoi la meniu
            return -1;
        } else if (choice == optionCount - 1) {
            // Continuă de la nivelul curent
            inDungeon = true;
            System.out.println("\n▶️ Continui aventura de la nivelul " + currentDungeonLevel + "!");
            return currentDungeonLevel;
        } else {
            // Checkpoint selectat
            int selectedCheckpoint = (choice - 2) * CHECKPOINT_INTERVAL + CHECKPOINT_INTERVAL;
            currentDungeonLevel = selectedCheckpoint;
            inDungeon = true;
            System.out.println("\n🎯 Începi de la checkpoint-ul nivelul " + selectedCheckpoint + "!");
            return selectedCheckpoint;
        }
    }

    /**
     * Afișează checkpoint-urile disponibile.
     */
    private void showAvailableCheckpoints() {
        System.out.println("📍 Progresul tău în Dungeon:");
        System.out.println("   🎯 Nivelul curent: " + currentDungeonLevel);
        System.out.println("   🏁 Cel mai înalt checkpoint: " + (highestCheckpoint > 0 ? highestCheckpoint : "Niciunul"));
        System.out.println();

        if (highestCheckpoint > 0) {
            System.out.println("🔓 Checkpoint-uri deblocate:");
            for (int checkpoint = CHECKPOINT_INTERVAL; checkpoint <= highestCheckpoint; checkpoint += CHECKPOINT_INTERVAL) {
                System.out.println("   ✅ Nivelul " + checkpoint);
            }
        } else {
            System.out.println("🔒 Nu ai încă checkpoint-uri deblocate.");
            System.out.println("💡 Ajunge la nivelul 10 pentru primul checkpoint!");
        }

        System.out.println();
    }

    /**
     * Procesează victoria unei lupte și verifică pentru checkpoint-uri.
     */
    public boolean processVictory(int battleLevel, Erou erou) {
        currentDungeonLevel = battleLevel + 1;

        // Verifică dacă am atins un nou checkpoint
        if (battleLevel % CHECKPOINT_INTERVAL == 0 && battleLevel > highestCheckpoint) {
            unlockCheckpoint(battleLevel, erou);
            return true; // Nou checkpoint deblokcat
        }

        return false; // Nu e checkpoint
    }

    /**
     * Deblochează un nou checkpoint.
     */
    private void unlockCheckpoint(int checkpointLevel, Erou erou) {
        highestCheckpoint = checkpointLevel;

        System.out.println("\n🎉 *** CHECKPOINT DEBLOKCAT! ***");
        System.out.println("🏁 Ai atins nivelul " + checkpointLevel + "!");
        System.out.println("💾 Progresul tău a fost salvat automat!");
        System.out.println("🔓 Poți acum să începi de la acest nivel oricând!");

        // Recompense pentru checkpoint
        int bonusGold = checkpointLevel * 10;
        int bonusXP = checkpointLevel * 5;

        erou.adaugaGold(bonusGold);
        System.out.println();
        System.out.println("🎁 Recompense checkpoint:");
        System.out.println("   💰 Bonus Gold: +" + bonusGold);
        System.out.println("   ⭐ Bonus XP: +" + bonusXP);

        // Oferă XP bonus
         erou.adaugaXp(bonusXP);

        System.out.println();
        System.out.println("🔥 Ești gata pentru următoarele provocări!");
    }

    /**
     * Gestionează înfrângerea în dungeon.
     */
    public void processDefeat(int battleLevel) {
        System.out.println("\n💀 Ai fost învins la nivelul " + battleLevel + "!");

        if (highestCheckpoint > 0) {
            System.out.println("🔄 Poți să reîncerci de la cel mai apropiat checkpoint (Nivelul " + getLastCheckpoint() + ")");
        } else {
            System.out.println("🔄 Poți să reîncerci de la începutul dungeon-ului (Nivelul 1)");
        }

        // Resetează la ultimul checkpoint sau la început
        currentDungeonLevel = getLastCheckpoint() > 0 ? getLastCheckpoint() : 1;
        inDungeon = false;
    }

    /**
     * Returnează ultimul checkpoint deblokcat.
     */
    private int getLastCheckpoint() {
        if (highestCheckpoint == 0) {
            return 0;
        }

        // Găsește ultimul checkpoint complet
        return (highestCheckpoint / CHECKPOINT_INTERVAL) * CHECKPOINT_INTERVAL;
    }

    /**
     * Afișează statistici despre progresul în dungeon.
     */
    public void showDungeonStats() {
        System.out.println("\n📊 === STATISTICI DUNGEON ===");
        System.out.println("🎯 Nivelul curent: " + currentDungeonLevel);
        System.out.println("🏁 Cel mai înalt nivel atins: " + highestCheckpoint);
        System.out.println("🔓 Checkpoint-uri deblocate: " + (highestCheckpoint / CHECKPOINT_INTERVAL));
        System.out.println("📈 Progres total: " + String.format("%.1f", (highestCheckpoint / (double) MAX_DUNGEON_LEVEL) * 100) + "%");

        if (highestCheckpoint >= MAX_DUNGEON_LEVEL) {
            System.out.println("👑 FELICITĂRI! Ai terminat dungeon-ul complet!");
        } else {
            int nextCheckpoint = ((highestCheckpoint / CHECKPOINT_INTERVAL) + 1) * CHECKPOINT_INTERVAL;
            System.out.println("🎯 Următorul checkpoint: Nivelul " + nextCheckpoint);
        }
        System.out.println("============================");
    }

    /**
     * Verifică dacă jucătorul poate continua în dungeon.
     */
    public boolean canContinue() {
        return currentDungeonLevel <= MAX_DUNGEON_LEVEL;
    }

    /**
     * Calculează dificultatea pentru un nivel dat.
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
     * Resetează complet progresul dungeon-ului.
     */
    public void resetProgress(Scanner scanner) {
        System.out.println("\n⚠️ RESETARE PROGRES DUNGEON");
        System.out.println("Aceasta va șterge toate checkpoint-urile și progresul!");
        System.out.println("Vei începe din nou de la nivelul 1!");

        if (Validator.readConfirmation(scanner, "Ești sigur că vrei să resetezi progresul?")) {
            currentDungeonLevel = 1;
            highestCheckpoint = 0;
            inDungeon = false;
            System.out.println("\n✅ Progresul a fost resetat!");
        } else {
            System.out.println("\n❌ Resetarea a fost anulată.");
        }
    }

    // Getteri și setteri
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



    /**
     * Verifică dacă un nivel este checkpoint.
     */
    public boolean isCheckpointLevel(int level) {
        return level % CHECKPOINT_INTERVAL == 0;
    }
}