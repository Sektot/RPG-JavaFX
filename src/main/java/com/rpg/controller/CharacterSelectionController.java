package com.rpg.controller;

import com.rpg.factory.CharacterFactory;
import com.rpg.model.characters.Erou;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CharacterSelectionController {

    private Stage stage;
    private TextField nameField = new TextField();
    private String selectedClass = "";
    private CheckBox godModeCheckbox = new CheckBox("⚡ GOD MODE (Testing)");

    public CharacterSelectionController(Stage stage) {
        this.stage = stage;
    }

    public Scene createScene() {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #1a1a2e;");

        // Title
        Label title = new Label("⚔️ Creează-ți Eroul ⚔️");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #FFD700;");

        // Name input
        nameField.setPromptText("Numele eroului");
        nameField.setStyle("-fx-font-size: 16px; -fx-padding: 10;");
        nameField.setMaxWidth(300);

        // Class selection
        Label classLabel = new Label("Alege Clasa:");
        classLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

        HBox classButtons = new HBox(10);
        classButtons.setAlignment(Pos.CENTER);

        // Create all buttons first
        Button warriorBtn = new Button("⚔️ MOLDOVEAN\n(Warrior)");
        Button mageBtn = new Button("🔮 ARDELEAN\n(Wizard)");
        Button rogueBtn = new Button("🗡️ OLTEAN\n(Rogue)");

        // Style constants
        final String normalWarriorStyle = "-fx-font-size: 14px; -fx-padding: 15; -fx-background-color: #8B0000; -fx-text-fill: white;";
        final String selectedWarriorStyle = "-fx-font-size: 14px; -fx-padding: 15; -fx-background-color: #FF0000; -fx-text-fill: white; -fx-border-color: gold; -fx-border-width: 3;";
        final String normalMageStyle = "-fx-font-size: 14px; -fx-padding: 15; -fx-background-color: #00008B; -fx-text-fill: white;";
        final String selectedMageStyle = "-fx-font-size: 14px; -fx-padding: 15; -fx-background-color: #0000FF; -fx-text-fill: white; -fx-border-color: gold; -fx-border-width: 3;";
        final String normalRogueStyle = "-fx-font-size: 14px; -fx-padding: 15; -fx-background-color: #2F4F2F; -fx-text-fill: white;";
        final String selectedRogueStyle = "-fx-font-size: 14px; -fx-padding: 15; -fx-background-color: #228B22; -fx-text-fill: white; -fx-border-color: gold; -fx-border-width: 3;";

        // Set initial styles
        warriorBtn.setStyle(normalWarriorStyle);
        mageBtn.setStyle(normalMageStyle);
        rogueBtn.setStyle(normalRogueStyle);

        // Set action handlers
        warriorBtn.setOnAction(e -> {
            selectedClass = "MOLDOVEAN";
            warriorBtn.setStyle(selectedWarriorStyle);
            mageBtn.setStyle(normalMageStyle);
            rogueBtn.setStyle(normalRogueStyle);
        });

        mageBtn.setOnAction(e -> {
            selectedClass = "ARDELEAN";
            mageBtn.setStyle(selectedMageStyle);
            warriorBtn.setStyle(normalWarriorStyle);
            rogueBtn.setStyle(normalRogueStyle);
        });

        rogueBtn.setOnAction(e -> {
            selectedClass = "OLTEAN";
            rogueBtn.setStyle(selectedRogueStyle);
            warriorBtn.setStyle(normalWarriorStyle);
            mageBtn.setStyle(normalMageStyle);
        });

        classButtons.getChildren().addAll(warriorBtn, mageBtn, rogueBtn);

        // GOD MODE checkbox
        godModeCheckbox.setStyle("-fx-font-size: 16px; -fx-text-fill: #FFD700; -fx-font-weight: bold;");
        Label godModeInfo = new Label("(Level 30, 50k Gold, All Abilities, Epic Gear)");
        godModeInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #FFA500;");
        VBox godModeBox = new VBox(5, godModeCheckbox, godModeInfo);
        godModeBox.setAlignment(Pos.CENTER);

        Button createBtn = new Button("✨ Creează Erou ✨");
        createBtn.setStyle("-fx-font-size: 18px; -fx-padding: 15 30; -fx-background-color: #e94560; -fx-text-fill: white; -fx-font-weight: bold;");
        createBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty() || selectedClass.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Completează toate câmpurile!").showAndWait();
                return;
            }

            // FIX: Convertește string-ul în enum CharacterClass
            CharacterFactory.CharacterClass charClass = switch (selectedClass) {
                case "MOLDOVEAN" -> CharacterFactory.CharacterClass.WARRIOR;
                case "ARDELEAN" -> CharacterFactory.CharacterClass.WIZARD;
                case "OLTEAN" -> CharacterFactory.CharacterClass.ROGUE;
                default -> throw new IllegalArgumentException("Clasă necunoscută: " + selectedClass);
            };

            // ⚡ GOD MODE: Check checkbox or name prefix
            Erou hero;
            if (godModeCheckbox.isSelected() || name.toUpperCase().startsWith("GOD") || name.toUpperCase().startsWith("TEST")) {
                System.out.println("⚡⚡⚡ GOD MODE ENABLED! ⚡⚡⚡");
                hero = CharacterFactory.createGodModeHero(charClass, name);
            } else {
                hero = CharacterFactory.create(charClass, name);
            }
            stage.setScene(new TownMenuController(stage, hero).createScene());
        });

        root.getChildren().addAll(title, nameField, classLabel, classButtons, godModeBox, createBtn);
        return new Scene(root, 1900, 1080);
    }
}