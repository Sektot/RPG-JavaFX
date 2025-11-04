package com.rpg.controller;

import com.rpg.model.abilities.ConfiguredAbility;
import com.rpg.model.characters.Erou;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controller for selecting the 6-ability loadout before entering a dungeon.
 * Allows players to:
 * - Choose 6 abilities from their unlocked pool
 * - Reorder abilities
 * - Use/save/load templates
 */
public class LoadoutSelectionController {

    private Stage stage;
    private Erou hero;
    private Consumer<Boolean> onComplete; // Callback: true = enter dungeon, false = cancel

    // UI Components
    private VBox activeLoadoutBox;
    private ListView<String> availableAbilitiesView;
    private Label loadoutCountLabel;

    private List<Button> loadoutSlots = new ArrayList<>(6);

    public LoadoutSelectionController(Stage stage, Erou hero, Consumer<Boolean> onComplete) {
        this.stage = stage;
        this.hero = hero;
        this.onComplete = onComplete;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0f0f1e;");

        // Title
        VBox titleBox = new VBox(5);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(20));

        Label titleLabel = new Label("🎒 Prepare Your Loadout");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #e94560;");

        Label subtitleLabel = new Label("Choose 6 abilities to bring into the dungeon");
        subtitleLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 14px;");

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        root.setTop(titleBox);

        // Main content: Left = Active Loadout, Right = Available Abilities
        HBox mainContent = new HBox(30);
        mainContent.setPadding(new Insets(20));
        mainContent.setAlignment(Pos.CENTER);

        // Left: Active Loadout (6 slots)
        VBox leftPanel = createActiveLoadoutPanel();
        leftPanel.setMinWidth(400);

        // Right: Available Abilities
        VBox rightPanel = createAvailableAbilitiesPanel();
        rightPanel.setMinWidth(350);

        mainContent.getChildren().addAll(leftPanel, rightPanel);
        root.setCenter(mainContent);

        // Bottom: Templates and Action Buttons
        VBox bottomBox = new VBox(15);
        bottomBox.setPadding(new Insets(20));

        // Templates
        HBox templateBox = createTemplateButtons();
        templateBox.setAlignment(Pos.CENTER);

        // Action buttons
        HBox actionBox = new HBox(15);
        actionBox.setAlignment(Pos.CENTER);

        Button enterButton = new Button("⚔️ Enter Dungeon");
        styleButton(enterButton, "#27ae60", true);
        enterButton.setOnAction(e -> enterDungeon());

        Button cancelButton = new Button("◀ Back");
        styleButton(cancelButton, "#7f8c8d", false);
        cancelButton.setOnAction(e -> cancel());

        actionBox.getChildren().addAll(enterButton, cancelButton);

        bottomBox.getChildren().addAll(templateBox, actionBox);
        root.setBottom(bottomBox);

        // Load current loadout
        loadCurrentLoadout();

        return new Scene(root, 1200, 750);
    }

    private VBox createActiveLoadoutPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 10;");

        // Header
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label header = new Label("🎯 Active Loadout");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        header.setStyle("-fx-text-fill: #ecf0f1;");

        loadoutCountLabel = new Label("(0/6)");
        loadoutCountLabel.setStyle("-fx-text-fill: #e94560; -fx-font-size: 16px; -fx-font-weight: bold;");

        headerBox.getChildren().addAll(header, loadoutCountLabel);

        // Active loadout slots (6 buttons)
        activeLoadoutBox = new VBox(10);
        for (int i = 0; i < 6; i++) {
            Button slotButton = createLoadoutSlotButton(i);
            loadoutSlots.add(slotButton);
            activeLoadoutBox.getChildren().add(slotButton);
        }

        // Clear button
        Button clearButton = new Button("🗑️ Clear Loadout");
        clearButton.setStyle(
                "-fx-background-color: #e74c3c; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 13px; " +
                "-fx-padding: 8px 20px; " +
                "-fx-background-radius: 5;"
        );
        clearButton.setOnAction(e -> clearLoadout());

        panel.getChildren().addAll(headerBox, new Separator(), activeLoadoutBox, clearButton);
        return panel;
    }

    private Button createLoadoutSlotButton(int slotIndex) {
        Button button = new Button((slotIndex + 1) + ". [Empty Slot]");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle(
                "-fx-background-color: #16213e; " +
                "-fx-text-fill: #7f8c8d; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 15px; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: #2c3e50; " +
                "-fx-border-radius: 5;"
        );

        button.setOnAction(e -> removeFromLoadout(slotIndex));

        return button;
    }

    private VBox createAvailableAbilitiesPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 10;");

        // Header
        Label header = new Label("📚 Available Abilities");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        header.setStyle("-fx-text-fill: #ecf0f1;");

        Label subtitle = new Label("Click to add to loadout");
        subtitle.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px;");

        // List view
        availableAbilitiesView = new ListView<>();
        availableAbilitiesView.setStyle(
                "-fx-background-color: #16213e; " +
                "-fx-control-inner-background: #16213e; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px;"
        );
        VBox.setVgrow(availableAbilitiesView, Priority.ALWAYS);

        availableAbilitiesView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String abilityId = availableAbilitiesView.getSelectionModel().getSelectedItem();
                if (abilityId != null && !abilityId.startsWith("(")) {
                    addToLoadout(abilityId);
                }
            }
        });

        // Add button
        Button addButton = new Button("➕ Add Selected");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setStyle(
                "-fx-background-color: #3498db; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 13px; " +
                "-fx-padding: 10px; " +
                "-fx-background-radius: 5;"
        );
        addButton.setOnAction(e -> {
            String abilityId = availableAbilitiesView.getSelectionModel().getSelectedItem();
            if (abilityId != null && !abilityId.startsWith("(")) {
                addToLoadout(abilityId);
            }
        });

        panel.getChildren().addAll(header, subtitle, new Separator(), availableAbilitiesView, addButton);

        // Load available abilities
        loadAvailableAbilities();

        return panel;
    }

    private HBox createTemplateButtons() {
        HBox box = new HBox(10);

        Label label = new Label("Quick Templates:");
        label.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 13px;");

        Button balancedBtn = new Button("⚖️ Balanced");
        Button bossRushBtn = new Button("👑 Boss Rush");
        Button aoeBtn = new Button("💥 AOE Farm");
        Button survivalBtn = new Button("🛡️ Survival");

        styleTemplateButton(balancedBtn);
        styleTemplateButton(bossRushBtn);
        styleTemplateButton(aoeBtn);
        styleTemplateButton(survivalBtn);

        balancedBtn.setOnAction(e -> loadTemplate("Balanced"));
        bossRushBtn.setOnAction(e -> loadTemplate("Boss Rush"));
        aoeBtn.setOnAction(e -> loadTemplate("AOE Farm"));
        survivalBtn.setOnAction(e -> loadTemplate("Survival"));

        box.getChildren().addAll(label, balancedBtn, bossRushBtn, aoeBtn, survivalBtn);
        return box;
    }

    private void styleTemplateButton(Button button) {
        button.setStyle(
                "-fx-background-color: #34495e; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 12px; " +
                "-fx-padding: 8px 15px; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;"
        );
    }

    private void loadCurrentLoadout() {
        List<ConfiguredAbility> currentLoadout = hero.getActiveLoadoutAbilities();

        for (int i = 0; i < 6; i++) {
            if (i < currentLoadout.size()) {
                ConfiguredAbility ability = currentLoadout.get(i);
                updateSlotButton(i, ability.getDisplayName());
            } else {
                updateSlotButton(i, null);
            }
        }

        updateLoadoutCount();
    }

    private void loadAvailableAbilities() {
        availableAbilitiesView.getItems().clear();

        if (hero.getUnlockedAbilityCount() == 0) {
            availableAbilitiesView.getItems().add("(No abilities unlocked)");
            return;
        }

        // Get all unlocked abilities
        for (ConfiguredAbility ability : hero.getAbilityLoadout().getAllUnlockedAbilities()) {
            availableAbilitiesView.getItems().add(ability.getBaseAbilityId());
        }
    }

    private void addToLoadout(String abilityId) {
        if (hero.getLoadoutSize() >= 6) {
            showAlert("Loadout Full", "You can only have 6 abilities in your loadout!\nRemove an ability first.");
            return;
        }

        // Check if already in loadout
        if (hero.getActiveLoadoutAbilities().stream()
                .anyMatch(a -> a.getBaseAbilityId().equals(abilityId))) {
            showAlert("Already Added", "This ability is already in your loadout!");
            return;
        }

        boolean success = hero.addAbilityToLoadout(abilityId);
        if (success) {
            loadCurrentLoadout();
        }
    }

    private void removeFromLoadout(int slotIndex) {
        List<ConfiguredAbility> currentLoadout = hero.getActiveLoadoutAbilities();

        if (slotIndex < currentLoadout.size()) {
            ConfiguredAbility ability = currentLoadout.get(slotIndex);
            hero.removeAbilityFromLoadout(ability.getBaseAbilityId());
            loadCurrentLoadout();
        }
    }

    private void clearLoadout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear Loadout");
        confirm.setHeaderText("Clear all abilities from loadout?");
        confirm.setContentText("This will remove all 6 abilities. You can add them back afterward.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            hero.clearLoadout();
            loadCurrentLoadout();
        }
    }

    private void loadTemplate(String templateName) {
        boolean success = hero.loadLoadoutTemplate(templateName);

        if (success) {
            loadCurrentLoadout();
        } else {
            showAlert("Template Empty", "The \"" + templateName + "\" template is empty.\nCustomize your loadout and save it first!");
        }
    }

    private void updateSlotButton(int slotIndex, String abilityName) {
        Button button = loadoutSlots.get(slotIndex);

        if (abilityName == null) {
            button.setText((slotIndex + 1) + ". [Empty Slot]");
            button.setStyle(
                    "-fx-background-color: #16213e; " +
                    "-fx-text-fill: #7f8c8d; " +
                    "-fx-font-size: 14px; " +
                    "-fx-padding: 15px; " +
                    "-fx-background-radius: 5; " +
                    "-fx-border-color: #2c3e50; " +
                    "-fx-border-radius: 5;"
            );
        } else {
            button.setText((slotIndex + 1) + ". " + abilityName);
            button.setStyle(
                    "-fx-background-color: #9b59b6; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 14px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 15px; " +
                    "-fx-background-radius: 5; " +
                    "-fx-border-color: #8e44ad; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 5; " +
                    "-fx-cursor: hand;"
            );
        }
    }

    private void updateLoadoutCount() {
        int count = hero.getLoadoutSize();
        loadoutCountLabel.setText("(" + count + "/6)");

        if (count < 1) {
            loadoutCountLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 16px; -fx-font-weight: bold;");
        } else if (count < 6) {
            loadoutCountLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 16px; -fx-font-weight: bold;");
        } else {
            loadoutCountLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 16px; -fx-font-weight: bold;");
        }
    }

    private void enterDungeon() {
        if (hero.getLoadoutSize() < 1) {
            showAlert("No Abilities", "You must select at least 1 ability before entering the dungeon!");
            return;
        }

        // Save current loadout as last-used
        hero.saveLoadoutTemplate("Last Used");

        // Confirm and proceed
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Enter Dungeon");
        confirm.setHeaderText("Ready to enter with " + hero.getLoadoutSize() + " abilities?");
        confirm.setContentText("These abilities will be available in combat.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            onComplete.accept(true);
        }
    }

    private void cancel() {
        onComplete.accept(false);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void styleButton(Button button, String color, boolean primary) {
        button.setStyle(
                "-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: " + (primary ? "16px" : "14px") + "; " +
                "-fx-font-weight: " + (primary ? "bold" : "normal") + "; " +
                "-fx-padding: " + (primary ? "15px 40px" : "10px 30px") + "; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;"
        );
        button.setMinWidth(primary ? 220 : 150);
    }
}
