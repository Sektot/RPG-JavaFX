package com.rpg.model.characters;

import com.rpg.model.effects.DebuffStack;
import com.rpg.model.enemies.EnemyAffix;
import com.rpg.model.enemies.EnemyTier;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.service.LootGenerator;
import com.rpg.utils.GameConstants;
import com.rpg.utils.RandomUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// CLASA PT INAMIC
public class Inamic implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // Statistici de bază
    private final String nume;
    private final int nivel;
    private int viata;
    private final int viataMaxima;
    private final int defense;
    private int damage;
    private boolean boss;
    private boolean inspectat = false;

    // Reward-uri
    private final int goldReward;
    private final int xpReward;
    private final int shards;
    private final double dropChance;

    // Loot system - NECESAR pentru GameService
    private List<ObiectEchipament> lootTable;

    // Rezistențe și vulnerabilități
    private String tipDamageRezistent;
    private String tipDamageVulnerabil;

    // Debuff-uri active
    private final Map<String, DebuffStack> debuffuriActive;

    // Abilități speciale
    private double critChance;
    private final List<String> abilitatiSpeciale;

    private int regenerareViata = 0;

    // 🆕 Elite tier and affix system
    private EnemyTier tier = EnemyTier.NORMAL;
    private List<EnemyAffix> affixes = new ArrayList<>();

    // Affix state tracking
    private boolean hasShield = false;
    private int shieldHealth = 0;
    private boolean hasTeleported = false;
    private boolean hasSummonedMinion = false;

    // 🆕 Enemy archetype system
    private com.rpg.model.enemies.EnemyArchetype archetype = null;

    // 🆕 Enemy ability system
    private List<com.rpg.model.enemies.EnemyAbility> abilities = new ArrayList<>();
    private Map<com.rpg.model.enemies.EnemyAbility, Integer> abilityCooldowns = new HashMap<>();

    // Ability buff/debuff tracking
    private int abilityDamageReductionTurns = 0;  // Shield Wall
    private double abilityDamageReductionAmount = 0.0;
    private int abilityDamageBuffTurns = 0;  // Battle Cry, Enrage
    private double abilityDamageBuffAmount = 0.0;
    private int abilityDefenseDebuffTurns = 0;  // Desperate Gambit
    private double abilityDefenseDebuffAmount = 0.0;
    private boolean abilityEvasionActive = false;  // Evasion ability
    private int abilityEvasionTurns = 0;


    // constructor cu parametri necesari pt enemy generator
    public Inamic(String nume, int nivel, int viataMaxima, int defense, int gold, int xpOferit,boolean isBoss) {
        this.nume = nume;
        this.nivel = nivel;
        this.viataMaxima = viataMaxima;
        this.viata = viataMaxima;
        this.defense = defense;
        this.goldReward = gold;
        this.xpReward = xpOferit;
        this.boss = isBoss;
        this.debuffuriActive = new HashMap<>();
        this.abilitatiSpeciale = new ArrayList<>();
        this.lootTable = new ArrayList<>();

        // Calculează damage și alte statistici
        this.damage = GameConstants.calculateEnemyDamage(nivel);
        this.critChance = GameConstants.ENEMY_BASE_CRIT_CHANCE;
        this.dropChance = GameConstants.calculateDropChance(boss);
        this.shards = nivel / 2; // Formula simplă pentru shards


        generateResistances();
        generateSpecialAbilities();
        generateLootTable();
    }

// genereaza rezistente la inamici
    private void generateResistances() {
        String[] damageTypes = {
                "physical", "magical", "fire", "ice", "lightning",
                "poison", "holy", "shadow", "arcane", "nature"
        };

        // 30% șansă să aibă rezistență la un tip
        if (RandomUtils.chancePercent(30.0)) {
            this.tipDamageRezistent = RandomUtils.randomElement(damageTypes);
        }

        // 20% șansă să aibă vulnerabilitate la un tip (diferit de rezistență)
        if (RandomUtils.chancePercent(20.0)) {
            do {
                this.tipDamageVulnerabil = RandomUtils.randomElement(damageTypes);
            } while (tipDamageVulnerabil != null &&
                    tipDamageVulnerabil.equals(tipDamageRezistent));
        }
    }


    // genereaza abilitati speciale in dependenta de nivel
    private void generateSpecialAbilities() {
        if (boss) {
            abilitatiSpeciale.add("Boss Rage");
            abilitatiSpeciale.add("Area Attack");
            if (nivel >= 10) {
                abilitatiSpeciale.add("Heal");
            }
        }

        if (nivel >= 5) {
            abilitatiSpeciale.add("Power Strike");
        }

        if (nivel >= 15) {
            abilitatiSpeciale.add("Debuff Attack");
        }
    }

// genereaza loot-ul la un inamic , necesar la sf bataliei
    private void generateLootTable() {
        LootGenerator lootGen = new LootGenerator();
        this.lootTable = lootGen.generatePossibleLoot(nivel, boss);
    }

    // =====================
    // metode necesare cand le dai capac

// getter pt xp oferit
    public int getXpOferit() {
        return xpReward;
    }

// getter pt gold
    public int getGold() {
        return goldReward;
    }

//getter pt sharduri
    public int getShards() {
        return shards;
    }

// drop chance pt generare de loot
    public double getDropChance() {
        return dropChance;
    }

// getter pt loot table
    public List<ObiectEchipament> getLootTable() {
        return new ArrayList<>(lootTable);
    }

    // ===============================

    //PT ENEMY GENERATOR

// setter pt a determina daca e boss
    public boolean isBoss() { return boss; }
    public void setBoss(boolean boss) { this.boss = boss; }

// seteaza tip de vulnerabilitati
    public void setTipDamageVulnerabil(String tipDamageVulnerabil) {
        this.tipDamageVulnerabil = tipDamageVulnerabil;
    }

// seteaza tipul de rezistente
    public void setTipDamageRezistent(String tipDamageRezistent) {
        this.tipDamageRezistent = tipDamageRezistent;
    }

// seteaza bonusul de crit chance
    public void setCritChanceBonus(double critChanceBonus) {
        // Proprietăți suplimentare pentru EnemyGeneratorService
        this.critChance += critChanceBonus;
    }

// setter de regen doar la boss e folosit
    public void setRegenerareViata(int regenerareViata) {
        this.regenerareViata = regenerareViata;
    }



    // ================== METODE PENTRU BATTLESERVICE ==================

    /**
     * Primește damage.
     */
    public void iaDamage(int damage) {
        int finalDamage = Math.max(1, damage - defense);
        viata = Math.max(0, viata - finalDamage);

        System.out.printf("💥 %s primește %d damage! (%d/%d HP)\n",
                nume, finalDamage, viata, viataMaxima);

        if (viata <= viataMaxima * 0.3 && boss) {
            this.damage = (int) (this.damage * 1.1);
            this.critChance += 2.0;
        }
    }

    /**
     * Aplică un debuff pe inamic cu efecte îmbunătățite.
     */
    public void aplicaDebuff(String nume, int durata, int damagePerTurn) {
        Map<String, Double> effects = new HashMap<>();

        switch (nume.toLowerCase()) {
            case "burn" -> {
                effects.put("damage_per_turn", (double) damagePerTurn);
                effects.put("defense", 0.9); // -10% defense while burning
                System.out.printf("🔥 %s arde și suferă -10%% defense!\\n", this.nume);
            }
            case "poison" -> {
                effects.put("damage_per_turn", (double) damagePerTurn);
                effects.put("strength", 0.85); // -15% strength while poisoned
                System.out.printf("☠️ %s este otrăvit și slăbit (-15%% strength)!\\n", this.nume);
            }
            case "freeze", "slow" -> {
                if (nume.equals("freeze")) {
                    effects.put("movement_disabled", 1.0);
                    effects.put("dexterity", 0.3); // -70% dexterity when frozen
                    System.out.printf("❄️ %s este înghețat complet!\\n", this.nume);
                } else {
                    effects.put("dexterity", 0.7); // -30% dexterity when slowed
                    System.out.printf("🐌 %s este încetinit (-30%% dexterity)!\\n", this.nume);
                }
            }
            case "stun", "paralyzed" -> {
                effects.put("action_disabled", 1.0);
                System.out.printf("⚡ %s nu poate acționa!\\n", this.nume);
            }
            case "defense_down" -> {
                effects.put("defense", 0.6); // -40% defense
                effects.put("damage_per_turn", (double) damagePerTurn);
                System.out.printf("🛡️ %s are defense-ul redus cu 40%%!\\n", this.nume);
            }
            case "weakness" -> {
                effects.put("strength", 0.7); // -30% strength
                effects.put("damage", 0.7); // -30% damage output
                System.out.printf("💪 %s este slăbit (-30%% STR și DMG)!\\n", this.nume);
            }
            default -> {
                // Generic debuff
                effects.put("damage_per_turn", (double) damagePerTurn);
                System.out.printf("⚡ %s este afectat de %s!\\n", this.nume, nume);
            }
        }

        // Adaugă sau update debuff-ul
        if (debuffuriActive.containsKey(nume)) {
            debuffuriActive.get(nume).addStack(durata);
            System.out.printf("   📈 Debuff %s reînnoit! (Stacks: +1)\\n", nume);
        } else {
            debuffuriActive.put(nume, new DebuffStack(effects, durata, GameConstants.MAX_DEBUFF_STACKS));
            System.out.printf("   ✨ Debuff %s aplicat pentru %d ture!\\n", nume, durata);
        }
    }

    /**
     * Verifică dacă inamicul este stunned (nu poate acționa)
     */
    public boolean esteStunned() {
        for (Map.Entry<String, DebuffStack> entry : debuffuriActive.entrySet()) {
            DebuffStack debuff = entry.getValue();
            if (debuff.isActive()) {
                String debuffName = entry.getKey().toLowerCase();
                if (debuffName.equals("stun") || debuffName.equals("freeze") ||
                        debuffName.equals("paralyzed") || debuffName.equals("sleep")) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Actualizează stările inamicului.
     */
    public void actualizeazaStari() {
        // Procesează debuff-urile active
        debuffuriActive.entrySet().removeIf(entry -> {
            String debuffName = entry.getKey();
            DebuffStack debuff = entry.getValue();

            if (debuff.getEffects().containsKey("damage_per_turn")) {
                int dotDamage = debuff.getEffects().get("damage_per_turn").intValue();
                if (dotDamage > 0) {
                    viata = Math.max(0, viata - dotDamage);
                    System.out.printf("🔥 %s primește %d damage de la %s! (%d/%d HP) \n ",
                            nume, dotDamage, debuffName, viata, viataMaxima);
                }
            }

            debuff.decreaseDuration();

            if (!debuff.isActive()) {
                System.out.println("⏰ Debuff-ul " + debuffName + " a expirat pentru " + nume);
                return true;
            }

            return false;
        });

        // Regenerarea pentru boss-uri și inamici cu regenerare
        if ((boss || regenerareViata > 0) && viata > 0 && viata < viataMaxima) {
            int regenAmount = boss ? Math.max(1, viataMaxima / 50) : regenerareViata;
            vindeca(regenAmount);
        }
    }

    /**
     * Procesează debuff-urile și returnează loguri pentru UI.
     * Similar cu actualizeazaStari() dar returnează lista de evenimente.
     */
    public java.util.List<String> processDebuffsWithLogs() {
        java.util.List<String> logs = new java.util.ArrayList<>();

        // Procesează debuff-urile active
        debuffuriActive.entrySet().removeIf(entry -> {
            String debuffName = entry.getKey();
            DebuffStack debuff = entry.getValue();

            if (debuff.getEffects().containsKey("damage_per_turn")) {
                int dotDamage = debuff.getEffects().get("damage_per_turn").intValue();
                if (dotDamage > 0) {
                    viata = Math.max(0, viata - dotDamage);

                    // Add to logs for UI
                    String icon = getDebuffIcon(debuffName);
                    logs.add(icon + " " + nume + " takes " + dotDamage + " damage from " + debuffName + "!");

                    if (!esteViu()) {
                        logs.add("💀 " + nume + " died from " + debuffName + "!");
                    }
                }
            }

            debuff.decreaseDuration();

            if (!debuff.isActive()) {
                logs.add("⏰ " + debuffName + " expired on " + nume);
                return true;
            }

            return false;
        });

        // Regenerarea pentru boss-uri și inamici cu regenerare
        if ((boss || regenerareViata > 0) && viata > 0 && viata < viataMaxima) {
            int regenAmount = boss ? Math.max(1, viataMaxima / 50) : regenerareViata;
            vindeca(regenAmount);
            if (regenAmount > 0) {
                logs.add("💚 " + nume + " regenerates " + regenAmount + " HP!");
            }
        }

        return logs;
    }

    private String getDebuffIcon(String debuffName) {
        String lower = debuffName.toLowerCase();
        if (lower.contains("burn") || lower.contains("fire")) return "🔥";
        if (lower.contains("poison")) return "☠️";
        if (lower.contains("bleed")) return "🩸";
        if (lower.contains("freeze") || lower.contains("ice")) return "❄️";
        if (lower.contains("shock") || lower.contains("lightning")) return "⚡";
        return "💀";
    }

    /**
     * Setează starea de inspecție.
     */
    public void setInspectat(boolean inspectat) {
        this.inspectat = inspectat;
    }

    /**
     * Verifică dacă inamicul a fost inspectat.
     */
    public boolean esteInspectat() {
        return inspectat;
    }

    /**
     * Returnează debuff-urile active (simplified - doar nume și durată).
     */
    public Map<String, Integer> getDebuffuriActive() {
        Map<String, Integer> activeDebuffs = new HashMap<>();
        for (Map.Entry<String, DebuffStack> entry : debuffuriActive.entrySet()) {
            if (entry.getValue().isActive()) {
                activeDebuffs.put(entry.getKey(), entry.getValue().getDurata());
            }
        }
        return activeDebuffs;
    }

    /**
     * Returnează debuff-urile active complete (cu toate detaliile pentru tooltip-uri).
     */
    public Map<String, DebuffStack> getDebuffStacksActive() {
        Map<String, DebuffStack> activeDebuffs = new HashMap<>();
        for (Map.Entry<String, DebuffStack> entry : debuffuriActive.entrySet()) {
            if (entry.getValue().isActive()) {
                activeDebuffs.put(entry.getKey(), entry.getValue());
            }
        }
        return activeDebuffs;
    }

    /**
     * Returnează durata unui debuff specific.
     */
    public int getDebuffDuration(String debuffName) {
        DebuffStack debuff = debuffuriActive.get(debuffName);
        return (debuff != null && debuff.isActive()) ? debuff.getDurata() : 0;
    }

    /**
     * Returnează damage-ul per turn al unui debuff specific.
     */
    public int getDebuffDamage(String debuffName) {
        DebuffStack debuff = debuffuriActive.get(debuffName);
        if (debuff != null && debuff.isActive()) {
            Map<String, Double> effects = debuff.getAllEffects();
            if (effects.containsKey("damage_per_turn")) {
                return effects.get("damage_per_turn").intValue();
            }
        }
        return 0;
    }

    /**
     * Vindecă inamicul.
     */
    public void vindeca(int amount) {
        int vindecareReala = Math.min(amount, viataMaxima - viata);
        viata += vindecareReala;
        if (vindecareReala > 0) {
            System.out.printf("💚 %s se vindecă cu %d HP! (%d/%d) \n",
                    nume, vindecareReala, viata, viataMaxima);
        }
    }

    /**
     * Verifică dacă inamicul este viu.
     */
    public boolean esteViu() {
        return viata > 0;
    }

    // ================== GETTERI ==================

    public String getNume() { return nume; }
    public int getNivel() { return nivel; }
    public int getViata() { return viata; }
    public void setViata(int viata) {
        this.viata = Math.max(0, Math.min(viata, viataMaxima));
    }
    public int getViataMaxima() { return viataMaxima; }
    public int getDefense() { return defense; }


   // @Override
    public int getDefenseTotal() {
        double defenseMultiplier = 1.0;

        // Aplică modificatorii din debuff-uri
        for (DebuffStack debuff : debuffuriActive.values()) {
            if (debuff.isActive() && debuff.getEffects().containsKey("defense")) {
                defenseMultiplier *= debuff.getEffects().get("defense");
            }
        }

        return (int) (defense * defenseMultiplier);
    }

    /**
     * Calculează damage-ul total cu modificatori de debuff.
     */
    public int getDamage() {        //total
        double damageMultiplier = 1.0;

        // Aplică modificatorii din debuff-uri
        for (DebuffStack debuff : debuffuriActive.values()) {
            if (debuff.isActive()) {
                if (debuff.getEffects().containsKey("damage")) {
                    damageMultiplier *= debuff.getEffects().get("damage");
                }
                if (debuff.getEffects().containsKey("strength")) {
                    damageMultiplier *= debuff.getEffects().get("strength");
                }
            }
        }

        return (int) (damage * damageMultiplier);
    }


    public void setDamage(int damage) {
        this.damage = damage;
    }

    public String getTipDamageRezistent() { return tipDamageRezistent; }
    public String getTipDamageVulnerabil() { return tipDamageVulnerabil; }
    public double getCritChance() { return critChance; }
    public List<String> getAbilitatiSpeciale() { return new ArrayList<>(abilitatiSpeciale); }

    // 🆕 Elite tier and affix getters/setters
    public EnemyTier getTier() { return tier; }
    public void setTier(EnemyTier tier) { this.tier = tier; }

    public com.rpg.model.enemies.EnemyArchetype getArchetype() { return archetype; }
    public void setArchetype(com.rpg.model.enemies.EnemyArchetype archetype) { this.archetype = archetype; }

    public List<EnemyAffix> getAffixes() { return new ArrayList<>(affixes); }
    public void setAffixes(List<EnemyAffix> affixes) { this.affixes = new ArrayList<>(affixes); }
    public void addAffix(EnemyAffix affix) { this.affixes.add(affix); }
    public boolean hasAffix(EnemyAffix affix) { return this.affixes.contains(affix); }

    // Affix state getters/setters
    public boolean hasShield() { return hasShield; }
    public void setHasShield(boolean hasShield) { this.hasShield = hasShield; }
    public int getShieldHealth() { return shieldHealth; }
    public void setShieldHealth(int shieldHealth) { this.shieldHealth = shieldHealth; }
    public boolean hasTeleported() { return hasTeleported; }
    public void setHasTeleported(boolean hasTeleported) { this.hasTeleported = hasTeleported; }
    public boolean hasSummonedMinion() { return hasSummonedMinion; }
    public void setHasSummonedMinion(boolean hasSummonedMinion) { this.hasSummonedMinion = hasSummonedMinion; }

    /**
     * Returns formatted enemy name with tier icon and affixes.
     */
    public String getFormattedName() {
        StringBuilder sb = new StringBuilder();

        // Add tier icon if not normal
        if (tier != EnemyTier.NORMAL) {
            sb.append(tier.getIcon()).append(" ");
        }

        sb.append(nume);

        // Add affix icons
        if (!affixes.isEmpty()) {
            sb.append(" ");
            for (EnemyAffix affix : affixes) {
                sb.append(affix.getIcon());
            }
        }

        return sb.toString();
    }

    /**
     * Returns detailed tooltip with tier and affixes.
     */
    public String getTooltip() {
        StringBuilder sb = new StringBuilder();
        sb.append(getFormattedName()).append("\n");
        sb.append("Level ").append(nivel).append(" | ");
        sb.append(tier.getFormattedName()).append("\n");
        sb.append("HP: ").append(viata).append("/").append(viataMaxima).append("\n");

        if (!affixes.isEmpty()) {
            sb.append("\nAffixes:\n");
            for (EnemyAffix affix : affixes) {
                sb.append("  ").append(affix.getTooltip()).append("\n");
            }
        }

        if (tipDamageRezistent != null) {
            sb.append("Resistant: ").append(tipDamageRezistent).append("\n");
        }
        if (tipDamageVulnerabil != null) {
            sb.append("Weak to: ").append(tipDamageVulnerabil).append("\n");
        }

        return sb.toString();
    }


    @Override
    public String toString() {
        String bossMarker = boss ? " 💀 BOSS" : "";
        String healthBar = createHealthBar();
        return String.format("%s (Nivel %d)%s %s [%d/%d HP | %d DEF | %d DMG]",
                nume, nivel, bossMarker, healthBar, viata, viataMaxima, defense, damage);
    }

    private String createHealthBar() {
        double percent = (double) viata / viataMaxima * 100;
        if (percent > 75) return "🟢";
        else if (percent > 50) return "🟡";
        else if (percent > 25) return "🟠";
        else return "🔴";
    }

    // Adaugă aceste metode în clasa Inamic:

    // FIX: Alias pentru iaDamage()
    public int primesteDamage(int damage) {
        int finalDamage = Math.max(1, damage - defense);
        viata = Math.max(0, viata - finalDamage);
        System.out.printf("💥 %s primește %d damage! (%d/%d HP)\\n",
                nume, finalDamage, viata, viataMaxima);

        // Boss rage mode la HP scăzut
        if (viata <= viataMaxima * 0.3 && boss) {
            this.damage = (int) (this.damage * 1.1);
            this.critChance += 2.0;
        }

        return finalDamage; // return actual damage dealt
    }

    public int calculeazaDamage() {
        double damageMultiplier = 1.0;

        // Aplică modificatorii din debuff-uri (cod existent)
        for (DebuffStack debuff : debuffuriActive.values()) {
            if (debuff.isActive()) {
                if (debuff.getEffects().containsKey("damage")) {
                    damageMultiplier *= debuff.getEffects().get("damage");
                }
                if (debuff.getEffects().containsKey("strength")) {
                    damageMultiplier *= debuff.getEffects().get("strength");
                }
            }
        }

        int baseDamage = (int) (damage * damageMultiplier);

        // 🎲 ADAUGĂ VARIAȚIE PER-HIT (±15%)
        int variatie = (int)(baseDamage * 0.15);
        java.util.Random random = new java.util.Random();
        int minDamage = Math.max(1, baseDamage - variatie);
        int maxDamage = baseDamage + variatie;

        return random.nextInt(maxDamage - minDamage + 1) + minDamage;
    }

    // FIX: Alias pentru getGold()
    public int getGoldReward() {
        return goldReward;
    }

    // FIX: Alias pentru getXpOferit()
    public int getExpReward() {
        return xpReward;
    }

    // FIX: Alias pentru getLootTable()
    public List<ObiectEchipament> getLoot() {
        return new ArrayList<>(lootTable);
    }

    // FIX: Metodă nouă pentru Șaorma reward
    public int getShaormaReward() {
        return boss ? 1 : 0; // Doar boss-ii dau șaorme de revival
    }

    // ==================== ABILITY SYSTEM ====================

    /**
     * Gets the list of abilities this enemy has.
     */
    public List<com.rpg.model.enemies.EnemyAbility> getAbilities() {
        return new ArrayList<>(abilities);
    }

    /**
     * Sets the abilities for this enemy.
     */
    public void setAbilities(List<com.rpg.model.enemies.EnemyAbility> abilities) {
        this.abilities = new ArrayList<>(abilities);
        // Initialize cooldowns
        for (com.rpg.model.enemies.EnemyAbility ability : abilities) {
            abilityCooldowns.put(ability, 0);
        }
    }

    /**
     * Adds a single ability to this enemy.
     */
    public void addAbility(com.rpg.model.enemies.EnemyAbility ability) {
        if (!abilities.contains(ability)) {
            abilities.add(ability);
            abilityCooldowns.put(ability, 0);
        }
    }

    /**
     * Checks if an ability is ready to use (cooldown expired and HP threshold met).
     */
    public boolean isAbilityReady(com.rpg.model.enemies.EnemyAbility ability) {
        // Check cooldown
        if (abilityCooldowns.getOrDefault(ability, 0) > 0) {
            return false;
        }

        // Check HP threshold
        double currentHpPercent = (double) viata / viataMaxima;
        return ability.isUsableAtHP(currentHpPercent);
    }

    /**
     * Marks an ability as used, putting it on cooldown.
     */
    public void useAbility(com.rpg.model.enemies.EnemyAbility ability) {
        abilityCooldowns.put(ability, ability.getCooldown());
    }

    /**
     * Updates all ability cooldowns at the start of turn.
     */
    public void updateAbilityCooldowns() {
        for (com.rpg.model.enemies.EnemyAbility ability : abilityCooldowns.keySet()) {
            int current = abilityCooldowns.get(ability);
            if (current > 0) {
                abilityCooldowns.put(ability, current - 1);
            }
        }
    }

    /**
     * Gets a random usable ability, or null if none available.
     */
    public com.rpg.model.enemies.EnemyAbility chooseRandomAbility() {
        List<com.rpg.model.enemies.EnemyAbility> usableAbilities = new ArrayList<>();

        for (com.rpg.model.enemies.EnemyAbility ability : abilities) {
            if (isAbilityReady(ability)) {
                usableAbilities.add(ability);
            }
        }

        if (usableAbilities.isEmpty()) {
            return null;
        }

        java.util.Random random = new java.util.Random();
        return usableAbilities.get(random.nextInt(usableAbilities.size()));
    }

    /**
     * Gets cooldown remaining for an ability.
     */
    public int getAbilityCooldown(com.rpg.model.enemies.EnemyAbility ability) {
        return abilityCooldowns.getOrDefault(ability, 0);
    }

    // ==================== ABILITY BUFF/DEBUFF MANAGEMENT ====================

    /**
     * Updates all ability-related buff/debuff timers at end of turn.
     */
    public void updateAbilityEffects() {
        // Damage reduction (Shield Wall)
        if (abilityDamageReductionTurns > 0) {
            abilityDamageReductionTurns--;
            if (abilityDamageReductionTurns == 0) {
                abilityDamageReductionAmount = 0.0;
            }
        }

        // Damage buff (Battle Cry, Enrage)
        if (abilityDamageBuffTurns > 0) {
            abilityDamageBuffTurns--;
            if (abilityDamageBuffTurns == 0) {
                abilityDamageBuffAmount = 0.0;
            }
        }

        // Defense debuff (Desperate Gambit)
        if (abilityDefenseDebuffTurns > 0) {
            abilityDefenseDebuffTurns--;
            if (abilityDefenseDebuffTurns == 0) {
                abilityDefenseDebuffAmount = 0.0;
            }
        }

        // Evasion (100% dodge)
        if (abilityEvasionTurns > 0) {
            abilityEvasionTurns--;
            if (abilityEvasionTurns == 0) {
                abilityEvasionActive = false;
            }
        }
    }

    /**
     * Applies Shield Wall effect (damage reduction).
     */
    public void applyShieldWall(int turns, double reductionPercent) {
        this.abilityDamageReductionTurns = turns;
        this.abilityDamageReductionAmount = reductionPercent;
    }

    /**
     * Applies damage buff (Battle Cry, Enrage).
     */
    public void applyDamageBuff(int turns, double buffPercent) {
        this.abilityDamageBuffTurns = turns;
        this.abilityDamageBuffAmount = buffPercent;
    }

    /**
     * Applies defense debuff (Desperate Gambit).
     */
    public void applyDefenseDebuff(int turns, double debuffPercent) {
        this.abilityDefenseDebuffTurns = turns;
        this.abilityDefenseDebuffAmount = debuffPercent;
    }

    /**
     * Activates evasion (100% dodge).
     */
    public void activateEvasion(int turns) {
        this.abilityEvasionActive = true;
        this.abilityEvasionTurns = turns;
    }

    /**
     * Gets effective damage after ability buffs/debuffs.
     */
    public int getEffectiveDamage() {
        double finalDamage = damage;

        // Apply damage buff if active
        if (abilityDamageBuffTurns > 0) {
            finalDamage *= (1.0 + abilityDamageBuffAmount);
        }

        return (int) finalDamage;
    }

    /**
     * Gets effective defense after ability buffs/debuffs.
     */
    public int getEffectiveDefense() {
        double finalDefense = defense;

        // Apply defense debuff if active
        if (abilityDefenseDebuffTurns > 0) {
            finalDefense *= (1.0 - abilityDefenseDebuffAmount);
        }

        return Math.max(0, (int) finalDefense);
    }

    /**
     * Gets damage reduction from abilities (Shield Wall).
     */
    public double getAbilityDamageReduction() {
        return abilityDamageReductionTurns > 0 ? abilityDamageReductionAmount : 0.0;
    }

    /**
     * Gets remaining turns for damage buff.
     */
    public int getAbilityDamageBuffTurns() {
        return abilityDamageBuffTurns;
    }

    /**
     * Gets damage buff amount.
     */
    public double getAbilityDamageBuffAmount() {
        return abilityDamageBuffAmount;
    }

    /**
     * Checks if evasion is currently active.
     */
    public boolean hasAbilityEvasion() {
        return abilityEvasionActive;
    }

    /**
     * Gets a formatted status string showing active ability effects.
     */
    public String getAbilityEffectsStatus() {
        StringBuilder sb = new StringBuilder();

        if (abilityDamageReductionTurns > 0) {
            sb.append(String.format("🛡️ Shield Wall: %.0f%% reduction (%d turns)\n",
                    abilityDamageReductionAmount * 100, abilityDamageReductionTurns));
        }

        if (abilityDamageBuffTurns > 0) {
            sb.append(String.format("⚔️ Damage Buff: +%.0f%% damage (%d turns)\n",
                    abilityDamageBuffAmount * 100, abilityDamageBuffTurns));
        }

        if (abilityDefenseDebuffTurns > 0) {
            sb.append(String.format("💔 Defense Debuff: -%.0f%% defense (%d turns)\n",
                    abilityDefenseDebuffAmount * 100, abilityDefenseDebuffTurns));
        }

        if (abilityEvasionActive) {
            sb.append(String.format("💨 Evasion: 100%% dodge (%d turns)\n", abilityEvasionTurns));
        }

        return sb.toString();
    }


}