package com.rpg.controller;

import com.rpg.factory.CharacterFactory;
import com.rpg.model.characters.Erou;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CharacterSelectionController {

    private Stage stage;
    private TextField nameField = new TextField();
    private String selectedClass = "";

    public CharacterSelectionController(Stage stage) {
        this.stage = stage;
    }

    public Scene createScene() {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));

        nameField.setPromptText("Numele eroului");

        HBox classButtons = new HBox(10);
        classButtons.setAlignment(Pos.CENTER);

        Button warriorBtn = new Button("MOLDOVEAN");
        warriorBtn.setOnAction(e -> selectedClass = "MOLDOVEAN");

        Button mageBtn = new Button("ARDELEAN");
        mageBtn.setOnAction(e -> selectedClass = "ARDELEAN");

        Button rogueBtn = new Button("OLTEAN");
        rogueBtn.setOnAction(e -> selectedClass = "OLTEAN");

        classButtons.getChildren().addAll(warriorBtn, mageBtn, rogueBtn);

        Button createBtn = new Button("Creează Erou");
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

            Erou hero = CharacterFactory.create(charClass, name);
             stage.setScene(new TownMenuController(stage, hero).createScene()); // uncomment when TownMenuController exists
        });

        root.getChildren().addAll(new Label("Alege-ți Eroul"), nameField, classButtons, createBtn);
        return new Scene(root, 800, 600);
    }
}