package com.rpg.factory;

import com.rpg.model.characters.Erou;
import com.rpg.model.characters.classes.Ardelean;
import com.rpg.model.characters.classes.Moldovean;
import com.rpg.model.characters.classes.Oltean;
import com.rpg.model.items.BuffPotion;
import com.rpg.model.items.EnchantScroll;
import com.rpg.model.items.FlaskPiece;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.service.LootGenerator;

/**
 * Factory pentru crearea personajelor în joc.
 * VERSIUNE CORECTATĂ cu metoda createErou pentru GameService.
 */
public class CharacterFactory {

    /**
     * Enumerația pentru tipurile de clase disponibile.
     */
    public enum CharacterClass {
        WARRIOR("Warrior", "⚔️", "Războinic puternic cu viață mare și atacuri fizice devastatoare"),
        ROGUE("Rogue", "🗡️", "Tâlhar agil cu atacuri rapide și abilități de stealth"),
        WIZARD("Wizard", "🔮", "Vrăjitor inteligent cu magie puternică și abilități elementale");

        private final String className;
        private final String icon;
        private final String description;

        CharacterClass(String className, String icon, String description) {
            this.className = className;
            this.icon = icon;
            this.description = description;
        }

        public String getClassName() { return className; }
        public String getIcon() { return icon; }
        public String getDescription() { return description; }
    }

    /**
     * Creează un erou nou bazat pe clasă și nume - NECESARĂ pentru GameService.
     */
    public static Erou create(CharacterClass characterClass, String nume) {
        Erou erou = switch (characterClass) {
            case WARRIOR -> new Moldovean(nume);  // Schimbat
            case ROGUE -> new Oltean(nume);       // Schimbat
            case WIZARD -> new Ardelean(nume);    // Schimbat
        };

        // Ensure hero starts with full HP and resources
        erou.setViataCurenta(erou.getViataMaxima());
        erou.setResursaCurenta(erou.getResursaMaxima());

        return erou;
    }

    /**
     * Returnează o descriere detaliată a unei clase.
     */
    public static String getClassDescription(CharacterClass characterClass) {
        return switch (characterClass) {
            case WARRIOR ->
                    "⚔️ MOLDOVEAN (WARRIOR):\n" +
                            "• Viață maximă: Foarte mare\n" +
                            "• Resursa: Rage (se generează în luptă)\n" +
                            "• Specializare: Damage fizic și tanking\n" +
                            "• Abilități: Shield wall, berserker mode, atacuri puternice\n" +
                            "• Ideal pentru: Jucători care vor să fie în prima linie";

            case ROGUE ->
                    "🗡️ OLTEAN (ROGUE):\n" +
                            "• Viață: Moderată\n" +
                            "• Resursa: Energy (regenerare rapidă)\n" +
                            "• Specializare: Damage rapid și mobility\n" +
                            "• Abilități: Stealth, critical strikes, poison\n" +
                            "• Ideal pentru: Jucători care vor tactici rapide";

            case WIZARD ->
                    "🔮 ARDELEAN (WIZARD):\n" +
                            "• Viață: Mică (dar are mana shield)\n" +
                            "• Resursa: Mana (regenerare bazată pe intelligence)\n" +
                            "• Specializare: Damage magic la distanță\n" +
                            "• Abilități: Fireball, lightning, ice spells\n" +
                            "• Ideal pentru: Jucători care preferă magia";
        };
    }



    /**
     * ⚡ GOD MODE HERO - Pentru testing complet al jocului
     * Caracteristici:
     * - Nivel 30 cu toate abilities deblocate
     * - 50,000 gold pentru shopping spree
     * - 1,000 shards pentru enhancements
     * - Toate tipurile de consumabile
     * - Flask pieces pentru upgrades
     * - Enchant scrolls de toate tipurile
     * - Buff potions pentru testare
     * - 10 Șaorme Revival
     * - Stat points pentru training
     */
    public static Erou createGodModeHero(CharacterClass characterClass, String nume) {
        System.out.println("\n⚡ CREAREA GOD MODE HERO...");

        // Creează eroul
        Erou erou = create(characterClass, nume);

        // 🎯 LEVEL UP la 30
        System.out.println("📈 Level up to 30...");
        for (int i = 1; i < 30; i++) {
            erou.adaugaXp(erou.getXpNecesarPentruUrmatoarelNivel());
            if (erou.hasLeveledUp()) {
                erou.processLevelUp();
            }
        }

        // 💰 RESURSE MAXIME
        System.out.println("💰 Adding resources...");
        erou.setGold(50000);              // 50k gold
        erou.adaugaScrap(1000);          // 1k shards

        // 🧪 POTIONS STANDARD
        System.out.println("🧪 Adding potions...");
        erou.adaugaHealthPotions(50);     // 50 HP potions
        erou.adaugaManaPotions(50);       // 50 Mana potions

        // 🌯 ȘAORME REVIVAL
        System.out.println("🌯 Adding Șaorme Revival...");
        erou.adaugaShaormaRevival(10);    // 10 revival items

        // 🧪 FLASK PIECES - toate tipurile
        System.out.println("🧪 Adding Flask Pieces...");
        erou.addFlaskPieces(FlaskPiece.FlaskType.HEALTH, 20);
        erou.addFlaskPieces(FlaskPiece.FlaskType.MANA, 20);
        erou.addFlaskPieces(FlaskPiece.FlaskType.UNIVERSAL, 15);

        // 📜 ENCHANT SCROLLS - toate tipurile la nivel 3
        System.out.println("📜 Adding Enchant Scrolls...");
        for (EnchantScroll.EnchantType type : EnchantScroll.EnchantType.values()) {
            erou.addEnchantScroll(type, 5, 3); // 5 scrolluri nivel 3 pentru fiecare tip
        }

        // ✨ BUFF POTIONS - toate tipurile
        System.out.println("✨ Adding Buff Potions...");
        for (BuffPotion.BuffType type : BuffPotion.BuffType.values()) {
            erou.addBuffPotion(type, 10); // 10 din fiecare
        }

        // 🎯 STAT POINTS EXTRA pentru training
        System.out.println("🎯 Adding stat points...");
        // Eroul are deja stat points de la level up, dar adaugă extra
        // (Notă: Erou nu are setStatPoints public, deci primește automat la level up)

        // ⚔️ ECHIPAMENT EPIC DE START
        System.out.println("⚔️ Generating epic equipment...");
        generateStarterEquipment(erou);

        // 💚 HEAL COMPLET
        erou.setViataCurenta(erou.getViataMaxima());
        erou.setResursaCurenta(erou.getResursaMaxima());

        // 📊 AFIȘEAZĂ STATISTICI
        System.out.println("\n✅ GOD MODE HERO CREAT!");
        System.out.println("═".repeat(60));
        System.out.printf("👤 %s - Nivel %d\n", erou.getNume(), erou.getNivel());
        System.out.printf("💰 Gold: %d | 💎 Shards: %d\n", erou.getGold(), erou.getScrap());
        System.out.printf("🧪 HP Potions: %d | 💙 Mana Potions: %d\n",
                erou.getHealthPotions(), erou.getManaPotions());
        System.out.printf("🌯 Șaorme Revival: %d\n", erou.getShaormaRevival());
        System.out.printf("💪 STR: %d | 🎯 DEX: %d | 🧠 INT: %d\n",
                erou.getStrength(), erou.getDexterity(), erou.getIntelligence());
        System.out.printf("❤️ HP: %d/%d | 💙 %s: %d/%d\n",
                erou.getViata(), erou.getViataMaxima(),
                erou.getTipResursa(), erou.getResursaCurenta(), erou.getResursaMaxima());
        System.out.println("═".repeat(60));

        return erou;
    }

    /**
     * Generează echipament epic de start pentru God Mode Hero.
     */
    private static void generateStarterEquipment(Erou erou) {
        // Generează un set complet de echipament RARE/EPIC nivel 25-30
        ObiectEchipament weapon = LootGenerator.generateRandomItem(28);
        ObiectEchipament armor = LootGenerator.generateItemByType(
                ObiectEchipament.TipEchipament.ARMOR, 28, ObiectEchipament.Raritate.EPIC);
        ObiectEchipament helmet = LootGenerator.generateItemByType(
                ObiectEchipament.TipEchipament.HELMET, 28, ObiectEchipament.Raritate.RARE);
        ObiectEchipament boots = LootGenerator.generateItemByType(
                ObiectEchipament.TipEchipament.BOOTS, 28, ObiectEchipament.Raritate.RARE);
        ObiectEchipament gloves = LootGenerator.generateItemByType(
                ObiectEchipament.TipEchipament.GLOVES, 28, ObiectEchipament.Raritate.RARE);

        // Adaugă în inventar
        erou.adaugaInInventar(weapon);
        erou.adaugaInInventar(armor);
        erou.adaugaInInventar(helmet);
        erou.adaugaInInventar(boots);
        erou.adaugaInInventar(gloves);

        // Echipează automat
        erou.echipeaza(weapon);
        erou.echipeaza(armor);
        erou.echipeaza(helmet);
        erou.echipeaza(boots);
        erou.echipeaza(gloves);

        // Adaugă și alte 10 iteme aleatorii în inventar pentru variety
        for (int i = 0; i < 10; i++) {
            int randomLevel = 20 + (i * 2); // Nivele 20-38
            erou.adaugaInInventar(LootGenerator.generateRandomItem(randomLevel));
        }
    }
}