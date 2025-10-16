package com.rpg.service.dto;

/**
 * Rezultatul gambling-ului
 */
public class GambleResultDTO {
    private final boolean success;
    private final String message;
    private final int betAmount;
    private final int winnings;
    private final int heroRoll;
    private final int tavernKeeperRoll;

    public GambleResultDTO(boolean success, String message, int betAmount,
                           int winnings, int heroRoll, int tavernKeeperRoll) {
        this.success = success;
        this.message = message;
        this.betAmount = betAmount;
        this.winnings = winnings;
        this.heroRoll = heroRoll;
        this.tavernKeeperRoll = tavernKeeperRoll;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getBetAmount() {
        return betAmount;
    }

    public int getWinnings() {
        return winnings;
    }

    public int getHeroRoll() {
        return heroRoll;
    }

    public int getTavernKeeperRoll() {
        return tavernKeeperRoll;
    }

    public boolean isWin() {
        return winnings > betAmount;
    }

    public boolean isDraw() {
        return winnings == betAmount && betAmount > 0;
    }
}
