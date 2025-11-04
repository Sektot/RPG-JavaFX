package com.rpg.service;

import com.rpg.model.characters.Inamic;
import com.rpg.utils.GameConstants;
import com.rpg.utils.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.lang.reflect.Field;

/**
 * Generator de inamici romanizați, organizați pe nivele de dungeons.
 * Fiecare gamă de nivele (1-10, 11-20, etc.) are inamici specifici culturii românești.
 */
public class EnemyGeneratorRomanesc {

    // NIVELE 1-10: INAMICI SLABI
    private static final String[][] INAMICI_SLABI = {
            {"Cerșetor de la Metrou", "🚇", "fire", "poison"},
            {"Manelist Cu Bluetooth", "🎵", "lightning", "magical"},
            {"Boschetar", "🧟", "holy", "darkness"},
            {"Maidanez Turbat", "🐕", "poison", "physical"},
            {"Interlop De Cartier", "😎", "physical", "darkness"},
            {"Lăutar Beat", "🎻", "ice", "fire"},
            {"Karen Românească", "👵", "physical", "holy"},
            {"Vecinu cu Bormașina", "🔊", "lightning", "physical"}
    };

    private static final String[] BOSS_SLABI = {
            "Șeful Cerșetorilor Sindicalizați", "Regele Șobolanilor", "Câinele Alpha", "Lăutarul Șef"
    };

    // NIVELE 11-20: INAMICI MEDII
    private static final String[][] INAMICI_MEDII = {
            {"Inspector ANAF", "💼", "fire", "holy"},
            {"Profesor Care Te Pică", "📚", "physical", "magical"},
            {"Funcționar Public", "📋", "fire", "ice"},
            {"Controlor STB", "🎫", "physical", "darkness"},
            {"Jandarm Cu Bastonu'", "👮", "fire", "physical"},
            {"Paznic De La Kaufland", "🛒", "lightning", "physical"},
            {"Polițist Șpăgar", "🚓", "holy", "darkness"},
            {"Primarul Corupt", "🏛️", "holy", "magical"}
    };

    private static final String[] BOSS_MEDII = {
            "Directorul ANAF cu Auditul Final", "omandantul Jandarmilor cu Gazul Lacrimogen", "Primarul General cu Șpaga Multiplă"
    };

    // NIVELE 21-30: INAMICI PUTERNICI
    private static final String[][] INAMICI_PUTERNICI = {
            {"Fantoma lui Ceaușescu", "👻", "holy", "physical"},
            {"Spiritul lui Vadim", "🗣️", "holy", "darkness"},
            {"Videanu Cu Lopata", "🔨", "fire", "physical"},
            {"Basescu Beat", "🍺", "fire", "poison"},
            {"Dragnea Cu Dosare", "📁", "fire", "darkness"},
            {"Iliescu Nemuritor", "🧛", "holy", "shadow"},
            {"Ponta Cu Plagiat", "📄", "fire", "magical"},
            {"Viorica Dăncilă Lost", "🤷", "lightning", "darkness"}
    };
    private static final String[] BOSS_PUTERNICI = {
            "Ceaușescu Revenit Cu Armata", "Iliescu The Eternal", "Becali Boss Final Cu Ingerașii"
    };

    // NIVELE 31-40: INAMICI LEGENDARI
    private static final String[][] INAMICI_LEGENDARI = {
            {"PSD-ist Cu Pensie Specială", "💰", "holy", "darkness"},
            {"Groapă Din Șosea", "🕳️", "ice", "physical"},
            {"TIR Neasigurat", "🚚", "fire", "physical"},
            {"Scanteie De La Colectiv", "🔥", "ice", "fire"},
            {"Cutremur Neprognozat", "🌍", "lightning", "physical"},
            {"Latrina 3 Presenter", "📺", "holy", "darkness"},
            {"Inundație din Cauza Primarului", "🌊", "lightning", "ice"},
            {"Poluare București", "🏭", "holy", "poison"}
    };

    private static final String[] BOSS_LEGENDARI = {
            "Marele Cutremur 7.5", "Mega-Groapă Din Unirii", "Traficul De Vinerea Seara"
    };

    // NIVELE 41-50: INAMICI EPICI
    private static final String[][] INAMICI_EPICI = {
            {"ANAF Cu AI", "🤖", "lightning", "fire"},
            {"Antena 3 Final Form", "📡", "holy", "darkness"},
            {"PSD Cu Majoritate Absolută", "🏛️", "holy", "shadow"},
            {"Salariu Minim De Bază", "💵", "fire", "ice"},
            {"Sistem Sanitar Colapsed", "🏥", "fire", "poison"},
            {"Infrastructura În Ruine", "🏗️", "lightning", "physical"},
            {"Emigrarea Totală", "✈️", "holy", "darkness"},
            {"România Fără Internet", "📵", "lightning", "magical"}
    };

    private static final String[] BOSS_EPICI = {
            "MEGA-ANAF: The Final Audit",
            "Ceaușescu With Soviet Backup",
            "Iliescu Level 999: Simply Refuses to Die",
            "PSD-PNL Fusion: Eternal Corruption",
            "Antena 3 + Latrina 3 Combo: Truth Is Dead"
    };

    /**
     * Generează inamici pentru un nivel dat, bazat pe tier-ul dungeonului.
     */
    public List<Inamic> genereazaInamici(int nivel) {
        List<Inamic> inamici = new ArrayList<>();

        // La multiplu de 5, DOAR boss
        if (nivel % 5 == 0) {
            Inamic boss = genereazaBoss(nivel);
            inamici.add(boss);
        } else {
            // 3-5 inamici normali
            for (int i = 0; i < RandomUtils.randomInt(3, 5); i++) {
                inamici.add(genereazaInamicNormal(nivel));
            }
        }

        return inamici;
    }

//    /**
//     * Generează un inamic normal bazat pe nivelul dungeonului.
//     */
//    public Inamic genereazaInamicNormal(int nivel) {
//        String[][] tabelInamici;
//        String prefix;
//
//        // Determină tier-ul și selectează inamicii corespunzători
//        if (nivel <= 10) {
//            tabelInamici = INAMICI_SLABI;
//            prefix = "";
//        } else if (nivel <= 20) {
//            tabelInamici = INAMICI_MEDII;
//            prefix = "";
//        } else if (nivel <= 30) {
//            tabelInamici = INAMICI_PUTERNICI;
//            prefix = "Puternic ";
//        } else if (nivel <= 40) {
//            tabelInamici = INAMICI_LEGENDARI;
//            prefix = "Legendar ";
//        } else {
//            tabelInamici = INAMICI_EPICI;
//            prefix = "Epic ";
//        }
//
//        // Selectează un inamic aleatoriu din tier
//        String[] inamicData = RandomUtils.randomElement(tabelInamici);
//        String nume = prefix + inamicData[0] + " " + inamicData[1] + " Lv" + nivel;
//
//        // Calculează statistici
//        int viataMaxima = GameConstants.ENEMY_BASE_HEALTH + (nivel * GameConstants.ENEMY_HEALTH_PER_LEVEL);
//        int defense = GameConstants.ENEMY_BASE_DEFENSE + (nivel * GameConstants.ENEMY_DEFENSE_PER_LEVEL);
//        int gold = GameConstants.ENEMY_BASE_GOLD + (nivel * GameConstants.ENEMY_GOLD_PER_LEVEL);
//        int xpOferit = GameConstants.ENEMY_BASE_XP + (nivel * GameConstants.ENEMY_XP_PER_LEVEL);
//
//        // 🛡️ SAFETY CHECK - Dacă constantele lipsesc, folosește fallback
//        if (viataMaxima <= 0) {
//            viataMaxima = 50 + (nivel * 20);  // Fallback: 70 HP la nivel 1
//            System.out.println("⚠️ WARNING: ENEMY_BASE_HEALTH e 0 - folosesc fallback!");
//        }
//        if (gold <= 0) {
//            gold = 10 + (nivel * 5);  // Fallback: 15 gold la nivel 1
//            System.out.println("⚠️ WARNING: ENEMY_BASE_GOLD e 0 - folosesc fallback!");
//        }
//        if (xpOferit <= 0) {
//            xpOferit = 20 + (nivel * 8);  // Fallback: 28 XP la nivel 1
//            System.out.println("⚠️ WARNING: ENEMY_BASE_XP e 0 - folosesc fallback!");
//        }
//
//        // Variație aleatorie
//        viataMaxima = RandomUtils.applyRandomVariation(viataMaxima, 20);
//        defense = RandomUtils.applyRandomVariation(defense, 15);
//        gold = RandomUtils.applyRandomVariation(gold, 30);
//        xpOferit = RandomUtils.applyRandomVariation(xpOferit, 25);
//
//        // 🔍 DEBUG pentru a vedea valorile calculate
//        System.out.printf("🔍 ENEMY STATS CALC: HP=%d, Defense=%d, Gold=%d, XP=%d%n",
//                viataMaxima, defense, gold, xpOferit);
//
//        // Creează inamicul
//        Inamic inamic = new Inamic(nume, nivel, viataMaxima, defense, gold, xpOferit, false);
//
//        // Setează vulnerabilități specifice
//        inamic.setTipDamageVulnerabil(inamicData[2]);
//        inamic.setTipDamageRezistent(inamicData[3]);
//        inamic.setCritChanceBonus(RandomUtils.randomInt(0, 5));
//       // inamic.setPoateAplicaDebuff(RandomUtils.chancePercent(20.0));
//
//        return inamic;
//    }


    public Inamic genereazaInamicNormal(int nivel) {
        String[][] tabelInamici;
        String prefix;

        // Determină tier-ul și selectează inamicii corespunzători
        if (nivel <= 10) {
            tabelInamici = INAMICI_SLABI;
            prefix = "";
        } else if (nivel <= 20) {
            tabelInamici = INAMICI_MEDII;
            prefix = "";
        } else if (nivel <= 30) {
            tabelInamici = INAMICI_PUTERNICI;
            prefix = "Puternic ";
        } else if (nivel <= 40) {
            tabelInamici = INAMICI_LEGENDARI;
            prefix = "Legendar ";
        } else {
            tabelInamici = INAMICI_EPICI;
            prefix = "Epic ";
        }

        // ✅ RandomUtils funcționează perfect
        String[] inamicData = RandomUtils.randomElement(tabelInamici);

        // ✅ FIX NUME - folosește elementele din array
        String nume = prefix + inamicData[0] + " " + inamicData[1] + " Lv" + nivel;

        // ✅ GameConstants funcționează perfect
        int viataMaxima = GameConstants.ENEMY_BASE_HEALTH + (nivel * GameConstants.ENEMY_HEALTH_PER_LEVEL);
        int defense = GameConstants.ENEMY_BASE_DEFENSE + (nivel * GameConstants.ENEMY_DEFENSE_PER_LEVEL);
        int gold = GameConstants.ENEMY_BASE_GOLD + (nivel * GameConstants.ENEMY_GOLD_PER_LEVEL);
        int xpOferit = GameConstants.ENEMY_BASE_XP + (nivel * GameConstants.ENEMY_XP_PER_LEVEL);

        // Variație aleatorie
        viataMaxima = RandomUtils.applyRandomVariation(viataMaxima, 20);
        defense = RandomUtils.applyRandomVariation(defense, 15);
        gold = RandomUtils.applyRandomVariation(gold, 30);
        xpOferit = RandomUtils.applyRandomVariation(xpOferit, 25);

        // Safety check
        viataMaxima = Math.max(10, viataMaxima);
        gold = Math.max(1, gold);
        xpOferit = Math.max(1, xpOferit);
        defense = Math.max(0, defense);

        System.out.printf("✅ ENEMY FINAL: %s | HP=%d, Gold=%d, XP=%d%n",
                nume, viataMaxima, gold, xpOferit);

        // Creează inamicul
        Inamic inamic = new Inamic(nume, nivel, viataMaxima, defense, gold, xpOferit, false);

        // 🔍 DEBUG DAMAGE
        System.out.printf("🔍 DAMAGE CHECK: GameConstants.calculateEnemyDamage(%d) = %d, inamic.getDamage() = %d%n",
                nivel, GameConstants.calculateEnemyDamage(nivel), inamic.getDamage());

        // 🩹 DAMAGE FIX dacă e 0
        if (inamic.getDamage() <= 0) {
            try {
                java.lang.reflect.Field damageCamp = Inamic.class.getDeclaredField("damage");
                damageCamp.setAccessible(true);
                int forceaDamage = 8 + (nivel * 3);
                damageCamp.setInt(inamic, forceaDamage);
                System.out.printf("🔧 DAMAGE FORȚAT: %d%n", forceaDamage);
            } catch (Exception e) {
                System.out.println("⚠️ Nu pot forța damage: " + e.getMessage());
            }
        }

        // Setează vulnerabilități
        inamic.setTipDamageVulnerabil(inamicData[2]);
        inamic.setTipDamageRezistent(inamicData[3]);
        inamic.setCritChanceBonus(RandomUtils.randomInt(0, 5));

        // 🆕 Enhance enemy with tier and affixes
        EnemyAffixService.enhanceEnemy(inamic, nivel, false);

        return inamic;
    }


    /**
     * Generează un boss românesc pentru un nivel dat.
     */
    public Inamic genereazaBoss(int nivel) {
        String[] tabelBossi;
        String prefix;
        String emoji;

        // Determină tier-ul boss-ului
        if (nivel <= 10) {
            tabelBossi = BOSS_SLABI;
            prefix = "BOSS";
            emoji = "👑";
        } else if (nivel <= 20) {
            tabelBossi = BOSS_MEDII;
            prefix = "BOSS PUTERNIC";
            emoji = "💀";
        } else if (nivel <= 30) {
            tabelBossi = BOSS_PUTERNICI;
            prefix = "BOSS LEGENDAR";
            emoji = "⚡";
        } else if (nivel <= 40) {
            tabelBossi = BOSS_LEGENDARI;
            prefix = "BOSS MITIC";
            emoji = "🔥";
        } else {
            tabelBossi = BOSS_EPICI;
            prefix = "BOSS EPIC";
            emoji = "💫";
        }

        String numeBoss = RandomUtils.randomElement(tabelBossi);
        String nume = emoji + " " + prefix + ": " + numeBoss + " " + emoji;

        // Statistici boss (mai mari decât inamicii normali)
        int viataMaxima = (GameConstants.ENEMY_BASE_HEALTH + (nivel * GameConstants.ENEMY_HEALTH_PER_LEVEL)) * 3;
        int defense = (GameConstants.ENEMY_BASE_DEFENSE + (nivel * GameConstants.ENEMY_DEFENSE_PER_LEVEL)) * 2;
        int gold = (GameConstants.ENEMY_BASE_GOLD + (nivel * GameConstants.ENEMY_GOLD_PER_LEVEL)) * 4;
        int xpOferit = (GameConstants.ENEMY_BASE_XP + (nivel * GameConstants.ENEMY_XP_PER_LEVEL)) * 3;

        // Creează boss-ul
        Inamic boss = new Inamic(nume, nivel, viataMaxima, defense, gold, xpOferit, true);

        // Boss-ii au vulnerabilități și rezistențe speciale
        boss.setTipDamageVulnerabil(getVulnerabilitateBoss(nivel));
        boss.setTipDamageRezistent(getRezistentaBoss(nivel));
        boss.setCritChanceBonus(RandomUtils.randomInt(10, 20));
        boss.setRegenerareViata(Math.max(2, viataMaxima / 15));

        // 🆕 Enhance boss with affixes (will be set to BOSS tier automatically)
        EnemyAffixService.enhanceEnemy(boss, nivel, true);

        return boss;
    }

    /**
     * Returnează vulnerabilitatea specifică pentru boss-i pe nivele.
     */
    private String getVulnerabilitateBoss(int nivel) {
        if (nivel <= 10) return "fire";
        if (nivel <= 20) return "holy";
        if (nivel <= 30) return "ice";
        if (nivel <= 40) return "lightning";
        return "divine";
    }

    /**
     * Returnează rezistența specifică pentru boss-i pe nivele.
     */
    private String getRezistentaBoss(int nivel) {
        if (nivel <= 10) return "physical";
        if (nivel <= 20) return "darkness";
        if (nivel <= 30) return "fire";
        if (nivel <= 40) return "all_magic";
        return "physical_and_magical";
    }

    /**
     * Generează descriere pentru inamic (flavor text).
     */
    public static String getEnemyDescription(int nivel) {
        if (nivel <= 10) {
            return "Un inamic slab care bântuie străzile și cartierele sărace.";
        } else if (nivel <= 20) {
            return "O creatură supranaturală din folclorul românesc.";
        } else if (nivel <= 30) {
            return "Un monstru puternic din legendele străvechi ale țării.";
        } else if (nivel <= 40) {
            return "O ființă legendară din mitologia dacică și românească.";
        } else {
            return "Un război epic din cele mai întunecate povești ale neamului.";
        }
    }
}