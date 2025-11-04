package com.rpg.ui;

import com.rpg.model.effects.BuffStack;
import com.rpg.model.effects.DebuffStack;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * Visual display for buffs and debuffs in combat
 * Shows icons with tooltips for each status effect
 */
public class StatusEffectDisplay extends HBox {

    private static final int ICON_SIZE = 32;
    private static final int MAX_VISIBLE = 8; // Maximum icons to show before scrolling

    public StatusEffectDisplay() {
        setSpacing(4);
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-padding: 5px; -fx-background-color: rgba(0, 0, 0, 0.3); -fx-background-radius: 5px;");
    }

    /**
     * Update display with current buffs
     */
    public void updateBuffs(java.util.Map<String, BuffStack> buffs) {
        getChildren().clear();

        if (buffs == null || buffs.isEmpty()) {
            Label emptyLabel = new Label("No Buffs");
            emptyLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 10px; -fx-font-style: italic;");
            getChildren().add(emptyLabel);
            return;
        }

        int count = 0;
        for (java.util.Map.Entry<String, BuffStack> entry : buffs.entrySet()) {
            if (count >= MAX_VISIBLE) {
                // Show "..." if too many buffs
                Label more = new Label("+" + (buffs.size() - MAX_VISIBLE));
                more.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
                getChildren().add(more);
                break;
            }

            StackPane icon = createBuffIcon(entry.getKey(), entry.getValue());
            getChildren().add(icon);
            count++;
        }
    }

    /**
     * Update display with current debuffs (full DebuffStack objects)
     */
    public void updateDebuffs(java.util.Map<String, DebuffStack> debuffs) {
        getChildren().clear();

        if (debuffs == null || debuffs.isEmpty()) {
            Label emptyLabel = new Label("No Debuffs");
            emptyLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 10px; -fx-font-style: italic;");
            getChildren().add(emptyLabel);
            return;
        }

        int count = 0;
        for (java.util.Map.Entry<String, DebuffStack> entry : debuffs.entrySet()) {
            if (count >= MAX_VISIBLE) {
                Label more = new Label("+" + (debuffs.size() - MAX_VISIBLE));
                more.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
                getChildren().add(more);
                break;
            }

            StackPane icon = createDebuffIcon(entry.getKey(), entry.getValue());
            getChildren().add(icon);
            count++;
        }
    }

    /**
     * Update display with simplified debuffs (just name and duration)
     * Used for enemy debuffs which use simplified format
     */
    public void updateDebuffsSimple(java.util.Map<String, Integer> debuffsSimple) {
        getChildren().clear();

        if (debuffsSimple == null || debuffsSimple.isEmpty()) {
            Label emptyLabel = new Label("No Debuffs");
            emptyLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 10px; -fx-font-style: italic;");
            getChildren().add(emptyLabel);
            return;
        }

        int count = 0;
        for (java.util.Map.Entry<String, Integer> entry : debuffsSimple.entrySet()) {
            if (count >= MAX_VISIBLE) {
                Label more = new Label("+" + (debuffsSimple.size() - MAX_VISIBLE));
                more.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
                getChildren().add(more);
                break;
            }

            StackPane icon = createSimpleDebuffIcon(entry.getKey(), entry.getValue());
            getChildren().add(icon);
            count++;
        }
    }

    /**
     * Create a buff icon with tooltip
     */
    private StackPane createBuffIcon(String buffName, BuffStack buff) {
        StackPane icon = new StackPane();

        // Background circle
        Circle background = new Circle(ICON_SIZE / 2.0);
        background.setFill(getBuffColor(buffName));
        background.setStroke(Color.WHITE);
        background.setStrokeWidth(2);

        // Icon/symbol
        Label symbol = new Label(getBuffSymbol(buffName));
        symbol.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        symbol.setTextFill(Color.WHITE);

        // Duration counter (with stacks if > 1)
        String durationText = String.valueOf(buff.getDurata());
        if (buff.getStacks() > 1) {
            durationText = buff.getStacks() + "×" + buff.getDurata();
        }
        Label duration = new Label(durationText);
        duration.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        duration.setTextFill(Color.WHITE);
        duration.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); " +
            "-fx-background-radius: 8px; -fx-padding: 1px 4px;");
        duration.setTranslateY(ICON_SIZE / 2.0 - 5);
        duration.setTranslateX(ICON_SIZE / 2.0 - 10);

        icon.getChildren().addAll(background, symbol, duration);

        // Tooltip with details
        Tooltip tooltip = new Tooltip(createBuffTooltip(buffName, buff));
        tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: #2c3e50; " +
            "-fx-text-fill: white; -fx-padding: 8px; -fx-background-radius: 5px;");
        Tooltip.install(icon, tooltip);

        return icon;
    }

    /**
     * Create a debuff icon with tooltip (full DebuffStack)
     */
    private StackPane createDebuffIcon(String debuffName, DebuffStack debuff) {
        StackPane icon = new StackPane();

        // Background circle
        Circle background = new Circle(ICON_SIZE / 2.0);
        background.setFill(getDebuffColor(debuffName));
        background.setStroke(Color.DARKRED);
        background.setStrokeWidth(2);

        // Icon/symbol
        Label symbol = new Label(getDebuffSymbol(debuffName));
        symbol.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        symbol.setTextFill(Color.WHITE);

        // Duration counter (with stacks if > 1)
        String durationText = String.valueOf(debuff.getDurata());
        if (debuff.getStacks() > 1) {
            durationText = debuff.getStacks() + "×" + debuff.getDurata();
        }
        Label duration = new Label(durationText);
        duration.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        duration.setTextFill(Color.WHITE);
        duration.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); " +
            "-fx-background-radius: 8px; -fx-padding: 1px 4px;");
        duration.setTranslateY(ICON_SIZE / 2.0 - 5);
        duration.setTranslateX(ICON_SIZE / 2.0 - 10);

        icon.getChildren().addAll(background, symbol, duration);

        // Tooltip with details
        Tooltip tooltip = new Tooltip(createDebuffTooltip(debuffName, debuff));
        tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: #2c3e50; " +
            "-fx-text-fill: white; -fx-padding: 8px; -fx-background-radius: 5px;");
        Tooltip.install(icon, tooltip);

        return icon;
    }

    /**
     * Create a simple debuff icon (just name and duration)
     * Used for enemy debuffs which use simplified format
     */
    private StackPane createSimpleDebuffIcon(String debuffName, int duration) {
        StackPane icon = new StackPane();

        // Background circle
        Circle background = new Circle(ICON_SIZE / 2.0);
        background.setFill(getDebuffColor(debuffName));
        background.setStroke(Color.DARKRED);
        background.setStrokeWidth(2);

        // Icon/symbol
        Label symbol = new Label(getDebuffSymbol(debuffName));
        symbol.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        symbol.setTextFill(Color.WHITE);

        // Duration counter
        Label durationLabel = new Label(String.valueOf(duration));
        durationLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        durationLabel.setTextFill(Color.WHITE);
        durationLabel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); " +
            "-fx-background-radius: 8px; -fx-padding: 1px 4px;");
        durationLabel.setTranslateY(ICON_SIZE / 2.0 - 5);
        durationLabel.setTranslateX(ICON_SIZE / 2.0 - 10);

        icon.getChildren().addAll(background, symbol, durationLabel);

        // Simple tooltip
        Tooltip tooltip = new Tooltip(debuffName + "\nDuration: " + duration + " turns");
        tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: #2c3e50; " +
            "-fx-text-fill: white; -fx-padding: 8px; -fx-background-radius: 5px;");
        Tooltip.install(icon, tooltip);

        return icon;
    }

    /**
     * Get color for buff type
     */
    private Color getBuffColor(String buffName) {
        String lower = buffName.toLowerCase();

        if (lower.contains("strength") || lower.contains("attack") || lower.contains("furie") || lower.contains("putere")) {
            return Color.web("#e74c3c"); // Red for attack buffs
        } else if (lower.contains("defense") || lower.contains("scut") || lower.contains("fortareata")) {
            return Color.web("#3498db"); // Blue for defense buffs
        } else if (lower.contains("speed") || lower.contains("viteza") || lower.contains("dexterity")) {
            return Color.web("#f39c12"); // Orange for speed buffs
        } else if (lower.contains("magic") || lower.contains("intelligence") || lower.contains("mana")) {
            return Color.web("#9b59b6"); // Purple for magic buffs
        } else if (lower.contains("ascuns") || lower.contains("stealth")) {
            return Color.web("#34495e"); // Dark for stealth
        } else {
            return Color.web("#2ecc71"); // Green for general buffs
        }
    }

    /**
     * Get color for debuff type
     */
    private Color getDebuffColor(String debuffName) {
        String lower = debuffName.toLowerCase();

        if (lower.contains("burn") || lower.contains("fire")) {
            return Color.web("#e74c3c"); // Red for burn
        } else if (lower.contains("poison") || lower.contains("otrava")) {
            return Color.web("#27ae60"); // Green for poison
        } else if (lower.contains("freeze") || lower.contains("ice") || lower.contains("gheata")) {
            return Color.web("#3498db"); // Blue for freeze
        } else if (lower.contains("stun") || lower.contains("shock")) {
            return Color.web("#f39c12"); // Orange for stun
        } else if (lower.contains("bleed") || lower.contains("sangerare")) {
            return Color.web("#c0392b"); // Dark red for bleed
        } else {
            return Color.web("#7f8c8d"); // Gray for general debuffs
        }
    }

    /**
     * Get symbol for buff type
     */
    private String getBuffSymbol(String buffName) {
        String lower = buffName.toLowerCase();

        if (lower.contains("strength") || lower.contains("attack") || lower.contains("furie") || lower.contains("putere")) {
            return "⚔";
        } else if (lower.contains("defense") || lower.contains("scut") || lower.contains("fortareata")) {
            return "🛡";
        } else if (lower.contains("speed") || lower.contains("viteza") || lower.contains("dexterity")) {
            return "⚡";
        } else if (lower.contains("magic") || lower.contains("intelligence") || lower.contains("mana")) {
            return "✨";
        } else if (lower.contains("ascuns") || lower.contains("stealth")) {
            return "👤";
        } else if (lower.contains("heal") || lower.contains("regen")) {
            return "💚";
        } else {
            return "⬆";
        }
    }

    /**
     * Get symbol for debuff type
     */
    private String getDebuffSymbol(String debuffName) {
        String lower = debuffName.toLowerCase();

        if (lower.contains("burn") || lower.contains("fire")) {
            return "🔥";
        } else if (lower.contains("poison") || lower.contains("otrava")) {
            return "☠";
        } else if (lower.contains("freeze") || lower.contains("ice") || lower.contains("gheata")) {
            return "❄";
        } else if (lower.contains("stun") || lower.contains("shock")) {
            return "⚡";
        } else if (lower.contains("bleed") || lower.contains("sangerare")) {
            return "🩸";
        } else if (lower.contains("silence")) {
            return "🔇";
        } else {
            return "⬇";
        }
    }

    /**
     * Create tooltip text for buff
     */
    private String createBuffTooltip(String buffName, BuffStack buff) {
        StringBuilder tooltip = new StringBuilder();
        tooltip.append("✨ ").append(buffName).append("\n");
        tooltip.append("Duration: ").append(buff.getDurata()).append(" turns\n");

        if (buff.getStacks() > 1) {
            tooltip.append("Stacks: ").append(buff.getStacks()).append("/").append(buff.getMaxStacks()).append("\n");
        }

        if (buff.getModificatori() != null && !buff.getModificatori().isEmpty()) {
            tooltip.append("\nEffects:\n");
            buff.getAllModifiers().forEach((stat, value) -> {
                int percent = (int)((value - 1.0) * 100);
                String sign = percent >= 0 ? "+" : "";
                tooltip.append("  • ").append(capitalize(stat.replace("_", " ")))
                    .append(": ").append(sign).append(percent).append("%\n");
            });
        }

        return tooltip.toString();
    }

    /**
     * Create tooltip text for debuff
     */
    private String createDebuffTooltip(String debuffName, DebuffStack debuff) {
        StringBuilder tooltip = new StringBuilder();
        tooltip.append("🔥 ").append(debuffName).append("\n");
        tooltip.append("Duration: ").append(debuff.getDurata()).append(" turns\n");

        if (debuff.getStacks() > 1) {
            tooltip.append("Stacks: ").append(debuff.getStacks()).append("/").append(debuff.getMaxStacks()).append("\n");
        }

        if (debuff.getEffects() != null && !debuff.getEffects().isEmpty()) {
            tooltip.append("\nEffects:\n");
            debuff.getAllEffects().forEach((stat, value) -> {
                if (stat.equals("damage_per_turn")) {
                    tooltip.append("  • DoT: ").append((int)value.doubleValue()).append(" damage/turn\n");
                } else {
                    int percent = (int)((value - 1.0) * 100);
                    String sign = percent >= 0 ? "+" : "";
                    tooltip.append("  • ").append(capitalize(stat.replace("_", " ")))
                        .append(": ").append(sign).append(percent).append("%\n");
                }
            });
        }

        return tooltip.toString();
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
