package com.rpg.service;

import com.rpg.model.characters.Erou;
import com.rpg.model.items.FlaskPiece;
import com.rpg.utils.GameConstants;
import com.rpg.utils.Validator;

import java.util.Map;
import java.util.Scanner;

/**
 * Service pentru upgrade poțiuni folosind Flask Pieces.
 * Permite upgrade de la Basic -> Enhanced -> Superior -> Master.
 */
public class PotionUpgradeService {

    // Tipuri de poțiuni upgraded
    public enum PotionTier {
        BASIC("Basic", 1.0, 0, "🧪"),
        ENHANCED("Enhanced", 1.5, 50, "✨"),
        SUPERIOR("Superior", 2.0, 75, "💎"),
        MASTER("Master", 3.0, 100, "🌟");

        private final String displayName;
        private final double multiplier;
        private final int baseValue;
        private final String icon;

        PotionTier(String displayName, double multiplier, int baseValue, String icon) {
            this.displayName = displayName;
            this.multiplier = multiplier;
            this.baseValue = baseValue;
            this.icon = icon;
        }

        public String getDisplayName() { return displayName; }
        public double getMultiplier() { return multiplier; }
        public int getBaseValue() { return baseValue; }
        public String getIcon() { return icon; }

        public PotionTier getNext() {
            return switch (this) {
                case BASIC -> ENHANCED;
                case ENHANCED -> SUPERIOR;
                case SUPERIOR -> MASTER;
                case MASTER -> MASTER; // Max level
            };
        }
    }

    public void openUpgradeService(Erou erou, Scanner scanner) {
        boolean inService = true;

        while (inService) {
            displayUpgradeMenu(erou);

            System.out.println("\n🎯 Opțiuni disponibile:");
            System.out.println("1. 🧪 Upgrade Health Potions");
            System.out.println("2. 💙 Upgrade Mana Potions");
            System.out.println("3. 📊 Vezi poțiunile actuale");
            System.out.println("4. ℹ️ Informații despre upgrade");
            System.out.println("5. 🔙 Înapoi");

            System.out.print("\n➤ Alege opțiunea (1-5): ");
            int choice = Validator.readValidChoice(scanner, 1, 5);

            switch (choice) {
                case 1 -> upgradeHealthPotions(erou, scanner);
                case 2 -> upgradeManaPotions(erou, scanner);
                case 3 -> showCurrentPotions(erou);
                case 4 -> showUpgradeInfo();
                case 5 -> inService = false;
            }

            if (inService && choice != 5) {
                waitForEnter();
            }
        }
    }

    private void displayUpgradeMenu(Erou erou) {
        System.out.println("\n" + "═".repeat(60));
        System.out.println(" 🧙‍♂️ ALCHEMY WORKSHOP - POTION UPGRADES");
        System.out.println("═".repeat(60));
        System.out.printf("👤 %s | 💰 Gold: %d | 🔮 Shards: %d\n",
                erou.getNume(), erou.getGold(), erou.getScrap());

        // Afișează Flask Pieces disponibile
        System.out.println("\n🧪 FLASK PIECES DISPONIBILE:");
        Map<FlaskPiece.FlaskType, Integer> flaskPieces = getFlaskPieces(erou);

        if (flaskPieces.isEmpty()) {
            System.out.println("❌ Nu ai Flask Pieces!");
            System.out.println("💡 Flask Pieces cad din Boss-i cu șansă mare!");
        } else {
            flaskPieces.forEach((type, quantity) ->
                    System.out.printf(" %s %s: %d bucăți\n",
                            type.getIcon(), type.getDisplayName(), quantity));
        }
    }

    private void upgradeHealthPotions(Erou erou, Scanner scanner) {
        System.out.println("\n" + "═".repeat(50));
        System.out.println(" 🧪 UPGRADE HEALTH POTIONS");
        System.out.println("═".repeat(50));

        PotionTier currentTier = getHealthPotionTier(erou);
        PotionTier nextTier = currentTier.getNext();

        System.out.printf("Tier actual: %s %s Health Potions\n",
                currentTier.getIcon(), currentTier.getDisplayName());
        System.out.printf("Healing actual: %d HP\n", getHealthPotionHealing(currentTier));
        System.out.printf("Poțiuni disponibile: %d\n", erou.getHealthPotions());

        if (currentTier == PotionTier.MASTER) {
            System.out.println("\n🌟 Poțiunile tale sunt deja la nivelul maxim!");
            return;
        }

        System.out.println("\n⬆️ UPGRADE DISPONIBIL:");
        System.out.printf("Upgrade la: %s %s Health Potions\n",
                nextTier.getIcon(), nextTier.getDisplayName());
        System.out.printf("Healing nou: %d HP (+%d)\n",
                getHealthPotionHealing(nextTier),
                getHealthPotionHealing(nextTier) - getHealthPotionHealing(currentTier));

        // Calculează costul
        int flaskCost = calculateFlaskCost(currentTier, nextTier);
        int goldCost = calculateGoldCost(currentTier, nextTier);

        System.out.printf("\n💰 Cost upgrade: %d Flask Pieces + %d Gold\n",
                flaskCost, goldCost);

        // Verifică resurse
        int healthFlasks = getFlaskQuantity(erou, FlaskPiece.FlaskType.HEALTH);
        int universalFlasks = getFlaskQuantity(erou, FlaskPiece.FlaskType.UNIVERSAL);
        int totalFlasks = healthFlasks + universalFlasks;

        if (totalFlasks < flaskCost || erou.getGold() < goldCost) {
            System.out.println("\n❌ RESURSE INSUFICIENTE:");
            if (totalFlasks < flaskCost) {
                System.out.printf("🧪 Îți lipsesc %d Flask Pieces\n", flaskCost - totalFlasks);
            }
            if (erou.getGold() < goldCost) {
                System.out.printf("💰 Îți lipsesc %d Gold\n", goldCost - erou.getGold());
            }
            return;
        }

        System.out.printf("\n❓ Confirmi upgrade pentru %d Flask Pieces + %d Gold? (y/n): ",
                flaskCost, goldCost);

        if (Validator.readConfirmation(scanner, "")) {
            // Consumă resurse
            consumeFlaskPieces(erou, FlaskPiece.FlaskType.HEALTH, flaskCost);
            erou.scadeGold(goldCost);

            // Aplică upgrade
            setHealthPotionTier(erou, nextTier);

            System.out.println("\n✅ UPGRADE REUȘIT!");
            System.out.printf("🧪 Health Potions upgraded la %s %s!\n",
                    nextTier.getIcon(), nextTier.getDisplayName());
            System.out.printf("💚 Healing nou: %d HP per poțiune!\n",
                    getHealthPotionHealing(nextTier));
        } else {
            System.out.println("\n❌ Upgrade anulat.");
        }
    }

    private void upgradeManaPotions(Erou erou, Scanner scanner) {
        System.out.println("\n" + "═".repeat(50));
        System.out.println(" 💙 UPGRADE MANA POTIONS");
        System.out.println("═".repeat(50));

        PotionTier currentTier = getManaPotionTier(erou);
        PotionTier nextTier = currentTier.getNext();

        System.out.printf("Tier actual: %s %s Mana Potions\n",
                currentTier.getIcon(), currentTier.getDisplayName());
        System.out.printf("Restore actual: %d %s\n",
                getManaPotionRestore(currentTier), erou.getTipResursa());
        System.out.printf("Poțiuni disponibile: %d\n", erou.getManaPotions());

        if (currentTier == PotionTier.MASTER) {
            System.out.println("\n🌟 Poțiunile tale sunt deja la nivelul maxim!");
            return;
        }

        System.out.println("\n⬆️ UPGRADE DISPONIBIL:");
        System.out.printf("Upgrade la: %s %s Mana Potions\n",
                nextTier.getIcon(), nextTier.getDisplayName());
        System.out.printf("Restore nou: %d %s (+%d)\n",
                getManaPotionRestore(nextTier), erou.getTipResursa(),
                getManaPotionRestore(nextTier) - getManaPotionRestore(currentTier));

        // Calculează costul
        int flaskCost = calculateFlaskCost(currentTier, nextTier);
        int goldCost = calculateGoldCost(currentTier, nextTier);

        System.out.printf("\n💰 Cost upgrade: %d Flask Pieces + %d Gold\n",
                flaskCost, goldCost);

        // Verifică resurse
        int manaFlasks = getFlaskQuantity(erou, FlaskPiece.FlaskType.MANA);
        int universalFlasks = getFlaskQuantity(erou, FlaskPiece.FlaskType.UNIVERSAL);
        int totalFlasks = manaFlasks + universalFlasks;

        if (totalFlasks < flaskCost || erou.getGold() < goldCost) {
            System.out.println("\n❌ RESURSE INSUFICIENTE:");
            if (totalFlasks < flaskCost) {
                System.out.printf("🧪 Îți lipsesc %d Flask Pieces\n", flaskCost - totalFlasks);
            }
            if (erou.getGold() < goldCost) {
                System.out.printf("💰 Îți lipsesc %d Gold\n", goldCost - erou.getGold());
            }
            return;
        }

        System.out.printf("\n❓ Confirmi upgrade pentru %d Flask Pieces + %d Gold? (y/n): ",
                flaskCost, goldCost);

        if (Validator.readConfirmation(scanner, "")) {
            // Consumă resurse
            consumeFlaskPieces(erou, FlaskPiece.FlaskType.MANA, flaskCost);
            erou.scadeGold(goldCost);

            // Aplică upgrade
            setManaPotionTier(erou, nextTier);

            System.out.println("\n✅ UPGRADE REUȘIT!");
            System.out.printf("💙 Mana Potions upgraded la %s %s!\n",
                    nextTier.getIcon(), nextTier.getDisplayName());
            System.out.printf("🔋 Restore nou: %d %s per poțiune!\n",
                    getManaPotionRestore(nextTier), erou.getTipResursa());
        } else {
            System.out.println("\n❌ Upgrade anulat.");
        }
    }

    // Metode helper pentru tracking tier-urile poțiunilor
    // Acestea vor fi implementate în Erou class
    private PotionTier getHealthPotionTier(Erou erou) {
        return erou.getHealthPotionTier();
    }

    private PotionTier getManaPotionTier(Erou erou) {
        return erou.getManaPotionTier();
    }

    private void setHealthPotionTier(Erou erou, PotionTier tier) {
        erou.setHealthPotionTier(tier);
    }

    private void setManaPotionTier(Erou erou, PotionTier tier) {
        erou.setManaPotionTier(tier);
    }
    private int getHealthPotionHealing(PotionTier tier) {
        return (int)(GameConstants.HEALTH_POTION_HEAL * tier.getMultiplier()) + tier.getBaseValue();
    }

    private int getManaPotionRestore(PotionTier tier) {
        return (int)(GameConstants.MANA_POTION_RESTORE * tier.getMultiplier()) + tier.getBaseValue();
    }

    private int calculateFlaskCost(PotionTier current, PotionTier next) {
        return switch (next) {
            case ENHANCED -> 3;
            case SUPERIOR -> 5;
            case MASTER -> 8;
            default -> 1;
        };
    }

    private int calculateGoldCost(PotionTier current, PotionTier next) {
        return switch (next) {
            case ENHANCED -> 200;
            case SUPERIOR -> 500;
            case MASTER -> 1000;
            default -> 100;
        };
    }

    private Map<FlaskPiece.FlaskType, Integer> getFlaskPieces(Erou erou) {
        return erou.getAllFlaskPieces();
    }

    private int getFlaskQuantity(Erou erou, FlaskPiece.FlaskType type) {
        return erou.getFlaskPiecesQuantity(type);
    }

    private void consumeFlaskPieces(Erou erou, FlaskPiece.FlaskType preferredType, int amount) {
        erou.consumeFlaskPieces(preferredType, amount);
    }



    private void showCurrentPotions(Erou erou) {
        System.out.println("\n" + "═".repeat(50));
        System.out.println(" 📊 POȚIUNILE TALE ACTUALE");
        System.out.println("═".repeat(50));

        PotionTier healthTier = getHealthPotionTier(erou);
        PotionTier manaTier = getManaPotionTier(erou);

        System.out.printf("🧪 Health Potions: %s %s (x%d)\n",
                healthTier.getIcon(), healthTier.getDisplayName(), erou.getHealthPotions());
        System.out.printf("   💚 Healing: %d HP per poțiune\n", getHealthPotionHealing(healthTier));

        System.out.printf("💙 Mana Potions: %s %s (x%d)\n",
                manaTier.getIcon(), manaTier.getDisplayName(), erou.getManaPotions());
        System.out.printf("   🔋 Restore: %d %s per poțiune\n",
                getManaPotionRestore(manaTier), erou.getTipResursa());
    }

    private void showUpgradeInfo() {
        System.out.println("\n" + "═".repeat(60));
        System.out.println(" ℹ️ INFORMAȚII UPGRADE POȚIUNI");
        System.out.println("═".repeat(60));

        System.out.println("🧪 FLASK PIECES:");
        System.out.println("  • Cad din Boss-i cu șansă mare (70-80%)");
        System.out.println("  • Health Flask Pieces - pentru health potions");
        System.out.println("  • Mana Flask Pieces - pentru mana potions");
        System.out.println("  • Universal Flask Pieces - pentru orice poțiuni");

        System.out.println("\n⬆️ TIER-URILE POȚIUNILOR:");
        for (PotionTier tier : PotionTier.values()) {
            int healthHealing = getHealthPotionHealing(tier);
            int manaRestore = getManaPotionRestore(tier);
            System.out.printf("  %s %s: %dx healing/restore (+%d base)\n",
                    tier.getIcon(), tier.getDisplayName(),
                    (int)(tier.getMultiplier() * 100), tier.getBaseValue());
        }

        System.out.println("\n💰 COSTURI UPGRADE:");
        System.out.println("  Basic → Enhanced: 3 Flask + 200 Gold");
        System.out.println("  Enhanced → Superior: 5 Flask + 500 Gold");
        System.out.println("  Superior → Master: 8 Flask + 1000 Gold");
    }

    private void waitForEnter() {
        System.out.println("\n📝 Apasă Enter pentru a continua...");
        try {
            System.in.read();
        } catch (Exception e) {
            // Ignore
        }
    }
}