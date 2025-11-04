package com.rpg.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Animated health bar with smooth transitions and visual feedback
 * Pokemon/Fear & Hunger style health display
 */
public class AnimatedHealthBar extends VBox {

    private ProgressBar healthBar;
    private ProgressBar damageBar; // Shows "ghost" damage that catches up
    private Label healthLabel;
    private Rectangle flashOverlay;

    private int currentHP;
    private int maxHP;
    private double currentPercentage;

    public AnimatedHealthBar(int maxHP) {
        this.maxHP = maxHP;
        this.currentHP = maxHP;
        this.currentPercentage = 1.0;

        setSpacing(3);
        setAlignment(Pos.CENTER);

        createHealthBar();
    }

    private void createHealthBar() {
        // Health label (HP: 100/100)
        healthLabel = new Label(currentHP + " / " + maxHP);
        healthLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: white; " +
            "-fx-effect: dropshadow(gaussian, black, 2, 0.7, 0, 0);"
        );

        // Container for bars
        StackPane barContainer = new StackPane();

        // Damage bar (background - shows lost HP in red temporarily)
        damageBar = new ProgressBar(1.0);
        damageBar.setPrefWidth(200);
        damageBar.setPrefHeight(20);
        damageBar.setStyle(
            "-fx-accent: #c0392b; " + // Dark red
            "-fx-control-inner-background: #2c3e50; " +
            "-fx-background-radius: 10px;"
        );

        // Main health bar (foreground)
        healthBar = new ProgressBar(1.0);
        healthBar.setPrefWidth(200);
        healthBar.setPrefHeight(20);
        healthBar.setStyle(
            "-fx-accent: #2ecc71; " + // Green
            "-fx-control-inner-background: transparent; " +
            "-fx-background-radius: 10px;"
        );

        // Flash overlay for damage feedback
        flashOverlay = new Rectangle(200, 20);
        flashOverlay.setFill(Color.RED);
        flashOverlay.setOpacity(0);
        flashOverlay.setArcWidth(10);
        flashOverlay.setArcHeight(10);

        barContainer.getChildren().addAll(damageBar, healthBar, flashOverlay);

        getChildren().addAll(healthLabel, barContainer);
    }

    /**
     * Update HP with smooth animation
     */
    public void updateHP(int newHP, boolean isDamage) {
        if (newHP == currentHP) return;

        int oldHP = currentHP;
        currentHP = Math.max(0, Math.min(newHP, maxHP));
        double newPercentage = (double) currentHP / maxHP;

        // Update label immediately
        healthLabel.setText(currentHP + " / " + maxHP);

        if (isDamage && newHP < oldHP) {
            // Taking damage - animate health bar down, damage bar follows slowly
            animateDamage(newPercentage);
        } else {
            // Healing - animate health bar up
            animateHeal(newPercentage);
        }

        // Update color based on HP percentage
        updateBarColor(newPercentage);

        currentPercentage = newPercentage;
    }

    /**
     * Animate damage taken (health bar drops, damage bar catches up)
     */
    private void animateDamage(double newPercentage) {
        // Flash red
        Timeline flash = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(flashOverlay.opacityProperty(), 0.5)),
            new KeyFrame(Duration.millis(100), new KeyValue(flashOverlay.opacityProperty(), 0))
        );
        flash.play();

        // Health bar drops quickly
        Timeline healthDrop = new Timeline(
            new KeyFrame(Duration.millis(200),
                new KeyValue(healthBar.progressProperty(), newPercentage)
            )
        );
        healthDrop.play();

        // Damage bar catches up slowly (ghost effect)
        Timeline damageCatchup = new Timeline(
            new KeyFrame(Duration.millis(600),
                new KeyValue(damageBar.progressProperty(), newPercentage)
            )
        );
        damageCatchup.setDelay(Duration.millis(300)); // Wait before catching up
        damageCatchup.play();
    }

    /**
     * Animate healing (health bar rises)
     */
    private void animateHeal(double newPercentage) {
        // Flash green
        Rectangle greenFlash = new Rectangle(200, 20);
        greenFlash.setFill(Color.GREEN);
        greenFlash.setOpacity(0.5);
        greenFlash.setArcWidth(10);
        greenFlash.setArcHeight(10);

        StackPane parent = (StackPane) healthBar.getParent();
        parent.getChildren().add(greenFlash);

        Timeline flash = new Timeline(
            new KeyFrame(Duration.millis(300), new KeyValue(greenFlash.opacityProperty(), 0))
        );
        flash.setOnFinished(e -> parent.getChildren().remove(greenFlash));
        flash.play();

        // Both bars rise together
        Timeline healthRise = new Timeline(
            new KeyFrame(Duration.millis(300),
                new KeyValue(healthBar.progressProperty(), newPercentage),
                new KeyValue(damageBar.progressProperty(), newPercentage)
            )
        );
        healthRise.play();
    }

    /**
     * Update bar color based on HP percentage
     */
    private void updateBarColor(double percentage) {
        String color;
        if (percentage > 0.5) {
            color = "#2ecc71"; // Green
        } else if (percentage > 0.25) {
            color = "#f39c12"; // Orange
        } else {
            color = "#e74c3c"; // Red
        }

        healthBar.setStyle(
            "-fx-accent: " + color + "; " +
            "-fx-control-inner-background: transparent; " +
            "-fx-background-radius: 10px;"
        );
    }

    /**
     * Pulse animation when HP is critical
     */
    public void pulseIfCritical() {
        if (currentPercentage <= 0.25) {
            Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(healthBar.scaleXProperty(), 1.0)),
                new KeyFrame(Duration.millis(300), new KeyValue(healthBar.scaleXProperty(), 1.05)),
                new KeyFrame(Duration.millis(600), new KeyValue(healthBar.scaleXProperty(), 1.0))
            );
            pulse.setCycleCount(Timeline.INDEFINITE);
            pulse.play();
        }
    }

    // Getters
    public int getCurrentHP() { return currentHP; }
    public int getMaxHP() { return maxHP; }
    public double getPercentage() { return currentPercentage; }

    public void setMaxHP(int maxHP) {
        this.maxHP = maxHP;
        currentPercentage = (double) currentHP / maxHP;
        healthLabel.setText(currentHP + " / " + maxHP);
        healthBar.setProgress(currentPercentage);
        damageBar.setProgress(currentPercentage);
    }
}
