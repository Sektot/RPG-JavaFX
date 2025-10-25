package com.rpg.controller;

import com.rpg.utils.DialogHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Options/Settings Controller for game configuration
 */
public class OptionsController {

    private Stage stage;
    private Runnable onBackCallback;

    // Display settings
    private static boolean isFullscreen = false;
    private static double currentWidth = 1200;
    private static double currentHeight = 800;

    public OptionsController(Stage stage, Runnable onBackCallback) {
        this.stage = stage;
        this.onBackCallback = onBackCallback;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        root.setTop(createHeader());
        root.setCenter(createOptionsContent());
        root.setBottom(createFooter());

        return new Scene(root, 800, 600);
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #16213e;");

        Label title = new Label("⚙️ OPTIONS");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #f1c40f;");

        header.getChildren().add(title);
        return header;
    }

    private VBox createOptionsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: #1a1a2e;");

        // Display Settings Section
        VBox displaySection = createDisplaySettings();

        content.getChildren().addAll(displaySection);
        return content;
    }

    private VBox createDisplaySettings() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");
        section.setMaxWidth(600);

        Label sectionTitle = new Label("🖥️ DISPLAY SETTINGS");
        sectionTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f1c40f;");

        // Fullscreen Toggle
        HBox fullscreenBox = new HBox(15);
        fullscreenBox.setAlignment(Pos.CENTER_LEFT);

        Label fullscreenLabel = new Label("Fullscreen Mode:");
        fullscreenLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        fullscreenLabel.setPrefWidth(200);

        CheckBox fullscreenCheckBox = new CheckBox();
        fullscreenCheckBox.setSelected(isFullscreen);
        fullscreenCheckBox.setStyle("-fx-text-fill: white;");

        Button applyFullscreenBtn = new Button("Apply");
        styleButton(applyFullscreenBtn, "#27ae60");
        applyFullscreenBtn.setOnAction(e -> {
            isFullscreen = fullscreenCheckBox.isSelected();
            stage.setFullScreen(isFullscreen);

            if (isFullscreen) {
                DialogHelper.showInfo("Fullscreen Mode",
                    "Fullscreen mode enabled!\n\nPress ESC to exit fullscreen.");
            } else {
                DialogHelper.showInfo("Windowed Mode", "Windowed mode enabled!");
            }
        });

        fullscreenBox.getChildren().addAll(fullscreenLabel, fullscreenCheckBox, applyFullscreenBtn);

        // Resolution Settings (only for windowed mode)
        HBox resolutionBox = new HBox(15);
        resolutionBox.setAlignment(Pos.CENTER_LEFT);

        Label resolutionLabel = new Label("Window Resolution:");
        resolutionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        resolutionLabel.setPrefWidth(200);

        ComboBox<String> resolutionComboBox = new ComboBox<>();
        resolutionComboBox.getItems().addAll(
            "800x600",
            "1024x768",
            "1280x720",
            "1280x800",
            "1366x768",
            "1600x900",
            "1920x1080"
        );
        resolutionComboBox.setValue((int)currentWidth + "x" + (int)currentHeight);
        resolutionComboBox.setStyle("-fx-font-size: 14px;");
        resolutionComboBox.setPrefWidth(150);

        Button applyResolutionBtn = new Button("Apply");
        styleButton(applyResolutionBtn, "#3498db");
        applyResolutionBtn.setOnAction(e -> {
            String selected = resolutionComboBox.getValue();
            if (selected != null) {
                String[] parts = selected.split("x");
                currentWidth = Double.parseDouble(parts[0]);
                currentHeight = Double.parseDouble(parts[1]);

                // Check if resolution fits screen
                Screen screen = Screen.getPrimary();
                double screenWidth = screen.getBounds().getWidth();
                double screenHeight = screen.getBounds().getHeight();

                if (currentWidth > screenWidth || currentHeight > screenHeight) {
                    DialogHelper.showWarning("Resolution Too Large",
                        "The selected resolution (" + selected + ") is larger than your screen!\n" +
                        "Screen size: " + (int)screenWidth + "x" + (int)screenHeight);
                    return;
                }

                stage.setWidth(currentWidth);
                stage.setHeight(currentHeight);
                stage.centerOnScreen();

                DialogHelper.showSuccess("Resolution Changed",
                    "Window resolution set to " + selected);
            }
        });

        resolutionBox.getChildren().addAll(resolutionLabel, resolutionComboBox, applyResolutionBtn);

        // Disable resolution settings when in fullscreen
        fullscreenCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            resolutionComboBox.setDisable(newVal);
            applyResolutionBtn.setDisable(newVal);
        });
        resolutionComboBox.setDisable(isFullscreen);
        applyResolutionBtn.setDisable(isFullscreen);

        // Current Display Info
        VBox infoBox = new VBox(5);
        infoBox.setPadding(new Insets(10));
        infoBox.setStyle("-fx-background-color: #0f0f1e; -fx-background-radius: 5;");

        Label infoTitle = new Label("💡 Display Information:");
        infoTitle.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 14px; -fx-font-weight: bold;");

        Screen screen = Screen.getPrimary();
        Label screenInfo = new Label(String.format("Screen Resolution: %.0fx%.0f",
            screen.getBounds().getWidth(), screen.getBounds().getHeight()));
        screenInfo.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px;");

        Label currentInfo = new Label(String.format("Current Window: %.0fx%.0f",
            stage.getWidth(), stage.getHeight()));
        currentInfo.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px;");

        Label fullscreenInfo = new Label("Fullscreen: " + (stage.isFullScreen() ? "ON" : "OFF"));
        fullscreenInfo.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px;");

        infoBox.getChildren().addAll(infoTitle, screenInfo, currentInfo, fullscreenInfo);

        section.getChildren().addAll(
            sectionTitle,
            new Separator(),
            fullscreenBox,
            resolutionBox,
            new Separator(),
            infoBox
        );

        return section;
    }

    private HBox createFooter() {
        HBox footer = new HBox(20);
        footer.setPadding(new Insets(15));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #16213e;");

        Button backButton = new Button("🔙 Back");
        styleButton(backButton, "#e74c3c");
        backButton.setOnAction(e -> {
            if (onBackCallback != null) {
                onBackCallback.run();
            }
        });

        footer.getChildren().add(backButton);
        return footer;
    }

    private void styleButton(Button button, String color) {
        button.setStyle(
            "-fx-background-color: " + color + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 16px; " +
            "-fx-padding: 10 30; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e -> button.setOpacity(0.8));
        button.setOnMouseExited(e -> button.setOpacity(1.0));
    }

    // Static getters for other controllers to use
    public static double getCurrentWidth() {
        return currentWidth;
    }

    public static double getCurrentHeight() {
        return currentHeight;
    }

    public static boolean isFullscreen() {
        return isFullscreen;
    }
}
