package com.rpg.model.effects;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Clasa pentru stacking de buff-uri
 */
public class BuffStack implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Double> modificatori;
    private int durata;
    private int stacks;
    private int maxStacks;

    public BuffStack(Map<String, Double> modificatori, int durata, int maxStacks) {
        this.modificatori = new HashMap<>(modificatori);
        this.durata = durata;
        this.stacks = 1;
        this.maxStacks = maxStacks;
    }

    /**
     * Adaugă un stack la buff
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
     * Verifică dacă buff-ul este activ
     */
    public boolean isActive() {
        return durata > 0;
    }

    /**
     * Returnează toți modificatorii multiplicați cu nr de stacks
     */
    public Map<String, Double> getAllModifiers() {
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, Double> entry : modificatori.entrySet()) {
            // Modificatorii se aplică multiplicativ pe stacks
            double baseValue = entry.getValue();
            double stackMultiplier = 1.0 + ((baseValue - 1.0) * stacks);
            result.put(entry.getKey(), stackMultiplier);
        }
        return result;
    }

    // Getteri
    public Map<String, Double> getModificatori() {
        return new HashMap<>(modificatori);
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