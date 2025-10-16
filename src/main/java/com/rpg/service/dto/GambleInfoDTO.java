package com.rpg.service.dto;

/**
 * Informații despre gambling
 */
public class GambleInfoDTO {
    private final int minBet;
    private final int maxBet;
    private final String rules;

    public GambleInfoDTO(int minBet, int maxBet, String rules) {
        this.minBet = minBet;
        this.maxBet = maxBet;
        this.rules = rules;
    }

    public int getMinBet() {
        return minBet;
    }

    public int getMaxBet() {
        return maxBet;
    }

    public String getRules() {
        return rules;
    }
}
