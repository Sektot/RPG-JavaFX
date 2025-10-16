package com.rpg.service;

import com.rpg.model.characters.Erou;
import com.rpg.model.items.BuffPotion;
import com.rpg.model.items.FlaskPiece;
import com.rpg.utils.Validator;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Service pentru upgrade Buff Potions folosind Universal Flask Shards.
 * Sistemul de tier-uri mărește durata și intensitatea buff-urilor.
 */
public class BuffPotionUpgradeService {

    /**
     * Tier-uri pentru Buff Potions - măresc durata și bonusurile.
     */
    public enum BuffPotionTier {
        BASIC("Basic", 1.0, 3, "🧪"),
        ENHANCED("Enhanced", 1.3, 4, "✨"),
        SUPERIOR("Superior", 1.6, 5, "💎"),
        MASTER("Master", 2.0, 6, "🌟");

        private final String displayName;
        private final double bonusMultiplier;  // Multiplică bonusurile
        private final int duration;             // Numărul de lupte
        private final String icon;

        BuffPotionTier(String displayName, double bonusMultiplier, int duration, String icon) {
            this.displayName = displayName;
            this.bonusMultiplier = bonusMultiplier;
            this.duration = duration;
            this.icon = icon;
        }

        public String getDisplayName() { return displayName; }
        public double getBonusMultiplier() { return bonusMultiplier; }
        public int getDuration() { return duration; }
        public String getIcon() { return icon; }

        public BuffPotionTier getNext() {
            return switch (this) {
                case BASIC -> ENHANCED;
                case ENHANCED -> SUPERIOR;
                case SUPERIOR -> MASTER;
                case MASTER -> MASTER;
            };
        }
    }

    // Tracking tier-uri pentru fiecare tip de buff potion
    private Map<BuffPotion.BuffType, BuffPotionTier> buffPotionTiers = new HashMap<>();

    public void openUpgradeService(Erou erou, Scanner scanner) {
        initializeTiers(erou);

        boolean inService = true;
        while (inService) {
            displayUpgradeMenu(erou);

            System.out.println("\n🎯 Opțiuni disponibile:");
            System.out.println("1. 💪 Upgrade Strength Potions");
            System.out.println("2. 🏃 Upgrade Dexterity Potions");
            System.out.println("3. 🧠 Upgrade Intelligence Potions");
            System.out.println("4. ⚔️ Upgrade Damage Potions");
            System.out.println("5. 🛡️ Upgrade Defense Potions");
            System.out.println("6. ⚡ Upgrade Critical Potions");
            System.out.println("7. 💨 Upgrade Speed Potions");
            System.out.println("8. 🔥 Upgrade Berserker Potions");
            System.out.println("9. ⛰️ Upgrade Fortification Potions");
            System.out.println("10. 🌟 Upgrade Master Potions");
            System.out.println("11. ℹ️ Informații despre upgrade");
            System.out.println("12. 🔙 Înapoi");

            System.out.print("\n➤ Alege opțiunea (1-12): ");
            int choice = Validator.readValidChoice(scanner, 1, 12);

            switch (choice) {
                case 1 -> upgradeBuffPotion(erou, BuffPotion.BuffType.STRENGTH, scanner);
                case 2 -> upgradeBuffPotion(erou, BuffPotion.BuffType.DEXTERITY, scanner);
                case 3 -> upgradeBuffPotion(erou, BuffPotion.BuffType.INTELLIGENCE, scanner);
                case 4 -> upgradeBuffPotion(erou, BuffPotion.BuffType.DAMAGE, scanner);
                case 5 -> upgradeBuffPotion(erou, BuffPotion.BuffType.DEFENSE, scanner);
                case 6 -> upgradeBuffPotion(erou, BuffPotion.BuffType.CRITICAL, scanner);
                case 7 -> upgradeBuffPotion(erou, BuffPotion.BuffType.SPEED, scanner);
                case 8 -> upgradeBuffPotion(erou, BuffPotion.BuffType.BERSERKER, scanner);
                case 9 -> upgradeBuffPotion(erou, BuffPotion.BuffType.FORTIFICATION, scanner);
                case 10 -> upgradeBuffPotion(erou, BuffPotion.BuffType.MASTER, scanner);
                case 11 -> showUpgradeInfo();
                case 12 -> inService = false;
            }

            if (inService && choice != 12) {
                waitForEnter();
            }
        }
    }

    private void initializeTiers(Erou erou) {
        // Inițializează toate tier-urile la BASIC dacă nu există
        for (BuffPotion.BuffType type : BuffPotion.BuffType.values()) {
            buffPotionTiers.putIfAbsent(type, BuffPotionTier.BASIC);
        }
    }

    private void displayUpgradeMenu(Erou erou) {
        System.out.println("\n" + "═".repeat(60));
        System.out.println(" 🧙‍♂️ ALCHEMY WORKSHOP - BUFF POTION UPGRADES");
        System.out.println("═".repeat(60));
        System.out.printf("👤 %s | 💰 Gold: %d\n", erou.getNume(), erou.getGold());

        // Afișează Universal Flask Pieces
        int universalFlasks = erou.getFlaskPiecesQuantity(FlaskPiece.FlaskType.UNIVERSAL);
        System.out.println("\n✨ UNIVERSAL FLASK PIECES DISPONIBILE: " + universalFlasks);

        if (universalFlasks == 0) {
            System.out.println("❌ Nu ai Universal Flask Pieces!");
            System.out.println("💡 Universal Flask Pieces cad din Boss-i!");
        }

        // Afișează buff potions și tier-urile lor
        System.out.println("\n🧪 BUFF POTIONS ACTUALE:");
        Map<BuffPotion.BuffType, Integer> buffPotions = erou.getAllBuffPotions();

        if (buffPotions.isEmpty()) {
            System.out.println("❌ Nu ai Buff Potions! Cumpără-le de la Shop!");
        } else {
            buffPotions.forEach((type, quantity) -> {
                BuffPotionTier tier = buffPotionTiers.getOrDefault(type, BuffPotionTier.BASIC);
                System.out.printf(" %s %s %s: %d bucăți (Durata: %d lupte, Bonus: x%.1f)\n",
                        tier.getIcon(), type.getIcon(), type.getDisplayName(),
                        quantity, tier.getDuration(), tier.getBonusMultiplier());
            });
        }
    }

    private void upgradeBuffPotion(Erou erou, BuffPotion.BuffType type, Scanner scanner) {
        System.out.println("\n" + "═".repeat(50));
        System.out.printf(" %s UPGRADE %s\n", type.getIcon(), type.getDisplayName());
        System.out.println("═".repeat(50));

        BuffPotionTier currentTier = buffPotionTiers.getOrDefault(type, BuffPotionTier.BASIC);
        BuffPotionTier nextTier = currentTier.getNext();

        int quantity = erou.getBuffPotionQuantity(type);
        System.out.printf("Poțiuni disponibile: %d\n", quantity);
        System.out.printf("Tier actual: %s %s\n", currentTier.getIcon(), currentTier.getDisplayName());
        System.out.printf("  • Durata: %d lupte\n", currentTier.getDuration());
        System.out.printf("  • Bonus multiplier: x%.1f\n", currentTier.getBonusMultiplier());

        if (currentTier == BuffPotionTier.MASTER) {
            System.out.println("\n🌟 Poțiunile tale sunt deja la nivelul maxim!");
            return;
        }

        // Calculează costurile
        int flaskCost = calculateFlaskCost(currentTier);
        int goldCost = calculateGoldCost(currentTier);

        System.out.println("\n⬆️ UPGRADE DISPONIBIL:");
        System.out.printf("Upgrade la: %s %s\n", nextTier.getIcon(), nextTier.getDisplayName());
        System.out.printf("  • Durata nouă: %d lupte (+%d)\n",
                nextTier.getDuration(), nextTier.getDuration() - currentTier.getDuration());
        System.out.printf("  • Bonus nou: x%.1f (+%.1f)\n",
                nextTier.getBonusMultiplier(),
                nextTier.getBonusMultiplier() - currentTier.getBonusMultiplier());

        System.out.printf("\n💰 COST UPGRADE:\n");
        System.out.printf("✨ %d Universal Flask Pieces\n", flaskCost);
        System.out.printf("💰 %d Gold\n", goldCost);

        // Verifică resursele
        int availableFlasks = erou.getFlaskPiecesQuantity(FlaskPiece.FlaskType.UNIVERSAL);

        if (availableFlasks < flaskCost || erou.getGold() < goldCost) {
            System.out.println("\n❌ RESURSE INSUFICIENTE:");
            if (availableFlasks < flaskCost) {
                System.out.printf("✨ Îți lipsesc %d Universal Flask Pieces\n",
                        flaskCost - availableFlasks);
            }
            if (erou.getGold() < goldCost) {
                System.out.printf("💰 Îți lipsesc %d Gold\n", goldCost - erou.getGold());
            }
            return;
        }

        // Preview efecte
        showUpgradePreview(type, nextTier);

        System.out.printf("\n❓ Confirmi upgrade pentru %d Universal Flask + %d Gold? (y/n): ",
                flaskCost, goldCost);

        if (Validator.readConfirmation(scanner, "")) {
            // Consumă resurse
            erou.consumeFlaskPieces(FlaskPiece.FlaskType.UNIVERSAL, flaskCost);
            erou.scadeGold(goldCost);

            // Aplică upgrade
            buffPotionTiers.put(type, nextTier);

            System.out.println("\n✅ UPGRADE REUȘIT!");
            System.out.printf("✨ %s upgraded la %s %s!\n",
                    type.getDisplayName(), nextTier.getIcon(), nextTier.getDisplayName());
            System.out.printf("⏰ Durata nouă: %d lupte!\n", nextTier.getDuration());
            System.out.printf("💪 Bonus nou: x%.1f!\n", nextTier.getBonusMultiplier());
        } else {
            System.out.println("\n❌ Upgrade anulat.");
        }
    }

    private void showUpgradePreview(BuffPotion.BuffType type, BuffPotionTier tier) {
        System.out.println("\n📊 PREVIEW EFECTE:");
        Map<String, Double> bonuses = type.getBonuses();

        bonuses.forEach((stat, baseBonus) -> {
            double upgradedBonus = baseBonus * tier.getBonusMultiplier();
            System.out.printf("  • %s: +%.1f (vs +%.1f base)\n",
                    stat, upgradedBonus, baseBonus);
        });
    }

    private int calculateFlaskCost(BuffPotionTier current) {
        return switch (current) {
            case BASIC -> 2;      // BASIC -> ENHANCED: 2 Universal Flasks
            case ENHANCED -> 4;   // ENHANCED -> SUPERIOR: 4 Universal Flasks
            case SUPERIOR -> 6;   // SUPERIOR -> MASTER: 6 Universal Flasks
            default -> 1;
        };
    }

    private int calculateGoldCost(BuffPotionTier current) {
        return switch (current) {
            case BASIC -> 150;     // BASIC -> ENHANCED: 150 gold
            case ENHANCED -> 350;  // ENHANCED -> SUPERIOR: 350 gold
            case SUPERIOR -> 700;  // SUPERIOR -> MASTER: 700 gold
            default -> 100;
        };
    }

    private void showUpgradeInfo() {
        System.out.println("\n" + "═".repeat(60));
        System.out.println(" ℹ️ INFORMAȚII UPGRADE BUFF POTIONS");
        System.out.println("═".repeat(60));

        System.out.println("\n✨ UNIVERSAL FLASK PIECES:");
        System.out.println("  • Cad din Boss-i cu șansă mare");
        System.out.println("  • Sunt folosite pentru upgrade ORICE tip de buff potion");
        System.out.println("  • Mai rare decât Health/Mana Flask Pieces");

        System.out.println("\n⬆️ TIER-URILE BUFF POTIONS:");
        for (BuffPotionTier tier : BuffPotionTier.values()) {
            System.out.printf("  %s %s: %d lupte durata, x%.1f bonus multiplier\n",
                    tier.getIcon(), tier.getDisplayName(),
                    tier.getDuration(), tier.getBonusMultiplier());
        }

        System.out.println("\n💰 COSTURI UPGRADE:");
        System.out.println("  Basic → Enhanced: 2 Universal Flask + 150 Gold");
        System.out.println("  Enhanced → Superior: 4 Universal Flask + 350 Gold");
        System.out.println("  Superior → Master: 6 Universal Flask + 700 Gold");

        System.out.println("\n💡 EXEMPLU:");
        System.out.println("  Strength Potion (Basic): +5 STR pentru 3 lupte");
        System.out.println("  Strength Potion (Master): +10 STR pentru 6 lupte!");
    }

    private void waitForEnter() {
        System.out.println("\n📍 Apasă Enter pentru a continua...");
        try {
            System.in.read();
        } catch (Exception e) {
            // Ignore
        }
    }

    // Metode pentru salvare/load tier-uri
    public BuffPotionTier getTier(BuffPotion.BuffType type) {
        return buffPotionTiers.getOrDefault(type, BuffPotionTier.BASIC);
    }

    public void setTier(BuffPotion.BuffType type, BuffPotionTier tier) {
        buffPotionTiers.put(type, tier);
    }

    public Map<BuffPotion.BuffType, BuffPotionTier> getAllTiers() {
        return new HashMap<>(buffPotionTiers);
    }
}