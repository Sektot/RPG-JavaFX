package com.rpg.dungeon.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Event interactiv în dungeon (similar cu Darkest Dungeon)
 */
public class DungeonEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;
    private String description;
    private List<EventChoice> choices;

    public DungeonEvent(String title, String description) {
        this.title = title;
        this.description = description;
        this.choices = new ArrayList<>();
    }

    public void addChoice(EventChoice choice) {
        this.choices.add(choice);
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<EventChoice> getChoices() { return new ArrayList<>(choices); }

    /**
     * O alegere într-un event
     */
    public static class EventChoice implements Serializable {
        private static final long serialVersionUID = 1L;

        private String text;
        private String icon;
        private EventOutcome successOutcome;
        private EventOutcome failureOutcome;
        private double successChance;

        public EventChoice(String text, String icon, double successChance) {
            this.text = text;
            this.icon = icon;
            this.successChance = successChance;
        }

        public void setSuccessOutcome(EventOutcome outcome) {
            this.successOutcome = outcome;
        }

        public void setFailureOutcome(EventOutcome outcome) {
            this.failureOutcome = outcome;
        }

        public String getText() { return text; }
        public String getIcon() { return icon; }
        public EventOutcome getSuccessOutcome() { return successOutcome; }
        public EventOutcome getFailureOutcome() { return failureOutcome; }
        public double getSuccessChance() { return successChance; }
    }

    /**
     * Rezultatul unei alegeri
     */
    public static class EventOutcome implements Serializable {
        private static final long serialVersionUID = 1L;

        private String resultText;
        private int goldChange;
        private int healthChange;
        private List<RunItem> itemRewards;

        public EventOutcome(String resultText) {
            this.resultText = resultText;
            this.goldChange = 0;
            this.healthChange = 0;
            this.itemRewards = new ArrayList<>();
        }

        public void setGoldChange(int gold) { this.goldChange = gold; }
        public void setHealthChange(int health) { this.healthChange = health; }
        public void addItemReward(RunItem item) { this.itemRewards.add(item); }

        public String getResultText() { return resultText; }
        public int getGoldChange() { return goldChange; }
        public int getHealthChange() { return healthChange; }
        public List<RunItem> getItemRewards() { return new ArrayList<>(itemRewards); }
    }
}
