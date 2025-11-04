package com.rpg.ui;

import javafx.animation.*;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Floating damage/heal numbers that appear during combat
 */
public class FloatingText {

    public enum TextType {
        DAMAGE,          // Regular damage - white
        CRITICAL,        // Critical damage - red/orange
        HEAL,            // Healing - green
        DODGE,           // Dodged - gray
        MISS,            // Missed - gray
        BUFF,            // Buff applied - blue
        DEBUFF,          // Debuff applied - purple
        RESOURCE_GAIN,   // Resource gained - cyan
        RESOURCE_LOSS    // Resource spent - yellow
    }

    /**
     * Create and animate a floating text at the specified position
     */
    public static void show(Pane container, String text, double x, double y, TextType type) {
        Label label = new Label(text);

        // Style based on type
        switch (type) {
            case DAMAGE -> {
                label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; " +
                    "-fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 0, 0);");
            }
            case CRITICAL -> {
                label.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; " +
                    "-fx-text-fill: #ff4444; -fx-effect: dropshadow(gaussian, #ffaa00, 4, 0.8, 0, 0);");
            }
            case HEAL -> {
                label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; " +
                    "-fx-text-fill: #2ecc71; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 0, 0);");
            }
            case DODGE -> {
                label.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-font-style: italic; " +
                    "-fx-text-fill: #95a5a6; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 0, 0);");
            }
            case MISS -> {
                label.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-font-style: italic; " +
                    "-fx-text-fill: #7f8c8d; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 0, 0);");
            }
            case BUFF -> {
                label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; " +
                    "-fx-text-fill: #3498db; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 0, 0);");
            }
            case DEBUFF -> {
                label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; " +
                    "-fx-text-fill: #9b59b6; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 0, 0);");
            }
            case RESOURCE_GAIN -> {
                label.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; " +
                    "-fx-text-fill: #1abc9c; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 0, 0);");
            }
            case RESOURCE_LOSS -> {
                label.setStyle("-fx-font-size: 18px; " +
                    "-fx-text-fill: #f39c12; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 0, 0);");
            }
        }

        // Position
        label.setLayoutX(x);
        label.setLayoutY(y);
        label.setOpacity(0); // Start invisible

        container.getChildren().add(label);

        // Animation sequence
        SequentialTransition sequence = createAnimation(label, type);
        sequence.setOnFinished(e -> container.getChildren().remove(label));
        sequence.play();
    }

    /**
     * Create animation based on text type
     */
    private static SequentialTransition createAnimation(Label label, TextType type) {
        SequentialTransition sequence = new SequentialTransition();

        // Fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(100), label);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Movement and effects based on type
        ParallelTransition movement;

        if (type == TextType.CRITICAL) {
            // Critical: Pop out, shake, then float up
            movement = createCriticalAnimation(label);
        } else if (type == TextType.DODGE || type == TextType.MISS) {
            // Dodge/Miss: Slide sideways and fade
            movement = createDodgeAnimation(label);
        } else if (type == TextType.HEAL || type == TextType.RESOURCE_GAIN) {
            // Heal/Gain: Float up with slight bounce
            movement = createHealAnimation(label);
        } else {
            // Default: Simple float up
            movement = createDefaultAnimation(label);
        }

        // Fade out
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), label);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        sequence.getChildren().addAll(fadeIn, movement, fadeOut);
        return sequence;
    }

    private static ParallelTransition createCriticalAnimation(Label label) {
        ParallelTransition parallel = new ParallelTransition();

        // Scale up (pop effect)
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), label);
        scale.setFromX(0.5);
        scale.setFromY(0.5);
        scale.setToX(1.2);
        scale.setToY(1.2);

        // Shake
        TranslateTransition shake = new TranslateTransition(Duration.millis(200), label);
        shake.setByX(5);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);

        // Float up
        TranslateTransition float_ = new TranslateTransition(Duration.millis(800), label);
        float_.setByY(-80);

        parallel.getChildren().addAll(scale, shake, float_);
        return parallel;
    }

    private static ParallelTransition createDodgeAnimation(Label label) {
        ParallelTransition parallel = new ParallelTransition();

        // Slide to the side
        TranslateTransition slide = new TranslateTransition(Duration.millis(400), label);
        slide.setByX(Math.random() > 0.5 ? 40 : -40);
        slide.setByY(-20);

        parallel.getChildren().add(slide);
        return parallel;
    }

    private static ParallelTransition createHealAnimation(Label label) {
        ParallelTransition parallel = new ParallelTransition();

        // Gentle bounce up
        TranslateTransition bounce = new TranslateTransition(Duration.millis(600), label);
        bounce.setByY(-60);
        bounce.setInterpolator(Interpolator.EASE_OUT);

        // Slight scale pulse
        ScaleTransition pulse = new ScaleTransition(Duration.millis(600), label);
        pulse.setFromX(0.8);
        pulse.setFromY(0.8);
        pulse.setToX(1.0);
        pulse.setToY(1.0);

        parallel.getChildren().addAll(bounce, pulse);
        return parallel;
    }

    private static ParallelTransition createDefaultAnimation(Label label) {
        ParallelTransition parallel = new ParallelTransition();

        // Simple float up
        TranslateTransition float_ = new TranslateTransition(Duration.millis(600), label);
        float_.setByY(-50);

        parallel.getChildren().add(float_);
        return parallel;
    }

    /**
     * Show multiple damage numbers in sequence (for multi-hit abilities)
     */
    public static void showMultiHit(Pane container, int[] damages, double x, double y, boolean[] crits) {
        for (int i = 0; i < damages.length; i++) {
            int damage = damages[i];
            boolean isCrit = crits != null && i < crits.length && crits[i];

            // Offset each number slightly
            double offsetX = x + (i * 15) - (damages.length * 7.5);
            double offsetY = y - (i * 10);

            // Delay each number slightly
            final int index = i;
            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(Duration.millis(index * 150));
            delay.setOnFinished(e -> {
                String text = String.valueOf(damage);
                TextType type = isCrit ? TextType.CRITICAL : TextType.DAMAGE;
                show(container, text, offsetX, offsetY, type);
            });
            delay.play();
        }
    }
}
