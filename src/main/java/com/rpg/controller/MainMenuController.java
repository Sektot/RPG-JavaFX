package com.rpg.controller;

import com.rpg.service.SaveLoadServiceFX;
import com.rpg.utils.DialogHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller pentru meniul principal - VERSIUNE ACTUALIZATĂ
 */
public class MainMenuController {

    private Stage stage;
    private SaveLoadServiceFX saveLoadService;

    public MainMenuController(Stage stage) {
        this.stage = stage;
        this.saveLoadService = new SaveLoadServiceFX();
    }

    public Scene createScene() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(50));
        layout.setStyle("-fx-background-color: linear-gradient(to bottom, #FCD116 0%, #CE1126 33%, #002B7F 66%, #CE1126 100%);");

        // Title
        Label title = new Label("🏛️ RPG ROMÂNESC 🏛️");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Legenda din Bucale");
        subtitle.setStyle("-fx-font-size: 18px; -fx-text-fill: #FFD700;");

        // Buttons
        Button newGameBtn = createStyledButton("🎮 Joc Nou");
        Button loadGameBtn = createStyledButton("📂 Încarcă Joc");
        Button optionsBtn = createStyledButton("⚙️ Opțiuni");
        Button exitBtn = createStyledButton("🚪 Ieșire");

        // Event handlers
        newGameBtn.setOnAction(e -> startNewGame());
        loadGameBtn.setOnAction(e -> loadGame());
        optionsBtn.setOnAction(e -> openOptions());
        exitBtn.setOnAction(e -> exitGame());

        layout.getChildren().addAll(title, subtitle, newGameBtn, loadGameBtn, optionsBtn, exitBtn);

        return new Scene(layout, 800, 600);
    }

    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-min-width: 250px; " +
                        "-fx-pref-height: 50px; " +
                        "-fx-background-color: #333; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;"
        );

        // Hover effect
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-min-width: 250px; " +
                        "-fx-pref-height: 50px; " +
                        "-fx-background-color: #555; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;"
        ));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-min-width: 250px; " +
                        "-fx-pref-height: 50px; " +
                        "-fx-background-color: #333; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;"
        ));

        return btn;
    }

    private void startNewGame() {
        CharacterSelectionController charController = new CharacterSelectionController(stage);
        stage.setScene(charController.createScene());
    }

    private void loadGame() {
        // Verifică dacă există salvări
        if (!saveLoadService.hasSaves()) {
            DialogHelper.showInfo("Nu există salvări",
                    "Nu ai jocuri salvate!\n\n" +
                            "Creează un erou nou pentru a începe.");
            return;
        }

        // Deschide controller-ul de load
        SaveLoadControllerFX saveLoadController = new SaveLoadControllerFX(stage);
        stage.setScene(saveLoadController.createScene());
    }

    private void openOptions() {
        OptionsController optionsController = new OptionsController(stage, () -> {
            // Return to main menu
            stage.setScene(createScene());
        });
        stage.setScene(optionsController.createScene());
    }

    private void exitGame() {
        if (DialogHelper.showConfirmation("Confirmare Ieșire",
                "Ești sigur că vrei să ieși din joc?")) {
            System.exit(0);
        }
    }
}