package com.rpg.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;

import java.util.Random;

/**
 * Screen shake utility for impactful visual feedback
 */
public class ScreenShake {

    private static final Random random = new Random();

    public enum ShakeIntensity {
        LIGHT(2, 100),      // Small shake, 100ms
        MEDIUM(5, 200),     // Medium shake, 200ms
        HEAVY(10, 300),     // Heavy shake, 300ms
        CRITICAL(15, 400);  // Massive shake, 400ms

        final double magnitude;
        final int durationMs;

        ShakeIntensity(double magnitude, int durationMs) {
            this.magnitude = magnitude;
            this.durationMs = durationMs;
        }
    }

    /**
     * Shake a node (typically the root pane)
     */
    public static void shake(Node node, ShakeIntensity intensity) {
        if (node == null) return;

        // Store original position
        double originalX = node.getTranslateX();
        double originalY = node.getTranslateY();

        Timeline timeline = new Timeline();
        int frames = intensity.durationMs / 20; // 20ms per frame

        for (int i = 0; i < frames; i++) {
            double progress = (double) i / frames;
            double magnitude = intensity.magnitude * (1 - progress); // Decay over time

            double offsetX = (random.nextDouble() - 0.5) * 2 * magnitude;
            double offsetY = (random.nextDouble() - 0.5) * 2 * magnitude;

            KeyFrame keyFrame = new KeyFrame(
                Duration.millis(i * 20),
                new KeyValue(node.translateXProperty(), originalX + offsetX),
                new KeyValue(node.translateYProperty(), originalY + offsetY)
            );
            timeline.getKeyFrames().add(keyFrame);
        }

        // Return to original position
        KeyFrame finalFrame = new KeyFrame(
            Duration.millis(intensity.durationMs),
            new KeyValue(node.translateXProperty(), originalX),
            new KeyValue(node.translateYProperty(), originalY)
        );
        timeline.getKeyFrames().add(finalFrame);

        timeline.play();
    }

    /**
     * Shake based on damage amount (auto-scale intensity)
     */
    public static void shakeForDamage(Node node, int damage, int maxHP) {
        if (node == null) return;

        double damagePercent = (double) damage / maxHP;

        ShakeIntensity intensity;
        if (damagePercent >= 0.5) {
            intensity = ShakeIntensity.CRITICAL; // 50%+ HP damage
        } else if (damagePercent >= 0.3) {
            intensity = ShakeIntensity.HEAVY;    // 30-50% HP damage
        } else if (damagePercent >= 0.15) {
            intensity = ShakeIntensity.MEDIUM;   // 15-30% HP damage
        } else {
            intensity = ShakeIntensity.LIGHT;    // < 15% HP damage
        }

        shake(node, intensity);
    }

    /**
     * Horizontal shake only (for certain effects)
     */
    public static void shakeHorizontal(Node node, ShakeIntensity intensity) {
        if (node == null) return;

        double originalX = node.getTranslateX();

        Timeline timeline = new Timeline();
        int frames = intensity.durationMs / 20;

        for (int i = 0; i < frames; i++) {
            double progress = (double) i / frames;
            double magnitude = intensity.magnitude * (1 - progress);

            double offsetX = (random.nextDouble() - 0.5) * 2 * magnitude;

            KeyFrame keyFrame = new KeyFrame(
                Duration.millis(i * 20),
                new KeyValue(node.translateXProperty(), originalX + offsetX)
            );
            timeline.getKeyFrames().add(keyFrame);
        }

        KeyFrame finalFrame = new KeyFrame(
            Duration.millis(intensity.durationMs),
            new KeyValue(node.translateXProperty(), originalX)
        );
        timeline.getKeyFrames().add(finalFrame);

        timeline.play();
    }

    /**
     * Vertical shake only (for certain effects)
     */
    public static void shakeVertical(Node node, ShakeIntensity intensity) {
        if (node == null) return;

        double originalY = node.getTranslateY();

        Timeline timeline = new Timeline();
        int frames = intensity.durationMs / 20;

        for (int i = 0; i < frames; i++) {
            double progress = (double) i / frames;
            double magnitude = intensity.magnitude * (1 - progress);

            double offsetY = (random.nextDouble() - 0.5) * 2 * magnitude;

            KeyFrame keyFrame = new KeyFrame(
                Duration.millis(i * 20),
                new KeyValue(node.translateYProperty(), originalY + offsetY)
            );
            timeline.getKeyFrames().add(keyFrame);
        }

        KeyFrame finalFrame = new KeyFrame(
            Duration.millis(intensity.durationMs),
            new KeyValue(node.translateYProperty(), originalY)
        );
        timeline.getKeyFrames().add(finalFrame);

        timeline.play();
    }
}
