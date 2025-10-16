package com.rpg.service;

import com.rpg.model.characters.Erou;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.utils.Validator;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Service pentru disenchanting items în shards.
 * Permite descompunerea echipamentului pentru a obține shards.
 */
public class DisenchantService {

    public void openDisenchantService(Erou erou, Scanner scanner) {
        boolean inService = true;

        while (inService) {
            displayDisenchantMenu(erou);

            List<ObiectEchipament> disenchantableItems = getDisenchantableItems(erou);

            if (disenchantableItems.isEmpty()) {
                System.out.println("\n📦 Nu ai obiecte care pot fi disenchanted!");
                System.out.println("💡 Doar obiectele neechipate pot fi disenchanted.");
                System.out.println("\n1. 🔙 Înapoi");
                Validator.readValidChoice(scanner, 1, 1);
                return;
            }

            System.out.println("\n📋 OBIECTE DISPONIBILE PENTRU DISENCHANT:");
            System.out.println();

            int totalShardsValue = 0;
            for (int i = 0; i < disenchantableItems.size(); i++) {
                ObiectEchipament item = disenchantableItems.get(i);
                int shardsValue = calculateDisenchantValue(item);
                totalShardsValue += shardsValue;

                System.out.printf("%d. %s %s\n", i + 1, getRarityIcon(item.getRaritate()),
                        item.getNume());
                System.out.printf("   📊 %s | Nivel %d | 🔮 %d shards\n",
                        item.getRaritate().getDisplayName(), item.getNivelNecesar(), shardsValue);
                System.out.println();
            }

            System.out.println("═".repeat(50));
            System.out.printf("💎 TOTAL SHARDS POSIBILE: %d\n", totalShardsValue);
            System.out.printf("🔮 Shards actuale: %d\n", erou.getShards());
            System.out.printf("🔮 Total după disenchant: %d\n", erou.getShards() + totalShardsValue);

            System.out.println("\n🎯 Opțiuni disponibile:");
            System.out.println((disenchantableItems.size() + 1) + ". 💎 Disenchant toate obiectele");
            System.out.println((disenchantableItems.size() + 2) + ". ℹ️ Informații despre disenchanting");
            System.out.println((disenchantableItems.size() + 3) + ". 🔙 Înapoi");

            System.out.print("\n➤ Alege opțiunea (1-" + (disenchantableItems.size() + 3) + "): ");
            int choice = Validator.readValidChoice(scanner, 1, disenchantableItems.size() + 3);

            if (choice <= disenchantableItems.size()) {
                // Disenchant item individual
                ObiectEchipament selectedItem = disenchantableItems.get(choice - 1);
                disenchantSingleItem(erou, selectedItem, scanner);
            } else if (choice == disenchantableItems.size() + 1) {
                // Disenchant toate
                disenchantAllItems(erou, disenchantableItems, scanner);
            } else if (choice == disenchantableItems.size() + 2) {
                // Informații
                showDisenchantInfo();
                waitForEnter();
            } else {
                // Înapoi
                inService = false;
            }
        }
    }

    private void displayDisenchantMenu(Erou erou) {
        System.out.println("\n" + "═".repeat(60));
        System.out.println(" 🔮 DISENCHANT WORKSHOP");
        System.out.println("═".repeat(60));
        System.out.println("✨ Descompune echipamentul în shards magice!");
        System.out.println();
        System.out.printf("👤 %s | 🔮 Shards: %d | 📦 Obiecte: %d\n",
                erou.getNume(), erou.getShards(), erou.getInventar().size());
    }

    private void disenchantSingleItem(Erou erou, ObiectEchipament item, Scanner scanner) {
        int shardsValue = calculateDisenchantValue(item);

        System.out.println("\n" + "═".repeat(50));
        System.out.println(" 🔮 DISENCHANT INDIVIDUAL");
        System.out.println("═".repeat(50));

        System.out.printf("📦 Obiect: %s %s\n", getRarityIcon(item.getRaritate()), item.getNume());
        System.out.printf("✨ Raritate: %s | 📊 Nivel: %d\n",
                item.getRaritate().getDisplayName(), item.getNivelNecesar());

        if (!item.getBonuses().isEmpty()) {
            System.out.println("💪 Bonusuri care se vor pierde:");
            item.getBonuses().forEach((stat, bonus) ->
                    System.out.printf("   • +%d %s\n", bonus, stat));
        }

        System.out.printf("\n🔮 Vei primi: %d shards\n", shardsValue);
        System.out.printf("🔮 Total shards după: %d\n", erou.getShards() + shardsValue);

        System.out.println("\n⚠️ ATENȚIE: Această acțiune este IREVERSIBILĂ!");
        System.out.printf("❓ Confirmi disenchanting pentru %s? (y/n): ", item.getNume());

        if (Validator.readConfirmation(scanner, "")) {
            // Efectuează disenchant
            erou.removeFromInventar(item);
            erou.adaugaShards(shardsValue);

            System.out.println("\n✅ DISENCHANT REUȘIT!");
            System.out.printf("🔮 Ai primit %d shards din %s!\n", shardsValue, item.getNume());
            System.out.printf("💎 Total shards: %d\n", erou.getShards());
            System.out.println("✨ Energia magică a obiectului a fost eliberată!");

            waitForEnter();
        } else {
            System.out.println("\n❌ Disenchant anulat.");
        }
    }

    private void disenchantAllItems(Erou erou, List<ObiectEchipament> items, Scanner scanner) {
        int totalShards = items.stream().mapToInt(this::calculateDisenchantValue).sum();

        System.out.println("\n" + "═".repeat(50));
        System.out.println(" 💎 DISENCHANT MASIV");
        System.out.println("═".repeat(50));

        System.out.printf("📦 Vei disenchanta %d obiecte\n", items.size());
        System.out.printf("🔮 Total shards: %d\n", totalShards);
        System.out.printf("🔮 Shards după disenchant: %d\n", erou.getShards() + totalShards);

        System.out.println("\n📋 Lista obiectelor:");
        for (ObiectEchipament item : items) {
            System.out.printf("  • %s %s (+%d shards)\n",
                    getRarityIcon(item.getRaritate()), item.getNume(),
                    calculateDisenchantValue(item));
        }

        System.out.println("\n⚠️ ATENȚIE: Această acțiune este IREVERSIBILĂ!");
        System.out.println("⚠️ TOATE obiectele vor fi distruse permanent!");

        System.out.printf("\n❓ Confirmi disenchanting pentru TOATE obiectele? (y/n): ");

        if (Validator.readConfirmation(scanner, "")) {
            // Efectuează disenchant masiv
            for (ObiectEchipament item : items) {
                erou.removeFromInventar(item);
            }
            erou.adaugaShards(totalShards);

            System.out.println("\n✅ DISENCHANT MASIV REUȘIT!");
            System.out.printf("🔮 Ai primit %d shards din %d obiecte!\n", totalShards, items.size());
            System.out.printf("💎 Total shards: %d\n", erou.getShards());
            System.out.println("✨ O explozie de energie magică umple camera!");
            System.out.println("🌟 Acum poți folosi shards-urile pentru upgrade echipament!");

            waitForEnter();
        } else {
            System.out.println("\n❌ Disenchant anulat.");
        }
    }

    private List<ObiectEchipament> getDisenchantableItems(Erou erou) {
        // Doar obiectele neechipate pot fi disenchanted
        return erou.getInventar().stream()
                .filter(item -> !item.isEquipped())
                .collect(Collectors.toList());
    }

    private int calculateDisenchantValue(ObiectEchipament item) {
        // Formula pentru calcularea valorii în shards
        int baseShards = switch (item.getRaritate()) {
            case COMMON -> 2;
            case UNCOMMON -> 5;
            case RARE -> 12;
            case EPIC -> 25;
            case LEGENDARY -> 50;
        };

        // Bonus bazat pe nivel
        int levelBonus = item.getNivelNecesar() / 5; // +1 shard per 5 nivele

        // Bonus bazat pe numărul de bonusuri
        int bonusCount = item.getBonuses().size();
        int bonusMultiplier = Math.max(1, bonusCount);

        // Calculul final
        int totalShards = (baseShards + levelBonus) * bonusMultiplier;

        return Math.max(1, totalShards); // Minim 1 shard
    }

    private void showDisenchantInfo() {
        System.out.println("\n" + "═".repeat(60));
        System.out.println(" ℹ️ INFORMAȚII DISENCHANTING");
        System.out.println("═".repeat(60));

        System.out.println("🔮 CE ESTE DISENCHANTING-UL?");
        System.out.println("  • Descompune echipamentul în shards magice");
        System.out.println("  • Shards-urile pot fi folosite pentru upgrade echipament");
        System.out.println("  • Procesul este IREVERSIBIL - obiectul este distrus!");

        System.out.println("\n💎 VALORI DISENCHANT BAZATE PE RARITATE:");
        System.out.println("  ⚪ Common: 2 shards de bază");
        System.out.println("  💚 Uncommon: 5 shards de bază");
        System.out.println("  💙 Rare: 12 shards de bază");
        System.out.println("  💜 Epic: 25 shards de bază");
        System.out.println("  🌟 Legendary: 50 shards de bază");

        System.out.println("\n📊 BONUSURI SUPLIMENTARE:");
        System.out.println("  • +1 shard per 5 nivele ale obiectului");
        System.out.println("  • Multiplicator bazat pe numărul de bonusuri");
        System.out.println("  • Obiectele cu mai multe stats dau mai mulți shards");

        System.out.println("\n🚫 RESTRICȚII:");
        System.out.println("  • Doar obiectele NEECHIPATE pot fi disenchanted");
        System.out.println("  • Procesul este ireversibil");
        System.out.println("  • Nu poți recupera obiectul după disenchant");

        System.out.println("\n💡 SFATURI:");
        System.out.println("  • Disenchant obiectele vechi când găsești upgrade-uri");
        System.out.println("  • Shards-urile sunt mai valoroase decât gold-ul pentru upgrade");
        System.out.println("  • Obiectele rare dau mulți shards - valorifică-le înțelept!");
    }

    private String getRarityIcon(ObiectEchipament.Raritate raritate) {
        return switch (raritate) {
            case COMMON -> "⚪";
            case UNCOMMON -> "💚";
            case RARE -> "💙";
            case EPIC -> "💜";
            case LEGENDARY -> "🌟";
        };
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