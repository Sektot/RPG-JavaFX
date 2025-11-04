package com.rpg.dungeon.controller;

import com.rpg.dungeon.model.DungeonProgression;
import com.rpg.model.characters.Erou;
import com.rpg.utils.DialogHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Shop pentru upgrade-uri permanente în dungeon system
 */
public class DungeonUpgradeShopController {

    private Stage stage;
    private Erou hero;
    private Runnable onExitCallback;

    private Label tokensLabel;
    private VBox upgradesPanel;

    public DungeonUpgradeShopController(Stage stage, Erou hero, Runnable onExitCallback) {
        this.stage = stage;
        this.hero = hero;
        this.onExitCallback = onExitCallback;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        root.setTop(createHeader());
        root.setCenter(createContent());
        root.setBottom(createFooter());

        return new Scene(root, 1900, 1080);
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #16213e;");

        Label title = new Label("🏛️ DUNGEON PROGRESSION SHOP");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        Label subtitle = new Label("Purchase permanent upgrades with Dungeon Tokens");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #f1c40f;");

        tokensLabel = new Label();
        updateTokensLabel();
        tokensLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        header.getChildren().addAll(title, subtitle, tokensLabel);
        return header;
    }

    private ScrollPane createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.TOP_CENTER);

        // Statistics section
        content.getChildren().add(createStatisticsSection());

        // Upgrades section
        Label upgradesTitle = new Label("⬆️ PERMANENT UPGRADES");
        upgradesTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        content.getChildren().add(upgradesTitle);

        upgradesPanel = new VBox(15);
        upgradesPanel.setAlignment(Pos.TOP_CENTER);
        createUpgradeButtons();
        content.getChildren().add(upgradesPanel);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #1a1a2e; -fx-background-color: transparent;");
        return scroll;
    }

    private VBox createStatisticsSection() {
        VBox statsBox = new VBox(10);
        statsBox.setPadding(new Insets(15));
        statsBox.setStyle("-fx-background-color: #0f0f1e; -fx-background-radius: 10;");
        statsBox.setMaxWidth(600);

        Label statsTitle = new Label("📊 YOUR STATISTICS");
        statsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f1c40f;");

        DungeonProgression prog = hero.getDungeonProgression();

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(30);
        statsGrid.setVgap(8);
        statsGrid.setAlignment(Pos.CENTER);

        addStatRow(statsGrid, 0, "Total Runs:", prog.getTotalRuns());
        addStatRow(statsGrid, 1, "Victories:", prog.getSuccessfulRuns());
        addStatRow(statsGrid, 2, "Deaths:", prog.getTotalDeaths());
        addStatRow(statsGrid, 3, "Success Rate:", String.format("%.1f%%", prog.getSuccessRate()));
        addStatRow(statsGrid, 4, "Deepest Depth:", prog.getDeepestDepthReached());
        addStatRow(statsGrid, 5, "Enemies Killed:", prog.getTotalEnemiesKilled());
        addStatRow(statsGrid, 6, "Bosses Defeated:", prog.getTotalBossesDefeated());

        statsBox.getChildren().addAll(statsTitle, statsGrid);
        return statsBox;
    }

    private void addStatRow(GridPane grid, int row, String label, Object value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 14px;");

        Label valueNode = new Label(value.toString());
        valueNode.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private void createUpgradeButtons() {
        upgradesPanel.getChildren().clear();

        DungeonProgression prog = hero.getDungeonProgression();

        for (DungeonProgression.UpgradeType type : DungeonProgression.UpgradeType.values()) {
            upgradesPanel.getChildren().add(createUpgradeCard(type, prog));
        }
    }

    private HBox createUpgradeCard(DungeonProgression.UpgradeType type, DungeonProgression prog) {
        HBox card = new HBox(20);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(700);

        // Icon and info
        VBox infoBox = new VBox(5);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label nameLabel = new Label(type.getIcon() + " " + type.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label descLabel = new Label(type.getDescription());
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #b0b0b0;");
        descLabel.setWrapText(true);

        int currentLevel = prog.getCurrentLevel(type);
        int maxLevel = prog.getMaxLevel(type);
        Label levelLabel = new Label("Level: " + currentLevel + " / " + maxLevel);
        levelLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #f1c40f;");

        infoBox.getChildren().addAll(nameLabel, descLabel, levelLabel);

        // Purchase button
        Button purchaseBtn = new Button();
        if (prog.canUpgrade(type)) {
            int cost = prog.getUpgradeCost(type);
            purchaseBtn.setText("Buy (" + cost + " 🎫)");
            purchaseBtn.setStyle(
                "-fx-font-size: 14px; " +
                "-fx-background-color: #27ae60; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 10px 20px; " +
                "-fx-background-radius: 6;"
            );
            purchaseBtn.setOnAction(e -> purchaseUpgrade(type));

            // Disable if not enough tokens
            if (prog.getDungeonTokens() < cost) {
                purchaseBtn.setDisable(true);
                purchaseBtn.setStyle(
                    "-fx-font-size: 14px; " +
                    "-fx-background-color: #555555; " +
                    "-fx-text-fill: #888888; " +
                    "-fx-padding: 10px 20px; " +
                    "-fx-background-radius: 6;"
                );
            }
        } else {
            purchaseBtn.setText("MAX LEVEL");
            purchaseBtn.setDisable(true);
            purchaseBtn.setStyle(
                "-fx-font-size: 14px; " +
                "-fx-background-color: #e94560; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 10px 20px; " +
                "-fx-background-radius: 6;"
            );
        }

        card.getChildren().addAll(infoBox, purchaseBtn);
        return card;
    }

    private void purchaseUpgrade(DungeonProgression.UpgradeType type) {
        DungeonProgression prog = hero.getDungeonProgression();

        if (prog.purchaseUpgrade(type)) {
            DialogHelper.showSuccess(
                "✅ Upgrade Purchased!",
                "You purchased: " + type.getName() + "\nNew level: " + prog.getCurrentLevel(type)
            );

            // Refresh UI
            updateTokensLabel();
            createUpgradeButtons();
        } else {
            DialogHelper.showError(
                "❌ Cannot Purchase",
                "Not enough tokens or max level reached!"
            );
        }
    }

    private VBox createFooter() {
        VBox footer = new VBox(10);
        footer.setPadding(new Insets(20));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #16213e;");

        Button backBtn = new Button("🏠 Return to Town");
        backBtn.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-background-color: #e74c3c; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 12px 30px; " +
            "-fx-background-radius: 6;"
        );
        backBtn.setOnAction(e -> {
            if (onExitCallback != null) {
                onExitCallback.run();
            }
        });

        footer.getChildren().add(backBtn);
        return footer;
    }

    private void updateTokensLabel() {
        int tokens = hero.getDungeonProgression().getDungeonTokens();
        tokensLabel.setText("🎫 Dungeon Tokens: " + tokens);
    }
}
