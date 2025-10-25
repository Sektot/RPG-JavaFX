package com.rpg.model.characters;

import com.rpg.model.abilities.Abilitate;
import com.rpg.model.effects.BuffStack;
import com.rpg.model.items.BuffPotion;
import com.rpg.model.items.EnchantScroll;
import com.rpg.model.items.FlaskPiece;
import com.rpg.model.items.Jewel;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.service.PotionUpgradeService;
import com.rpg.service.dto.EquipResult;
import com.rpg.utils.GameConstants;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


// Clasa aia blanao , blueprint si parinte pt celelalte clase de joc
public class Erou implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // Statistici de bază
    private final String nume;
    private int nivel;
    private int xp;
    private int xpNecesarPentruUrmatoarelNivel;

    // Resurse
    private int viata;
    private int viataMaxima;
    private int gold;
    private int scrap;

    // shaorma
    private int shaormaRevival = 1;

    // Statistici principale
    private int strength;
    private int dexterity;
    private int intelligence;
    private int statPoints;
    private int passivePoints; // For talent tree allocation
    private Set<Integer> allocatedTalentNodes = new HashSet<>();

    // 🌳 TALENT TREE BONUSES (persisted)
    private double talentCritChance = 0;
    private double talentCritMultiplier = 0;
    private double talentLifesteal = 0;
    private double talentDodge = 0;
    private double talentAttackSpeed = 0;
    private double talentDamageBonus = 0;
    private int talentFlatHP = 0;
    private int talentFlatDefense = 0;
    private double talentHPPercent = 0;
    private double talentDefensePercent = 0;

    // ⏱️ CONDITION TRACKING (transient - not saved)
    private transient long lastHitTime = 0;     // When hero was last hit
    private transient long lastKillTime = 0;    // When hero last killed an enemy
    private transient boolean isInBossFight = false;  // Currently fighting a boss

    // 🔮 CONDITIONAL BONUSES FROM TALENT TREE (transient - recalculated dynamically)
    // These are populated by checking allocated nodes against current conditions
    private transient double conditionalDamage_FullHP = 0;
    private transient double conditionalDamage_LowHP = 0;
    private transient double conditionalDefense_FullHP = 0;
    private transient double conditionalCrit_NotHitRecently = 0;

    // Statistici derivate
    private int defense;

    // Inventar și consumabile
    private final List<ObiectEchipament> inventar;
    private final List<Jewel> jewelInventory;  // Separate inventory for jewels
    private final List<com.rpg.model.inventory.ItemPocket> itemPockets;  // Custom pockets for organization
    protected List<Abilitate> abilitati;
    private int healthPotions;
    private int manaPotions;

    // Buff-uri active
    private final Map<String, BuffStack> buffuriActive;

    // 🆕 RUN ITEM MODIFIERS (pentru dungeon roguelike)
    private transient double runItemDamageMultiplier = 1.0;
    private transient int runItemFlatDamage = 0;
    private transient double runItemDefenseMultiplier = 1.0;
    private transient int runItemFlatDefense = 0;
    private transient double runItemDodgeBonus = 0.0;
    private transient double runItemLifesteal = 0.0;
    private transient int runItemRegenPerTurn = 0;
    private transient double runItemGoldMultiplier = 1.0;
    private transient double runItemCritBonus = 0.0;
    private transient Map<String, Integer> runItemElementalDamage = new HashMap<>();

    // 🆕 DUNGEON META-PROGRESSION (persists between dungeon runs)
    private com.rpg.dungeon.model.DungeonProgression dungeonProgression;

    // RESURSE PENTRU CLASE SPECIFICE - PUBLIC pentru accesibilitate
    public int resursaCurenta = 0;
    public int resursaMaxima = 100;
    public String tipResursa = "Mana";

    // Resurse specifice claselor
    private int mana = 0;
    private int rage = 0;
    private int rageMaxim = 100;
    private int energy = 100;
    private int energyMaxim = 100;

    // Echipament
    private Map<String, ObiectEchipament> echipat;

    // Slot-urile noi
    public static final String MAIN_HAND = "MAIN_HAND";
    public static final String OFF_HAND = "OFF_HAND";
    public static final String ARMOR = "ARMOR";
    public static final String HELMET = "HELMET";
    public static final String GLOVES = "GLOVES";
    public static final String BOOTS = "BOOTS";
    public static final String RING1 = "RING1";
    public static final String RING2 = "RING2";
    public static final String NECKLACE = "NECKLACE";

//baza pentru a construi orice erou
    public Erou(String nume, int strength, int dexterity, int intelligence) {
        this.nume = nume;
        this.nivel = 1;
        this.xp = 0;
        this.xpNecesarPentruUrmatoarelNivel = GameConstants.BASE_XP_REQUIRED;
        this.strength = strength;
        this.dexterity = dexterity;
        this.intelligence = intelligence;
        this.statPoints = 100;

        calculateDerivedStats();
        this.viata = this.viataMaxima;
        this.gold = GameConstants.INITIAL_GOLD;
        this.scrap = 0;
        this.shaormaRevival = 1; // Începe cu 0 șaorme
        this.healthPotions = GameConstants.INITIAL_POTIONS;
        this.manaPotions = GameConstants.INITIAL_MANA_POTIONS;

        this.inventar = new ArrayList<>();
        this.jewelInventory = new ArrayList<>();  // Initialize jewel inventory
        this.itemPockets = new ArrayList<>();  // Initialize item pockets
        this.abilitati = new ArrayList<>();
        this.buffuriActive = new HashMap<>();
        this.echipat = new HashMap<>();

        // Inițializează resursa curentă cu maxima
        this.resursaCurenta = this.resursaMaxima;
    }

    private void calculateDerivedStats() {
        // Base calculations
        int baseHP = GameConstants.BASE_HEALTH +
                (strength * GameConstants.HEALTH_PER_STRENGTH) +
                (nivel * GameConstants.HEALTH_PER_LEVEL);

        // Apply talent tree flat HP bonus
        baseHP += talentFlatHP;

        // Apply talent tree % HP bonus
        double totalHPPercent = talentHPPercent / 100.0;

        // Apply dungeon progression HP bonus (permanent upgrade)
        if (getDungeonProgression() != null) {
            totalHPPercent += getDungeonProgression().getMaxHpBonusPercent();
        }

        this.viataMaxima = (int)(baseHP * (1.0 + totalHPPercent));

        // Mana calculation
        int manaMaxima = GameConstants.BASE_MANA +
                (intelligence * GameConstants.MANA_PER_INTELLIGENCE) +
                (nivel * GameConstants.MANA_PER_LEVEL);

        // Base defense
        int baseDefense = GameConstants.BASE_DEFENSE + (strength / 3) + (dexterity / 4);

        // Apply talent tree flat defense bonus
        baseDefense += talentFlatDefense;

        // Apply talent tree % defense bonus
        this.defense = (int)(baseDefense * (1.0 + talentDefensePercent / 100.0));

        if (this.viata > this.viataMaxima) this.viata = this.viataMaxima;
        if (this.mana > manaMaxima) this.mana = manaMaxima;
    }

    // ================== SISTEMUL DE REVIVAL CU ȘAORMA! ==================

//metoda pt a adauga shaorme
    public void adaugaShaormaRevival(int cantitate) {
        this.shaormaRevival += cantitate;
        if (cantitate > 0) {
            System.out.printf("🌯 Ai primit %d Șaorma de Revival! (Total: %d)\n",
                    cantitate, shaormaRevival);
        }
    }

// metoda pentru a folosi sistem de revival
    public boolean folosesteShaormaRevival() {
       //aici verifica daca am shaorme daca nu am da false
        if (shaormaRevival <= 0) {
            return false; // Nu are șaorme
        }
//aici scade nr de shaorme daca am
        shaormaRevival--;

        // ress user cu 50% viață și resurse
        int revivedHealth = viataMaxima / 2;
        int revivedResources = resursaMaxima / 2;

        this.viata = revivedHealth;
        this.resursaCurenta = revivedResources;


        //printuri pt display
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("   🌯✨ ȘAORMA DE REVIVAL ACTIVATĂ! ✨🌯");
        System.out.println("  ═══════════════════════════════════════");
        System.out.printf("💚 %s s-a reîntors din tărâmul umbrelor!\n", nume);
        System.out.printf("❤️  Viață restaurată: %d/%d (50%%)\n", viata, viataMaxima);
        System.out.printf("🔋 %s restaurate: %d/%d (50%%)\n", tipResursa, resursaCurenta, resursaMaxima);
        System.out.printf("🌯 Șaorme rămase: %d\n", shaormaRevival);
        System.out.println("\n✨ Gustul delicațiunii magice îți dă putere să continui!");
        System.out.println("💪 Reîntoarce-te în luptă, erou!");

        return true;
    }

//verifica daca are shaorme se foloseste in gameservice pt handling death
    public boolean areShaormaRevival() {
        return shaormaRevival > 0;
    }

    // ================== DUNGEON TICKET/TOKEN SYSTEM ==================
    // These methods delegate to DungeonProgression to keep tokens in one place

    public void adaugaDungeonTickets(int cantitate) {
        getDungeonProgression().addTokens(cantitate);
        if (cantitate > 0) {
            System.out.printf("🎫 Ai primit %d Dungeon Tokens! (Total: %d)\n",
                    cantitate, getDungeonProgression().getDungeonTokens());
        }
    }

    public int getDungeonTickets() {
        return getDungeonProgression().getDungeonTokens();
    }

    public boolean areDungeonTickets() {
        return getDungeonProgression().getDungeonTokens() > 0;
    }

    public boolean folosesteDungeonTicket() {
        if (getDungeonProgression().getDungeonTokens() <= 0) {
            return false;
        }
        getDungeonProgression().addTokens(-1);
        return true;
    }


//============================
//    public int getXpNecesar() {
//        return xpNecesarPentruUrmatoarelNivel;
//    }

//============================================
    //metode pt character factory de unde seteaza atributele cand se creeaza caracterul
    public void setXp(int xp) {
        this.xp = Math.max(0, xp);
    }

    public void setGold(int gold) {
        this.gold = Math.max(0, gold);
    }

    public void setViataCurenta(int viata) {
        this.viata = Math.max(0, Math.min(viata, viataMaxima));
    }

    public void setResursaCurenta(int resursa) {
        this.resursaCurenta = Math.max(0, Math.min(resursa, resursaMaxima));
        switch (tipResursa.toLowerCase()) {
            case "rage" -> this.rage = this.resursaCurenta;
            case "energy" -> this.energy = this.resursaCurenta;
            default -> this.mana = this.resursaCurenta;
        }
    }

    //metoda cand mori doar display de linii nimic fancy
    public void afiseazaMeniuMoarte() {
        //BattleOneLiners.displayDeathOneLiner(this);
        System.out.println("\n💀 " + "═".repeat(60));
        System.out.println("        ⚰️  EROUL A CĂZUT ÎN LUPTĂ ⚰️");
        System.out.println("═".repeat(60));
        System.out.printf("💀 %s (Nivel %d) a fost învins...\n", nume, nivel);
        System.out.println();

//        // ✨ ADAUGĂ AICI
//        String deathMsg = getDeathMessage();
//        if (deathMsg != null && !deathMsg.isEmpty()) {
//            System.out.println(deathMsg);
//            System.out.println();
//        }

        // Afișează statisticile finale
        System.out.println("📊 Progresul tău până acum:");
        System.out.printf("🏆 Nivel atins: %d\n", nivel);
        System.out.printf("⭐ XP acumulat: %d\n", xp);
        System.out.printf("💰 Gold: %d | 🔧 Scrap: %d | 🌯 Șaorme Revival: %d\n",
                gold, scrap, shaormaRevival);

        System.out.println("\n⚱️ În luptă mori, dar spiritul tău persistă...");

        if (shaormaRevival > 0) {
            System.out.printf("\n🌯 ✨ AI %d ȘAORMA DE REVIVAL DISPONIBILĂ! ✨\n", shaormaRevival);
            System.out.println("🍖 Această delicatesă magică poate să îți redea viața!");
        } else {
            System.out.println("\n🌯 Nu ai Șaorme de Revival...");
            System.out.println("💡 Șaormele de Revival cad doar din Boss-i!");
        }

        System.out.println("\n" + "═".repeat(60));
    }

    // Metode pt shop

    public boolean scadeGold(int amount) {
        if (gold >= amount) {
            gold -= amount;
            return true;
        }
        return false;
    }

    public void adaugaHealthPotions(int amount) {
        this.healthPotions += Math.max(0, amount);
    }

    public void adaugaManaPotions(int amount) {
        this.manaPotions += Math.max(0, amount);
    }

//    public void vindecaComplet() {
//        this.viata = this.viataMaxima;
//        System.out.println("💚 " + nume + " a fost vindecat complet!");
//    }


    // asta e pentru regem de mana folosita odata mai jos
    public void regenereazaResursa(int amount) {
        this.resursaCurenta = Math.min(resursaMaxima, resursaCurenta + amount);
        switch (tipResursa.toLowerCase()) {
            case "rage" -> this.rage = this.resursaCurenta;
            case "energy" -> this.energy = this.resursaCurenta;
            default -> this.mana = this.resursaCurenta;
        }
    }

    //================================
    // Metode pt trainer is apelate in trainer service

    public int getStatPoints() {
        return statPoints;
    }

    public boolean decreaseStatPoints(int amount) {
        if (statPoints >= amount) {
            statPoints -= amount;
            return true;
        }
        return false;
    }

    public void increaseStatPoints(int amount) {
        this.statPoints += amount;
    }

    public int getPassivePoints() {
        return passivePoints;
    }

    public void increasePassivePoints(int amount) {
        this.passivePoints += amount;
    }

    /**
     * Migrates old stat points to passive points (for talent tree)
     * Call this once after loading old saves
     */
    public void migrateStatPointsToPassive() {
        if (statPoints > 0) {
            passivePoints += statPoints;
            statPoints = 0;
            System.out.println("📊 Migrated " + statPoints + " stat points to passive points for talent tree");
        }
    }

    public boolean decreasePassivePoints(int amount) {
        if (passivePoints >= amount) {
            passivePoints -= amount;
            return true;
        }
        return false;
    }

    public Set<Integer> getAllocatedTalentNodes() {
        return allocatedTalentNodes;
    }

    public void setAllocatedTalentNodes(Set<Integer> nodes) {
        this.allocatedTalentNodes = nodes;
    }

    // Talent tree bonus getters
    public double getTalentCritChance() { return talentCritChance; }
    public double getTalentCritMultiplier() { return talentCritMultiplier; }
    public double getTalentLifesteal() { return talentLifesteal; }
    public double getTalentDodge() { return talentDodge; }
    public double getTalentAttackSpeed() { return talentAttackSpeed; }
    public double getTalentDamageBonus() { return talentDamageBonus; }
    public int getTalentFlatHP() { return talentFlatHP; }
    public int getTalentFlatDefense() { return talentFlatDefense; }
    public double getTalentHPPercent() { return talentHPPercent; }
    public double getTalentDefensePercent() { return talentDefensePercent; }

    // Talent tree bonus modifiers (for allocation/deallocation)
    public void modifyTalentCritChance(double amount) { this.talentCritChance += amount; }
    public void modifyTalentCritMultiplier(double amount) { this.talentCritMultiplier += amount; }
    public void modifyTalentLifesteal(double amount) { this.talentLifesteal += amount; }
    public void modifyTalentDodge(double amount) { this.talentDodge += amount; }
    public void modifyTalentAttackSpeed(double amount) { this.talentAttackSpeed += amount; }
    public void modifyTalentDamageBonus(double amount) { this.talentDamageBonus += amount; }
    public void modifyTalentFlatHP(int amount) {
        this.talentFlatHP += amount;
        calculateDerivedStats(); // Recalculate max HP
    }
    public void modifyTalentFlatDefense(int amount) {
        this.talentFlatDefense += amount;
        calculateDerivedStats(); // Recalculate defense
    }
    public void modifyTalentHPPercent(double amount) {
        this.talentHPPercent += amount;
        calculateDerivedStats(); // Recalculate max HP
    }
    public void modifyTalentDefensePercent(double amount) {
        this.talentDefensePercent += amount;
        calculateDerivedStats(); // Recalculate defense
    }

    // Condition tracking getters/setters
    public long getLastHitTime() { return lastHitTime; }
    public void setLastHitTime(long time) { this.lastHitTime = time; }
    public void recordHit() { this.lastHitTime = System.currentTimeMillis(); }

    public long getLastKillTime() { return lastKillTime; }
    public void setLastKillTime(long time) { this.lastKillTime = time; }
    public void recordKill() { this.lastKillTime = System.currentTimeMillis(); }

    public boolean isInBossFight() { return isInBossFight; }
    public void setInBossFight(boolean inBossFight) { this.isInBossFight = inBossFight; }

    // Check if conditions are met (for conditional bonuses)
    public boolean isAtFullHP() {
        return viata >= viataMaxima;
    }

    public boolean isAtLowHP() {
        return viata < (viataMaxima * 0.35);
    }

    public boolean wasHitRecently() {
        return (System.currentTimeMillis() - lastHitTime) < 4000; // 4 seconds
    }

    public boolean wasNotHitRecently() {
        return !wasHitRecently() || lastHitTime == 0;
    }

    public boolean killedRecently() {
        return (System.currentTimeMillis() - lastKillTime) < 4000; // 4 seconds
    }

    // Setters for conditional bonuses (called by TalentTreeController when allocating nodes)
    public void setConditionalDamage_FullHP(double value) { this.conditionalDamage_FullHP = value; }
    public void setConditionalDamage_LowHP(double value) { this.conditionalDamage_LowHP = value; }
    public void setConditionalDefense_FullHP(double value) { this.conditionalDefense_FullHP = value; }
    public void setConditionalCrit_NotHitRecently(double value) { this.conditionalCrit_NotHitRecently = value; }

    /**
     * Get conditional damage bonus based on current state
     */
    public double getConditionalDamageBonus() {
        double bonus = 0;
        if (isAtFullHP() && conditionalDamage_FullHP > 0) {
            bonus += conditionalDamage_FullHP;
        }
        if (isAtLowHP() && conditionalDamage_LowHP > 0) {
            bonus += conditionalDamage_LowHP;
        }
        return bonus;
    }

    /**
     * Get conditional defense bonus based on current state
     */
    public double getConditionalDefenseBonus() {
        double bonus = 0;
        if (isAtFullHP() && conditionalDefense_FullHP > 0) {
            bonus += conditionalDefense_FullHP;
        }
        return bonus;
    }

    /**
     * Get conditional crit bonus based on current state
     */
    public double getConditionalCritBonus() {
        double bonus = 0;
        if (wasNotHitRecently() && conditionalCrit_NotHitRecently > 0) {
            bonus += conditionalCrit_NotHitRecently;
        }
        return bonus;
    }

    public void increaseStrength(int amount) {
        this.strength += amount;
        calculateDerivedStats();
        System.out.printf("💪 Strength: %d (+%d)\n", strength, amount);
    }

    public void increaseDexterity(int amount) {
        this.dexterity += amount;
        calculateDerivedStats();
        System.out.printf("🎯 Dexterity: %d (+%d)\n", dexterity, amount);
    }

    public void increaseIntelligence(int amount) {
        this.intelligence += amount;
        calculateDerivedStats();
        System.out.printf("🧠 Intelligence: %d (+%d)\n", intelligence, amount);
    }


    // asta e pt upgrade de echipament
    public boolean scadeScrap(int amount) {
        if (scrap >= amount) {
            scrap -= amount;
            return true;
        }
        return false;
    }

    //=================
    //Metode pt echipament

    //ia o mapa de echipamente si returneaza un hashmap cu cele echipate
    //folosit in battle pt a determina dmg si staturi
    public Map<String, ObiectEchipament> getEchipat() {
        return new HashMap<>(echipat);
    }

    //metoda pt echipare, verifica nivelul eroului si nivelul echipamentului si daca obiectull nu e null
    public void echipeaza(ObiectEchipament item) {
        if (item == null || nivel < item.getNivelNecesar()) {
            return;
        }

        // extrage tipul itemului ca string pentru key in echipat map
        //double check sa nu fie null
        //dar chiar daca e il returneaza ca default desi nu ecazu
        String tipItem = item.getTip() != null ? item.getTip().toString() : "DEFAULT";
        //asta se foloseste ca sa dezechipeze un item automat din
        //slotul ala in care ai echipat acum un item
        if (echipat.containsKey(tipItem)) {
            echipat.get(tipItem).setEquipped(false);
        }

        //aici seteaza noul item ca true
        item.setEquipped(true);
       //adauga sau da replace la item cu valorile sale
        echipat.put(tipItem, item);
        //calculeaza staturile in functie de item
        calculateDerivedStats();
        System.out.println("✅ " + item.getNume() + " a fost echipat!");
    }

    //in esenta se fac aceleasi chekuri ca si la cel de echipeaza
    public void dezechipeaza(ObiectEchipament item) {
        if (item != null && item.isEquipped()) {
            String tipItem = item.getTip() != null ? item.getTip().toString() : "DEFAULT";
            //da remove la tipul de item
            echipat.remove(tipItem);
           //seteaza echipat la false
            item.setEquipped(false);
            //calculeaza din nou staturile fara item
            calculateDerivedStats();
            System.out.println("❌ " + item.getNume() + " a fost dezechipat!");
        }
    }

// metoda folosita la vinzare sau la disenchant
    public boolean removeFromInventar(ObiectEchipament item) {
       //da remove la item din inventar
        boolean removed = inventar.remove(item);
        //aici verifica daca e echipat si ii da remove direct de acolo
        if (removed && item.isEquipped()) {
            dezechipeaza(item);
        }
        // returneaza bool, daca e removed=true, othewise false
        return removed;
    }



    // getteri de staturi====================
    public int getStrengthTotal() {
        return strength + getEquipmentBonus("strength");
    }

    public int getDexterityTotal() {
        return dexterity + getEquipmentBonus("dexterity");
    }

    public int getIntelligenceTotal() {
        return intelligence + getEquipmentBonus("intelligence");
    }

//    // un getter pt bonusurile de la echipamente
//    private int getEquipmentBonus(String stat) {
//        int bonus = 0;
//        for (ObiectEchipament item : echipat.values()) {
//            if (item != null && item.getBonuses() != null && item.getBonuses().containsKey(stat)) {
//                bonus += item.getBonuses().get(stat);
//            }
//        }
//        //returneaza un int cu bonusurile
//        return bonus;
//    }

    // Getteri de resurse mana si alea alea=====================

    public int getResursaCurenta() {
        return resursaCurenta;
    }

    public int getResursaMaxima() {
        return resursaMaxima;
    }

    public String getTipResursa() {
        return tipResursa;
    }

    public void setTipResursa(String tipResursa) {
        this.tipResursa = tipResursa;
    }

    public boolean areResursaSuficienta(int cost) {
        return resursaCurenta >= cost;
    }

    public boolean areResursaSuficienta(Abilitate ability) {
        return areResursaSuficienta(ability.getCostMana());
    }

    public boolean consumaResursa(int cost) {
        if (!areResursaSuficienta(cost)) return false;
        resursaCurenta = Math.max(0, resursaCurenta - cost);
        return true;
    }

    public boolean consumaResursa(Abilitate ability) {
        return consumaResursa(ability.getCostMana());
    }

    public void regenResursa(int amount) {
        resursaCurenta = Math.min(resursaMaxima, resursaCurenta + amount);
    }

    // ================== METODE PENTRU ABILITĂȚI ==================

//Metoda pt initializare de abilitati care e suprscrisa atunci cand alegi o clasa
    public void initializeazaAbilitati() {
        abilitati = new ArrayList<>();
    }

// la fel ca si cea de sus doar ca cu abilitatile obtinute la un nivel
    public Abilitate abilitateSpecialaNivel(int nivel) {
        return null;
    }

//metoda de regen, suprascrisa daca e moldo sau oltean care folosesc alte resurse
    public int regenNormal() {
        return GameConstants.RESOURCE_REGEN_PER_TURN;
    }

    //metoda folosita pentru a da check daca sunt abilitati noi la lvl up si le adauga
    public void adaugaAbilitate(Abilitate abilitate) {
        if (abilitate != null) {
            abilitati.add(abilitate);
        }
    }

    // Metode de utilizare a potiunilor

   // metoda de folosire la health potion
    public boolean useHealthPotion() {
        if (healthPotions > 0 && viata < viataMaxima) {
            healthPotions--;
            int healAmount = getHealthPotionHealing(); // Folosește tier-ul actual
            vindeca(healAmount);

            System.out.printf("🧪 %s Berice folosită!\n", healthPotionTier.getIcon());
            return true;
        }
        return false;
    }

// metoda de folosire a manapotion
// În Erou.java, adaugă această metodă dacă lipsește:
public boolean useManaPotion() {
    if (manaPotions > 0 && resursaCurenta < resursaMaxima) {
        manaPotions--;
        int restoreAmount = getManaPotionRestore();
        regenereazaResursa(restoreAmount);

        System.out.printf("💙 %s Energizant Profi folosit!\\n", manaPotionTier.getIcon());
        System.out.printf("🔋 +%d %s! (%d/%d)\\n",
                restoreAmount, tipResursa, resursaCurenta, resursaMaxima);
        return true;
    }
    return false;
}


// metoda de folosire a buff potions
    public boolean useBuffPotion(BuffPotion.BuffType type) {
        int available = buffPotions.getOrDefault(type, 0);
        if (available <= 0) {
            return false;
        }

        // Consumă potion-ul
        buffPotions.put(type, available - 1);

        // Obține bonusurile poțiunii
        Map<String, Double> bonuses = type.getBonuses();

        // Aplică buff-ul - durează pentru 1 luptă
        // Folosim prefix "BuffPotion_" pentru a diferenția de alte buff-uri
        aplicaBuff("BuffPotion_" + type.name(), bonuses, 1);

        System.out.printf("🧪 Ai folosit %s %s!\n", type.getIcon(), type.getDisplayName());
        System.out.println("✨ Efecte active:");

        // Afișează efectele
        bonuses.forEach((stat, value) -> {
            String displayValue = value > 0 ? "+" + value : String.valueOf(value);
            System.out.printf("   • %s %s\n", displayValue, formatStatName(stat));
        });

        return true;
    }

  // helper pt formatarea numelor statisticilor
    private String formatStatName(String stat) {
        return switch (stat.toLowerCase()) {
            case "strength" -> "💪 Strength";
            case "dexterity" -> "🎯 Dexterity";
            case "intelligence" -> "🧠 Intelligence";
            case "damage_bonus" -> "⚔️ Damage Bonus";
            case "defense" -> "🛡️ Defense";
            case "crit_chance" -> "⚡ Critical Chance";
            case "dodge_chance" -> "💨 Dodge Chance";
            case "hit_chance" -> "🎯 Hit Chance";
            default -> stat;
        };
    }


    // ================== METODE DE ȘANSE ==================

    public double getHitChance() {
        double baseHitChance = GameConstants.BASE_HIT_CHANCE;
        double dexBonus = getDexterityTotal() * GameConstants.HIT_CHANCE_PER_DEX;
        double levelBonus = nivel * GameConstants.HIT_CHANCE_PER_LEVEL;
        double equipmentBonus = getEquipmentBonus("hit_chance"); // ✅ ADAUGĂ echipament
        return Math.min(95.0, baseHitChance + dexBonus + levelBonus + equipmentBonus);
    }

    public double getCritChanceTotal() {
        double baseCritChance = GameConstants.BASE_CRIT_CHANCE;
        double dexBonus = getDexterityTotal() * GameConstants.CRIT_CHANCE_PER_DEX;
        double equipmentBonus = getEquipmentBonus("crit_chance"); // ✅ ADAUGĂ echipament
        double runItemBonus = runItemCritBonus * 100; // Convert 0.15 -> 15%
        double talentBonus = talentCritChance; // 🌳 Talent tree bonus
        double conditionalBonus = getConditionalCritBonus(); // 🔮 Conditional bonus
        return Math.min(95.0, baseCritChance + dexBonus + equipmentBonus + runItemBonus + talentBonus + conditionalBonus);
    }

    public double getDodgeChanceTotal() {
        double baseDodgeChance = GameConstants.BASE_DODGE_CHANCE;
        double dexBonus = getDexterityTotal() * GameConstants.DODGE_CHANCE_PER_DEX;
        double equipmentBonus = getEquipmentBonus("dodge_chance"); // ✅ ADAUGĂ echipament
        double runItemBonus = runItemDodgeBonus * 100; // Convert 0.15 -> 15%
        double talentBonus = talentDodge; // 🌳 Talent tree bonus
        return Math.min(75.0, baseDodgeChance + dexBonus + equipmentBonus + runItemBonus + talentBonus);
    }

    public double getCritMultiplierTotal() {
        double baseMultiplier = GameConstants.CRIT_DAMAGE_MULTIPLIER; // Usually 2.0 (200%)
        double talentBonus = talentCritMultiplier / 100.0; // Convert 50% -> 0.5
        return baseMultiplier + talentBonus; // e.g., 2.0 + 0.5 = 2.5x damage
    }

    public double getLifestealTotal() {
        double runItemBonus = this.runItemLifesteal; // Already in decimal form (0.15 = 15%)
        double talentBonus = this.talentLifesteal / 100.0; // Convert 5% -> 0.05
        return runItemBonus + talentBonus;
    }

    public int getDefenseTotal() {
        return defense + getEquipmentBonus("defense");
    }

    // ================== METODE DE ACTUALIZARE STĂRI ==================

    public void actualizeazaStari() {
        // Procesează buff-urile pentru heal
        for (BuffStack buff : buffuriActive.values()) {
            if (buff.isActive()) {
                Map<String,Double> mods = buff.getAllModifiers();
                if (mods.containsKey("heal_per_turn")) {
                    int healAmount = mods.get("heal_per_turn").intValue();
                    vindeca(healAmount);
                    System.out.printf(
                            "🌿 Regenerare: %s primește +%d HP din Nature buff%n",
                            getNume(), healAmount
                    );
                }
            }
        }


        // Codul existent pentru aplicarea efectelor
        aplicaEfecteleBuffurilor();
        regenResursa(regenNormal());
        if (viata < viataMaxima) {
            vindeca(GameConstants.NATURAL_HEALTH_REGEN);
        }
    }



// metoda de aplicare buffuri

    public void aplicaBuff(String nume, Map<String, Double> modificatori, int durata) {
        if (modificatori == null || modificatori.isEmpty()) {
            return;
        }
        if (buffuriActive.containsKey(nume)) {
            buffuriActive.get(nume).addStack(durata);
        } else {
            buffuriActive.put(nume, new BuffStack(modificatori, durata, GameConstants.MAX_BUFF_STACKS));
        }
        System.out.printf("✨ Buff %s aplicat pentru %d ture!%n", nume, durata);
    }



// proceseaza si expira buffurile active
    // trece prin toate buffurile
    //da decrease la duration pt fiecare
    //da check daca e expirat
    //mesaj de display
    //da remove la buff
    public void aplicaEfecteleBuffurilor() {
        buffuriActive.entrySet().removeIf(entry -> {
            BuffStack buff = entry.getValue();
            buff.decreaseDuration();
            if (!buff.isActive()) {
                System.out.println("⏰ Buff " + entry.getKey() + " a expirat pentru " + nume);
                return true;
            }
            return false;
        });
    }

    //self explenatory se adauga xp, apelata dupa procesarea luptei
    public void adaugaXp(int xp) {
        this.xp += xp;
        System.out.printf("✨ +%d XP (Total: %d/%d)\\n",
                xp, this.xp, xpNecesarPentruUrmatoarelNivel);

        // 🆕 TRIGGER LEVEL-UP AUTOMAT
        int oldLevel = this.nivel;
        int newLevel = processLevelUp();

        if (newLevel > oldLevel) {
            System.out.printf("🎉 MULTIPLE LEVEL UP! %d -> %d\\n", oldLevel, newLevel);
            System.out.println("════════════════════════════════════════");
            System.out.println("    🌟 CONGRATULATIONS! 🌟");
            System.out.println("════════════════════════════════════════");
            System.out.printf("🎯 Noul tău nivel: %d\\n", newLevel);
            System.out.printf("📊 Stat Points disponibili: %d\\n", statPoints);
            System.out.printf("❤️  Viață maximă: %d\\n", viataMaxima);
            System.out.println("💡 Vizitează Trainer-ul pentru a upgrala stats!");
            System.out.println("════════════════════════════════════════");
        }
    }

    // verificare daca xp = xp pt lvl up
    public boolean hasLeveledUp() {
        return xp >= xpNecesarPentruUrmatoarelNivel;
    }

    // procesarea la lvl up
// procesarea la lvl up
    public int processLevelUp() {
        if (!hasLeveledUp()) return nivel;

        int levelsGained = 0;

        while (xp >= xpNecesarPentruUrmatoarelNivel) {
            xp -= xpNecesarPentruUrmatoarelNivel;
            nivel++;
            levelsGained++;

            xpNecesarPentruUrmatoarelNivel = (int)(GameConstants.BASE_XP_REQUIRED *
                    Math.pow(GameConstants.XP_MULTIPLIER, nivel - 1));

            // Passive points for talent tree (replaces old stat point system)
            int passivePointsEarned = 3; // Base passive points (was STAT_POINTS_PER_LEVEL)
            if (nivel % 5 == 0) passivePointsEarned += 2; // Bonus at multiples of 5
            passivePoints += passivePointsEarned;

            // 🔇 QUIET LEVEL UP - doar essentials
            System.out.printf("📈 Level %d → %d (+%d passive points)\\n",
                    nivel - 1, nivel, passivePointsEarned);

            int oldViataMax = viataMaxima;
            calculateDerivedStats();

            int viataBonus = viataMaxima - oldViataMax;
            viata += viataBonus; // Bonus HP la level-up

            // Abilități noi
            Abilitate nouaAbilitate = abilitateSpecialaNivel(nivel);
            if (nouaAbilitate != null) {
                adaugaAbilitate(nouaAbilitate);
                System.out.println("🎉 Abilitate nouă: " + nouaAbilitate.getNume());
            }
        }

        return nivel;
    }


    public void adaugaGold(int gold) { this.gold += gold; }
   // public void decreaseGold(int amount) { this.gold = Math.max(0, this.gold - amount); }
    public void adaugaScrap(int scrap) { this.scrap += scrap; }
   // public void decreaseScrap(int amount) { this.scrap = Math.max(0, this.scrap - amount); }


    //ii clar ce face ori din inamici ori din shop
    public void adaugaInInventar(ObiectEchipament obiect) {
        if (obiect != null) {
            inventar.add(obiect);
        }
    }

    // ==================== JEWEL INVENTORY MANAGEMENT ====================

    /**
     * Adds a jewel to the jewel inventory
     */
    public void addJewel(Jewel jewel) {
        if (jewel != null) {
            jewelInventory.add(jewel);
            System.out.printf("💎 Gained jewel: %s\n", jewel.getName());
        }
    }

    /**
     * Removes a jewel from the jewel inventory
     */
    public boolean removeJewel(Jewel jewel) {
        return jewelInventory.remove(jewel);
    }

    /**
     * Gets all jewels in inventory
     */
    public List<Jewel> getJewelInventory() {
        return new ArrayList<>(jewelInventory);
    }

    /**
     * Gets all unsocketed jewels (available for insertion)
     */
    public List<Jewel> getAvailableJewels() {
        return jewelInventory.stream()
                .filter(jewel -> !jewel.isSocketed())
                .toList();
    }

    /**
     * Gets all socketed jewels
     */
    public List<Jewel> getSocketedJewels() {
        return jewelInventory.stream()
                .filter(Jewel::isSocketed)
                .toList();
    }

    /**
     * Finds a jewel by name
     */
    public Jewel findJewelByName(String name) {
        return jewelInventory.stream()
                .filter(jewel -> jewel.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets the total count of jewels in inventory
     */
    public int getJewelCount() {
        return jewelInventory.size();
    }

    /**
     * Sells a jewel for gold
     */
    public boolean sellJewel(Jewel jewel) {
        if (jewel == null || jewel.isSocketed()) {
            return false; // Cannot sell socketed jewels
        }

        if (jewelInventory.remove(jewel)) {
            int sellPrice = (int)(jewel.getPrice() * 0.7); // 70% of purchase price
            gold += sellPrice;
            System.out.printf("💰 Sold %s for %d gold!\n", jewel.getName(), sellPrice);
            return true;
        }

        return false;
    }

    // vindeca eroul cu set amount, ori in potions ori din buffuri de regen
    //e folosita si la inamici
    public void vindeca(int amount) {
        int viataVindecata = Math.min(amount, viataMaxima - viata);
        viata += viataVindecata;
        if (viataVindecata > 0) {
            System.out.printf("💚 %s se vindecă cu %d HP! (%d/%d)\n",
                    nume, viataVindecata, viata, viataMaxima);
        }
    }

    // metoda pt calcul de dmgin dependenta de defense cu un minim de 0
    public void iaDamage(int damage) {
        int finalDamage = Math.max(0, damage - getDefenseTotal());
       // int oldViata = viata;
        viata = Math.max(0, viata - finalDamage);

        // DEBUGGING pentru a vedea exact ce se întâmplă
      //  System.out.printf("DEBUG: %s primește %d damage! HP: %d -> %d\n",
              //  nume, finalDamage, oldViata, viata);

        if (viata == 0) {
            System.out.println("🔥 EROUL A MURIT COMPLET! viata = 0");
        }
    }

    // un bool pt a verifica daca e in viata
    public boolean esteViu() {
        boolean viu = viata > 0;
        if (!viu) {
          //  System.out.println("DEBUG: esteViu() = false, viata = " + viata);
        }
        return viu;
    }


    //====================================
    // determinarea de tieruri la potions, mapa de flask pieces, buff potions si scrolluri
    // tieruri de flask potions
    private PotionUpgradeService.PotionTier healthPotionTier = PotionUpgradeService.PotionTier.BASIC;
    private PotionUpgradeService.PotionTier manaPotionTier = PotionUpgradeService.PotionTier.BASIC;

    // flask pieces
    private Map<FlaskPiece.FlaskType, Integer> flaskPieces = new HashMap<>();

    // buff pot in inventar
    private Map<BuffPotion.BuffType, Integer> buffPotions = new HashMap<>();

    // ench scroll in inventar
    private Map<EnchantScroll.EnchantType, EnchantScroll> enchantScrolls = new HashMap<>();


    // =========================
    // Metode pt potion upgrades

 //getter pt tier la hp potion
    public PotionUpgradeService.PotionTier getHealthPotionTier() {
        return healthPotionTier;
    }

// setter pt hp potion
    public void setHealthPotionTier(PotionUpgradeService.PotionTier tier) {
        this.healthPotionTier = tier;
    }

 // getter mana potion
    public PotionUpgradeService.PotionTier getManaPotionTier() {
        return manaPotionTier;
    }

// setter la mana potion
    public void setManaPotionTier(PotionUpgradeService.PotionTier tier) {
        this.manaPotionTier = tier;
    }

    // upgrade methods
    public void upgradeHealthPotionTier() {
        this.healthPotionTier = healthPotionTier.getNext();
    }

    public void upgradeManaPotionTier() {
        this.manaPotionTier = manaPotionTier.getNext();
    }

// calcul de heal pe baza la tier
    public int getHealthPotionHealing() {
        int baseHealing = GameConstants.HEALTH_POTION_HEAL;
        return (int)(baseHealing * healthPotionTier.getMultiplier()) + healthPotionTier.getBaseValue();
    }

// acelasi lucru da pt mana potion
    public int getManaPotionRestore() {
        int baseRestore = GameConstants.MANA_POTION_RESTORE;
        return (int)(baseRestore * manaPotionTier.getMultiplier()) + manaPotionTier.getBaseValue();
    }

    //METODE DE FL. PIECES===================

//metoda de a adauga flask sharduri folosita ca loot
    public void addFlaskPieces(FlaskPiece.FlaskType type, int quantity) {
        flaskPieces.merge(type, Math.max(0, quantity), Integer::sum);
        System.out.printf("🧪 Ai primit %d %s %s!\n",
                quantity, type.getIcon(), type.getDisplayName());
    }

// metoda de a consuma piecurile folosita in a upgrada
    public boolean consumeFlaskPieces(FlaskPiece.FlaskType preferredType, int quantity) {

        // Încearcă să consume din tipul preferat
        int availablePreferred = flaskPieces.getOrDefault(preferredType, 0);
        int availableUniversal = flaskPieces.getOrDefault(FlaskPiece.FlaskType.UNIVERSAL, 0);

        if (availablePreferred + availableUniversal < quantity) {
            return false; // Nu sunt destule
        }

        // Consumă din preferred type primul
        int fromPreferred = Math.min(quantity, availablePreferred);
        int fromUniversal = quantity - fromPreferred;

        if (fromPreferred > 0) {
            flaskPieces.put(preferredType, availablePreferred - fromPreferred);
        }
        if (fromUniversal > 0) {
            flaskPieces.put(FlaskPiece.FlaskType.UNIVERSAL, availableUniversal - fromUniversal);
        }

        return true;
    }

// getter de flsk piece-uri disponibile
    public int getFlaskPiecesQuantity(FlaskPiece.FlaskType type) {
        return flaskPieces.getOrDefault(type, 0);
    }

// getter din toate flask pieceurile
    public Map<FlaskPiece.FlaskType, Integer> getAllFlaskPieces() {
        return new HashMap<>(flaskPieces);
    }

    // ================== METODE PENTRU BUFF POTIONS ==================

//metoda de a adauga buff potions de folosita pt fiecare tip in shop
    public void addBuffPotion(BuffPotion.BuffType type, int quantity) {
        buffPotions.merge(type, Math.max(0, quantity), Integer::sum);
        System.out.printf("🧪 Ai primit %d %s %s!\n",
                quantity, type.getIcon(), type.getDisplayName());
    }


// getter pt cate buff potions am de tipuri
    public int getBuffPotionQuantity(BuffPotion.BuffType type) {
        return buffPotions.getOrDefault(type, 0);
    }

//getter pt toate buff potions
    public Map<BuffPotion.BuffType, Integer> getAllBuffPotions() {
        return new HashMap<>(buffPotions);
    }

    // ====================================
//METODE PT SCROLL-uri

// metoda ca sa adaug scrolluri in inventar
    public void addEnchantScroll(EnchantScroll.EnchantType type, int quantity, int level) {
        EnchantScroll existingScroll = enchantScrolls.get(type);

        if (existingScroll != null && existingScroll.getEnchantLevel() == level) {
            // Același nivel, adaugă cantitatea
            existingScroll.addQuantity(quantity);
        } else {
            // Nivel diferit sau scroll nou
            enchantScrolls.put(type, new EnchantScroll(type, quantity, level));
        }

        System.out.printf("📜 Ai primit %d %s (Nivel %d)!\n",
                quantity, type.getDisplayName(), level);
    }

// metoda pt a folosi scrolluri
    public boolean useEnchantScroll(EnchantScroll.EnchantType type, ObiectEchipament weapon) {
        EnchantScroll scroll = enchantScrolls.get(type);
        if (scroll == null || !scroll.canUse()) {
            System.out.println("❌ Nu ai acest tip de scroll sau nu mai ai bucăți!");
            return false;
        }

// FOLOSEȘTE:
        if (!weapon.isWeapon()) {
            System.out.println("❌ Enchant scrolls pot fi folosite doar pe arme!");
            return false;
        }

        // Verifică costul în gold
        int goldCost = scroll.getApplicationCost();
        if (gold < goldCost) {
            System.out.printf("❌ Îți lipsesc %d gold pentru a aplica enchantment-ul!%n",
                    goldCost - gold);
            return false;
        }

        // Verifică dacă weapon-ul are deja acest enchantment
        if (weapon.hasEnchantment(type.getDamageType())) {
            int currentDamage = weapon.getEnchantmentDamage(type.getDamageType());
            int newDamage = scroll.getEnchantDamage();

            System.out.printf("⚠️  Weapon-ul are deja %s enchantment (%d damage)!%n",
                    type.getDamageType(), currentDamage);
            System.out.printf("🔄 Noul enchantment va fi %d damage.%n", newDamage);
            System.out.printf("💡 Enchantment-ul existent va fi %s!%n",
                    newDamage > currentDamage ? "îmbunătățit" : "înlocuit");
        }

        // Consumă scroll-ul și gold-ul
        scroll.consumeQuantity(1);
        scadeGold(goldCost);

        // Aplică enchantment la armă folosind noua metodă
        weapon.applyEnchantment(type.getDamageType(), scroll.getEnchantDamage());

        // Actualizează numele armei să includă enchantment-ul
        updateWeaponNameWithEnchantments(weapon);

        // Afișează succesul
        System.out.println("\\n✨ " + "═".repeat(50));
        System.out.println("   🎉 ENCHANTMENT APLICAT CU SUCCES! 🎉");
        System.out.println("═".repeat(50));
        System.out.printf("⚔️  Armă: %s%n", weapon.getNume());
        System.out.printf("🔥 Enchantment: %s %s (+%d %s damage)%n",
                type.getIcon(), type.getDisplayName(),
                scroll.getEnchantDamage(), type.getDamageType());
        System.out.printf("✨ Efect special: %s%n", type.getSpecialEffect());
        System.out.printf("💰 Cost: %d gold (Rămâne: %d gold)%n", goldCost, gold);

        // Afișează toate enchantment-urile active
        Map<String, Integer> allEnchants = weapon.getAllEnchantments();
        if (allEnchants.size() > 1) {
            System.out.println("🌟 Toate enchantment-urile active:");
            allEnchants.forEach((enchantType, damage) -> {
                String icon = getEnchantmentIcon(enchantType);
                System.out.printf("   %s %s: +%d damage%n",
                        icon, enchantType.toUpperCase(), damage);
            });
        }
        System.out.println("═".repeat(50));

        return true;
    }

// actualizare nume la weapon in dependenta de enchant
    private void updateWeaponNameWithEnchantments(ObiectEchipament weapon) {
        // Înlătură enchantment-urile anterioare din nume
        String baseName = weapon.getNume().replaceAll("\\s*\\[[^\\]]+\\]", "");

        Map<String, Integer> enchantments = weapon.getAllEnchantments();
        if (enchantments.isEmpty()) {
            weapon.setNume(baseName);
            return;
        }

        // Construiește noul nume cu toate enchantment-urile
        StringBuilder enchantDisplay = new StringBuilder(" [");
        enchantments.forEach((type, damage) -> {
            String icon = getEnchantmentIcon(type);
            enchantDisplay.append(icon).append(type.toUpperCase()).append(" ");
        });
        enchantDisplay.append("]");

        weapon.setNume(baseName + enchantDisplay.toString());
    }


// iconitele pentru numele la weapon cu enchant sau pur si simplu enchanturi
    public String getEnchantmentIcon(String enchantType) {
        return switch (enchantType.toLowerCase()) {
            case "fire" -> "🔥";
            case "ice" -> "❄️";
            case "lightning" -> "⚡";
            case "poison" -> "☠️";
            case "holy" -> "✨";
            case "shadow" -> "🌑";
            case "arcane" -> "🔮";
            case "nature" -> "🌿";
            default -> "✨";
        };
    }


// getter cu toate enchanturile
    public Map<EnchantScroll.EnchantType, EnchantScroll> getAllEnchantScrolls() {
        return new HashMap<>(enchantScrolls);
    }

//==============================================================

// metoda de afisare a tot ce il intereseaza pe player

    public void afiseazaStatusComplet() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("        📊 STATUS COMPLET - " + nume);
        System.out.println("═".repeat(70));

        // =================== INFORMATII DE BAZA ===================
        System.out.printf("🎯 Nivel: %d | ⭐ XP: %d/%d (%.1f%%)\n",
                nivel, xp, xpNecesarPentruUrmatoarelNivel,
                ((double) xp / xpNecesarPentruUrmatoarelNivel) * 100);

        System.out.printf("❤️  Viață: %d/%d | %s %s: %d/%d\n",
                viata, viataMaxima, getResourceIcon(), tipResursa,
                resursaCurenta, resursaMaxima);

        // =================== STATISTICI PRINCIPALE ===================
        System.out.println("\n📈 STATISTICI:");
        System.out.printf("💪 Strength: %d (%d base + %d echipament)\n",
                getStrengthTotal(), strength, getEquipmentBonus("strength"));
        System.out.printf("🏃 Dexterity: %d (%d base + %d echipament)\n",
                getDexterityTotal(), dexterity, getEquipmentBonus("dexterity"));
        System.out.printf("🧠 Intelligence: %d (%d base + %d echipament)\n",
                getIntelligenceTotal(), intelligence, getEquipmentBonus("intelligence"));
        System.out.printf("🛡️  Defense: %d (%d base + %d echipament)\n",
                getDefenseTotal(), defense, getEquipmentBonus("defense"));

        // =================== RESURSE SI CONSUMABILE ===================
        System.out.println("\n💰 RESURSE:");
        System.out.printf("💰 Gold: %d | 🔧 Scrap: %d | 🎯 Puncte stat: %d\n",
                gold, scrap, statPoints);
        System.out.printf("🧪 Berice: %d | 💙 Energizant Profi %s: %d\n",
                healthPotions, tipResursa.toLowerCase(), manaPotions);

        // Adaugă informațiile despre noile sisteme
        System.out.println("\n" + "═".repeat(50));
        System.out.println(" 🧪 SISTEME ADVANCED");
        System.out.println("═".repeat(50));

        // Potion tiers
        System.out.printf("🧪 Berice: %s %s (%d HP/use)\n",
                healthPotionTier.getIcon(), healthPotionTier.getDisplayName(), getHealthPotionHealing());
        System.out.printf("💙 Energizat Profi: %s %s (%d %s/use)\n",
                manaPotionTier.getIcon(), manaPotionTier.getDisplayName(),
                getManaPotionRestore(), tipResursa);

        // Flask pieces
        if (!flaskPieces.isEmpty()) {
            System.out.println("\n🧪 FLASK PIECES:");
            flaskPieces.forEach((type, quantity) ->
                    System.out.printf("  %s %s: %d\n", type.getIcon(), type.getDisplayName(), quantity));
        }

        // Buff potions
        if (!buffPotions.isEmpty()) {
            System.out.println("\n✨ BUFF POTIONS:");
            buffPotions.forEach((type, quantity) ->
                    System.out.printf("  %s %s: %d\n", type.getIcon(), type.getDisplayName(), quantity));
        }

        // Enchant scrolls
        if (!enchantScrolls.isEmpty()) {
            System.out.println("\n📜 ENCHANT SCROLLS:");
            enchantScrolls.forEach((type, scroll) ->
                    System.out.printf("  %s: %d (Nivel %d)\n",
                            scroll.toString(), scroll.getQuantity(), scroll.getEnchantLevel()));
        }

        System.out.println("\n");

        // SAORMA REVIVAL TATI
        if (shaormaRevival > 0) {
            System.out.printf("🌯 ✨ ȘAORME DE REVIVAL: %d ✨\n", shaormaRevival);
            System.out.println("   💡 Pot fi folosite pentru reînviere în caz de moarte!");
        } else {
            System.out.println("🌯 Șaorme de Revival: 0");
            System.out.println("   💡 Caută Boss-i pentru a obține această delicatesă rară!");
        }



// =================== ECHIPAMENT COMPLET ===================
        Map<String, ObiectEchipament> currentEquipment = new HashMap<>(echipat);

        int totalEquippedItems = currentEquipment.size();


        System.out.println("\n" + "═".repeat(50));
        System.out.println(" 🎒 ECHIPAMENT COMPLET");
        System.out.println("═".repeat(50));

        Map<String, String> slotNames = Map.of(
                "Helmet", "⛑️ Cască",
                "Armor", "🛡️ Armură",
                "Gloves", "🧤 Mănuși",
                "Boots", "🥾 Încălțăminte",
                "Weapon", "⚔️ Armă principală",
                "Shield", "🛡️ Scut",
                "Ring", "💍 Inel",
                "Necklace", "📿 Colier"
        );




        for (Map.Entry<String, String> slot : slotNames.entrySet()) {
            String slotType = slot.getKey();
            String slotDisplayName = slot.getValue();
            ObiectEchipament equippedItem = currentEquipment.get(slotType);

            if (equippedItem != null) {
                System.out.printf("%-20s ✅ %s\n", slotDisplayName + ":", equippedItem.getNume());
                System.out.printf("%-20s 📊 %s | Nivel %d\n", "",
                        equippedItem.getRaritate().getDisplayName(),
                        equippedItem.getNivelNecesar());
                if (!equippedItem.getBonuses().isEmpty()) {
                    System.out.printf("%-20s ✨ ", "");
                    equippedItem.getBonuses().forEach((stat, bonus) ->
                            System.out.print("+" + bonus + " " + stat + " "));
                    System.out.println();
                }
            } else {
                System.out.printf("%-20s ❌ [GOL]\n", slotDisplayName + ":");
                System.out.printf("%-20s 💡 Niciun obiect echipat\n", "");
            }
            System.out.println();
        }


        // Calculează statisticile echipamentului


        int totalItems = inventar.size();

        System.out.println("═".repeat(50));
        System.out.printf("📊 SUMAR ECHIPAMENT: %d/%d slot-uri ocupate\n",
                totalEquippedItems, slotNames.size());
        System.out.printf("📦 Total obiecte în inventar: %d\n", totalItems);

        // =================== PROGRES ȘI OBIECTIVE ===================
        System.out.println("\n" + "═".repeat(50));
        System.out.println("        🏆 PROGRES ȘI OBIECTIVE");
        System.out.println("═".repeat(50));

        // Progres către următorul nivel
        double levelProgress = ((double) xp / xpNecesarPentruUrmatoarelNivel) * 100;
        System.out.printf("📈 Progres level-up: %.1f%%\n", levelProgress);

        // Recomandări bazate pe statistici
        if (statPoints > 0) {
            System.out.printf("🎯 Ai %d puncte de stat! Vizitează Trainer-ul pentru upgrade!\n", statPoints);
        }

        if (totalEquippedItems < slotNames.size()) {
            int emptySlots = slotNames.size() - totalEquippedItems;
            System.out.printf("⚠️  %d slot-uri goale! Caută echipament nou în dungeon!\n", emptySlots);
        }

        if (shaormaRevival == 0) {
            System.out.println("🌯 Caută Boss-i pentru Șaorme de Revival - îți pot salva viața!");
        }

        System.out.println("═".repeat(70));
    }

// metoda pt afisare in dependenta de resursa
    private String getResourceIcon() {
        return switch (tipResursa.toLowerCase()) {
            case "rage" -> "💢";
            case "energy" -> "⚡";
            default -> "💙";
        };
    }



    // ================== GETTERI ==================

    public String getNume() { return nume; }
    public int getNivel() { return nivel; }
    public int getXp() { return xp; }
    public int getXpNecesarPentruUrmatoarelNivel() { return xpNecesarPentruUrmatoarelNivel; }
    public int getViata() { return viata; }
    public int getViataMaxima() { return viataMaxima; }
    public int getStrength() { return strength; }
    public int getDexterity() { return dexterity; }
    public int getIntelligence() { return intelligence; }
    public int getDefense() { return defense; }
    public int getGold() { return gold; }
    public int getScrap() { return scrap; }
    public int getHealthPotions() { return healthPotions; }
    public int getManaPotions() { return manaPotions; }
   // public List<ObiectEchipament> getInventar() { return new ArrayList<>(inventar); }
    public List<Abilitate> getAbilitati() { return new ArrayList<>(abilitati); }
    public Map<String, BuffStack> getBuffuriActive() { return new HashMap<>(buffuriActive); }

    public int getShaormaRevival() {
        return shaormaRevival;
    }

    @Override
    public String toString() {
        return String.format("%s (Nivel %d) [HP: %d/%d | %s: %d/%d | STR: %d DEX: %d INT: %d]",
                nume, nivel, viata, viataMaxima, tipResursa, resursaCurenta, resursaMaxima,shaormaRevival,
                getStrengthTotal(), getDexterityTotal(), getIntelligenceTotal());
    }

    public int getRageMaxim() {
        return rageMaxim;
    }

    public void setRageMaxim(int rageMaxim) {
        this.rageMaxim = rageMaxim;
    }

    public int getEnergyMaxim() {
        return energyMaxim;
    }

    public void setEnergyMaxim(int energyMaxim) {
        this.energyMaxim = energyMaxim;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int getRage() {
        return rage;
    }

    public void setRage(int rage) {
        this.rage = rage;
    }


// ==================== GETTERS PENTRU ECHIPAMENT ====================

    public ObiectEchipament getMainHandWeapon() {
        return echipat != null ? echipat.get(MAIN_HAND) : null;
    }

    public ObiectEchipament getOffHandItem() {
        return echipat != null ? echipat.get(OFF_HAND) : null;
    }

    public ObiectEchipament getArmorEquipped() {
        return echipat != null ? echipat.get(ARMOR) : null;
    }

    public ObiectEchipament getHelmetEquipped() {
        return echipat != null ? echipat.get(HELMET) : null;
    }

    public ObiectEchipament getGlovesEquipped() {
        return echipat != null ? echipat.get(GLOVES) : null;
    }

    public ObiectEchipament getBootsEquipped() {
        return echipat != null ? echipat.get(BOOTS) : null;
    }

    public ObiectEchipament getRing1Equipped() {
        return echipat != null ? echipat.get(RING1) : null;
    }

    public ObiectEchipament getRing2Equipped() {
        return echipat != null ? echipat.get(RING2) : null;
    }

    public ObiectEchipament getNecklaceEquipped() {
        return echipat != null ? echipat.get(NECKLACE) : null;
    }


// ==================== COMPATIBILITATE CU CODUL VECHI ====================

    // Pentru compatibilitate cu InventoryServiceFX
    public ObiectEchipament getArmaEchipata() {
        return getMainHandWeapon();
    }

    public ObiectEchipament getArmuraEchipata() {
        return getArmorEquipped();
    }

    public ObiectEchipament getAccesoriuEchipat() {
        // Returnează primul accesoriu găsit (pentru compatibilitate)
        if (getRing1Equipped() != null) return getRing1Equipped();
        if (getRing2Equipped() != null) return getRing2Equipped();
        if (getNecklaceEquipped() != null) return getNecklaceEquipped();
        if (getHelmetEquipped() != null) return getHelmetEquipped();
        if (getGlovesEquipped() != null) return getGlovesEquipped();
        if (getBootsEquipped() != null) return getBootsEquipped();
        return null;
    }

    // ==================== METODE DE ECHIPARE AVANSATĂ ====================

    /**
     * 🎯 MASTER EQUIP METHOD - Gestionează toate tipurile de echipament
     */
    public EquipResult equipItem(ObiectEchipament item) {
        if (item == null || nivel < item.getNivelNecesar()) {
            return new EquipResult(false, "Nivel insuficient!", null);
        }

        if (echipat == null) {
            echipat = new HashMap<>();
        }

        try {
            return switch (item.getTip()) {
                case WEAPON_ONE_HANDED -> equipOneHandedWeapon(item);
                case WEAPON_TWO_HANDED -> equipTwoHandedWeapon(item);
                case SHIELD -> equipShield(item);
                case OFF_HAND_WEAPON -> equipOffHandWeapon(item);
                case OFF_HAND_MAGIC -> equipOffHandMagic(item);
                case ARMOR -> equipInSlot(item, ARMOR);
                case HELMET -> equipInSlot(item, HELMET);
                case GLOVES -> equipInSlot(item, GLOVES);
                case BOOTS -> equipInSlot(item, BOOTS);
                case RING -> equipRing(item);
                case NECKLACE -> equipInSlot(item, NECKLACE);
                default -> new EquipResult(false, "Tip de echipament necunoscut!", null);
            };
        } finally {
            calculateDerivedStats(); // Recalculează după echipare
            inventar.remove(item); // Scoate din inventar
        }
    }

    /**
     * ⚔️ Echipare armă one-handed
     */
    private EquipResult equipOneHandedWeapon(ObiectEchipament weapon) {
        List<ObiectEchipament> unequippedItems = new ArrayList<>();

        // Verifică dacă avem two-handed în main hand
        ObiectEchipament mainHand = getMainHandWeapon();
        if (mainHand != null && mainHand.isTwoHanded()) {
            unequippedItems.add(mainHand);
            echipat.remove(MAIN_HAND);
            inventar.add(mainHand);
        }

        // Echipează în main hand
        ObiectEchipament oldMainHand = echipat.put(MAIN_HAND, weapon);
        if (oldMainHand != null) {
            unequippedItems.add(oldMainHand);
            inventar.add(oldMainHand);
        }

        return new EquipResult(true,
                "Echipat în main hand: " + weapon.getNume(),
                unequippedItems.isEmpty() ? null : unequippedItems.get(0));
    }

    /**
     * 🗡️ Echipare armă two-handed
     */
    private EquipResult equipTwoHandedWeapon(ObiectEchipament weapon) {
        List<ObiectEchipament> unequippedItems = new ArrayList<>();

        // Dezechipează main hand și off hand
        ObiectEchipament mainHand = echipat.remove(MAIN_HAND);
        ObiectEchipament offHand = echipat.remove(OFF_HAND);

        if (mainHand != null) {
            unequippedItems.add(mainHand);
            inventar.add(mainHand);
        }
        if (offHand != null) {
            unequippedItems.add(offHand);
            inventar.add(offHand);
        }

        // Echipează two-handed în main hand
        echipat.put(MAIN_HAND, weapon);

        return new EquipResult(true,
                "Echipat two-handed: " + weapon.getNume() +
                        (unequippedItems.size() > 0 ? " (dezechipat: " + unequippedItems.size() + " items)" : ""),
                unequippedItems.isEmpty() ? null : unequippedItems.get(0));
    }

    /**
     * 🛡️ Echipare scut
     */
    private EquipResult equipShield(ObiectEchipament shield) {
        // Verifică dacă avem two-handed weapon
        ObiectEchipament mainHand = getMainHandWeapon();
        if (mainHand != null && mainHand.isTwoHanded()) {
            return new EquipResult(false,
                    "Nu poți folosi scut cu " + mainHand.getNume() + " (two-handed)!", null);
        }

        return equipInSlot(shield, OFF_HAND);
    }

    /**
     * ⚔️ Echipare off-hand weapon
     */
    private EquipResult equipOffHandWeapon(ObiectEchipament weapon) {
        // Verifică dacă avem two-handed weapon
        ObiectEchipament mainHand = getMainHandWeapon();
        if (mainHand != null && mainHand.isTwoHanded()) {
            return new EquipResult(false,
                    "Nu poți folosi off-hand cu " + mainHand.getNume() + " (two-handed)!", null);
        }

        return equipInSlot(weapon, OFF_HAND);
    }

    /**
     * 📖 Echipare off-hand magic
     */
    private EquipResult equipOffHandMagic(ObiectEchipament item) {
        ObiectEchipament mainHand = getMainHandWeapon();
        if (mainHand != null && mainHand.isTwoHanded()) {
            return new EquipResult(false,
                    "Nu poți folosi off-hand cu " + mainHand.getNume() + " (two-handed)!", null);
        }

        return equipInSlot(item, OFF_HAND);
    }

    /**
     * 💍 Echipare ring (găsește primul slot liber)
     */
    private EquipResult equipRing(ObiectEchipament ring) {
        if (getRing1Equipped() == null) {
            return equipInSlot(ring, RING1);
        } else if (getRing2Equipped() == null) {
            return equipInSlot(ring, RING2);
        } else {
            // Înlocuiește primul ring
            return equipInSlot(ring, RING1);
        }
    }

    /**
     * 🎯 Helper pentru echipare în slot specific
     */
    private EquipResult equipInSlot(ObiectEchipament item, String slot) {
        ObiectEchipament oldItem = echipat.put(slot, item);

        if (oldItem != null) {
            inventar.add(oldItem);
            return new EquipResult(true,
                    "Echipat: " + item.getNume() + " (înlocuit: " + oldItem.getNume() + ")",
                    oldItem);
        }

        return new EquipResult(true, "Echipat: " + item.getNume(), null);
    }

    // ==================== DEECHIPARE ====================

    /**
     * Deechipează un item din slot
     */
    public EquipResult unequipFromSlot(String slot) {
        if (echipat == null) return new EquipResult(false, "Niciun echipament!", null);

        ObiectEchipament item = echipat.remove(slot);
        if (item != null) {
            inventar.add(item);
            calculateDerivedStats();
            return new EquipResult(true, "Deechipat: " + item.getNume(), item);
        }

        return new EquipResult(false, "Slot gol!", null);
    }

    // ==================== COMPATIBILITATE CU CODUL VECHI ====================

    public void echipeazaArma(ObiectEchipament arma) {
        equipItem(arma);
    }

    public void echipeazaArmura(ObiectEchipament armura) {
        equipItem(armura);
    }

    public void echipeazaAccesoriu(ObiectEchipament accesoriu) {
        equipItem(accesoriu);
    }

    public void deechipeazaArma() {
        unequipFromSlot(MAIN_HAND);
    }

    public void deechipeazaArmura() {
        unequipFromSlot(ARMOR);
    }

    public void deechipeazaAccesoriu() {
        // Deechipează primul accesoriu găsit
        if (getRing1Equipped() != null) unequipFromSlot(RING1);
        else if (getRing2Equipped() != null) unequipFromSlot(RING2);
        else if (getNecklaceEquipped() != null) unequipFromSlot(NECKLACE);
        else if (getHelmetEquipped() != null) unequipFromSlot(HELMET);
        else if (getGlovesEquipped() != null) unequipFromSlot(GLOVES);
        else if (getBootsEquipped() != null) unequipFromSlot(BOOTS);
    }

    // ==================== ACTUALIZARE CALCUL BONUSURI ====================

    /**
     * Calculează toate bonusurile din echipament
     */
    private int getEquipmentBonus(String stat) {
        if (echipat == null) return 0;

        int bonus = 0;
        for (ObiectEchipament item : echipat.values()) {
            if (item != null) {
                Map<String, Integer> itemBonuses = item.getTotalBonuses();
                bonus += itemBonuses.getOrDefault(stat, 0);
            }
        }
        return bonus;
    }

    /**
     * Calculează damage-ul de combat (cu ambele mâini)
     */
    public int calculeazaDamageTotal() {
        int totalDamage = strength * 2; // Base damage

        // Main hand weapon
        ObiectEchipament mainHand = getMainHandWeapon();
        if (mainHand != null) {
            Map<String, Integer> bonuses = mainHand.getTotalBonuses();
            totalDamage += bonuses.getOrDefault("Damage", 0);
            totalDamage += bonuses.getOrDefault("damage_bonus", 0);
            totalDamage += bonuses.getOrDefault("attack_bonus", 0);
        }

        // Off hand weapon (la 50% eficiență)
        ObiectEchipament offHand = getOffHandItem();
        if (offHand != null && offHand.canEquipInOffHand() &&
                (offHand.getTip() == ObiectEchipament.TipEchipament.OFF_HAND_WEAPON ||
                        offHand.getTip() == ObiectEchipament.TipEchipament.WEAPON_ONE_HANDED)) {
            Map<String, Integer> bonuses = offHand.getTotalBonuses();
            totalDamage += (bonuses.getOrDefault("Damage", 0) / 2); // 50% pentru off-hand
            totalDamage += (bonuses.getOrDefault("damage_bonus", 0) / 2);
        }

        // Apply talent tree damage bonus (% increased damage)
        totalDamage = (int)(totalDamage * (1.0 + talentDamageBonus / 100.0));

        // Apply conditional damage bonus
        double conditionalBonus = getConditionalDamageBonus();
        if (conditionalBonus > 0) {
            totalDamage = (int)(totalDamage * (1.0 + conditionalBonus / 100.0));
        }

        return totalDamage;
    }






// ==================== INVENTAR (WRAPPER PENTRU COMPATIBILITATE) ====================

    public InventarWrapper getInventar() {
        return new InventarWrapper(this);
    }

    // Clasă internă wrapper pentru inventar
    public class InventarWrapper {
        private Erou erou;

        public InventarWrapper(Erou erou) {
            this.erou = erou;
        }

        // ========== METODE DE BAZĂ ==========

        public List<ObiectEchipament> getItems() {
            return erou.inventar;
        }

        public boolean addItem(ObiectEchipament item) {
            return erou.inventar.add(item);
        }

        public boolean removeItem(ObiectEchipament item) {
            return erou.inventar.remove(item);
        }

        // ========== METODE NOI NECESARE ==========

        /**
         * Returnează dimensiunea inventarului
         */
        public int size() {
            return erou.inventar.size();
        }

        /**
         * Returnează un stream pentru inventar
         */
        public java.util.stream.Stream<ObiectEchipament> stream() {
            return erou.inventar.stream();
        }

        /**
         * Șterge un obiect din inventar (alias pentru removeItem)
         */
        public boolean remove(ObiectEchipament item) {
            return removeItem(item);
        }

        /**
         * Verifică dacă un item este echipat
         */
        public boolean isEquipped(ObiectEchipament item) {
            return item != null && item.isEquipped();
        }

        public int getCapacitateMaxima() {
            return 50; // Sau orice capacitate maximă dorești
        }


        // ========== POȚIUNI VINDECARE ==========

        private Map<Integer, Integer> healthPotionsMap = new HashMap<>();

        public Map<Integer, Integer> getHealthPotions() {
            // 🔄 MIGRARE AUTOMATĂ din sistemul vechi
            if (healthPotionsMap.isEmpty() && erou.healthPotions > 0) {
                // Migrează poțiunile vechi în noul sistem
                int healAmount = erou.getHealthPotionHealing(); // Folosește tier-ul actual
                healthPotionsMap.put(healAmount, erou.healthPotions);

                System.out.printf("🔄 MIGRATED: %d poțiuni vechi → %d HP heal amount\\n",
                        erou.healthPotions, healAmount);

                // Opțional: resetează sistemul vechi pentru a evita confuzia
                // erou.healthPotions = 0;
            }
            return healthPotionsMap;
        }

        public boolean hasHealthPotion(int healAmount) {
            return healthPotionsMap.getOrDefault(healAmount, 0) > 0;
        }

        public void removeHealthPotion(int healAmount) {
            int current = healthPotionsMap.getOrDefault(healAmount, 0);
            if (current > 1) {
                healthPotionsMap.put(healAmount, current - 1);
            } else {
                healthPotionsMap.remove(healAmount);
            }
        }

        public void addHealthPotion(int healAmount) {
            healthPotionsMap.merge(healAmount, 1, Integer::sum);
        }



        // ========== POȚIUNI BUFF ==========

        private Map<BuffPotion.BuffType, Integer> buffPotionsMap = new HashMap<>();

        public Map<BuffPotion.BuffType, Integer> getBuffPotions() {
            return buffPotionsMap;
        }

        public boolean hasBuffPotion(BuffPotion.BuffType type) {
            return buffPotionsMap.getOrDefault(type, 0) > 0;
        }

        public void removeBuffPotion(BuffPotion.BuffType type) {
            int current = buffPotionsMap.getOrDefault(type, 0);
            if (current > 1) {
                buffPotionsMap.put(type, current - 1);
            } else {
                buffPotionsMap.remove(type);
            }
        }

        public void addBuffPotion(BuffPotion.BuffType type, int quantity) {
            buffPotionsMap.merge(type, quantity, Integer::sum);
        }

        // ========== ENCHANT SCROLLS ==========

        private List<EnchantScroll> enchantScrolls = new ArrayList<>();

        public List<EnchantScroll> getEnchantScrolls() {
            return enchantScrolls;
        }

        public void addEnchantScroll(EnchantScroll scroll) {
            enchantScrolls.add(scroll);
        }

        public boolean removeEnchantScroll(EnchantScroll scroll) {
            return enchantScrolls.remove(scroll);
        }

        // 🆕 METODĂ NOUĂ - Verifică dacă există scroll-uri
        public boolean hasEnchantScroll() {
            return !enchantScrolls.isEmpty();
        }

        // 🆕 METODĂ NOUĂ - Șterge primul scroll (fără argument)
        public EnchantScroll removeEnchantScroll() {
            if (!enchantScrolls.isEmpty()) {
                return enchantScrolls.remove(0);
            }
            return null;
        }

        // 🆕 METODĂ NOUĂ - Verifică dacă există un scroll specific de un tip
        public boolean hasEnchantScrollOfType(EnchantScroll.EnchantType type) {
            return enchantScrolls.stream()
                    .anyMatch(scroll -> scroll.getType() == type);
        }

        // 🆕 METODĂ NOUĂ - Șterge primul scroll de un tip specific
        public boolean removeEnchantScrollOfType(EnchantScroll.EnchantType type) {
            for (int i = 0; i < enchantScrolls.size(); i++) {
                if (enchantScrolls.get(i).getType() == type) {
                    enchantScrolls.remove(i);
                    return true;
                }
            }
            return false;
        }

        // 🆕 METODĂ NOUĂ - Obține primul scroll disponibil
        public EnchantScroll getFirstEnchantScroll() {
            return enchantScrolls.isEmpty() ? null : enchantScrolls.get(0);
        }

        // 🆕 METODĂ NOUĂ - Numărul de scroll-uri
        public int getEnchantScrollCount() {
            return enchantScrolls.size();
        }

        // Flask pieces
        private List<FlaskPiece> flaskPieces = new ArrayList<>();

        public List<FlaskPiece> getFlaskPieces() {
            return flaskPieces;
        }


    }

// ==================== METODE PENTRU POȚIUNI ====================

    public void addHealthPotion(int healAmount) {
        getInventar().getHealthPotions().merge(healAmount, 1, Integer::sum);
    }


    public void aplicaBuff(BuffPotion.BuffType buffType) {
        // Implementează logica de aplicare buff
        String buffName = buffType.getDisplayName();
        int duration = buffType.getDuration();
        Map<String, Double> effect = buffType.getEffect();

        // CORECT: constructor este (Map, int durata, int maxStacks)
        BuffStack buff = new BuffStack(effect, duration, 1);
        buffuriActive.put(buffName, buff);
    }

    public void addEnchantScroll(EnchantScroll scroll) {
        getInventar().getEnchantScrolls().add(scroll);
    }



    public void consumeScrap(int amount) {
        scrap = Math.max(0, scrap - amount);
    }


// ==================== METODE PENTRU COMBAT ====================

    public int calculeazaDamage() {
        // Calculează damage-ul bazat pe strength și echipament
        int baseDamage = strength * 2;

        ObiectEchipament arma = getArmaEchipata();
        if (arma != null) {
            baseDamage += arma.getTotalBonuses().getOrDefault("Damage", 0);
        }

        // 🆕 APLICĂ RUN ITEM DAMAGE MODIFIERS
        // Flat damage boost
        baseDamage += runItemFlatDamage;

        // Percentage damage boost
        baseDamage = (int) (baseDamage * runItemDamageMultiplier);

        // Elemental damage (nu se modifică cu % boost, se adaugă direct)
        if (runItemElementalDamage != null) {
            for (int elementalDmg : runItemElementalDamage.values()) {
                baseDamage += elementalDmg;
            }
        }

        return baseDamage;
    }

    public int primesteDamage(int damage) {
        // 🆕 APLICĂ RUN ITEM DEFENSE MODIFIERS
        int effectiveDefense = defense + runItemFlatDefense;
        effectiveDefense = (int) (effectiveDefense * runItemDefenseMultiplier);

        // Apply conditional defense bonus
        double conditionalDefenseBonus = getConditionalDefenseBonus();
        if (conditionalDefenseBonus > 0) {
            effectiveDefense = (int)(effectiveDefense * (1.0 + conditionalDefenseBonus / 100.0));
        }

        int finalDamage = Math.max(1, damage - effectiveDefense);
        viata = Math.max(0, viata - finalDamage);

        // Track that hero was hit (for conditional bonuses)
        if (finalDamage > 0) {
            recordHit();
        }

        return finalDamage;
    }


    public int getStatPointsToAllocate() {
        return statPoints;
    }


    // Removed - old checkLevelUp replaced by main leveling system in castiga*XP

    public int getExperienta() {
        return xp;
    }

    public int getExpNecesara() {
        return xpNecesarPentruUrmatoarelNivel;
    }

// ==================== METODE PENTRU ȘAORMA ====================

    public int getShaormaRevivalCount() {
        return shaormaRevival;
    }

// ==================== RUN ITEM MODIFIERS ====================

    public double getRunItemDamageMultiplier() { return runItemDamageMultiplier; }
    public void setRunItemDamageMultiplier(double multiplier) { this.runItemDamageMultiplier = multiplier; }

    public int getRunItemFlatDamage() { return runItemFlatDamage; }
    public void setRunItemFlatDamage(int damage) { this.runItemFlatDamage = damage; }

    public double getRunItemDefenseMultiplier() { return runItemDefenseMultiplier; }
    public void setRunItemDefenseMultiplier(double multiplier) { this.runItemDefenseMultiplier = multiplier; }

    public int getRunItemFlatDefense() { return runItemFlatDefense; }
    public void setRunItemFlatDefense(int defense) { this.runItemFlatDefense = defense; }

    public double getRunItemDodgeBonus() { return runItemDodgeBonus; }
    public void setRunItemDodgeBonus(double bonus) { this.runItemDodgeBonus = bonus; }

    public double getRunItemLifesteal() { return runItemLifesteal; }
    public void setRunItemLifesteal(double lifesteal) { this.runItemLifesteal = lifesteal; }

    public int getRunItemRegenPerTurn() { return runItemRegenPerTurn; }
    public void setRunItemRegenPerTurn(int regen) { this.runItemRegenPerTurn = regen; }

    public double getRunItemGoldMultiplier() { return runItemGoldMultiplier; }
    public void setRunItemGoldMultiplier(double multiplier) { this.runItemGoldMultiplier = multiplier; }

    public double getRunItemCritBonus() { return runItemCritBonus; }
    public void setRunItemCritBonus(double bonus) { this.runItemCritBonus = bonus; }

    public void setRunItemElementalDamage(String element, int damage) {
        if (runItemElementalDamage == null) {
            runItemElementalDamage = new HashMap<>();
        }
        runItemElementalDamage.put(element, damage);
    }

    public int getRunItemElementalDamage(String element) {
        if (runItemElementalDamage == null) return 0;
        return runItemElementalDamage.getOrDefault(element, 0);
    }

    public Map<String, Integer> getRunItemElementalDamageMap() {
        if (runItemElementalDamage == null) return new HashMap<>();
        return new HashMap<>(runItemElementalDamage);
    }

    public void clearRunItemElementalDamage() {
        if (runItemElementalDamage != null) {
            runItemElementalDamage.clear();
        }
    }

    // 🆕 DUNGEON PROGRESSION METHODS
    public com.rpg.dungeon.model.DungeonProgression getDungeonProgression() {
        if (dungeonProgression == null) {
            dungeonProgression = new com.rpg.dungeon.model.DungeonProgression();
        }
        return dungeonProgression;
    }

    public void setDungeonProgression(com.rpg.dungeon.model.DungeonProgression progression) {
        this.dungeonProgression = progression;
    }

    // 🎒 ITEM POCKETS METHODS
    public List<com.rpg.model.inventory.ItemPocket> getItemPockets() {
        return itemPockets;
    }

    public void addItemPocket(com.rpg.model.inventory.ItemPocket pocket) {
        itemPockets.add(pocket);
    }

    public void removeItemPocket(com.rpg.model.inventory.ItemPocket pocket) {
        itemPockets.remove(pocket);
    }

}