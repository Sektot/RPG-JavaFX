package com.rpg.controller;

import com.rpg.model.characters.Erou;
import com.rpg.service.ShopServiceFX;
import com.rpg.service.ShopServiceFX.ShopCategory;
import com.rpg.service.dto.PurchaseResult;
import com.rpg.service.dto.ShopItemDTO;
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
 * Controller pentru interfața Shop-ului în JavaFX
 */
public class ShopController {

    private Stage stage;
    private Erou hero;
    private ShopServiceFX shopService;

    // UI Components
    private Label goldLabel;
    private ListView<ShopItemDTO> itemListView;
    private TextArea itemDetailsArea;
    private Spinner<Integer> quantitySpinner;
    private Button buyButton;
    private ComboBox<ShopCategory> categoryComboBox;

    public ShopController(Stage stage, Erou hero) {
        this.stage = stage;
        this.hero = hero;
        this.shopService = new ShopServiceFX();
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setTop(createHeader());
        root.setCenter(createMainContent());
        root.setBottom(createFooter());

        // Styling
        root.setStyle("-fx-background-color: #2c3e50;");

        return new Scene(root, 1000, 700);
    }

    /**
     * Header cu titlul și gold-ul eroului
     */
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #34495e;");

        Label title = new Label("🪙 MAGAZINUL DIN BUCUREȘTI 🪙");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #f39c12;");

        goldLabel = new Label("💰 Gold: " + hero.getGold());
        goldLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #f1c40f;");

        header.getChildren().addAll(title, goldLabel);
        return header;
    }

    /**
     * Conținutul principal - lista de produse și detalii
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
     * Panel stâng - Categorii și lista de produse
     */
    private VBox createLeftPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #34495e; -fx-background-radius: 10;");

        Label categoryLabel = new Label("📂 Categorii:");
        categoryLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

        // ComboBox pentru categorii
        categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll(ShopCategory.values());
        categoryComboBox.setValue(ShopCategory.POTIUNI);
        categoryComboBox.setMaxWidth(Double.MAX_VALUE);
        categoryComboBox.setStyle("-fx-font-size: 14px;");

        // Când se schimbă categoria, reîncarcă produsele
        categoryComboBox.setOnAction(e -> loadItemsForCategory());

        Label itemsLabel = new Label("🛒 Produse Disponibile:");
        itemsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

        // ListView pentru produse
        itemListView = new ListView<>();
        itemListView.setStyle("-fx-font-size: 14px;");
        VBox.setVgrow(itemListView, Priority.ALWAYS);

        // Când se selectează un produs, afișează detaliile
        itemListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> displayItemDetails(newVal)
        );

        // Încarcă produsele pentru categoria selectată
        loadItemsForCategory();

        panel.getChildren().addAll(categoryLabel, categoryComboBox, itemsLabel, itemListView);
        return panel;
    }

    /**
     * Panel drept - Detalii produs și cumpărare
     */
    private VBox createRightPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #34495e; -fx-background-radius: 10;");

        Label detailsLabel = new Label("📋 Detalii Produs:");
        detailsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

        // TextArea pentru detalii
        itemDetailsArea = new TextArea();
        itemDetailsArea.setEditable(false);
        itemDetailsArea.setWrapText(true);
        itemDetailsArea.setStyle("-fx-font-size: 14px; -fx-control-inner-background: #2c3e50; -fx-text-fill: white;");
        itemDetailsArea.setPrefHeight(300);
        VBox.setVgrow(itemDetailsArea, Priority.ALWAYS);

        // Selector cantitate
        HBox quantityBox = new HBox(10);
        quantityBox.setAlignment(Pos.CENTER);

        Label quantityLabel = new Label("Cantitate:");
        quantityLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        quantitySpinner = new Spinner<>(1, 99, 1);
        quantitySpinner.setEditable(true);
        quantitySpinner.setPrefWidth(100);

        quantityBox.getChildren().addAll(quantityLabel, quantitySpinner);

        // Buton de cumpărare
        buyButton = new Button("💰 CUMPĂRĂ");
        buyButton.setMaxWidth(Double.MAX_VALUE);
        buyButton.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-color: #27ae60; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 15px; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;"
        );

        buyButton.setOnMouseEntered(e ->
                buyButton.setStyle(
                        "-fx-font-size: 18px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-color: #2ecc71; " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 15px; " +
                                "-fx-background-radius: 10; " +
                                "-fx-cursor: hand;"
                )
        );

        buyButton.setOnMouseExited(e ->
                buyButton.setStyle(
                        "-fx-font-size: 18px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-color: #27ae60; " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 15px; " +
                                "-fx-background-radius: 10; " +
                                "-fx-cursor: hand;"
                )
        );

        buyButton.setOnAction(e -> handlePurchase());
        buyButton.setDisable(true); // Disabled până când se selectează un produs

        panel.getChildren().addAll(detailsLabel, itemDetailsArea, quantityBox, buyButton);
        return panel;
    }

    /**
     * Footer cu butoane de navigare
     */
    private HBox createFooter() {
        HBox footer = new HBox(15);
        footer.setPadding(new Insets(15));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #34495e;");

        Button backButton = new Button("🔙 Înapoi la Oraș");
        backButton.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-background-color: #e74c3c; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 10px 20px; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );

        backButton.setOnAction(e -> {
            // Navighează înapoi la meniul orașului
            TownMenuController townController = new TownMenuController(stage, hero);
            stage.setScene(townController.createScene());
        });

        footer.getChildren().add(backButton);
        return footer;
    }

    /**
     * Încarcă produsele pentru categoria selectată
     */
    private void loadItemsForCategory() {

        ShopCategory selectedCategory = categoryComboBox.getValue();

        if (itemDetailsArea != null) {
            itemDetailsArea.clear();
        }
        if (selectedCategory == null) return;

        List<ShopItemDTO> items = shopService.getItemsByCategory(selectedCategory, hero.getNivel());

        itemListView.getItems().clear();
        itemListView.getItems().addAll(items);

        // Resetează selecția
        itemDetailsArea.clear();
        buyButton.setDisable(true);
    }

    /**
     * Afișează detaliile produsului selectat
     */
    private void displayItemDetails(ShopItemDTO item) {
        if (item == null) {
            itemDetailsArea.clear();
            buyButton.setDisable(true);
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        details.append(item.getName()).append("\n");
        details.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        details.append("📝 Descriere:\n");
        details.append(item.getDescription()).append("\n\n");
        details.append("💰 Preț: ").append(item.getPrice()).append(" gold\n");

        int quantity = quantitySpinner.getValue();
        int totalCost = item.getPrice() * quantity;
        details.append("💵 Cost Total (x").append(quantity).append("): ").append(totalCost).append(" gold\n\n");

        boolean canAfford = shopService.canAfford(hero, item, quantity);
        if (canAfford) {
            details.append("✅ Poți cumpăra acest produs!\n");
            details.append("💰 Gold rămas: ").append(hero.getGold() - totalCost).append(" gold");
        } else {
            details.append("❌ Nu ai destul gold!\n");
            details.append("💸 Îți lipsesc: ").append(totalCost - hero.getGold()).append(" gold");
        }

        itemDetailsArea.setText(details.toString());
        buyButton.setDisable(!canAfford);

        // Update când se schimbă cantitatea
        quantitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            displayItemDetails(item);
        });
    }

    /**
     * Gestionează achiziția
     */
    private void handlePurchase() {
        ShopItemDTO selectedItem = itemListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        int quantity = quantitySpinner.getValue();
        int totalCost = selectedItem.getPrice() * quantity;

        // Confirmare
        String confirmMsg = String.format(
                "Vrei să cumperi %dx %s pentru %d gold?",
                quantity, selectedItem.getName(), totalCost
        );

        if (!DialogHelper.showConfirmation("Confirmare Achiziție", confirmMsg)) {
            return;
        }

        // Execută achiziția
        PurchaseResult result = shopService.purchaseItem(hero, selectedItem, quantity);

        if (result.isSuccess()) {
            DialogHelper.showSuccess("Achiziție Reușită", result.getMessage());

            // Update UI
            goldLabel.setText("💰 Gold: " + hero.getGold());
            displayItemDetails(selectedItem); // Reafișează detaliile cu gold-ul actualizat
        } else {
            DialogHelper.showError("Achiziție Eșuată", result.getMessage());
        }
    }
}