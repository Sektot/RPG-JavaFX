package com.rpg.model.effects;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Clasa pentru stacking de debuff-uri
 */
public class DebuffStack implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Double> effects;
    private int durata;
    private int stacks;
    private int maxStacks;

    public DebuffStack(Map<String, Double> effects, int durata, int maxStacks) {
        this.effects = new HashMap<>(effects);
        this.durata = durata;
        this.stacks = 1;
        this.maxStacks = maxStacks;
    }

    /**
     * Adaugă un stack la debuff
     */
    public void addStack(int durataNou) {
        if (stacks < maxStacks) {
            stacks++;
        }
        // Refresh duration
        this.durata = Math.max(this.durata, durataNou);
    }

    /**
     * Decrementează durata
     */
    public void decreaseDuration() {
        if (durata > 0) {
            durata--;
        }
    }

    /**
     * Verifică dacă debuff-ul este activ
     */
    public boolean isActive() {
        return durata > 0;
    }

    /**
     * Returnează toți efectele multiplicați cu nr de stacks
     */
    public Map<String, Double> getAllEffects() {
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, Double> entry : effects.entrySet()) {
            String key = entry.getKey();
            double value = entry.getValue();

            // Pentru damage_per_turn, se adună
            if (key.equals("damage_per_turn")) {
                result.put(key, value * stacks);
            } else {
                // Pentru multiplicatori, se aplică mai intens pe stacks
                double stackMultiplier = 1.0 + ((value - 1.0) * stacks);
                result.put(key, stackMultiplier);
            }
        }
        return result;
    }

    // Getteri
    public Map<String, Double> getEffects() {
        return new HashMap<>(effects);
    }

    public int getDurata() {
        return durata;
    }

    public int getStacks() {
        return stacks;
    }

    public int getMaxStacks() {
        return maxStacks;
    }
}