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
 * SmithControllerFX - Controller pentru Enhancement
 */
public class SmithControllerFX {

    private Stage stage;
    private Erou hero;
    private TrainerSmithServiceFX smithService;

    private Label goldLabel;
    private Label scrapLabel;

    private int selectedTabIndex = 0; // Track selected tab (0=Enhancement, 1=Disenchant)

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

        return new Scene(root, 1900, 1080);
    }

    /**
     * Header cu resurse
     */
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #2d2d2d;");

        Label title = new Label("🔨 FIERĂRIA 🔨");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #ff9800;");

        HBox resources = new HBox(30);
        resources.setAlignment(Pos.CENTER);

        goldLabel = new Label("💰 Gold: " + hero.getGold());
        goldLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #ffd54f;");

        scrapLabel = new Label("🔧 Scrap: " + hero.getScrap());
        scrapLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #9c27b0;");

        resources.getChildren().addAll(goldLabel, scrapLabel);

        header.getChildren().addAll(title, resources);
        return header;
    }

    /**
     * Conținut principal - Enhancement și Disenchant tabs
     */
    private VBox createMainContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #2d2d2d;");

        Tab enhancementTab = new Tab("✨ Enhancement", createEnhancementPanel());
        enhancementTab.setClosable(false);

        Tab disenchantTab = new Tab("🔮 Disenchant", createDisenchantPanel());
        disenchantTab.setClosable(false);

        tabPane.getTabs().addAll(enhancementTab, disenchantTab);

        // Restore previously selected tab
        tabPane.getSelectionModel().select(selectedTabIndex);

        // Track tab changes
        tabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            selectedTabIndex = newVal.intValue();
        });

        VBox.setVgrow(tabPane, Priority.ALWAYS);
        content.getChildren().add(tabPane);

        return content;
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
                "🔧 Scrap: " + smithInfo.getAvailableShards() + " | " +
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

        Label costLabel = new Label("🔧 Cost: " + itemDTO.getEnhancementCost() + " scrap");
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
     * Panel disenchant - dezmembrare echipament pentru shards
     */
    private VBox createDisenchantPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #2d2d2d;");

        Label titleLabel = new Label("🔮 DISENCHANT ECHIPAMENT");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #9c27b0;");

        Label infoLabel = new Label(
                "Descompune echipamentul neechipat pentru a obține scrap!\n" +
                "Valorile scrap-ului depind de raritate, nivel și bonusuri."
        );
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #b0b0b0;");
        infoLabel.setWrapText(true);

        ListView<ObiectEchipament> itemsListView = new ListView<>();
        itemsListView.setStyle("-fx-font-size: 14px;");

        // Doar obiecte neechipate
        itemsListView.getItems().addAll(
            hero.getInventar().stream()
                .filter(item -> !item.isEquipped())
                .toList()
        );
        VBox.setVgrow(itemsListView, Priority.ALWAYS);

        if (itemsListView.getItems().isEmpty()) {
            Label noItemsLabel = new Label("📦 Nu ai obiecte neechipate care pot fi disenchanted!");
            noItemsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #ff9800;");
            panel.getChildren().addAll(titleLabel, infoLabel, noItemsLabel);
            return panel;
        }

        itemsListView.setCellFactory(lv -> new ListCell<ObiectEchipament>() {
            @Override
            protected void updateItem(ObiectEchipament item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setGraphic(createDisenchantItemCard(item));
                }
            }
        });

        // Disenchant ALL button
        Button disenchantAllButton = new Button("💎 DISENCHANT TOATE");
        disenchantAllButton.setStyle(
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-color: #ff5722; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 10px 20px; " +
                "-fx-background-radius: 8;"
        );
        disenchantAllButton.setOnAction(e -> handleDisenchantAll(itemsListView.getItems()));

        panel.getChildren().addAll(titleLabel, infoLabel, itemsListView, disenchantAllButton);
        return panel;
    }

    private HBox createDisenchantItemCard(ObiectEchipament item) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: #3d3d3d; " +
                "-fx-background-radius: 8;"
        );

        VBox infoBox = new VBox(5);

        String rarityIcon = getRarityIcon(item.getRaritate());
        Label nameLabel = new Label(rarityIcon + " " + item.getNume());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " +
                          getRarityColor(item.getRaritate()) + ";");

        Label rarityLabel = new Label("Raritate: " + item.getRaritate().getDisplayName() +
                                     " | Nivel: " + item.getNivelNecesar());
        rarityLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #b0b0b0;");

        int scrapValue = calculateDisenchantValue(item);
        Label scrapValueLabel = new Label("🔧 Primești: " + scrapValue + " scrap");
        scrapValueLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #9c27b0;");

        infoBox.getChildren().addAll(nameLabel, rarityLabel, scrapValueLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button disenchantButton = new Button("💥 Disenchant");
        disenchantButton.setStyle(
                "-fx-font-size: 12px; " +
                "-fx-background-color: #e91e63; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 8px 15px; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;"
        );
        disenchantButton.setOnAction(e -> handleDisenchant(item, scrapValue));

        card.getChildren().addAll(infoBox, spacer, disenchantButton);
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

    // ==================== DISENCHANT HANDLERS ====================

    private void handleDisenchant(ObiectEchipament item, int scrapValue) {
        String message = String.format(
                "⚠️ ATENȚIE: Această acțiune este IREVERSIBILĂ!\n\n" +
                "Obiect: %s %s\n" +
                "Raritate: %s | Nivel: %d\n\n" +
                "🔧 Vei primi: %d scrap\n" +
                "🔧 Total după: %d scrap\n\n" +
                "Confirmi disenchanting?",
                getRarityIcon(item.getRaritate()), item.getNume(),
                item.getRaritate().getDisplayName(), item.getNivelNecesar(),
                scrapValue, hero.getScrap() + scrapValue
        );

        if (DialogHelper.showConfirmation("Confirmare Disenchant", message)) {
            // Remove item and give scrap
            hero.removeFromInventar(item);
            hero.adaugaScrap(scrapValue);

            String successMsg = String.format(
                    "✅ DISENCHANT REUȘIT!\n\n" +
                    "🔧 Ai primit %d scrap din %s!\n" +
                    "🔧 Total scrap: %d\n\n" +
                    "✨ Energia magică a obiectului a fost eliberată!",
                    scrapValue, item.getNume(), hero.getScrap()
            );

            DialogHelper.showSuccess("Disenchant Complet", successMsg);
            refreshScene();
        }
    }

    private void handleDisenchantAll(java.util.List<ObiectEchipament> items) {
        if (items.isEmpty()) {
            DialogHelper.showError("Eroare", "Nu ai obiecte de disenchanted!");
            return;
        }

        int totalScrap = items.stream()
                .mapToInt(this::calculateDisenchantValue)
                .sum();

        String message = String.format(
                "⚠️ ATENȚIE: Această acțiune este IREVERSIBILĂ!\n" +
                "⚠️ TOATE obiectele vor fi distruse permanent!\n\n" +
                "📦 Vei disenchanta %d obiecte\n" +
                "🔧 Total scrap: %d\n" +
                "🔧 Scrap după disenchant: %d\n\n" +
                "Confirmi disenchanting pentru TOATE obiectele?",
                items.size(), totalScrap, hero.getScrap() + totalScrap
        );

        if (DialogHelper.showConfirmation("Confirmare Disenchant Masiv", message)) {
            // Remove all items
            for (ObiectEchipament item : items) {
                hero.removeFromInventar(item);
            }
            hero.adaugaScrap(totalScrap);

            String successMsg = String.format(
                    "✅ DISENCHANT MASIV REUȘIT!\n\n" +
                    "🔧 Ai primit %d scrap din %d obiecte!\n" +
                    "🔧 Total scrap: %d\n\n" +
                    "✨ O explozie de energie magică umple camera!\n" +
                    "🌟 Acum poți folosi scrap-ul pentru upgrade echipament!",
                    totalScrap, items.size(), hero.getScrap()
            );

            DialogHelper.showSuccess("Disenchant Complet", successMsg);
            refreshScene();
        }
    }

    // ==================== HELPER METHODS ====================

    private int calculateDisenchantValue(ObiectEchipament item) {
        // Formula pentru calcularea valorii în scraps
        // Higher base values for better scrap returns
        int baseScraps = switch (item.getRaritate()) {
            case COMMON -> 5;
            case UNCOMMON -> 15;
            case RARE -> 40;
            case EPIC -> 100;
            case LEGENDARY -> 250;
        };

        // Bonus bazat pe nivel - more generous
        int levelBonus = item.getNivelNecesar() * 2; // +2 scraps per level

        // Bonus bazat pe numărul de bonusuri
        int bonusCount = item.getBonuses().size();
        int bonusMultiplier = Math.max(1, bonusCount);

        // Bonus pentru enhancement level - significant bonus for upgraded items
        int enhancementBonus = item.getEnhancementLevel() * 10;

        // Calculul final
        int totalScraps = (baseScraps + levelBonus + enhancementBonus) * bonusMultiplier;

        return Math.max(5, totalScraps); // Minim 5 scraps
    }

    private String getRarityIcon(ObiectEchipament.Raritate raritate) {
        return switch (raritate) {
            case COMMON -> "⚪";
            case UNCOMMON -> "💚";
            case RARE -> "💙";
            case EPIC -> "💜";
            case LEGENDARY -> "🌟";
        };
    }

    private String getRarityColor(ObiectEchipament.Raritate raritate) {
        return switch (raritate) {
            case COMMON -> "#9e9e9e";
            case UNCOMMON -> "#4caf50";
            case RARE -> "#2196f3";
            case EPIC -> "#9c27b0";
            case LEGENDARY -> "#ff9800";
        };
    }
}