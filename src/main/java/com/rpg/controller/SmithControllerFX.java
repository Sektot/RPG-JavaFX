package com.rpg.controller;

import com.rpg.model.characters.Erou;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.service.TrainerSmithServiceFX;
import com.rpg.service.TrainerSmithServiceFX.StatType;
import com.rpg.service.dto.*;
import com.rpg.utils.DialogHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * SmithControllerFX - Controller pentru Training și Enhancement
 */
public class SmithControllerFX {

    private Stage stage;
    private Erou hero;
    private TrainerSmithServiceFX smithService;

    private Label goldLabel;
    private Label shardsLabel;
    private Label statPointsLabel;

    public SmithControllerFX(Stage stage, Erou hero) {
        this.stage = stage;
        this.hero = hero;
        this.smithService = new TrainerSmithServiceFX();
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setTop(createHeader());
        root.setCenter(createMainContent());
        root.setBottom(createFooter());

        root.setStyle("-fx-background-color: #1a1a1a;");

        return new Scene(root, 1000, 700);
    }

    /**
     * Header cu resurse
     */
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #2d2d2d;");

        Label title = new Label("🔨 FIERĂRIA & SALA DE ANTRENAMENT 🔨");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #ff9800;");

        HBox resources = new HBox(30);
        resources.setAlignment(Pos.CENTER);

        goldLabel = new Label("💰 Gold: " + hero.getGold());
        goldLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #ffd54f;");

        shardsLabel = new Label("🔮 Shards: " + hero.getShards());
        shardsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #9c27b0;");

        TrainingInfoDTO trainingInfo = smithService.getTrainingInfo(hero);
        statPointsLabel = new Label("⭐ Stat Points: " + trainingInfo.getAvailableStatPoints());
        statPointsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #4caf50;");

        resources.getChildren().addAll(goldLabel, shardsLabel, statPointsLabel);

        header.getChildren().addAll(title, resources);
        return header;
    }

    /**
     * Conținut cu tab-uri
     */
    private TabPane createMainContent() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab trainingTab = new Tab("💪 Training Stats", createTrainingPanel());
        Tab enhancementTab = new Tab("✨ Enhancement", createEnhancementPanel());

        tabPane.getTabs().addAll(trainingTab, enhancementTab);

        return tabPane;
    }

    /**
     * Panel training
     */
    private VBox createTrainingPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(30));
        panel.setStyle("-fx-background-color: #2d2d2d;");

        Label titleLabel = new Label("💪 ANTRENAMENT STATS");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #ff9800;");

        TrainingInfoDTO info = smithService.getTrainingInfo(hero);

        // Stat Points Section
        if (info.hasStatPoints()) {
            VBox statPointsSection = createStatPointsSection(info);
            panel.getChildren().add(statPointsSection);

            Separator sep1 = new Separator();
            sep1.setPadding(new Insets(10, 0, 10, 0));
            panel.getChildren().add(sep1);
        }

        // Training Section (cu gold)
        VBox trainingSection = createTrainingSection(info);
        panel.getChildren().add(trainingSection);

        ScrollPane scrollPane = new ScrollPane(panel);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2d2d2d; -fx-background-color: #2d2d2d;");

        VBox wrapper = new VBox(titleLabel, scrollPane);
        wrapper.setSpacing(15);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return wrapper;
    }

    private VBox createStatPointsSection(TrainingInfoDTO info) {
        VBox section = new VBox(15);
        section.setAlignment(Pos.CENTER);

        Label sectionTitle = new Label("⭐ ALOCĂ STAT POINTS (GRATUIT)");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4caf50;");

        Label pointsLabel = new Label("Puncte disponibile: " + info.getAvailableStatPoints());
        pointsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");

        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER);

        for (StatType statType : StatType.values()) {
            statsBox.getChildren().add(createStatCard(statType, info, true));
        }

        section.getChildren().addAll(sectionTitle, pointsLabel, statsBox);
        return section;
    }

    private VBox createTrainingSection(TrainingInfoDTO info) {
        VBox section = new VBox(15);
        section.setAlignment(Pos.CENTER);

        Label sectionTitle = new Label("💰 ANTRENAMENT CU GOLD");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ff9800;");

        Label costLabel = new Label("Cost: " + info.getTrainingCost() + " gold per nivel");
        costLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #ffd54f;");

        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER);

        for (StatType statType : StatType.values()) {
            statsBox.getChildren().add(createStatCard(statType, info, false));
        }

        section.getChildren().addAll(sectionTitle, costLabel, statsBox);
        return section;
    }

    private VBox createStatCard(StatType statType, TrainingInfoDTO info, boolean isFree) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: #3d3d3d; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: " + (isFree ? "#4caf50" : "#ff9800") + "; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 10;"
        );
        card.setPrefWidth(250);

        Label nameLabel = new Label(statType.getDisplayName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        int currentValue = switch (statType) {
            case STRENGTH -> info.getCurrentStrength();
            case DEXTERITY -> info.getCurrentDexterity();
            case INTELLIGENCE -> info.getCurrentIntelligence();
        };

        Label valueLabel = new Label("Valoare: " + currentValue);
        valueLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #4caf50; -fx-font-weight: bold;");

        Label descLabel = new Label(statType.getDescription());
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(220);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #bdbdbd;");

        Button upgradeButton;
        if (isFree) {
            upgradeButton = new Button("⭐ ALOCĂ +1");
            upgradeButton.setDisable(!info.hasStatPoints());
            styleButton(upgradeButton, "#4caf50");
            upgradeButton.setOnAction(e -> handleAllocateStatPoint(statType));
        } else {
            upgradeButton = new Button("💰 ANTRENEAZĂ +1");
            upgradeButton.setDisable(!info.canAffordTraining());
            styleButton(upgradeButton, "#ff9800");
            upgradeButton.setOnAction(e -> handleTrainStat(statType));
        }

        card.getChildren().addAll(nameLabel, valueLabel, descLabel, upgradeButton);
        return card;
    }

    /**
     * Panel enhancement
     */
    private VBox createEnhancementPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #2d2d2d;");

        Label titleLabel = new Label("✨ ENHANCEMENT ECHIPAMENT");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #9c27b0;");

        SmithInfoDTO smithInfo = smithService.getSmithInfo(hero);

        Label infoLabel = new Label(
                "🔮 Shards: " + smithInfo.getAvailableShards() + " | " +
                        "📜 Scrolls: " + smithInfo.getEnchantScrollCount() + " | " +
                        "⚔️ Iteme: " + smithInfo.getEnhanceableItemsCount()
        );
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        ListView<EnhancementItemDTO> itemsListView = new ListView<>();
        itemsListView.setStyle("-fx-font-size: 14px;");
        itemsListView.getItems().addAll(smithService.getEnhanceableItems(hero));
        VBox.setVgrow(itemsListView, Priority.ALWAYS);

        itemsListView.setCellFactory(lv -> new ListCell<EnhancementItemDTO>() {
            @Override
            protected void updateItem(EnhancementItemDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setGraphic(createEnhancementItemCard(item));
                }
            }
        });

        panel.getChildren().addAll(titleLabel, infoLabel, itemsListView);
        return panel;
    }

    private HBox createEnhancementItemCard(EnhancementItemDTO itemDTO) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: #3d3d3d; " +
                        "-fx-background-radius: 8;"
        );

        VBox infoBox = new VBox(5);

        Label nameLabel = new Label(itemDTO.getName() + " (+" + itemDTO.getCurrentLevel() + ")");
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #9c27b0;");

        StringBuilder bonusesText = new StringBuilder("Bonusuri: ");
        itemDTO.getCurrentBonuses().forEach((stat, bonus) ->
                bonusesText.append(stat).append(": +").append(bonus).append(" | ")
        );

        Label bonusesLabel = new Label(bonusesText.toString());
        bonusesLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4caf50;");

        Label costLabel = new Label("💎 Cost: " + itemDTO.getEnhancementCost() + " shards");
        costLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9c27b0;");

        if (itemDTO.getMaxAffordableLevels() > 1) {
            Label maxLabel = new Label("⚡ Max: +" + itemDTO.getMaxAffordableLevels() + " nivele");
            maxLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ff9800;");
            infoBox.getChildren().addAll(nameLabel, bonusesLabel, costLabel, maxLabel);
        } else {
            infoBox.getChildren().addAll(nameLabel, bonusesLabel, costLabel);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox buttonsBox = new VBox(5);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        Button enhance1Button = new Button("✨ +1");
        enhance1Button.setDisable(!itemDTO.canAfford());
        styleSmallButton(enhance1Button, "#9c27b0");
        enhance1Button.setOnAction(e -> handleEnhance(itemDTO.getItem(), false));

        Button enhanceMaxButton = new Button("🚀 MAX");
        enhanceMaxButton.setDisable(itemDTO.getMaxAffordableLevels() <= 1);
        styleSmallButton(enhanceMaxButton, "#ff9800");
        enhanceMaxButton.setOnAction(e -> handleEnhance(itemDTO.getItem(), true));

        Button scrollButton = new Button("📜 Scroll");
        scrollButton.setDisable(hero.getInventar().getEnchantScrolls().isEmpty());
        styleSmallButton(scrollButton, "#4caf50");
        scrollButton.setOnAction(e -> handleUseScroll(itemDTO.getItem()));

        buttonsBox.getChildren().addAll(enhance1Button, enhanceMaxButton, scrollButton);

        card.getChildren().addAll(infoBox, spacer, buttonsBox);
        return card;
    }

    /**
     * Footer
     */
    private HBox createFooter() {
        HBox footer = new HBox(15);
        footer.setPadding(new Insets(15));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #2d2d2d;");

        Button backButton = new Button("🔙 Înapoi");
        styleButton(backButton, "#e74c3c");
        backButton.setOnAction(e -> {
            TownMenuController townController = new TownMenuController(stage, hero);
            stage.setScene(townController.createScene());
        });

        footer.getChildren().add(backButton);
        return footer;
    }

    // ==================== HANDLERS ====================

    private void handleAllocateStatPoint(StatType statType) {
        AllocationResultDTO result = smithService.allocateStatPoint(hero, statType);

        if (result.isSuccess()) {
            DialogHelper.showSuccess("Stat Alocat!",
                    result.getMessage() + "\nNoua valoare: " + result.getNewValue());
            refreshScene();
        } else {
            DialogHelper.showError("Eroare", result.getMessage());
        }
    }

    private void handleTrainStat(StatType statType) {
        TrainingInfoDTO info = smithService.getTrainingInfo(hero);

        if (DialogHelper.showConfirmation("Antrenament",
                "Vrei să antrenezi " + statType.getDisplayName() + " pentru " +
                        info.getTrainingCost() + " gold?")) {

            TrainingResultDTO result = smithService.trainStat(hero, statType);

            if (result.isSuccess()) {
                DialogHelper.showSuccess("Antrenament Reușit!",
                        result.getMessage() + "\nNoua valoare: " + result.getNewValue());
                refreshScene();
            } else {
                DialogHelper.showError("Eroare", result.getMessage());
            }
        }
    }

    private void handleEnhance(ObiectEchipament item, boolean maxEnhance) {
        EnhancementResultDTO result;

        if (maxEnhance) {
            if (!DialogHelper.showConfirmation("Enhancement Maxim",
                    "Vrei să îmbunătățești la MAXIM acest item?")) {
                return;
            }
            result = smithService.enhanceItemMax(hero, item);
        } else {
            result = smithService.enhanceItem(hero, item);
        }

        if (result.isSuccess()) {
            StringBuilder msg = new StringBuilder();
            msg.append(result.getMessage()).append("\n\n");
            msg.append("Shards folosite: ").append(result.getShardsSpent()).append("\n\n");
            msg.append("Bonusuri crescute:\n");
            result.getBonusIncrease().forEach((stat, increase) ->
                    msg.append("  • ").append(stat).append(": +").append(increase).append("\n")
            );

            DialogHelper.showSuccess("Enhancement Reușit!", msg.toString());
            refreshScene();
        } else {
            DialogHelper.showError("Eroare", result.getMessage());
        }
    }

    private void handleUseScroll(ObiectEchipament item) {
        if (DialogHelper.showConfirmation("Enchant Scroll",
                "Vrei să folosești un Enchant Scroll pe acest item?\n(Enhancement GRATUIT)")) {

            EnhancementResultDTO result = smithService.useEnchantScroll(hero, item);

            if (result.isSuccess()) {
                StringBuilder msg = new StringBuilder();
                msg.append(result.getMessage()).append("\n\n");
                msg.append("Bonusuri crescute:\n");
                result.getBonusIncrease().forEach((stat, increase) ->
                        msg.append("  • ").append(stat).append(": +").append(increase).append("\n")
                );

                DialogHelper.showSuccess("Scroll Folosit!", msg.toString());
                refreshScene();
            } else {
                DialogHelper.showError("Eroare", result.getMessage());
            }
        }
    }

    private void refreshScene() {
        stage.setScene(createScene());
    }

    private void styleButton(Button btn, String color) {
        btn.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 10px 25px; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );
    }

    private void styleSmallButton(Button btn, String color) {
        btn.setMinWidth(80);
        btn.setStyle(
                "-fx-font-size: 12px; " +
                        "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 5px 10px; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;"
        );
    }
}