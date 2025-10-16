package com.rpg.model.abilities;

/**
 * Enumerația care definește tipurile de abilități disponibile în joc.
 * Categorisește abilitățile după natura lor pentru aplicarea logicii corespunzătoare.
 */
public enum AbilityType {

    /**
     * Abilități de atac care cauzează damage direct.
     */
    OFFENSIVE("Ofensiv"),

    /**
     * Abilități de apărare care reduc damage-ul primit sau cresc defense.
     */

    /**
     * Abilități de buff care aplică efecte pozitive.
     */
    BUFF("Buff"),

    /**
     * Abilități de debuff care aplică efecte negative inamicului.
     */
    DEBUFF("Debuff"),

    /**
     * Abilități speciale cu efecte unice sau combinate.
     */
    SPECIAL("Special"),

;

    private final String displayName;


    AbilityType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returnează numele afișat al tipului de abilitate.
     * @return Numele afișat
     */
    public String getDisplayName() {
        return displayName;
    }


    @Override
    public String toString() {
        return displayName;
    }
}