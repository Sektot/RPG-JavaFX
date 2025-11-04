package com.rpg.controller;

import com.rpg.service.SaveLoadServiceFX;
import com.rpg.utils.DialogHelper;
import com.rpg.utils.SpriteManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller pentru meniul principal - VERSIUNE ACTUALIZATĂ
 *
 * 🎨 SPRITE SUPPORT:
 * Place UI sprites in: resources/sprites/ui/main_menu/
 * - background.png - Main menu background
 * - button_normal.png - Normal button texture
 * - button_hover.png - Hover button texture
 * - title_banner.png - Optional title decoration
 * - frame.png - Optional menu frame/border
 */
public class MainMenuController {

    private Stage stage;
    private SaveLoadServiceFX saveLoadService;

    // 🎨 UI Textures
    private Image backgroundTexture;
    private Image buttonNormalTexture;
    private Image buttonHoverTexture;
    private Image titleBannerTexture;
    private Image frameTexture;

    public MainMenuController(Stage stage) {
        this.stage = stage;
        this.saveLoadService = new SaveLoadServiceFX();
        loadTextures();
    }

    /**
     * 🎨 Load all UI textures
     */
    private void loadTextures() {
        backgroundTexture = SpriteManager.getSprite("ui/main_menu", "background");
        buttonNormalTexture = SpriteManager.getSprite("ui/main_menu", "button_normal");
        buttonHoverTexture = SpriteManager.getSprite("ui/main_menu", "button_hover");
        titleBannerTexture = SpriteManager.getSprite("ui/main_menu", "title_banner");
        frameTexture = SpriteManager.getSprite("ui/main_menu", "frame");
    }

    public Scene createScene() {
        // Create main container with StackPane for layering
        StackPane root = new StackPane();

        // 🎨 Add background image (sprite or fallback)
        ImageView backgroundImageView = new ImageView();
        if (backgroundTexture != null) {
            // Use custom sprite if available
            backgroundImageView.setImage(backgroundTexture);
            System.out.println("✅ Using custom main menu background");
        } else {
            // Fallback to hardcoded background
            try {
                Image backgroundImage = new Image(getClass().getResourceAsStream("/com/garaDeNord.png"));
                backgroundImageView.setImage(backgroundImage);
                System.out.println("⚠️ Using fallback background (no sprite found)");
            } catch (Exception e) {
                System.err.println("❌ Could not load any background image: " + e.getMessage());
            }
        }
        backgroundImageView.setPreserveRatio(false); // Stretch to fill

        // Create your existing menu layout
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(50));
        //layout.setStyle("-fx-background-color: rgba(252, 209, 22, 0.3);");

        // Title
        Label title = new Label("🏛️ RPG ROMÂNESC 🏛️");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 0, 1, 1);");

        Label subtitle = new Label("Legenda din Bucale");
        subtitle.setStyle("-fx-font-size: 18px; -fx-text-fill: #FFD700; -fx-effect: dropshadow(gaussian, black, 2, 0, 1, 1);");

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
        root.getChildren().addAll(backgroundImageView, layout);

        Scene scene = new Scene(root, 1900, 1080);

        // BIND THE IMAGE SIZE TO SCENE SIZE - This is the key part!
        backgroundImageView.fitWidthProperty().bind(scene.widthProperty());
        backgroundImageView.fitHeightProperty().bind(scene.heightProperty());

        return scene;
    }


    /**
     * 🎨 Create styled button with texture support
     */
    private Button createStyledButton(String text) {
        Button btn = new Button(text);

        // Check if we have button textures
        if (buttonNormalTexture != null && buttonHoverTexture != null) {
            // Use texture-based styling
            String normalStyle = String.format(
                    "-fx-font-size: 16px; " +
                    "-fx-min-width: 250px; " +
                    "-fx-pref-height: 50px; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-image: url('%s'); " +
                    "-fx-background-size: cover; " +
                    "-fx-background-repeat: no-repeat; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0; " +
                    "-fx-effect: dropshadow(gaussian, black, 3, 0, 0, 2);",
                    buttonNormalTexture.getUrl()
            );

            String hoverStyle = String.format(
                    "-fx-font-size: 16px; " +
                    "-fx-min-width: 250px; " +
                    "-fx-pref-height: 50px; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-image: url('%s'); " +
                    "-fx-background-size: cover; " +
                    "-fx-background-repeat: no-repeat; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0; " +
                    "-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.5), 5, 0, 0, 0);",
                    buttonHoverTexture.getUrl()
            );

            btn.setStyle(normalStyle);
            btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
            btn.setOnMouseExited(e -> btn.setStyle(normalStyle));

        } else {
            // Fallback to color-based styling (original)
            String normalStyle =
                    "-fx-font-size: 16px; " +
                    "-fx-min-width: 250px; " +
                    "-fx-pref-height: 50px; " +
                    "-fx-background-color: #333; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 10; " +
                    "-fx-cursor: hand;";

            String hoverStyle =
                    "-fx-font-size: 16px; " +
                    "-fx-min-width: 250px; " +
                    "-fx-pref-height: 50px; " +
                    "-fx-background-color: #555; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 10; " +
                    "-fx-cursor: hand;";

            btn.setStyle(normalStyle);
            btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
            btn.setOnMouseExited(e -> btn.setStyle(normalStyle));
        }

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