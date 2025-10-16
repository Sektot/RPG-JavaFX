package com.rpg.controller;

import com.rpg.model.characters.Erou;
import com.rpg.service.TavernServiceFX;
import com.rpg.service.TavernServiceFX.DrinkType;
import com.rpg.service.dto.*;
import com.rpg.utils.DialogHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * TavernControllerFX - Controller pentru tavernă
 */
public class TavernControllerFX {

    private Stage stage;
    private Erou hero;
    private TavernServiceFX tavernService;

    private Label goldLabel;
    private Label hpLabel;
    private Label resourceLabel;
    private TextArea storyArea;

    public TavernControllerFX(Stage stage, Erou hero) {
        this.stage = stage;
        this.hero = hero;
        this.tavernService = new TavernServiceFX();
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setTop(createHeader());
        root.setCenter(createMainContent());
        root.setBottom(createFooter());

        root.setStyle("-fx-background-color: #2c1810;");

        return new Scene(root, 1000, 700);
    }

    /**
     * Header cu informații erou
     */
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #3e2723;");

        Label title = new Label("🍺 TAVERNA DIN BUCALE 🍺");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #ffab00;");

        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER);

        goldLabel = new Label("💰 Gold: " + hero.getGold());
        goldLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #ffd54f;");

        hpLabel = new Label("❤️ HP: " + hero.getViata() + "/" + hero.getViataMaxima());
        hpLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #ef5350;");

        resourceLabel = new Label("💙 " + hero.getTipResursa() + ": " +
                hero.getResursaCurenta() + "/" + hero.getResursaMaxima());
        resourceLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #42a5f5;");

        statsBox.getChildren().addAll(goldLabel, hpLabel, resourceLabel);

        header.getChildren().addAll(title, statsBox);
        return header;
    }

    /**
     * Conținut principal cu tab-uri
     */
    private TabPane createMainContent() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab restTab = new Tab("🛏️ Odihnă", createRestPanel());
        Tab gambleTab = new Tab("🎲 Gambling", createGamblePanel());
        Tab drinksTab = new Tab("🍺 Băuturi", createDrinksPanel());
        Tab storiesTab = new Tab("📖 Povești", createStoriesPanel());

        tabPane.getTabs().addAll(restTab, gambleTab, drinksTab, storiesTab);

        return tabPane;
    }

    /**
     * Panel odihnă
     */
    private VBox createRestPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(30));
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: #3e2723;");

        Label titleLabel = new Label("🛏️ ODIHNEȘTE-TE LA TAVERNĂ");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #ffab00;");

        RestInfoDTO restInfo = tavernService.getRestInfo(hero);

        TextArea infoArea = new TextArea();
        infoArea.setEditable(false);
        infoArea.setWrapText(true);
        infoArea.setPrefHeight(200);
        infoArea.setStyle("-fx-control-inner-background: #2c1810; -fx-text-fill: white; -fx-font-size: 14px;");

        StringBuilder info = new StringBuilder();
        info.append("═══════════════════════════════════\n");
        info.append("        INFORMAȚII ODIHNĂ\n");
        info.append("═══════════════════════════════════\n\n");
        info.append("💰 Cost: ").append(restInfo.getCost()).append(" gold\n\n");
        info.append("Recuperezi:\n");
        info.append("  ❤️  HP: +").append(restInfo.getHpToRecover()).append("\n");
        info.append("  💙 ").append(hero.getTipResursa()).append(": +").append(restInfo.getResourceToRecover()).append("\n\n");

        if (!restInfo.needsRest()) {
            info.append("✅ Ești deja complet odihnit!\n");
        } else if (!restInfo.canAfford()) {
            info.append("❌ Nu ai destul gold!\n");
            info.append("   Îți lipsesc: ").append(restInfo.getCost() - hero.getGold()).append(" gold\n");
        } else {
            info.append("✅ Poți să te odihnești!\n");
        }

        infoArea.setText(info.toString());

        Button restButton = new Button("💤 ODIHNEȘTE-TE");
        restButton.setMaxWidth(300);
        styleButton(restButton, "#4caf50");
        restButton.setDisable(!restInfo.canAfford() || !restInfo.needsRest());

        restButton.setOnAction(e -> handleRest());

        panel.getChildren().addAll(titleLabel, infoArea, restButton);
        return panel;
    }

    /**
     * Panel gambling
     */
    private VBox createGamblePanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(30));
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: #3e2723;");

        Label titleLabel = new Label("🎲 JOACĂ ZARURI");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #ffab00;");

        GambleInfoDTO gambleInfo = tavernService.getGambleInfo();

        TextArea rulesArea = new TextArea(gambleInfo.getRules());
        rulesArea.setEditable(false);
        rulesArea.setWrapText(true);
        rulesArea.setPrefHeight(120);
        rulesArea.setStyle("-fx-control-inner-background: #2c1810; -fx-text-fill: white; -fx-font-size: 14px;");

        HBox betBox = new HBox(10);
        betBox.setAlignment(Pos.CENTER);

        Label betLabel = new Label("Pariu:");
        betLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        Spinner<Integer> betSpinner = new Spinner<>(
                gambleInfo.getMinBet(),
                Math.min(gambleInfo.getMaxBet(), hero.getGold()),
                gambleInfo.getMinBet()
        );
        betSpinner.setEditable(true);
        betSpinner.setPrefWidth(150);

        Label goldSuffix = new Label("gold");
        goldSuffix.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        betBox.getChildren().addAll(betLabel, betSpinner, goldSuffix);

        Button gambleButton = new Button("🎲 ARUNCĂ ZARURILE");
        gambleButton.setMaxWidth(300);
        styleButton(gambleButton, "#ff9800");

        gambleButton.setOnAction(e -> handleGamble(betSpinner.getValue()));

        panel.getChildren().addAll(titleLabel, rulesArea, betBox, gambleButton);
        return panel;
    }

    /**
     * Panel băuturi
     */
    private VBox createDrinksPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(30));
        panel.setStyle("-fx-background-color: #3e2723;");

        Label titleLabel = new Label("🍺 MENIU BĂUTURI");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #ffab00;");

        VBox drinksList = new VBox(10);
        drinksList.setAlignment(Pos.CENTER);

        for (DrinkDTO drink : tavernService.getAvailableDrinks()) {
            drinksList.getChildren().add(createDrinkCard(drink));
        }

        ScrollPane scrollPane = new ScrollPane(drinksList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #3e2723; -fx-background-color: #3e2723;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        panel.getChildren().addAll(titleLabel, scrollPane);
        return panel;
    }

    private HBox createDrinkCard(DrinkDTO drink) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: #2c1810; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: #5d4037; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 10;"
        );
        card.setPrefWidth(600);

        VBox infoBox = new VBox(5);

        Label nameLabel = new Label(drink.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffab00;");

        Label descLabel = new Label(drink.getDescription());
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #bcaaa4;");

        Label effectLabel = new Label("✨ " + drink.getEffect());
        effectLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4caf50;");

        Label priceLabel = new Label("💰 " + drink.getPrice() + " gold");
        priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffd54f;");

        infoBox.getChildren().addAll(nameLabel, descLabel, effectLabel, priceLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button buyButton = new Button("Cumpără");
        styleButton(buyButton, "#8d6e63");
        buyButton.setDisable(hero.getGold() < drink.getPrice());
        buyButton.setOnAction(e -> handleBuyDrink(drink.getType()));

        card.getChildren().addAll(infoBox, spacer, buyButton);
        return card;
    }

    /**
     * Panel povești
     */
    private VBox createStoriesPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(30));
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: #3e2723;");

        Label titleLabel = new Label("📖 POVEȘTI DIN TAVERNĂ");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #ffab00;");

        storyArea = new TextArea();
        storyArea.setEditable(false);
        storyArea.setWrapText(true);
        storyArea.setPrefHeight(300);
        storyArea.setStyle(
                "-fx-control-inner-background: #2c1810; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 15px; " +
                        "-fx-font-family: 'Courier New';"
        );
        VBox.setVgrow(storyArea, Priority.ALWAYS);

        // Adaugă o poveste inițială
        storyArea.setText(tavernService.getRandomTavernStory());

        Button newStoryButton = new Button("📖 Ascultă Altă Poveste");
        styleButton(newStoryButton, "#795548");
        newStoryButton.setOnAction(e -> {
            storyArea.setText(tavernService.getRandomTavernStory());
        });

        panel.getChildren().addAll(titleLabel, storyArea, newStoryButton);
        return panel;
    }

    /**
     * Footer cu navigare
     */
    private HBox createFooter() {
        HBox footer = new HBox(15);
        footer.setPadding(new Insets(15));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #3e2723;");

        Button backButton = new Button("🔙 Înapoi la Oraș");
        styleButton(backButton, "#e74c3c");
        backButton.setOnAction(e -> {
            TownMenuController townController = new TownMenuController(stage, hero);
            stage.setScene(townController.createScene());
        });

        footer.getChildren().add(backButton);
        return footer;
    }

    // ==================== HANDLERS ====================

    private void handleRest() {
        if (DialogHelper.showConfirmation("Odihnă",
                "Vrei să te odihnești pentru 50 gold?")) {

            AbilityDTO.RestResultDTO result = tavernService.rest(hero);

            if (result.isSuccess()) {
                DialogHelper.showSuccess("Odihnit!",
                        "Te-ai odihnit!\n\n" +
                                "❤️  HP recuperat: " + result.getHpRecovered() + "\n" +
                                "💙 " + hero.getTipResursa() + " recuperat: " + result.getResourceRecovered());

                refreshUI();
            } else {
                DialogHelper.showError("Eroare", result.getMessage());
            }
        }
    }

    private void handleGamble(int bet) {
        GambleResultDTO result = tavernService.gambleDice(hero, bet);

        if (!result.isSuccess()) {
            DialogHelper.showError("Eroare", result.getMessage());
            return;
        }

        // Afișează rezultatul
        if (result.isWin()) {
            DialogHelper.showSuccess("Câștig!", result.getMessage());
        } else if (result.isDraw()) {
            DialogHelper.showInfo("Egalitate", result.getMessage());
        } else {
            DialogHelper.showWarning("Pierdere", result.getMessage());
        }

        refreshUI();
    }

    private void handleBuyDrink(DrinkType drinkType) {
        DrinkResultDTO result = tavernService.buyDrink(hero, drinkType);

        if (result.isSuccess()) {
            DialogHelper.showSuccess("Băutură cumpărată!", result.getMessage());
            refreshUI();
        } else {
            DialogHelper.showError("Eroare", result.getMessage());
        }
    }

    private void refreshUI() {
        goldLabel.setText("💰 Gold: " + hero.getGold());
        hpLabel.setText("❤️ HP: " + hero.getViata() + "/" + hero.getViataMaxima());
        resourceLabel.setText("💙 " + hero.getTipResursa() + ": " +
                hero.getResursaCurenta() + "/" + hero.getResursaMaxima());

        // Recreează scene-ul pentru a actualiza toate componentele
        stage.setScene(createScene());
    }

    private void styleButton(Button btn, String color) {
        btn.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 12px 30px; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e ->
                btn.setStyle(
                        "-fx-font-size: 16px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-color: derive(" + color + ", 20%); " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 12px 30px; " +
                                "-fx-background-radius: 8; " +
                                "-fx-cursor: hand;"
                )
        );

        btn.setOnMouseExited(e ->
                btn.setStyle(
                        "-fx-font-size: 16px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-color: " + color + "; " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 12px 30px; " +
                                "-fx-background-radius: 8; " +
                                "-fx-cursor: hand;"
                )
        );
    }
}