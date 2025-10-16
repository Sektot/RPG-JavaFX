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
    private static final int BASE_HITCHANCE_INCREMENT = 3;   // +3% hit chance per

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
}