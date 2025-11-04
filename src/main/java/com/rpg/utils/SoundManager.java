package com.rpg.utils;

import javafx.scene.media.AudioClip;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Sound manager for playing battle sound effects
 * Supports loading, caching, and playing audio clips
 */
public class SoundManager {

    private static final Map<SoundEffect, AudioClip> soundCache = new HashMap<>();
    private static boolean muted = false;
    private static double volume = 0.5; // Default 50% volume

    /**
     * Available sound effects
     */
    public enum SoundEffect {
        // Combat sounds
        ATTACK_HIT("attack_hit.wav"),
        ATTACK_MISS("attack_miss.wav"),
        CRITICAL_HIT("critical_hit.wav"),
        DODGE("dodge.wav"),

        // Damage types
        DAMAGE_LIGHT("damage_light.wav"),
        DAMAGE_HEAVY("damage_heavy.wav"),

        // Healing & buffs
        HEAL("heal.wav"),
        BUFF_APPLY("buff_apply.wav"),
        DEBUFF_APPLY("debuff_apply.wav"),

        // Abilities
        ABILITY_CAST("ability_cast.wav"),
        ULTIMATE_CAST("ultimate_cast.wav"),

        // UI sounds
        BUTTON_CLICK("button_click.wav"),
        POTION_USE("potion_use.wav"),

        // Battle events
        VICTORY("victory.wav"),
        DEFEAT("defeat.wav"),
        LEVEL_UP("level_up.wav"),

        // Special
        COMBO("combo.wav"),
        SCREEN_SHAKE("screen_shake.wav");

        private final String filename;

        SoundEffect(String filename) {
            this.filename = filename;
        }

        public String getFilename() {
            return filename;
        }
    }

    /**
     * Play a sound effect
     */
    public static void play(SoundEffect effect) {
        if (muted || effect == null) {
            return;
        }

        try {
            AudioClip clip = getOrLoadSound(effect);
            if (clip != null) {
                clip.setVolume(volume);
                clip.play();
            }
        } catch (Exception e) {
            // Silently fail if sound not found (game continues without sound)
            System.err.println("Could not play sound: " + effect.getFilename() + " - " + e.getMessage());
        }
    }

    /**
     * Play a sound effect with custom volume
     */
    public static void play(SoundEffect effect, double customVolume) {
        if (muted || effect == null) {
            return;
        }

        try {
            AudioClip clip = getOrLoadSound(effect);
            if (clip != null) {
                clip.setVolume(Math.min(1.0, Math.max(0.0, customVolume)));
                clip.play();
            }
        } catch (Exception e) {
            System.err.println("Could not play sound: " + effect.getFilename() + " - " + e.getMessage());
        }
    }

    /**
     * Get or load sound from cache
     */
    private static AudioClip getOrLoadSound(SoundEffect effect) {
        if (soundCache.containsKey(effect)) {
            return soundCache.get(effect);
        }

        try {
            // Try to load from resources
            URL soundURL = SoundManager.class.getResource("/sounds/" + effect.getFilename());

            if (soundURL == null) {
                // Sound file not found, cache null to avoid repeated lookups
                soundCache.put(effect, null);
                return null;
            }

            AudioClip clip = new AudioClip(soundURL.toExternalForm());
            soundCache.put(effect, clip);
            return clip;
        } catch (Exception e) {
            System.err.println("Failed to load sound: " + effect.getFilename() + " - " + e.getMessage());
            soundCache.put(effect, null);
            return null;
        }
    }

    /**
     * Set master volume (0.0 to 1.0)
     */
    public static void setVolume(double vol) {
        volume = Math.min(1.0, Math.max(0.0, vol));
    }

    /**
     * Get current volume
     */
    public static double getVolume() {
        return volume;
    }

    /**
     * Mute all sounds
     */
    public static void mute() {
        muted = true;
    }

    /**
     * Unmute all sounds
     */
    public static void unmute() {
        muted = false;
    }

    /**
     * Toggle mute
     */
    public static void toggleMute() {
        muted = !muted;
    }

    /**
     * Check if muted
     */
    public static boolean isMuted() {
        return muted;
    }

    /**
     * Clear sound cache (useful for reloading sounds)
     */
    public static void clearCache() {
        soundCache.clear();
    }

    /**
     * Preload common sounds for faster playback
     */
    public static void preloadCommonSounds() {
        // Preload frequently used sounds
        getOrLoadSound(SoundEffect.ATTACK_HIT);
        getOrLoadSound(SoundEffect.CRITICAL_HIT);
        getOrLoadSound(SoundEffect.DAMAGE_LIGHT);
        getOrLoadSound(SoundEffect.HEAL);
        getOrLoadSound(SoundEffect.BUTTON_CLICK);
    }

    /**
     * Play sound based on damage amount (auto-select appropriate sound)
     */
    public static void playDamageSound(int damage, int maxHP, boolean isCrit) {
        if (isCrit) {
            play(SoundEffect.CRITICAL_HIT);
        } else {
            double damagePercent = (double) damage / maxHP;
            if (damagePercent >= 0.3) {
                play(SoundEffect.DAMAGE_HEAVY);
            } else {
                play(SoundEffect.DAMAGE_LIGHT);
            }
        }
    }
}
