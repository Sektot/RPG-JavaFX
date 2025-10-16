package com.rpg.service;

import com.rpg.model.items.ObiectEchipament;
import com.rpg.model.items.ObiectEchipament.Raritate;
import com.rpg.model.items.ObiectEchipament.TipEchipament;
import com.rpg.utils.RandomUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LootGenerator {

    // ==================== ARME FIZICE (STR + ATTACK) ====================

    public enum PhysicalWeapon {
        // Tamed
        SABIE("Sabia", new String[]{"Dacică", "Moldovenească", "de Oțel", "Rusească", "lui Ștefan"}),
        TOPOR("Toporul", new String[]{"de Lemne", "Crăpat", "din Pădure", "de Război", "Bătrânesc"}),
        CIOMAG("Ciomăgelul", new String[]{"de Stejar", "Țărănesc", "Brutal", "Greu", "din Codru"}),
        BAT("Bâta", new String[]{"de Baseball", "Agresivă", "de Cartier", "Sportivă", "ProSport"}),
        LANȚ("Lanțul", new String[]{"Greu", "de Bicicletă", "Oxidat", "Industrial", "Șmecheresc"}),

        // Spicy
        SECUREA("Securea", new String[]{"MUE PSD", "Anticorupție", "Justiției", "ANAF Slayer", "Revoluționară"}),
        CIOCAN("Ciocanul", new String[]{"Liber", "Anticorupție", "lui Vadim", "Revoltei", "Dreptății"}),
        LOPATA("Lopata", new String[]{"lui Videanu", "de Șantier", "Devastatoare", "Socialismului", "Grădinii"}),
        FURCA("Furculița", new String[]{"Țărănească", "Revoltei", "Satului", "din 1907", "Furioasă"}),
        RANGĂ("Ranga", new String[]{"de Luptă", "Demolatoare", "Revoluționară", "Grosolană", "Brutală"});

        private final String baseName;
        private final String[] prefixes;

        PhysicalWeapon(String baseName, String[] prefixes) {
            this.baseName = baseName;
            this.prefixes = prefixes;
        }

        public String getBaseName() { return baseName; }
        public String[] getPrefixes() { return prefixes; }
    }

    // ==================== ARME AGILE (DEX + ATTACK) ====================

    public enum AgileWeapon {
        // Tamed
        PUMNAL("Pumnalul", new String[]{"Ascuțit", "Rapid", "din Umbră", "Subțire", "Viclean"}),
        ARC("Arcul", new String[]{"Lung", "de Vânătoare", "Precis", "Elastic", "Tradițional"}),
        ARBALETA("Arbaleta", new String[]{"Mecanică", "Silențioasă", "Precisă", "Modernă", "Tactică"}),
        KATANA("Katana", new String[]{"Japoneză", "Samurai", "Șmecheră", "Weeb", "Ascuțită"}),

        // Spicy
        BRICEAG("Briceagul", new String[]{"de Craiova", "Oltean", "Dubios", "Interlop", "de Cartier"}),
        CUȚIT("Cuțitul", new String[]{"de Bucătărie", "din Vestiar", "Țepar", "Dur", "Învechit"}),
        LAMA("Lama", new String[]{"Ascunsă", "Otrăvită", "Murdară", "Ruginită", "Periculoasă"}),
        FOARFECA("Foarfeca", new String[]{"Mare", "de Gradinar", "Tăioasă", "Dubioasă", "Improvizată"});

        private final String baseName;
        private final String[] prefixes;

        AgileWeapon(String baseName, String[] prefixes) {
            this.baseName = baseName;
            this.prefixes = prefixes;
        }

        public String getBaseName() { return baseName; }
        public String[] getPrefixes() { return prefixes; }
    }

    // ==================== ARME MAGICE (INT + MAGIC) ====================

    public enum MagicalWeapon {
        // Tamed
        TOIAG("Toiagul", new String[]{"Solomonarului", "Înțelept", "Vechi", "Mistic", "din Cluj"}),
        BAGHETA("Bagheta", new String[]{"Magică", "Fermecată", "Strălucitoare", "Ardeleanului", "Vrăjitorului"}),
        CARTE("Cartea", new String[]{"Vrăjilor", "Interzisă", "Veche", "Dacică", "Strămoșească"}),
        CRISTAL("Cristalul", new String[]{"Mistic", "Strălucitor", "Energetic", "Albastru", "Puterii"}),

        // Spicy
        BIBLIE("Biblia", new String[]{"Sfântă", "Bătătoare", "Grea", "Vechiului Testament", "Justiției"}),
        MANUAL("Manualul", new String[]{"de Logică", "lui Ben Shapiro", "Facts & Logic", "Anti-Prostie", "Educației"}),
        SMARTPHONE("Smartphone-ul", new String[]{"cu 5G", "Hackuit", "Programmer", "Google Fu", "Reddit Wisdom"});

        private final String baseName;
        private final String[] prefixes;

        MagicalWeapon(String baseName, String[] prefixes) {
            this.baseName = baseName;
            this.prefixes = prefixes;
        }

        public String getBaseName() { return baseName; }
        public String[] getPrefixes() { return prefixes; }
    }

    // ==================== ARMURI (DEFENSE + HP) ====================

    private static final String[][] ARMOR_TYPES = {
            // Tamed
            {"Vesta", "Antiglonț", "de Securitate", "Tactică", "Militară", "Kevlar"},
            {"Armura", "de Oțel", "Medievală", "Greoaie", "Dacică", "Întărită"},
            {"Haină", "de Piele", "Groasă", "Biker", "Neagră", "Rezistentă"},
            {"Jachetă", "Bomber", "de Iarnă", "Groasă", "Căptușită", "Protectoare"},
            {"Cămașă", "de Forță", "Îngrețoșată", "Kevlar", "Tactică", "Militară"},

            // Spicy
            {"Cămașă", "Roșie a Patriei", "Comunistă", "URSS", "Sovietică", "Tovarășului"},
            {"Geacă", "Adidas", "de Interlop", "Trei Dungi", "Gopnik", "Slav"},
            {"Vesta", "Gălbenie", "AUR", "Reflectorizantă", "Protestului", "Revoluției"},
            {"Hanorac", "Supreme", "Fake de la Obor", "Gucci Fals", "Louis Vuitton Țeapă", "Drip"},
            {"Uniforma", "Jandarmeriei", "Poliției", "Corupției", "ANAF-ului", "Autorității"}
    };

    // ==================== CĂȘTI/HELMETS ====================

    private static final String[][] HELMET_TYPES = {
            // Tamed
            {"Cască", "de Protecție", "de Muncă", "Militară", "Tactică", "Anti-impact"},
            {"Bandană", "Roșie", "Rebelă", "de Pirat", "Vintage", "Colorată"},
            {"Șapcă", "cu Cozoroc", "Trucker", "Sportivă", "Baseball", "New Era"},

            // Spicy
            {"Bonetă", "Interlop", "de Cartier", "Gansta", "Suspect", "Dubioasă"},
            {"Cască", "Jandarm", "Anti-Protest", "cu Vizor", "Riot Control", "Antiglonț"},
            {"Pălărie", "Cowboy", "Texas", "Yeehaw", "Western", "Sălbatică"}
    };

    // ==================== BOCANCI/BOOTS ====================

    private static final String[][] BOOTS_TYPES = {
            // Tamed
            {"Bocanci", "de Munte", "Militari", "Trekking", "Rezistenți", "Înalți"},
            {"Adidași", "de Alergare", "Sport", "Nike", "Confortabili", "Ușori"},
            {"Ghete", "de Lucru", "Protecție", "Steel Toe", "Industriale", "Solide"},

            // Spicy
            {"Crăpați", "Adidas", "Trei Dungi", "Slav", "Gopnik", "Fake"},
            {"Papuci", "de Casă", "Cozy", "Comfy", "Pensionarului", "Relaxare"},
            {"Bocanci", "Jandarm", "Anti-Protestatar", "Călcare", "Autorității", "Brutali"}
    };

    // ==================== MĂNUȘI/GLOVES ====================

    private static final String[][] GLOVES_TYPES = {
            // Tamed
            {"Mănuși", "de Lucru", "Protecție", "Groase", "Rezistente", "Iarnă"},
            {"Mănuși", "de Box", "Piele", "MMA", "Combat", "Fighting"},
            {"Mănuși", "Tactice", "Militare", "Airsoft", "Operațiuni", "Speciale"},

            // Spicy
            {"Pumni", "Americani", "de Fier", "Ilegali", "Brass Knuckles", "Dubioși"},
            {"Mănuși", "Latex", "Chirurgicale", "Sterilizate", "Medicale", "Dubioase"},
            {"Mănuși", "cu Ținte", "Punk", "Metal", "Gotice", "Rebele"}
    };

    // ==================== SCUTURI/SHIELDS ====================

    private static final String[][] SHIELD_TYPES = {
            // Tamed
            {"Scut", "Dacic", "Roman", "Medieval", "de Lemn", "Întărit"},
            {"Scut", "Riot", "Transparent", "Poliție", "Protecție", "Modern"},

            // Spicy
            {"Scut", "Jandarm", "Anti-Manifest", "Antiglonț", "Autorității", "Corupției"},
            {"Placuță", "Stradală", "STOP", "Rutieră", "Oprire Obligatorie", "Furată"},
            {"Capac", "de Tomberon", "Gunoi", "Improvizat", "Post-Apocaliptic", "Homeless"}
    };

    // ==================== ACCESORII ====================

    private static final String[][] RING_TYPES = {
            // Tamed
            {"Inel", "de Aur", "Argint", "Verigheta", "Logodnă", "Prețios"},
            {"Inel", "Mistic", "Puterii", "Fermecat", "Strălucitor", "Magic"},

            // Spicy
            {"Inel", "Furat", "de la Bunica", "Amanet", "Șpagă", "Corupție"},
            {"Inel", "Pope", "XXL", "Bling Bling", "Țigănesc", "Aurit"}
    };

    private static final String[][] NECKLACE_TYPES = {
            // Tamed
            {"Lanț", "de Aur", "Gros", "Lung", "Prețios", "Strălucitor"},
            {"Amuletă", "Magică", "Dacică", "Protectoare", "Veche", "Mistică"},
            {"Pandantiv", "Cristal", "Sacru", "Binecuvântat", "Sfânt", "Divin"},

            // Spicy
            {"Lanț", "Aurit", "Interlop", "Gangsta", "Țigănesc", "Bling"},
            {"Cruce", "Sfântă", "Mare", "Țigănească", "Aurită", "Enormă"},
            {"Lanț", "Mercedes", "BMW", "Audi Rings", "Marca Furată", "Logo"}
    };

    // ==================== SUFIXE PENTRU RARITATI MARI ====================

    private static final String[] EPIC_SUFFIXES = {
            "al Puterii", "Devastator", "Supreme", "Ultra", "Mega",
            "al Morții", "Apocaliptic", "Legendar", "Mitic", "Divin",
            "al Nimicirii", "Eternal", "Infinit", "Absolut", "Final",
            "MUE PSD", "Anti-Corupție", "al Justiției", "Revolution", "Freedom"
    };

    // ==================== METODE PRINCIPALE ====================

    /**
     * Generează un obiect aleatoriu bazat pe nivelul inamicului.
     */
    public static ObiectEchipament generateRandomItem(int enemyLevel) {
        Raritate raritate = determineRarity(enemyLevel);
        TipEchipament[] tipuri = TipEchipament.values();
        TipEchipament tip = RandomUtils.randomElement(tipuri);

        return generateItemByType(tip, enemyLevel, raritate);
    }

    /**
     * Generează un obiect pe baza tipului specificat.
     */
    public static ObiectEchipament generateItemByType(TipEchipament tip, int level, Raritate raritate) {
        String itemName;
        Map<String, Integer> bonuses;

        switch (tip) {
            case WEAPON -> {
                // Alege random între Physical, Agile, Magical
                double roll = RandomUtils.randomDouble();
                if (roll < 0.33) {
                    PhysicalWeapon weapon = RandomUtils.randomElement(PhysicalWeapon.values());
                    itemName = generateWeaponName(weapon.getBaseName(), weapon.getPrefixes(), raritate);
                    bonuses = calculatePhysicalWeaponBonuses(raritate);
                } else if (roll < 0.66) {
                    AgileWeapon weapon = RandomUtils.randomElement(AgileWeapon.values());
                    itemName = generateWeaponName(weapon.getBaseName(), weapon.getPrefixes(), raritate);
                    bonuses = calculateAgileWeaponBonuses(raritate);
                } else {
                    MagicalWeapon weapon = RandomUtils.randomElement(MagicalWeapon.values());
                    itemName = generateWeaponName(weapon.getBaseName(), weapon.getPrefixes(), raritate);
                    bonuses = calculateMagicalWeaponBonuses(raritate);
                }
            }
            case ARMOR -> {
                String[] armorData = RandomUtils.randomElement(ARMOR_TYPES);
                itemName = generateArmorName(armorData, raritate);
                bonuses = calculateDefensiveBonuses(raritate, 1.0);
            }
            case HELMET -> {
                String[] helmetData = RandomUtils.randomElement(HELMET_TYPES);
                itemName = generateArmorName(helmetData, raritate);
                bonuses = calculateDefensiveBonuses(raritate, 0.7);
            }
            case BOOTS -> {
                String[] bootsData = RandomUtils.randomElement(BOOTS_TYPES);
                itemName = generateArmorName(bootsData, raritate);
                bonuses = calculateDefensiveBonuses(raritate, 0.6);
            }
            case GLOVES -> {
                String[] glovesData = RandomUtils.randomElement(GLOVES_TYPES);
                itemName = generateArmorName(glovesData, raritate);
                bonuses = calculateDefensiveBonuses(raritate, 0.6);
            }
            case SHIELD -> {
                String[] shieldData = RandomUtils.randomElement(SHIELD_TYPES);
                itemName = generateArmorName(shieldData, raritate);
                bonuses = calculateShieldBonuses(raritate);
            }
            case RING -> {
                String[] ringData = RandomUtils.randomElement(RING_TYPES);
                itemName = generateAccessoryName(ringData, raritate);
                bonuses = calculateAccessoryBonuses(raritate);
            }
            case NECKLACE -> {
                String[] necklaceData = RandomUtils.randomElement(NECKLACE_TYPES);
                itemName = generateAccessoryName(necklaceData, raritate);
                bonuses = calculateAccessoryBonuses(raritate);
            }
            default -> {
                itemName = "Unknown Item";
                bonuses = new HashMap<>();
            }
        }

        int requiredLevel = Math.max(1, level + RandomUtils.randomInt(-2, 2));
        int pret = calculateItemPrice(requiredLevel, raritate);

        return new ObiectEchipament(
                itemName,
                requiredLevel,
                raritate,
                tip,
                bonuses.getOrDefault("strength", 0),
                bonuses.getOrDefault("dexterity", 0),
                bonuses.getOrDefault("intelligence", 0),
                bonuses.getOrDefault("defense", 0),
                pret
        );
    }

    // ==================== GENERARE NUME ====================

    private static String generateWeaponName(String baseName, String[] prefixes, Raritate raritate) {
        String prefix = RandomUtils.randomElement(prefixes);

        if (raritate.ordinal() >= 3) { // EPIC sau LEGENDARY
            String suffix = RandomUtils.randomElement(EPIC_SUFFIXES);
            return baseName + " " + prefix + " " + suffix;  // ✅ CORECT
        }

        return baseName + " " + prefix;  // ✅ CORECT
    }

    private static String generateArmorName(String[] armorData, Raritate raritate) {
        String baseName = armorData[0];
        String prefix = RandomUtils.randomElement(java.util.Arrays.copyOfRange(armorData, 1, armorData.length));

        if (raritate.ordinal() >= 3) {
            String suffix = RandomUtils.randomElement(EPIC_SUFFIXES);
            return baseName + " " + prefix + " " + suffix;  // ✅ CORECT
        }

        return baseName + " " + prefix;  // ✅ CORECT
    }

    private static String generateAccessoryName(String[] accessoryData, Raritate raritate) {
        String baseName = accessoryData[0];
        String prefix = RandomUtils.randomElement(java.util.Arrays.copyOfRange(accessoryData, 1, accessoryData.length));

        if (raritate.ordinal() >= 3) {
            String suffix = RandomUtils.randomElement(EPIC_SUFFIXES);
            return baseName + " " + prefix + " " + suffix;  // ✅ CORECT
        }

        return baseName + " " + prefix;  // ✅ CORECT
    }

    // ==================== CALCUL BONUSURI ====================

    private static Map<String, Integer> calculatePhysicalWeaponBonuses(Raritate raritate) {
        Map<String, Integer> bonuses = new HashMap<>();
        int baseBonus = (int)(raritate.getMultiplier() * 2);
        int attackBonus = baseBonus + RandomUtils.randomInt(2, 5);

        bonuses.put("strength", baseBonus + RandomUtils.randomInt(1, 4));
        bonuses.put("attack_bonus", attackBonus);

        if (raritate.ordinal() >= 2) {
            bonuses.put("damage_bonus", baseBonus);
        }

        return bonuses;
    }

    private static Map<String, Integer> calculateAgileWeaponBonuses(Raritate raritate) {
        Map<String, Integer> bonuses = new HashMap<>();
        int baseBonus = (int)(raritate.getMultiplier() * 2);
        int attackBonus = baseBonus + RandomUtils.randomInt(2, 5);

        bonuses.put("dexterity", baseBonus + RandomUtils.randomInt(1, 4));
        bonuses.put("attack_bonus", attackBonus);

        if (raritate.ordinal() >= 2) {
            bonuses.put("crit_chance", baseBonus / 2);
        }

        return bonuses;
    }

    private static Map<String, Integer> calculateMagicalWeaponBonuses(Raritate raritate) {
        Map<String, Integer> bonuses = new HashMap<>();
        int baseBonus = (int)(raritate.getMultiplier() * 2);
        int attackBonus = baseBonus + RandomUtils.randomInt(2, 5);

        bonuses.put("intelligence", baseBonus + RandomUtils.randomInt(1, 4));
        bonuses.put("attack_bonus", attackBonus);

        if (raritate.ordinal() >= 1) {
            bonuses.put("mana", baseBonus * 5);
        }

        return bonuses;
    }

    private static Map<String, Integer> calculateDefensiveBonuses(Raritate raritate, double multiplier) {
        Map<String, Integer> bonuses = new HashMap<>();
        int baseBonus = (int)(raritate.getMultiplier() * 2 * multiplier);

        bonuses.put("defense", baseBonus + RandomUtils.randomInt(2, 5));
        bonuses.put("viata", (int)(baseBonus * 8));

        if (raritate.ordinal() >= 2) {
            bonuses.put("damage_reduction", baseBonus / 2);
        }

        return bonuses;
    }

    private static Map<String, Integer> calculateShieldBonuses(Raritate raritate) {
        Map<String, Integer> bonuses = new HashMap<>();
        int baseBonus = (int)(raritate.getMultiplier() * 2);

        bonuses.put("defense", baseBonus * 2 + RandomUtils.randomInt(3, 6));
        bonuses.put("viata", baseBonus * 10);

        if (raritate.ordinal() >= 1) {
            bonuses.put("block_chance", baseBonus * 2);
        }

        return bonuses;
    }

    private static Map<String, Integer> calculateAccessoryBonuses(Raritate raritate) {
        Map<String, Integer> bonuses = new HashMap<>();
        int baseBonus = (int)(raritate.getMultiplier() * 2);

        double roll = RandomUtils.randomDouble();

        if (roll < 0.33) {
            bonuses.put("strength", baseBonus + RandomUtils.randomInt(0, 3));
            bonuses.put("attack_bonus", baseBonus);
        } else if (roll < 0.66) {
            bonuses.put("dexterity", baseBonus + RandomUtils.randomInt(0, 3));
            bonuses.put("crit_chance", baseBonus);
        } else {
            bonuses.put("intelligence", baseBonus + RandomUtils.randomInt(0, 3));
            bonuses.put("mana", baseBonus * 5);
        }

        if (raritate.ordinal() >= 3) {
            double secondRoll = RandomUtils.randomDouble();
            if (secondRoll < 0.5) {
                bonuses.put("defense", baseBonus / 2);
            } else {
                bonuses.put("viata", baseBonus * 3);
            }
        }

        return bonuses;
    }

    // ==================== HELPER METHODS ====================

    private static Raritate determineRarity(int level) {
        double roll = RandomUtils.randomDouble();

        if (level >= 20 && roll < 0.05) return Raritate.LEGENDARY;
        if (level >= 15 && roll < 0.15) return Raritate.EPIC;
        if (level >= 10 && roll < 0.25) return Raritate.RARE;
        if (level >= 5 && roll < 0.40) return Raritate.UNCOMMON;
        return Raritate.COMMON;
    }

    private static int calculateItemPrice(int level, Raritate raritate) {
        int basePrice = level * 10 + 20;
        return (int)(basePrice * raritate.getMultiplier());
    }

    // ==================== METODE PENTRU LOOT GENERATION ====================

    public List<ObiectEchipament> generatePossibleLoot(int enemyLevel, boolean isBoss) {
        List<ObiectEchipament> lootTable = new ArrayList<>();

        int itemCount = isBoss ? RandomUtils.randomInt(2, 4) : RandomUtils.randomInt(1, 3);

        for (int i = 0; i < itemCount; i++) {
            ObiectEchipament item = generateRandomItem(enemyLevel);

            if (isBoss && RandomUtils.chancePercent(30.0)) {
                item = generateRandomItem(enemyLevel + 2);
            }

            lootTable.add(item);
        }

        return lootTable;
    }

    public static List<ObiectEchipament> rollForLoot(List<ObiectEchipament> lootTable, double dropChance) {
        List<ObiectEchipament> actualLoot = new ArrayList<>();

        for (ObiectEchipament item : lootTable) {
            if (RandomUtils.chancePercent(dropChance)) {
                actualLoot.add(item);
            }
        }

        return actualLoot;
    }



    /**
     * ✨ METODĂ NOUĂ: Afișează stats-urile unui item când pică
     */
    public static void displayItemDrop(ObiectEchipament item) {
        String rarityColor = getRarityColor(item.getRaritate());
        String rarityIcon = getRarityIcon(item.getRaritate());

        System.out.println("\n📦 " + rarityColor + rarityIcon + " " + item.getNume() + " " + rarityIcon);
        System.out.println("   📊 " + item.getRaritate().getDisplayName() + " | " + item.getTip().getDisplayName());
        System.out.println("   🎯 Nivel necesar: " + item.getNivelNecesar());

        // Afișează bonusurile
        if (item.getStrengthBonus() > 0) {
            System.out.println("   💪 +" + item.getStrengthBonus() + " Strength");
        }
        if (item.getDexterityBonus() > 0) {
            System.out.println("   🎯 +" + item.getDexterityBonus() + " Dexterity");
        }
        if (item.getIntelligenceBonus() > 0) {
            System.out.println("   🧠 +" + item.getIntelligenceBonus() + " Intelligence");
        }
        if (item.getDefenseBonus() > 0) {
            System.out.println("   🛡️ +" + item.getDefenseBonus() + " Defense");
        }

        System.out.println("   💰 Valoare: " + item.getPret() + " gold");
    }

    private static String getRarityColor(Raritate raritate) {
        return switch (raritate) {
            case COMMON -> "⚪";
            case UNCOMMON -> "🟢";
            case RARE -> "🔵";
            case EPIC -> "🟣";
            case LEGENDARY -> "🟠";
        };
    }

    private static String getRarityIcon(Raritate raritate) {
        return switch (raritate) {
            case COMMON -> "⭐";
            case UNCOMMON -> "⭐⭐";
            case RARE -> "⭐⭐⭐";
            case EPIC -> "⭐⭐⭐⭐";
            case LEGENDARY -> "⭐⭐⭐⭐⭐";
        };
    }
}