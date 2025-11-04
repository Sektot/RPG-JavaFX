package com.rpg.controller;

import com.rpg.model.characters.Erou;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.service.ShopServiceFX;
import com.rpg.service.ShopServiceFX.ShopCategory;
import com.rpg.service.dto.PurchaseResult;
import com.rpg.service.dto.ShopItemDTO;
import com.rpg.utils.DialogHelper;
import com.rpg.utils.SpriteManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller pentru interfața Shop-ului în JavaFX
 *
 * 🎨 SPRITE SUPPORT:
 * Place UI sprites in: resources/sprites/ui/shop/
 * - background.png - Shop background
 * - header_bg.png - Header background
 * - panel_left_bg.png - Left panel background
 * - panel_right_bg.png - Right panel background
 * - button_buy.png / button_buy_hover.png
 * - button_back.png / button_back_hover.png
 * - item_frame.png - Optional item display frame
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

    // 🎨 UI Textures
    private Image backgroundTexture;
    private Image headerBgTexture;
    private Image panelBgTexture;
    private Image buttonBuyTexture;
    private Image buttonBuyHoverTexture;
    private Image buttonBackTexture;
    private Image buttonBackHoverTexture;

    public ShopController(Stage stage, Erou hero) {
        this.stage = stage;
        this.hero = hero;
        this.shopService = new ShopServiceFX();
        loadTextures();
    }

    /**
     * 🎨 Load all UI textures
     */
    private void loadTextures() {
        backgroundTexture = SpriteManager.getSprite("ui/shop", "background");
        headerBgTexture = SpriteManager.getSprite("ui/shop", "header_bg");
        panelBgTexture = SpriteManager.getSprite("ui/shop", "panel_bg");
        buttonBuyTexture = SpriteManager.getSprite("ui/shop", "button_buy");
        buttonBuyHoverTexture = SpriteManager.getSprite("ui/shop", "button_buy_hover");
        buttonBackTexture = SpriteManager.getSprite("ui/shop", "button_back");
        buttonBackHoverTexture = SpriteManager.getSprite("ui/shop", "button_back_hover");
    }

    public Scene createScene() {
        // 🎨 Use StackPane to layer background texture
        StackPane mainContainer = new StackPane();

        // Add background if available
        if (backgroundTexture != null) {
            ImageView backgroundView = new ImageView(backgroundTexture);
            backgroundView.setPreserveRatio(false);
            mainContainer.getChildren().add(backgroundView);

            // Bind background size to scene
            Scene scene = new Scene(mainContainer, 1900, 1080);
            backgroundView.fitWidthProperty().bind(scene.widthProperty());
            backgroundView.fitHeightProperty().bind(scene.heightProperty());
        }

        BorderPane root = new BorderPane();
        root.setTop(createHeader());
        root.setCenter(createMainContent());
        root.setBottom(createFooter());

        // Only use solid color if no background texture
        if (backgroundTexture == null) {
            root.setStyle("-fx-background-color: #2c3e50;");
        } else {
            root.setStyle("-fx-background-color: transparent;");
        }

        mainContainer.getChildren().add(root);

        Scene scene = new Scene(mainContainer, 1900, 1080);
        return scene;
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


// ✅ ADAUGĂ ACEST COD PENTRU A FIXA CULOAREA TEXTULUI:
        itemListView.setCellFactory(lv -> new ListCell<ShopItemDTO>() {
            @Override
            protected void updateItem(ShopItemDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    setStyle("-fx-text-fill: white;"); // ✨ Setează textul alb
                }
            }
        });

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
    /**
     * ✨ ÎMBUNĂTĂȚIT: Gestionează achiziția cu feedback extins
     */
    private void handlePurchase() {
        ShopItemDTO selectedItem = itemListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        int quantity = quantitySpinner.getValue();
        int totalCost = selectedItem.getPrice() * quantity;

        // Confirmare îmbunătățită
        StringBuilder confirmMsg = new StringBuilder();
        confirmMsg.append("🛍️ CONFIRMARE ACHIZIȚIE\n\n");
        confirmMsg.append("📦 Produs: ").append(selectedItem.getName()).append("\n");
        confirmMsg.append("🔢 Cantitate: ").append(quantity).append("x\n");
        confirmMsg.append("💰 Cost Total: ").append(totalCost).append(" gold\n");
        confirmMsg.append("💳 Gold rămas: ").append(hero.getGold() - totalCost).append(" gold\n\n");
        confirmMsg.append("Continui cu achiziția?");

        if (!DialogHelper.showConfirmation("Confirmare Achiziție", confirmMsg.toString())) {
            return;
        }

        // 🔧 DEBUG: Log achiziția
        System.out.printf("🛍️ PURCHASING: %s x%d for %d gold\n",
                selectedItem.getName(), quantity, totalCost);
        System.out.printf("💰 Gold before: %d\n", hero.getGold());

        // Execută achiziția
        PurchaseResult result = shopService.purchaseItem(hero, selectedItem, quantity);

        // 🔧 DEBUG: Log rezultatul
        System.out.printf("✅ Purchase result: %s - %s\n", result.isSuccess(), result.getMessage());
        System.out.printf("💰 Gold after: %d\n", hero.getGold());

        if (result.isSuccess()) {
            // Succes îmbunătățit cu detalii
            StringBuilder successMsg = new StringBuilder();
            successMsg.append("✅ ACHIZIȚIE REUȘITĂ!\n\n");
            successMsg.append("📦 ").append(result.getMessage()).append("\n");
            successMsg.append("💰 Cost: ").append(result.getGoldSpent()).append(" gold\n");
            successMsg.append("💳 Gold rămas: ").append(hero.getGold()).append(" gold\n\n");

            if (selectedItem.getId().startsWith("weapon_") || selectedItem.getId().startsWith("armor_")) {
                successMsg.append("🎒 Verifică inventarul pentru noul echipament!");
            }

            DialogHelper.showSuccess("Achiziție Reușită", successMsg.toString());

            // Update UI
            goldLabel.setText("💰 Gold: " + hero.getGold());
            displayItemDetails(selectedItem);
        } else {
            DialogHelper.showError("Achiziție Eșuată", result.getMessage());
        }
    }

}