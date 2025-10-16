package com.rpg.controller;

import com.rpg.model.characters.Erou;
import com.rpg.model.items.BuffPotion;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.service.InventoryServiceFX;
import com.rpg.service.InventoryServiceFX.InventoryCategory;
import com.rpg.service.dto.*;
import com.rpg.utils.DialogHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller JavaFX pentru inventar
 */
public class InventoryControllerFX {

    private Stage stage;
    private Erou hero;
    private InventoryServiceFX inventoryService;

    // UI Components
    private Label statsLabel;
    private ProgressBar capacityBar;
    private ComboBox<InventoryCategory> categoryComboBox;
    private ListView<InventoryItemDTO> itemListView;
    private TextArea itemDetailsArea;
    private VBox actionButtonsPanel;

    public InventoryControllerFX(Stage stage, Erou hero) {
        this.stage = stage;
        this.hero = hero;
        this.inventoryService = new InventoryServiceFX();
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setTop(createHeader());
        root.setCenter(createMainContent());
        root.setBottom(createFooter());

        root.setStyle("-fx-background-color: #1a1a2e;");

        return new Scene(root, 1000, 700);
    }

    /**
     * Header cu statistici inventar
     */
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #16213e;");

        Label title = new Label("🎒 INVENTARUL LUI " + hero.getNume().toUpperCase());
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        // Statistici
        InventoryStatsDTO stats = inventoryService.getInventoryStats(hero);

        statsLabel = new Label(String.format(
                "📦 Echipament: %d/%d | 🧪 Poțiuni: %d | 💪 Buff-uri: %d | ✨ Speciale: %d",
                stats.getTotalItems(), stats.getMaxCapacity(),
                stats.getPotionCount(), stats.getBuffPotionCount(),
                stats.getSpecialItemCount()
        ));
        statsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f1f1f1;");

        // Bara de capacitate
        capacityBar = new ProgressBar(stats.getUsagePercentage() / 100.0);
        capacityBar.setPrefWidth(600);
        capacityBar.setStyle("-fx-accent: " +
                (stats.getUsagePercentage() > 90 ? "#e74c3c" :
                        stats.getUsagePercentage() > 70 ? "#f39c12" : "#27ae60") + ";");

        header.getChildren().addAll(title, statsLabel, capacityBar);
        return header;
    }

    /**
     * Conținut principal
     */
    private HBox createMainContent() {
        HBox content = new HBox(15);
        content.setPadding(new Insets(20));

        // ✅ SCHIMBĂ ORDINEA - RIGHT ÎNAINTE DE LEFT:
        VBox rightPanel = createRightPanel();  // PRIMUL - creează itemDetailsArea
        VBox leftPanel = createLeftPanel();    // AL DOILEA - folosește itemDetailsArea

        content.getChildren().addAll(leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        return content;
    }
    /**
     * Panel stâng - Categorii și listă
     */
    private VBox createLeftPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");

        Label categoryLabel = new Label("📂 Filtrează:");
        categoryLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

        categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll(InventoryCategory.values());
        categoryComboBox.setValue(InventoryCategory.TOATE);
        categoryComboBox.setMaxWidth(Double.MAX_VALUE);
        categoryComboBox.setStyle("-fx-font-size: 14px;");
        categoryComboBox.setOnAction(e -> loadItemsForCategory());

        Label itemsLabel = new Label("📋 Iteme:");
        itemsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

        itemListView = new ListView<>();
        itemListView.setStyle("-fx-font-size: 14px;");
        VBox.setVgrow(itemListView, Priority.ALWAYS);

        // Custom cell pentru a colora diferit itemurile echipate
        itemListView.setCellFactory(lv -> new ListCell<InventoryItemDTO>() {
            @Override
            protected void updateItem(InventoryItemDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    if (item.isEquipped()) {
                        setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        itemListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> displayItemDetails(newVal)
        );

        loadItemsForCategory();

        panel.getChildren().addAll(categoryLabel, categoryComboBox, itemsLabel, itemListView);
        return panel;
    }

    /**
     * Panel drept - Detalii și acțiuni
     */
    private VBox createRightPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");

        Label detailsLabel = new Label("📋 Detalii Item:");
        detailsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

        itemDetailsArea = new TextArea();
        itemDetailsArea.setEditable(false);
        itemDetailsArea.setWrapText(true);
        itemDetailsArea.setStyle("-fx-font-size: 14px; -fx-control-inner-background: #1a1a2e; -fx-text-fill: white;");
        itemDetailsArea.setPrefHeight(300);
        VBox.setVgrow(itemDetailsArea, Priority.ALWAYS);

        Label actionsLabel = new Label("⚡ Acțiuni:");
        actionsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

        actionButtonsPanel = new VBox(10);
        actionButtonsPanel.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(detailsLabel, itemDetailsArea, actionsLabel, actionButtonsPanel);
        return panel;
    }

    /**
     * Footer cu navigare
     */
    private HBox createFooter() {
        HBox footer = new HBox(15);
        footer.setPadding(new Insets(15));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #16213e;");

        Button refreshBtn = new Button("🔄 Actualizează");
        styleButton(refreshBtn, "#3498db");
        refreshBtn.setOnAction(e -> refreshInventory());

        Button backButton = new Button("🔙 Înapoi");
        styleButton(backButton, "#e74c3c");
        backButton.setOnAction(e -> {
            TownMenuController townController = new TownMenuController(stage, hero);
            stage.setScene(townController.createScene());
        });

        footer.getChildren().addAll(refreshBtn, backButton);
        return footer;
    }

    /**
     * Încarcă itemurile pentru categoria selectată
     */
    private void loadItemsForCategory() {
        InventoryCategory selectedCategory = categoryComboBox.getValue();
        if (selectedCategory == null) return;

        List<InventoryItemDTO> items = inventoryService.getItemsByCategory(hero, selectedCategory);

        itemListView.getItems().clear();
        itemListView.getItems().addAll(items);

        itemDetailsArea.clear();
        actionButtonsPanel.getChildren().clear();
    }

    /**
     * Afișează detaliile itemului selectat
     */
    private void displayItemDetails(InventoryItemDTO item) {
        if (item == null) {
            itemDetailsArea.clear();
            actionButtonsPanel.getChildren().clear();
            return;
        }

        // Afișează detalii
        StringBuilder details = new StringBuilder();
        details.append("═══════════════════════════════\n");
        details.append(item.getName()).append("\n");
        details.append("═══════════════════════════════\n\n");
        details.append(item.getDescription());

        itemDetailsArea.setText(details.toString());

        // Creează butoane de acțiuni
        createActionButtons(item);
    }

    /**
     * Creează butoane de acțiuni pentru item
     */
    private void createActionButtons(InventoryItemDTO item) {
        actionButtonsPanel.getChildren().clear();

        switch (item.getType()) {
            case EQUIPMENT -> {
                Button equipBtn = new Button("⚔️ ECHIPEAZĂ");
                styleActionButton(equipBtn, "#27ae60");
                equipBtn.setOnAction(e -> handleEquipItem(item));
                actionButtonsPanel.getChildren().add(equipBtn);

                Button dropBtn = new Button("🗑️ Aruncă");
                styleActionButton(dropBtn, "#c0392b");
                dropBtn.setOnAction(e -> handleDropItem(item));
                actionButtonsPanel.getChildren().add(dropBtn);
            }

            case EQUIPMENT_EQUIPPED -> {
                Button unequipBtn = new Button("❌ DEECHIPEAZĂ");
                styleActionButton(unequipBtn, "#e67e22");
                unequipBtn.setOnAction(e -> handleUnequipItem(item));
                actionButtonsPanel.getChildren().add(unequipBtn);
            }

            case HEALING_POTION -> {
                Button useBtn = new Button("💚 FOLOSEȘTE");
                styleActionButton(useBtn, "#27ae60");
                useBtn.setOnAction(e -> handleUseHealingPotion(item));
                actionButtonsPanel.getChildren().add(useBtn);
            }

            case BUFF_POTION -> {
                Button useBtn = new Button("💪 ACTIVEAZĂ BUFF");
                styleActionButton(useBtn, "#9b59b6");
                useBtn.setOnAction(e -> handleUseBuffPotion(item));
                actionButtonsPanel.getChildren().add(useBtn);
            }

            case ENCHANT_SCROLL -> {
                Label infoLabel = new Label("📜 Folosește în Fierărie\npentru enhancement!");
                infoLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 14px; -fx-text-alignment: center;");
                infoLabel.setWrapText(true);
                actionButtonsPanel.getChildren().add(infoLabel);
            }

            case SPECIAL -> {
                Label infoLabel = new Label("✨ Item special\nFolosit automat\ncând e necesar");
                infoLabel.setStyle("-fx-text-fill: #3498db; -fx-font-size: 14px; -fx-text-alignment: center;");
                infoLabel.setWrapText(true);
                actionButtonsPanel.getChildren().add(infoLabel);
            }
        }
    }

    // ==================== ACTION HANDLERS ====================

    private void handleEquipItem(InventoryItemDTO itemDTO) {
        ObiectEchipament equipment = itemDTO.getEquipment();
        if (equipment == null) return;

        EquipResult result = inventoryService.equipItem(hero, equipment);

        if (result.isSuccess()) {
            DialogHelper.showSuccess("Echipat!", result.getMessage());

            if (result.hasPreviousItem()) {
                DialogHelper.showInfo("Info",
                        "Itemul anterior a fost deechipat și pus în inventar.");
            }

            refreshInventory();
        } else {
            DialogHelper.showError("Eroare", result.getMessage());
        }
    }

    private void handleUnequipItem(InventoryItemDTO itemDTO) {
        ObiectEchipament equipment = itemDTO.getEquipment();
        if (equipment == null) return;

        if (DialogHelper.showConfirmation("Deechipare",
                "Ești sigur că vrei să deechipezi acest item?")) {

            EquipResult result = inventoryService.unequipItem(hero, equipment);

            if (result.isSuccess()) {
                DialogHelper.showSuccess("Deechipat!", result.getMessage());
                refreshInventory();
            } else {
                DialogHelper.showError("Eroare", result.getMessage());
            }
        }
    }

    private void handleUseHealingPotion(InventoryItemDTO itemDTO) {
        if (!(itemDTO.getData() instanceof Integer healAmount)) return;

        UseItemResult result = inventoryService.useHealingPotion(hero, healAmount);

        if (result.isSuccess()) {
            DialogHelper.showSuccess("Vindecare!", result.getMessage());
            refreshInventory();
        } else {
            DialogHelper.showWarning("Atenție", result.getMessage());
        }
    }

    private void handleUseBuffPotion(InventoryItemDTO itemDTO) {
        if (!(itemDTO.getData() instanceof BuffPotion.BuffType buffType)) return;

        if (DialogHelper.showConfirmation("Activare Buff",
                "Vrei să activezi buff-ul: " + buffType.getDisplayName() + "?")) {

            UseItemResult result = inventoryService.useBuffPotion(hero, buffType);

            if (result.isSuccess()) {
                DialogHelper.showSuccess("Buff Activat!", result.getMessage());
                refreshInventory();
            } else {
                DialogHelper.showError("Eroare", result.getMessage());
            }
        }
    }

    private void handleDropItem(InventoryItemDTO itemDTO) {
        ObiectEchipament equipment = itemDTO.getEquipment();
        if (equipment == null) return;

        if (DialogHelper.showConfirmation("Aruncare Item",
                "Ești SIGUR că vrei să arunci: " + equipment.getNume() + "?\n" +
                        "Acest item va fi pierdut definitiv!")) {

            DropItemResult result = inventoryService.dropItem(hero, equipment);

            if (result.isSuccess()) {
                DialogHelper.showInfo("Aruncat", result.getMessage());
                refreshInventory();
            } else {
                DialogHelper.showError("Eroare", result.getMessage());
            }
        }
    }

    /**
     * Reîmprospătează inventarul
     */
    private void refreshInventory() {
        // Update statistici
        InventoryStatsDTO stats = inventoryService.getInventoryStats(hero);
        statsLabel.setText(String.format(
                "📦 Echipament: %d/%d | 🧪 Poțiuni: %d | 💪 Buff-uri: %d | ✨ Speciale: %d",
                stats.getTotalItems(), stats.getMaxCapacity(),
                stats.getPotionCount(), stats.getBuffPotionCount(),
                stats.getSpecialItemCount()
        ));
        capacityBar.setProgress(stats.getUsagePercentage() / 100.0);

        // Reload items
        loadItemsForCategory();
    }

    /**
     * Stilizează un buton normal
     */
    private void styleButton(Button btn, String color) {
        btn.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 10px 20px; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );
    }

    /**
     * Stilizează un buton de acțiune
     */
    private void styleActionButton(Button btn, String color) {
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 12px; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e ->
                btn.setStyle(
                        "-fx-font-size: 16px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-color: derive(" + color + ", 20%); " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 12px; " +
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
                                "-fx-padding: 12px; " +
                                "-fx-background-radius: 8; " +
                                "-fx-cursor: hand;"
                )
        );
    }
}