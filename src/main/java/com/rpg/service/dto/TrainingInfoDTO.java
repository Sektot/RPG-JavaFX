package com.rpg.service.dto;

/**
 * Informații despre training
 */
public class TrainingInfoDTO {
    private final int currentStrength;
    private final int currentDexterity;
    private final int currentIntelligence;
    private final int availableStatPoints;
    private final int statPointCost;
    private final int heroGold;
    private final int trainingCost;

    public TrainingInfoDTO(int currentStrength, int currentDexterity, int currentIntelligence,
                           int availableStatPoints, int statPointCost, int heroGold, int trainingCost) {
        this.currentStrength = currentStrength;
        this.currentDexterity = currentDexterity;
        this.currentIntelligence = currentIntelligence;
        this.availableStatPoints = availableStatPoints;
        this.statPointCost = statPointCost;
        this.heroGold = heroGold;
        this.trainingCost = trainingCost;
    }

    public int getCurrentStrength() { return currentStrength; }
    public int getCurrentDexterity() { return currentDexterity; }
    public int getCurrentIntelligence() { return currentIntelligence; }
    public int getAvailableStatPoints() { return availableStatPoints; }
    public int getStatPointCost() { return statPointCost; }
    public int getHeroGold() { return heroGold; }
    public int getTrainingCost() { return trainingCost; }
    public boolean hasStatPoints() { return availableStatPoints > 0; }
    public boolean canAffordTraining() { return heroGold >= trainingCost; }
}

