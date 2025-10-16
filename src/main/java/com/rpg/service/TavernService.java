package com.rpg.service;

import com.rpg.model.characters.Erou;
import com.rpg.utils.Validator;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

//Clasa pt taverna unde eroul isi da heal si buffuri
public class TavernService {

    // Buff-uri temporare pentru taverna
    private Map<String, Integer> tavernBuffs;
    private int buffRemainingBattles;

    public TavernService() {
        this.tavernBuffs = new HashMap<>();
        this.buffRemainingBattles = 0;
    }

//functia principala pentru display si alegere a optiunilor
    public void openTavern(Erou erou, Scanner scanner) {
        boolean inTavern = true;

        while (inTavern) {
            SaveLoadService.clearScreen();

            displayTavernWelcome(erou);

            System.out.println("\n🎯 Servicii disponibile:");
            System.out.println();
            System.out.println("1. 🛏️  Odihnă completă (50 gold)");
            System.out.println("   • Restaurează toată viața și resursele");
            System.out.println("   • Buff temporar pentru 3 lupte următoare");
            System.out.println("   • +10 la toate statisticile în luptă");
            System.out.println("   • +15% șansă critică și dodge");
            System.out.println();

            System.out.println("2. 🍺 Băutură revigorantă (Gratis)");
            System.out.println("   • Restaurează jumătate din viață și resurse");
            System.out.println("   • Fără buff-uri suplimentare");
            System.out.println();

            System.out.println("3. 🌯 Comandă Șaorma Specială (150 gold)");
            System.out.println("   • Regenerare completă + buff puternic");
            System.out.println("   • Șansă 10% pentru Șaorma de Revival BONUS!");
            System.out.println("   • Disponibil doar dacă ai nivel 10+");
            System.out.println();

            System.out.println("4. 📊 Verifică buff-urile active");
            System.out.println("0. 🚪 Ieși din tavernă");

            // Afișează buff-urile active dacă există
            if (hasActiveTavernBuffs()) {
                System.out.printf("\n✨ Ai buff-uri active pentru încă %d lupte!\n",
                        buffRemainingBattles);
            }

            //switch case pentru a alege optiuni
            System.out.print("\n➤ Alege opțiunea (0-4): ");
            int choice = Validator.readValidChoice(scanner, 0, 4);

            //fiecare cheama metoda respectiva si aplica buffuri daca e cazul
            switch (choice) {
                case 1 -> fullRest(erou, scanner);
                case 2 -> quickRest(erou, scanner);
                case 3 -> specialShaormaRest(erou, scanner);
                case 4 -> displayActiveBuffs(erou);
                case 0 -> {
                    System.out.println("\n🍺 Barmanița: 'La revedere, " + erou.getNume() +
                            "! Revino oricând pentru odihnă!'");
                    System.out.println("🏠 Taverna rămâne deschisă 24/7 pentru aventurieri!");
                    inTavern = false;
                }
            }

            if (inTavern && choice != 0) {
                waitForEnter();
            }
        }
    }

 //afiseaza dwelcome message
    private void displayTavernWelcome(Erou erou) {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("      🍺 TAVERNA LA NEA' GICU 🍺");
        System.out.println("═".repeat(60));
        System.out.println("🏠 O atmosferă caldă și primitoare te întâmpină...");
        System.out.println("🔥 Focul din șemineu pârâie liniștitor.");
        System.out.println();
        System.out.printf("🍺 Barmanița: 'Bine ai venit, %s!'\n", erou.getNume());
        System.out.printf("👤 'Văd că ești nivel %d. Impresionant!'\n", erou.getNivel());
        System.out.println();

        // Afișează starea eroului
        double healthPercent = ((double) erou.getViata() / erou.getViataMaxima()) * 100;
        double resourcePercent = ((double) erou.getResursaCurenta() / erou.getResursaMaxima()) * 100;

        System.out.println("📊 Starea ta actuală:");
        System.out.printf("❤️  Viață: %d/%d (%.0f%%)\n",
                erou.getViata(), erou.getViataMaxima(), healthPercent);
        System.out.printf("🔋 %s: %d/%d (%.0f%%)\n",
                erou.getTipResursa(), erou.getResursaCurenta(),
                erou.getResursaMaxima(), resourcePercent);
        System.out.printf("💰 Gold disponibil: %d | 🌯 Șaorme Revival: %d\n",
                erou.getGold(), erou.getShaormaRevival());

        // Recomandări bazate pe stare
        if (healthPercent < 50 || resourcePercent < 50) {
            System.out.println("\n💡 Barmanița: 'Arăți obosit! O odihnă îți va face bine.'");
        } else if (healthPercent == 100 && resourcePercent == 100) {
            System.out.println("\n😊 Barmanița: 'Arăți în formă! Poate vrei un buff pentru următoarele lupte?'");
        }
    }

//odihna completa daca ai 50 gold
    private void fullRest(Erou erou, Scanner scanner) {
        if (erou.getGold() < 50) {
            System.out.println("\n💰 Nu ai suficient gold pentru odihnă completă!");
            System.out.println("💡 Încercă băutura gratuită sau explorează dungeon-urile pentru gold.");
            return;
        }

        SaveLoadService.clearScreen();

        System.out.println("\n" + "═".repeat(50));
        System.out.println("        🛏️ ODIHNĂ COMPLETĂ");
        System.out.println("═".repeat(50));
        System.out.println("🍺 Barmanița: 'O cameră caldă și o masă copioasă te așteaptă!'");
        System.out.println("✨ Vei primi buff-uri puternice pentru următoarele 3 lupte!");
        System.out.println();
        System.out.println("📋 Beneficii complete:");
        System.out.println("   ❤️ Viață restaurată: 100%");
        System.out.printf("   🔋 %s restaurate: 100%%\n", erou.getTipResursa());
        System.out.println("   💪 +10 la toate statisticile în luptă");
        System.out.println("   ⚡ +15% șansă critică");
        System.out.println("   💨 +15% șansă de dodge");
        System.out.println("   🛡️ +5 defense în plus");
        System.out.println("   ⚔️ Durează 3 lupte");
        System.out.println();
        System.out.printf("💰 Cost: 50 gold (ai %d)\n", erou.getGold());

        if (!Validator.readConfirmation(scanner, "\nVrei să plătești 50 gold pentru odihnă completă?")) {
            System.out.println("🍺 Barmanița: 'Înțeleg. Poate altă dată!'");
            return;
        }

        // Plătește și restaurează
        erou.scadeGold(50);
        erou.setViataCurenta(erou.getViataMaxima());
        erou.setResursaCurenta(erou.getResursaMaxima());

        // Aplică buff-urile temporare
        applyTavernBuffs(erou, 3);

        System.out.println("\n" + "═".repeat(40));
        System.out.println("        ✅ ODIHNĂ COMPLETATĂ!");
        System.out.println("═".repeat(40));
        System.out.println("🛏️ Te-ai odihnit excelent în camera confortabilă!");
        System.out.println("🍖 Masa delicioasă ți-a restaurat puterea!");
        System.out.println("❤️ Viață restaurată: " + erou.getViata() + "/" + erou.getViataMaxima());
        System.out.printf("🔋 %s restaurate: %d/%d\n",
                erou.getTipResursa(), erou.getResursaCurenta(), erou.getResursaMaxima());
        System.out.println("✨ Buff-uri active pentru următoarele 3 lupte!");
        System.out.printf("💰 Gold rămas: %d\n", erou.getGold());

        System.out.println("\n🍺 Barmanița: 'Dormi bine și să ai lupte câștigătoare!'");
    }

   //odihna partiala fara buffuri
    private void quickRest(Erou erou, Scanner scanner) {
        SaveLoadService.clearScreen();

        System.out.println("\n" + "═".repeat(50));
        System.out.println("        🍺 BĂUTURĂ REVIGORANTĂ");
        System.out.println("═".repeat(50));
        System.out.println("🍺 Barmanița: 'Ia o băutură pe casa casei!'");
        System.out.println("✨ 'Este făcută din ierburi locale revigorante!'");

        // Calculează restaurarea
        int healthRestore = erou.getViataMaxima() / 2;
        int resourceRestore = erou.getResursaMaxima() / 2;

        int oldHealth = erou.getViata();
        int oldResource = erou.getResursaCurenta();

        erou.setViataCurenta(Math.min(erou.getViata() + healthRestore, erou.getViataMaxima()));
        erou.setResursaCurenta(Math.min(erou.getResursaCurenta() + resourceRestore, erou.getResursaMaxima()));

        int actualHealthRestored = erou.getViata() - oldHealth;
        int actualResourceRestored = erou.getResursaCurenta() - oldResource;

        System.out.println("\n🍺 *Savurezi băutura caldă și aromată*");
        System.out.println("✨ Te simți revigorat și mai energic!");
        System.out.println();
        System.out.printf("❤️ Viață restaurată: +%d (acum: %d/%d)\n",
                actualHealthRestored, erou.getViata(), erou.getViataMaxima());
        System.out.printf("🔋 %s restaurate: +%d (acum: %d/%d)\n",
                erou.getTipResursa(), actualResourceRestored,
                erou.getResursaCurenta(), erou.getResursaMaxima());

        if (actualHealthRestored == 0 && actualResourceRestored == 0) {
            System.out.println("😊 Barmanița: 'Văd că ești deja în formă excelentă!'");
            System.out.println("💡 'Poate vrei buff-urile de la odihnă completă?'");
        } else {
            System.out.println("🍺 Barmanița: 'Sper că te simți mai bine!'");
            System.out.println("💡 'Pentru buff-uri speciale, încearcă odihnă completă!'");
        }
    }


     //Șaorma specială cu bonus și șansă pentru revival șaorma.

    private void specialShaormaRest(Erou erou, Scanner scanner) {
        if (erou.getNivel() < 10) {
            System.out.println("\n⚠️ Șaorma Specială este disponibilă doar pentru aventurieri nivel 10+!");
            System.out.printf("💡 Tu ești nivel %d. Mai ai %d niveluri până să o poți comanda.\n",
                    erou.getNivel(), 10 - erou.getNivel());
            return;
        }

        if (erou.getGold() < 150) {
            System.out.println("\n💰 Nu ai suficient gold pentru Șaorma Specială!");
            System.out.printf("💡 Îți trebuie 150 gold, dar ai doar %d.\n", erou.getGold());
            return;
        }

        SaveLoadService.clearScreen();

        System.out.println("\n" + "═".repeat(50));
        System.out.println("        🌯 ȘAORMA SPECIALĂ A TAVERNEI");
        System.out.println("═".repeat(50));
        System.out.println("🍺 Barmanița: 'Ah, Șaorma noastră legendară!'");
        System.out.println("✨ 'Este pregătită cu ingrediente magice rare!'");
        System.out.println("🎲 'Și cine știe... poate primești și o surpriză!'");
        System.out.println();
        System.out.println("📋 Beneficii speciale:");
        System.out.println("   ❤️ Viață restaurată: 100%");
        System.out.printf("   🔋 %s restaurate: 100%%\n", erou.getTipResursa());
        System.out.println("   💪 +15 la toate statisticile în luptă");
        System.out.println("   ⚡ +25% șansă critică");
        System.out.println("   💨 +20% șansă de dodge");
        System.out.println("   🛡️ +10 defense în plus");
        System.out.println("   ⚔️ Durează 5 lupte (în loc de 3!)");
        System.out.println("   🎲 10% șansă pentru Șaorma de Revival BONUS!");
        System.out.println();
        System.out.printf("💰 Cost: 150 gold (ai %d)\n", erou.getGold());

        if (!Validator.readConfirmation(scanner, "\nVrei să plătești 150 gold pentru Șaorma Specială?")) {
            System.out.println("🍺 Barmanița: 'Poate altă dată când vei fi mai hotărât!'");
            return;
        }

        // Plătește și restaurează
        erou.scadeGold(150);
        erou.setViataCurenta(erou.getViataMaxima());
        erou.setResursaCurenta(erou.getResursaMaxima());

        // Aplică buff-urile speciale (mai puternice și mai lungi)
        applySpecialTavernBuffs(erou, 5);

        System.out.println("\n" + "═".repeat(40));
        System.out.println("        🌯 ȘAORMA SPECIALĂ SERVITĂ!");
        System.out.println("═".repeat(40));
        System.out.println("🍺 *Barmanița îți aduce o șaormă magnifică*");
        System.out.println("✨ *Aromele magice îți umplu simțurile*");
        System.out.println("🌯 *Gustul este absolut divin!*");
        System.out.println();
        System.out.println("❤️ Viață restaurată complet!");
        System.out.printf("🔋 %s restaurate complet!\n", erou.getTipResursa());
        System.out.println("💪 Buff-uri SPECIALE active pentru următoarele 5 lupte!");
        System.out.printf("💰 Gold rămas: %d\n", erou.getGold());

        // Șansă pentru Șaorma de Revival bonus!
        if (Math.random() < 0.10) { // 10% șansă
            erou.adaugaShaormaRevival(1);
            System.out.println("\n🎉 " + "═".repeat(40));
            System.out.println("    🌯 ✨ BONUS INCREDIBIL! ✨");
            System.out.println("═".repeat(40));
            System.out.println("🍺 Barmanița: 'WOW! Șaorma a fost atât de bună");
            System.out.println("   că bucătarul ți-a pregătit una specială");
            System.out.println("   pentru călătorie!'");
            System.out.println("🎁 Ai primit o Șaorma de Revival BONUS!");
            System.out.printf("🌯 Total șaorme: %d\n", erou.getShaormaRevival());
        } else {
            System.out.println("\n🍺 Barmanița: 'Sper că ți-a plăcut! Este rețeta noastră secretă!'");
        }
    }

    /**
     * Aplică buff-urile normale de la tavernă.
     */
    private void applyTavernBuffs(Erou erou, int battles) {
        tavernBuffs.clear();
        tavernBuffs.put("strength", 10);
        tavernBuffs.put("dexterity", 10);
        tavernBuffs.put("intelligence", 10);
        tavernBuffs.put("defense", 5);
        tavernBuffs.put("crit_chance", 15);
        tavernBuffs.put("dodge_chance", 15);

        buffRemainingBattles = battles;

        // Aplică buff-urile în sistemul de buff-uri al eroului
        Map<String, Double> buffModifiers = new HashMap<>();
        tavernBuffs.forEach((key, value) -> buffModifiers.put(key, value.doubleValue()));

        erou.aplicaBuff("TavernRest", buffModifiers, battles);
    }


    //Aplică buff-urile speciale de la Șaorma Specială.

    private void applySpecialTavernBuffs(Erou erou, int battles) {
        tavernBuffs.clear();
        tavernBuffs.put("strength", 15);
        tavernBuffs.put("dexterity", 15);
        tavernBuffs.put("intelligence", 15);
        tavernBuffs.put("defense", 10);
        tavernBuffs.put("crit_chance", 25);
        tavernBuffs.put("dodge_chance", 20);

        buffRemainingBattles = battles;

        // Aplică buff-urile speciale în sistemul de buff-uri al eroului
        Map<String, Double> buffModifiers = new HashMap<>();
        tavernBuffs.forEach((key, value) -> buffModifiers.put(key, value.doubleValue()));

        erou.aplicaBuff("SpecialShaorma", buffModifiers, battles);
    }

  //display pentru buffuri
    private void displayActiveBuffs(Erou erou) {
        SaveLoadService.clearScreen();

        System.out.println("\n" + "═".repeat(50));
        System.out.println("        ✨ BUFF-URI ACTIVE");
        System.out.println("═".repeat(50));

        if (!hasActiveTavernBuffs()) {
            System.out.println("❌ Nu ai buff-uri active de la tavernă.");
            System.out.println("💡 Comandă o odihnă completă sau Șaorma Specială pentru buff-uri!");
        } else {
            System.out.printf("✅ Ai buff-uri active pentru încă %d lupte!\n\n", buffRemainingBattles);

            System.out.println("📊 Buff-uri actuale:");
            tavernBuffs.forEach((stat, bonus) -> {
                String statName = switch (stat) {
                    case "strength" -> "💪 Strength";
                    case "dexterity" -> "🏃 Dexterity";
                    case "intelligence" -> "🧠 Intelligence";
                    case "defense" -> "🛡️ Defense";
                    case "crit_chance" -> "⚡ Crit Chance";
                    case "dodge_chance" -> "💨 Dodge Chance";
                    default -> stat;
                };

                String unit = stat.contains("chance") ? "%" : "";
                System.out.printf("   %s: +%d%s\n", statName, bonus, unit);
            });

            System.out.println("\n💡 Buff-urile se vor consuma după fiecare luptă.");
        }

        // Afișează și buff-urile din sistemul eroului
        if (!erou.getBuffuriActive().isEmpty()) {
            System.out.println("\n🌟 Alte buff-uri active:");
            erou.getBuffuriActive().forEach((name, buff) -> {
                if (buff.isActive()) {
                    System.out.printf("   • %s (%d ture rămase)\n", name, buff.getDurata());
                }
            });
        }
    }


    //Verifică dacă eroul are buff-uri active de la tavernă.

    public boolean hasActiveTavernBuffs() {
        return buffRemainingBattles > 0 && !tavernBuffs.isEmpty();
    }

//    /**
//     * Consumă un buff de la tavernă după luptă.
//     */
//    public void consumeTavernBuff() {
//        if (hasActiveTavernBuffs()) {
//            buffRemainingBattles--;
//            if (buffRemainingBattles <= 0) {
//                tavernBuffs.clear();
//                System.out.println("✨ Buff-urile de la tavernă s-au consumat.");
//            }
//        }
//    }

//    /**
//     * Returnează buff-urile active pentru calculele de luptă.
//     */
//    public Map<String, Integer> getActiveTavernBuffs() {
//        return hasActiveTavernBuffs() ? new HashMap<>(tavernBuffs) : new HashMap<>();
//    }

//doar metoda de a astepta pt enter
    private void waitForEnter() {
        System.out.println("\n📝 Apasă Enter pentru a continua...");
        try {
            System.in.read();
        } catch (Exception e) {
            // Ignore
        }
    }
}