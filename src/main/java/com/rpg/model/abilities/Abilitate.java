package com.rpg.model.abilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reprezintă o abilitate pe care un personaj o poate folosi în luptă.
 * Conține toate informațiile necesare pentru calculul damage-ului și efectelor.
 */
public class Abilitate implements Serializable {

    private static final long serialVersionUID = 1L;

    // Proprietăți de bază
    private String nume;
    private int damage;
    private List<String> tipuriDamage;
    private int costMana;
    private int hitChanceBonus;
    private int cooldown;
    private int cooldownRamasa;

    // Proprietăți pentru efecte
    private String debuffAplicat;
    private int durataDebuff;
    private int damageDebuff;
    private String buffAplicat;
    private int durataBuff;
    private Map<String, Double> modificatoriBuff;
    private Map<String, Double> influentaStatistici;

    // Tip de abilitate pentru categorisire
    private AbilityType abilityType;

    private int upgradeLevel = 0;
    private static final int MAX_UPGRADE = 5;

    private static final int BASE_DAMAGE_INCREMENT = 5;      // +5 damage per upgrade
    private static final int BASE_HITCHANCE_INCREMENT = 3;   // +3% hit chance per upgrade

    // 🆕 Ultimate ability properties
    private boolean isUltimate = false;
    private int requiredLevel = 1;  // Level required to unlock

    // 🆕 Passive ability properties
    private boolean isPassive = false;

    // 🆕 Combo system
    private String comboRequirement = null;  // Name of ability that must be used first
    private double comboBonusDamage = 0.0;   // Bonus damage multiplier when combo'd

    // 🆕 Resource generation (for abilities that generate resources)
    private int resourceGenerated = 0;  // Amount of rage/energy/mana generated

    // 🆕 Self-damage (for berserker-style abilities)
    private int selfDamage = 0;

    // 🆕 Healing
    private int healAmount = 0;
    private double healPercent = 0.0;  // Percentage of max HP to heal

    // 🆕 Multi-hit abilities
    private int numberOfHits = 1;

    // 🆕 AOE abilities
    private boolean isAOE = false;

    /**
     * Constructor complet pentru o abilitate.
     */
    public Abilitate(String nume, int damage, List<String> tipuriDamage, int costMana,
                     int cooldown, int hitChanceBonus, Map<String, Double> influentaStatistici,
                     String debuffAplicat, int durataDebuff, int damageDebuff) {
        this.nume = nume;
        this.damage = Math.max(0, damage);
        this.tipuriDamage = new ArrayList<>(tipuriDamage != null ? tipuriDamage : new ArrayList<>());
        this.costMana = Math.max(0, costMana);
        this.cooldown = Math.max(0, cooldown);
        this.cooldownRamasa = 0;
        this.hitChanceBonus = hitChanceBonus;
        this.influentaStatistici = new HashMap<>(influentaStatistici != null ? influentaStatistici : new HashMap<>());
        this.debuffAplicat = debuffAplicat;
        this.durataDebuff = Math.max(0, durataDebuff);
        this.damageDebuff = Math.max(0, damageDebuff);
        this.modificatoriBuff = new HashMap<>();

    }




    public int getUpgradeLevel() { return upgradeLevel; }
    public int getMaxUpgradeLevel() { return MAX_UPGRADE; }


    public void upgradeAbility() {
        if(upgradeLevel < MAX_UPGRADE) {
            upgradeLevel++;
            this.damage += BASE_DAMAGE_INCREMENT; // de ex. +5 damage
            this.hitChanceBonus += BASE_HITCHANCE_INCREMENT;
        }
    }

    /**
     * Verifică dacă abilitatea poate fi folosită (nu este în cooldown).
     */
    public boolean poateFiFolosita() {
        return cooldownRamasa == 0;
    }

    /**
     * Aplică cooldown-ul abilității.
     */
    public void aplicaCooldown() {
        cooldownRamasa = cooldown;
    }

    /**
     * Reduce cooldown-ul cu o tură.
     */
    public void reduceCooldown() {
        if (cooldownRamasa > 0) {
            cooldownRamasa--;
        }
    }

    /**
     * Calculează damage-ul bazat pe statisticile personajului.
     * @param statsMap Harta cu statisticile personajului
     * @return Damage-ul calculat
     */
    public int calculeazaDamage(Map<String, Integer> statsMap) {
        double totalDamage = damage;

        for (Map.Entry<String, Double> entry : influentaStatistici.entrySet()) {
            String stat = entry.getKey();
            Double multiplier = entry.getValue();
            Integer statValue = statsMap.getOrDefault(stat, 0);

            totalDamage += statValue * multiplier;
        }

        return Math.max(0, (int) Math.round(totalDamage));
    }


    /**
     * Setează buff-ul aplicat de abilitate.
     * @param buffAplicat Numele buff-ului
     * @param durata Durata buff-ului
     * @param modificatori Modificatorii buff-ului
     */
    public void setBuff(String buffAplicat, int durata, Map<String, Double> modificatori) {
        this.buffAplicat = buffAplicat;
        this.durataBuff = Math.max(0, durata);
        this.modificatoriBuff = new HashMap<>(modificatori != null ? modificatori : new HashMap<>());

    }

    // Getteri și setteri
    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }

    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = Math.max(0, damage); }




    public int getCostMana() { return costMana; }


    public int getHitChanceBonus() { return hitChanceBonus; }

    public int getCooldown() { return cooldown; }

    public int getCooldownRamasa() { return cooldownRamasa; }
    public void setCooldownRamasa(int cooldownRamasa) { this.cooldownRamasa = Math.max(0, cooldownRamasa); }

    public String getDebuffAplicat() { return debuffAplicat; }

    public int getDurataDebuff() { return durataDebuff; }

    public int getDamageDebuff() { return damageDebuff; }

    public String getBuffAplicat() { return buffAplicat; }

    public int getDurataBuff() { return durataBuff; }
    public Map<String, Double> getModificatoriBuff() {
        return new HashMap<>(modificatoriBuff);
    }


    /**
     * Returnează o descriere completă a abilității.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(nume).append(" (").append(abilityType.getDisplayName()).append(")\n");
        sb.append("  Damage: ").append(damage);
        if (!tipuriDamage.isEmpty()) {
            sb.append(" (").append(String.join(", ", tipuriDamage)).append(")");
        }
        sb.append("\n  Cost: ").append(costMana);
        if (cooldown > 0) {
            sb.append(", Cooldown: ").append(cooldown);
        }
        if (debuffAplicat != null) {
            sb.append("\n  Debuff: ").append(debuffAplicat).append(" (").append(durataDebuff).append(" ture)");
        }
        if (buffAplicat != null) {
            sb.append("\n  Buff: ").append(buffAplicat).append(" (").append(durataBuff).append(" ture)");
        }
        return sb.toString();
    }

    /**
     * Verifică egalitatea între două abilități.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Abilitate abilitate = (Abilitate) obj;
        return nume != null ? nume.equals(abilitate.nume) : abilitate.nume == null;
    }

    /**
     * Returnează hash code-ul abilității.
     */
    @Override
    public int hashCode() {
        return nume != null ? nume.hashCode() : 0;
    }

    // 🆕 Getters and setters for new properties

    public boolean isUltimate() { return isUltimate; }
    public Abilitate setUltimate(boolean ultimate) {
        isUltimate = ultimate;
        return this;
    }

    public int getRequiredLevel() { return requiredLevel; }
    public Abilitate setRequiredLevel(int level) {
        requiredLevel = level;
        return this;
    }

    public boolean isPassive() { return isPassive; }
    public Abilitate setPassive(boolean passive) {
        isPassive = passive;
        return this;
    }

    public String getComboRequirement() { return comboRequirement; }
    public Abilitate setComboRequirement(String requirement) {
        comboRequirement = requirement;
        return this;
    }

    public double getComboBonusDamage() { return comboBonusDamage; }
    public Abilitate setComboBonusDamage(double bonus) {
        comboBonusDamage = bonus;
        return this;
    }

    public int getResourceGenerated() { return resourceGenerated; }
    public Abilitate setResourceGenerated(int amount) {
        resourceGenerated = amount;
        return this;
    }

    public int getSelfDamage() { return selfDamage; }
    public Abilitate setSelfDamage(int damage) {
        selfDamage = damage;
        return this;
    }

    public int getHealAmount() { return healAmount; }
    public Abilitate setHealAmount(int amount) {
        healAmount = amount;
        return this;
    }

    public double getHealPercent() { return healPercent; }
    public Abilitate setHealPercent(double percent) {
        healPercent = percent;
        return this;
    }

    public int getNumberOfHits() { return numberOfHits; }
    public Abilitate setNumberOfHits(int hits) {
        numberOfHits = Math.max(1, hits);
        return this;
    }

    public boolean isAOE() { return isAOE; }
    public Abilitate setAOE(boolean aoe) {
        isAOE = aoe;
        return this;
    }

    public AbilityType getAbilityType() { return abilityType; }
    public Abilitate setAbilityType(AbilityType type) {
        abilityType = type;
        return this;
    }

    public List<String> getTipuriDamage() { return new ArrayList<>(tipuriDamage); }

    public Map<String, Double> getInfluentaStatistici() {
        return new HashMap<>(influentaStatistici);
    }
}