package com.rpg.service;

import com.rpg.model.items.Jewel;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.model.items.ObiectEchipament.Raritate;
import com.rpg.model.items.ObiectEchipament.TipEchipament;
import com.rpg.utils.RandomUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LootGenerator {

    // ==================== ARME ONE-HANDED FIZICE ====================
    public enum OneHandedPhysicalWeapon {
        // Swords
        SABIE("Sabia", new String[]{"Dacică", "Moldovenească", "de Oțel", "Rusească", "lui Ștefan"}, "sword"),
        SPADA("Spada", new String[]{"Elegantă", "de Duel", "Nobilă", "Rafinată", "Cavalerească"}, "sword"),
        CUTIT("Cuțitul", new String[]{"de Bucătărie", "din Vestiar", "Țepar", "Dur", "Învechit"}, "dagger"),
        PUMNAL("Pumnalul", new String[]{"Ascuțit", "Rapid", "din Umbră", "Subțire", "Viclean"}, "dagger"),

        // Blunt
        CIOMAG("Ciomăgelul", new String[]{"de Stejar", "Țărănesc", "Brutal", "Greu", "din Codru"}, "mace"),
        BAT("Bâta", new String[]{"de Baseball", "Agresivă", "de Cartier", "Sportivă", "ProSport"}, "club");

        private final String baseName;
        private final String[] prefixes;
        private final String weaponClass;

        OneHandedPhysicalWeapon(String baseName, String[] prefixes, String weaponClass) {
            this.baseName = baseName;
            this.prefixes = prefixes;
            this.weaponClass = weaponClass;
        }

        public String getBaseName() { return baseName; }
        public String[] getPrefixes() { return prefixes; }
        public String getWeaponClass() { return weaponClass; }
    }

    // ==================== ARME TWO-HANDED FIZICE ====================
    public enum TwoHandedPhysicalWeapon {
        TOPOR("Toporul", new String[]{"de Lemne", "Crăpat", "din Pădure", "de Război", "Bătrânesc"}, "axe"),
        SECUREA("Securea", new String[]{"MUE PSD", "Anticorupție", "Justiției", "ANAF Slayer", "Revoluționară"}, "axe"),
        CIOCAN("Ciocanul", new String[]{"Liber", "Anticorupție", "lui Vadim", "Revoltei", "Dreptății"}, "hammer"),
        LOPATA("Lopata", new String[]{"lui Videanu", "de Șantier", "Devastatoare", "Socialismului", "Grădinii"}, "shovel"),
        RANGA("Ranga", new String[]{"de Luptă", "Demolatoare", "Revoluționară", "Grosolană", "Brutală"}, "polearm");

        private final String baseName;
        private final String[] prefixes;
        private final String weaponClass;

        TwoHandedPhysicalWeapon(String baseName, String[] prefixes, String weaponClass) {
            this.baseName = baseName;
            this.prefixes = prefixes;
            this.weaponClass = weaponClass;
        }

        public String getBaseName() { return baseName; }
        public String[] getPrefixes() { return prefixes; }
        public String getWeaponClass() { return weaponClass; }
    }

    // ==================== RANGED WEAPONS ====================
    public enum RangedWeapon {
        ARC("Arcul", new String[]{"Lung", "de Vânătoare", "Precis", "Elastic", "Tradițional"}, "bow"),
        ARBALETA("Arbaleta", new String[]{"Mecanică", "Silențioasă", "Precisă", "Modernă", "Tactică"}, "crossbow");

        private final String baseName;
        private final String[] prefixes;
        private final String weaponClass;

        RangedWeapon(String baseName, String[] prefixes, String weaponClass) {
            this.baseName = baseName;
            this.prefixes = prefixes;
            this.weaponClass = weaponClass;
        }

        public String getBaseName() { return baseName; }
        public String[] getPrefixes() { return prefixes; }
        public String getWeaponClass() { return weaponClass; }
    }


    // ==================== ARME MAGICE ====================
    public enum MagicalWeapon {
        // One-handed magic
        BAGHETA("Bagheta", new String[]{"Magică", "Fermecată", "Strălucitoare", "Ardeleanului", "Vrăjitorului"}, "wand", true),
        SMARTPHONE("Smartphone-ul", new String[]{"cu 5G", "Hackuit", "Programmer", "Google Fu", "Reddit Wisdom"}, "device", true),

        // Two-handed magic
        TOIAG("Toiagul", new String[]{"Solomonarului", "Înțelept", "Vechi", "Mistic", "din Cluj"}, "staff", false),

        // Off-hand magic
        CARTE("Cartea", new String[]{"Vrăjilor", "Interzisă", "Veche", "Dacică", "Strămoșească"}, "tome", false),
        CRISTAL("Cristalul", new String[]{"Mistic", "Strălucitor", "Energetic", "Albastru", "Puterii"}, "orb", false),
        BIBLIE("Biblia", new String[]{"Sfântă", "Bătătoare", "Grea", "Vechiului Testament", "Justiției"}, "holy", false);

        private final String baseName;
        private final String[] prefixes;
        private final String weaponClass;
        private final boolean isOneHanded;

        MagicalWeapon(String baseName, String[] prefixes, String weaponClass, boolean isOneHanded) {
            this.baseName = baseName;
            this.prefixes = prefixes;
            this.weaponClass = weaponClass;
            this.isOneHanded = isOneHanded;
        }

        public String getBaseName() { return baseName; }
        public String[] getPrefixes() { return prefixes; }
        public String getWeaponClass() { return weaponClass; }
        public boolean isOneHanded() { return isOneHanded; }
    }

    // ==================== SHIELDS ====================
    public enum ShieldType {
        SCUT("Scut", new String[]{"Dacic", "Roman", "Medieval", "de Lemn", "Întărit"}, "shield"),
        SCUT_RIOT("Scut", new String[]{"Riot", "Transparent", "Poliție", "Protecție", "Modern"}, "riot_shield"),
        SCUT_JANDARM("Scut", new String[]{"Jandarm", "Anti-Manifest", "Antiglonț", "Autorității", "Corupției"}, "tactical_shield"),
        CAPAC("Capac", new String[]{"de Tomberon", "Gunoi", "Improvizat", "Post-Apocaliptic", "Homeless"}, "improvised");

        private final String baseName;
        private final String[] prefixes;
        private final String shieldClass;

        ShieldType(String baseName, String[] prefixes, String shieldClass) {
            this.baseName = baseName;
            this.prefixes = prefixes;
            this.shieldClass = shieldClass;
        }

        public String getBaseName() { return baseName; }
        public String[] getPrefixes() { return prefixes; }
        public String getShieldClass() { return shieldClass; }
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
        ObiectEchipament.WeaponHandedness handedness = ObiectEchipament.WeaponHandedness.ONE_HANDED;
        String weaponClass = "";
        boolean isOffHandCompatible = false;

        switch (tip) {
            case WEAPON_ONE_HANDED -> {
                // Alege random între Physical și Agile one-handed
                double roll = RandomUtils.randomDouble();
                if (roll < 0.7) { // 70% Physical
                    OneHandedPhysicalWeapon weapon = RandomUtils.randomElement(OneHandedPhysicalWeapon.values());
                    itemName = generateWeaponName(weapon.getBaseName(), weapon.getPrefixes(), raritate);
                    bonuses = calculatePhysicalWeaponBonuses(raritate);
                    weaponClass = weapon.getWeaponClass();

                    // Daggers/knives pot fi off-hand compatible
                    isOffHandCompatible = weaponClass.equals("dagger");
                } else { // 30% Magical one-handed
                    MagicalWeapon[] oneHandedMagic = java.util.Arrays.stream(MagicalWeapon.values())
                            .filter(MagicalWeapon::isOneHanded)
                            .toArray(MagicalWeapon[]::new);
                    MagicalWeapon weapon = RandomUtils.randomElement(oneHandedMagic);
                    itemName = generateWeaponName(weapon.getBaseName(), weapon.getPrefixes(), raritate);
                    bonuses = calculateMagicalWeaponBonuses(raritate);
                    weaponClass = weapon.getWeaponClass();
                    isOffHandCompatible = true; // Wands pot fi off-hand
                }
                handedness = ObiectEchipament.WeaponHandedness.ONE_HANDED;
            }

            case WEAPON_TWO_HANDED -> {
                double roll = RandomUtils.randomDouble();
                if (roll < 0.6) { // 60% Physical two-handed
                    TwoHandedPhysicalWeapon weapon = RandomUtils.randomElement(TwoHandedPhysicalWeapon.values());
                    itemName = generateWeaponName(weapon.getBaseName(), weapon.getPrefixes(), raritate);
                    bonuses = calculatePhysicalWeaponBonuses(raritate);
                    weaponClass = weapon.getWeaponClass();
                } else if (roll < 0.85) { // 25% Magical two-handed (staffs)
                    MagicalWeapon[] twoHandedMagic = java.util.Arrays.stream(MagicalWeapon.values())
                            .filter(w -> !w.isOneHanded() && w.getWeaponClass().equals("staff"))
                            .toArray(MagicalWeapon[]::new);
                    MagicalWeapon weapon = RandomUtils.randomElement(twoHandedMagic);
                    itemName = generateWeaponName(weapon.getBaseName(), weapon.getPrefixes(), raritate);
                    bonuses = calculateMagicalWeaponBonuses(raritate);
                    weaponClass = weapon.getWeaponClass();
                } else { // 15% Ranged two-handed
                    RangedWeapon weapon = RandomUtils.randomElement(RangedWeapon.values());
                    itemName = generateWeaponName(weapon.getBaseName(), weapon.getPrefixes(), raritate);
                    bonuses = calculateRangedWeaponBonuses(raritate);
                    weaponClass = weapon.getWeaponClass();
                }
                handedness = ObiectEchipament.WeaponHandedness.TWO_HANDED;
            }

            case SHIELD -> {
                ShieldType shield = RandomUtils.randomElement(ShieldType.values());
                itemName = generateWeaponName(shield.getBaseName(), shield.getPrefixes(), raritate);
                bonuses = calculateShieldBonuses(raritate);
                weaponClass = shield.getShieldClass();
                handedness = ObiectEchipament.WeaponHandedness.OFF_HAND_ONLY;
            }

            case OFF_HAND_WEAPON -> {
                // Generate off-hand specific daggers/weapons
                String[] offHandNames = {"Pumnal", "Stiletto", "Cuțit", "Dagger"};
                String[] offHandPrefixes = {"Rapid", "din Umbră", "Secundar", "de Backup", "Ascuns"};
                itemName = generateWeaponName(RandomUtils.randomElement(offHandNames),
                        offHandPrefixes, raritate);
                bonuses = calculateOffHandWeaponBonuses(raritate);
                weaponClass = "dagger";
                handedness = ObiectEchipament.WeaponHandedness.OFF_HAND_ONLY;
            }

            case OFF_HAND_MAGIC -> {
                MagicalWeapon[] offHandMagic = java.util.Arrays.stream(MagicalWeapon.values())
                        .filter(w -> !w.isOneHanded() && !w.getWeaponClass().equals("staff"))
                        .toArray(MagicalWeapon[]::new);
                MagicalWeapon weapon = RandomUtils.randomElement(offHandMagic);
                itemName = generateWeaponName(weapon.getBaseName(), weapon.getPrefixes(), raritate);
                bonuses = calculateOffHandMagicBonuses(raritate);
                weaponClass = weapon.getWeaponClass();
                handedness = ObiectEchipament.WeaponHandedness.OFF_HAND_ONLY;
            }

            case ARMOR -> {
                String[] armorData = RandomUtils.randomElement(ARMOR_TYPES);
                itemName = generateArmorName(armorData, raritate);
                bonuses = calculateDefensiveBonuses(raritate, 1.0);
                weaponClass = "armor";
            }
            case HELMET -> {
                String[] helmetData = RandomUtils.randomElement(HELMET_TYPES);
                itemName = generateArmorName(helmetData, raritate);
                bonuses = calculateDefensiveBonuses(raritate, 0.7);
                weaponClass = "helmet";
            }
            case BOOTS -> {
                String[] bootsData = RandomUtils.randomElement(BOOTS_TYPES);
                itemName = generateArmorName(bootsData, raritate);
                bonuses = calculateDefensiveBonuses(raritate, 0.6);
                weaponClass = "boots";
            }
            case GLOVES -> {
                String[] glovesData = RandomUtils.randomElement(GLOVES_TYPES);
                itemName = generateArmorName(glovesData, raritate);
                bonuses = calculateDefensiveBonuses(raritate, 0.6);
                weaponClass = "gloves";
            }
            case RING -> {
                String[] ringData = RandomUtils.randomElement(RING_TYPES);
                itemName = generateAccessoryName(ringData, raritate);
                bonuses = calculateAccessoryBonuses(raritate);
                weaponClass = "ring";
            }
            case NECKLACE -> {
                String[] necklaceData = RandomUtils.randomElement(NECKLACE_TYPES);
                itemName = generateAccessoryName(necklaceData, raritate);
                bonuses = calculateAccessoryBonuses(raritate);
                weaponClass = "necklace";
            }

            default -> {
                itemName = "Unknown Item";
                bonuses = new HashMap<>();
            }
        }

        int requiredLevel = Math.max(1, level + RandomUtils.randomInt(-2, 2));
        int pret = calculateItemPrice(requiredLevel, raritate);

// ✅ Folosește constructorul extins cu noile proprietăți
        return new ObiectEchipament(
                itemName,
                requiredLevel,
                raritate,
                tip,
                bonuses.getOrDefault("strength", 0),
                bonuses.getOrDefault("dexterity", 0),
                bonuses.getOrDefault("intelligence", 0),
                bonuses.getOrDefault("defense", 0),
                pret,
                handedness,      // ✅ Nou
                weaponClass,     // ✅ Nou
                isOffHandCompatible // ✅ Nou
        );
    }


// ==================== BONUSURI NOI ====================

    private static Map<String, Integer> calculateRangedWeaponBonuses(Raritate raritate) {
        Map<String, Integer> bonuses = new HashMap<>();
        int baseBonus = (int)(raritate.getMultiplier() * 2);

        bonuses.put("dexterity", baseBonus + RandomUtils.randomInt(2, 6));
        bonuses.put("Damage", baseBonus + RandomUtils.randomInt(3, 7));
        bonuses.put("hit_chance", baseBonus * 2); // Ranged = mai precis
        bonuses.put("crit_chance", baseBonus + 3);

        if (raritate.ordinal() >= 2) {
            bonuses.put("damage_bonus", baseBonus);
        }

        return bonuses;
    }

    private static Map<String, Integer> calculateOffHandWeaponBonuses(Raritate raritate) {
        Map<String, Integer> bonuses = new HashMap<>();
        int baseBonus = (int)(raritate.getMultiplier() * 1.5); // Slightly lower than main hand

        bonuses.put("dexterity", baseBonus + RandomUtils.randomInt(1, 3));
        bonuses.put("Damage", baseBonus + RandomUtils.randomInt(1, 4));
        bonuses.put("crit_chance", baseBonus * 2); // Off-hand = mai multe crit-uri
        bonuses.put("hit_chance", baseBonus);

        if (raritate.ordinal() >= 2) {
            bonuses.put("dodge_chance", baseBonus);
        }

        return bonuses;
    }

    private static Map<String, Integer> calculateOffHandMagicBonuses(Raritate raritate) {
        Map<String, Integer> bonuses = new HashMap<>();
        int baseBonus = (int)(raritate.getMultiplier() * 1.5);

        bonuses.put("intelligence", baseBonus + RandomUtils.randomInt(1, 4));
        bonuses.put("mana", baseBonus * 8);
        bonuses.put("mana_steal", baseBonus);

        if (raritate.ordinal() >= 1) {
            bonuses.put("elemental_damage", baseBonus);
        }

        if (raritate.ordinal() >= 2) {
            bonuses.put("crit_chance", baseBonus);
        }

        return bonuses;
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
        bonuses.put("Damage", attackBonus);

        // ✨ BONUSURI NOI
        if (raritate.ordinal() >= 1) { // UNCOMMON+
            bonuses.put("hit_chance", baseBonus + 2);
        }

        if (raritate.ordinal() >= 2) { // RARE+
            bonuses.put("crit_chance", baseBonus);
            bonuses.put("damage_bonus", baseBonus / 2);
        }

        if (raritate.ordinal() >= 3) { // EPIC+
            bonuses.put("lifesteal", Math.max(1, baseBonus / 3));
        }

        if (raritate == Raritate.LEGENDARY) {
            bonuses.put("elemental_damage", baseBonus);
            // Bonus aleator special
            String[] specialBonuses = {"gold_find", "fire_resistance"};
            String special = RandomUtils.randomElement(specialBonuses);
            bonuses.put(special, baseBonus + 5);
        }

        return bonuses;
    }

    private static Map<String, Integer> calculateAgileWeaponBonuses(Raritate raritate) {
        Map<String, Integer> bonuses = new HashMap<>();
        int baseBonus = (int)(raritate.getMultiplier() * 2);
        int attackBonus = baseBonus + RandomUtils.randomInt(2, 5);

        bonuses.put("dexterity", baseBonus + RandomUtils.randomInt(1, 4));
        bonuses.put("Damage", attackBonus);

        // ✨ BONUSURI NOI
        if (raritate.ordinal() >= 1) { // UNCOMMON+
            bonuses.put("crit_chance", baseBonus + 3);
        }

        if (raritate.ordinal() >= 2) { // RARE+
            bonuses.put("hit_chance", baseBonus + 5);
            bonuses.put("dodge_chance", baseBonus / 2);
        }

        if (raritate.ordinal() >= 3) { // EPIC+
            bonuses.put("damage_bonus", baseBonus);
        }

        if (raritate == Raritate.LEGENDARY) {
            bonuses.put("elemental_damage", baseBonus);
            bonuses.put("gold_find", baseBonus + 10);
        }

        return bonuses;
    }

    private static Map<String, Integer> calculateMagicalWeaponBonuses(Raritate raritate) {
        Map<String, Integer> bonuses = new HashMap<>();
        int baseBonus = (int)(raritate.getMultiplier() * 2);
        int attackBonus = baseBonus + RandomUtils.randomInt(2, 5);

        bonuses.put("intelligence", baseBonus + RandomUtils.randomInt(1, 4));
        bonuses.put("Damage", attackBonus);

        // ✨ BONUSURI NOI
        if (raritate.ordinal() >= 1) { // UNCOMMON+
            bonuses.put("mana", baseBonus * 5);
            bonuses.put("mana_steal", Math.max(1, baseBonus / 2));
        }

        if (raritate.ordinal() >= 2) { // RARE+
            bonuses.put("elemental_damage", baseBonus);
        }

        if (raritate.ordinal() >= 3) { // EPIC+
            bonuses.put("crit_chance", baseBonus);
            // Rezistențe elementale
            bonuses.put("fire_resistance", baseBonus);
            bonuses.put("ice_resistance", baseBonus);
        }

        if (raritate == Raritate.LEGENDARY) {
            bonuses.put("damage_bonus", baseBonus);
            bonuses.put("gold_find", baseBonus + 15);
        }

        return bonuses;
    }

    private static Map<String, Integer> calculateDefensiveBonuses(Raritate raritate, double multiplier) {
        Map<String, Integer> bonuses = new HashMap<>();
        int baseBonus = (int)(raritate.getMultiplier() * 2 * multiplier);

        bonuses.put("defense", baseBonus + RandomUtils.randomInt(2, 5));
        bonuses.put("health", (int)(baseBonus * 8));

        // ✨ BONUSURI NOI
        if (raritate.ordinal() >= 1) { // UNCOMMON+
            bonuses.put("dodge_chance", Math.max(1, baseBonus / 2));
        }

        if (raritate.ordinal() >= 2) { // RARE+
            bonuses.put("damage_reduction", baseBonus);
        }

        if (raritate.ordinal() >= 3) { // EPIC+
            bonuses.put("fire_resistance", baseBonus);
            bonuses.put("ice_resistance", baseBonus);
        }

        if (raritate == Raritate.LEGENDARY) {
            bonuses.put("gold_find", baseBonus + 5);
            // Bonus special defensiv
            String[] defensiveBonuses = {"block_chance", "lifesteal"};
            String special = RandomUtils.randomElement(defensiveBonuses);
            bonuses.put(special, Math.max(2, baseBonus / 2));
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

    // ==================== JEWEL LOOT GENERATION ====================

    /**
     * Rolls for jewel drop from boss kills
     * Bosses have higher chance to drop jewels
     */
    public static Jewel rollBossJewelDrop(int bossLevel) {
        double dropChance = 0.40; // 40% chance for bosses to drop jewels

        if (RandomUtils.chancePercent(dropChance * 100)) {
            return JewelGeneratorService.generateRandomJewel(bossLevel);
        }

        return null;
    }

    /**
     * Rolls for jewel drop from regular enemies
     * Much lower chance than bosses
     */
    public static Jewel rollRegularJewelDrop(int enemyLevel) {
        double dropChance = 0.05; // 5% chance for regular enemies

        if (RandomUtils.chancePercent(dropChance * 100)) {
            return JewelGeneratorService.generateRandomJewel(enemyLevel);
        }

        return null;
    }

    /**
     * Guaranteed jewel drop for special occasions
     * (quest rewards, secret chests, achievements)
     */
    public static Jewel generateGuaranteedJewel(int level, boolean highQuality) {
        if (highQuality) {
            // Higher chance for rare/epic/legendary
            double roll = RandomUtils.randomDouble();
            Jewel.JewelRarity rarity;

            if (roll < 0.10) {
                rarity = Jewel.JewelRarity.LEGENDARY;
            } else if (roll < 0.30) {
                rarity = Jewel.JewelRarity.EPIC;
            } else if (roll < 0.60) {
                rarity = Jewel.JewelRarity.RARE;
            } else {
                rarity = Jewel.JewelRarity.UNCOMMON;
            }

            Jewel.JewelType type = rollJewelType();
            return JewelGeneratorService.generateJewel(type, rarity, level);
        }

        return JewelGeneratorService.generateRandomJewel(level);
    }

    /**
     * Generates multiple jewels for special loot (secret rooms, bonus chests)
     */
    public static List<Jewel> generateJewelTreasure(int level, int count) {
        List<Jewel> jewels = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            // Slightly higher quality for treasure finds
            double roll = RandomUtils.randomDouble();
            if (roll < 0.3) {
                jewels.add(generateGuaranteedJewel(level, true));
            } else {
                jewels.add(JewelGeneratorService.generateRandomJewel(level));
            }
        }

        return jewels;
    }

    /**
     * Helper method to roll jewel type
     */
    private static Jewel.JewelType rollJewelType() {
        double roll = RandomUtils.randomDouble();

        if (roll < 0.25) return Jewel.JewelType.CRIMSON;
        if (roll < 0.50) return Jewel.JewelType.VIRIDIAN;
        if (roll < 0.75) return Jewel.JewelType.COBALT;
        if (roll < 0.95) return Jewel.JewelType.PRISMATIC;
        return Jewel.JewelType.UNIQUE;
    }

    /**
     * Displays jewel drop notification
     */
    public static void displayJewelDrop(Jewel jewel) {
        String rarityIcon = getJewelRarityIcon(jewel.getRarity());

        System.out.println("\n💎 " + rarityIcon + " " + jewel.getType().getIcon() + " " + jewel.getName() + " " + rarityIcon);
        System.out.println("   " + jewel.getType().getDisplayName() + " | " + jewel.getRarity().getDisplayName());
        System.out.println("   🎯 Level " + jewel.getRequiredLevel() + " | " + jewel.getModifiers().size() + " modifiers");

        // Show modifiers
        String[] modLines = jewel.getModifiersDescription().split("\n");
        for (String line : modLines) {
            System.out.println("   " + line);
        }

        System.out.println("   💰 Value: " + jewel.getPrice() + " gold");
    }

    /**
     * Gets rarity icon for jewels
     */
    private static String getJewelRarityIcon(Jewel.JewelRarity rarity) {
        return switch (rarity) {
            case COMMON -> "⚪";
            case UNCOMMON -> "🟢";
            case RARE -> "🔵";
            case EPIC -> "🟣";
            case LEGENDARY -> "🟡";
        };
    }
}