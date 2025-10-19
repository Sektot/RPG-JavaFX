package com.rpg.controller;

import com.rpg.model.characters.Erou;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.service.ShopServiceFX;
import com.rpg.service.dto.ShopItemDTO;
import com.rpg.utils.DialogHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;

/**
 * 🛍️ ADVANCED SHOP cu Trade Table, Buyback și Drag & Drop
 */
public class ShopAdvancedController {

    private Stage stage;
    private Erou hero;
    private ShopServiceFX shopService;

    // UI Components
    private ListView<TradeItem> playerInventoryList;
    private ListView<TradeItem> vendorInventoryList;
    private VBox tradeTable;
    private Label goldDifferenceLabel;
    private Button executeTradeButton;

    // Filters
    private ComboBox<ItemFilter> playerFilterCombo;
    private ComboBox<VendorFilter> vendorFilterCombo;

    // Trade data
    private List<TradeItem> playerTradeItems = new ArrayList<>();
    private List<TradeItem> vendorTradeItems = new ArrayList<>();
    private List<BuybackItem> buybackItems = new ArrayList<>();

    // Shop state
    private int dungeonEntriesCount = 0; // Pentru restock logic

    public ShopAdvancedController(Stage stage, Erou hero) {
        this.stage = stage;
        this.hero = hero;
        this.shopService = new ShopServiceFX();
        loadBuybackItems();
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setTop(createHeader());
        root.setCenter(createMainContent());
        root.setBottom(createFooter());
        root.setStyle("-fx-background-color: #1a1a2e;");

        return new Scene(root, 1400, 900);
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #16213e;");

        Label title = new Label("🛍️ ADVANCED SHOP - " + hero.getNume().toUpperCase());
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        Label goldLabel = new Label("💰 Gold: " + hero.getGold());
        goldLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #f1c40f;");

        header.getChildren().addAll(title, goldLabel);
        return header;
    }

    private HBox createMainContent() {
        HBox content = new HBox(15);
        content.setPadding(new Insets(20));

        // Left Panel - Player Inventory
        VBox leftPanel = createPlayerInventoryPanel();

        // Middle Panel - Trade Table
        VBox middlePanel = createTradeTablePanel();

        // Right Panel - Vendor Inventory
        VBox rightPanel = createVendorInventoryPanel();

        content.getChildren().addAll(leftPanel, middlePanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(middlePanel, Priority.NEVER);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        return content;
    }

    // ==================== PLAYER INVENTORY PANEL ====================

    private VBox createPlayerInventoryPanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(400);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");

        Label title = new Label("🎒 INVENTARUL TĂU");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        // Filter pentru player items
        playerFilterCombo = new ComboBox<>();
        playerFilterCombo.getItems().addAll(ItemFilter.values());
        playerFilterCombo.setValue(ItemFilter.ALL);
        playerFilterCombo.setMaxWidth(Double.MAX_VALUE);
        playerFilterCombo.setOnAction(e -> loadPlayerInventory());

        playerInventoryList = new ListView<>();
        playerInventoryList.setStyle("-fx-font-size: 14px; -fx-background-color: #2c3e50;");
        VBox.setVgrow(playerInventoryList, Priority.ALWAYS);

        setupPlayerInventoryDragAndDrop();
        loadPlayerInventory();

        panel.getChildren().addAll(title, playerFilterCombo, playerInventoryList);
        return panel;
    }

    // ==================== VENDOR INVENTORY PANEL ====================

    private VBox createVendorInventoryPanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(400);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");

        Label title = new Label("🏪 SHOP VENDOR");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        // Filter pentru vendor items + buyback
        vendorFilterCombo = new ComboBox<>();
        vendorFilterCombo.getItems().addAll(VendorFilter.values());
        vendorFilterCombo.setValue(VendorFilter.ALL);
        vendorFilterCombo.setMaxWidth(Double.MAX_VALUE);
        vendorFilterCombo.setOnAction(e -> loadVendorInventory());

        vendorInventoryList = new ListView<>();
        vendorInventoryList.setStyle("-fx-font-size: 14px; -fx-background-color: #2c3e50;");
        VBox.setVgrow(vendorInventoryList, Priority.ALWAYS);

        setupVendorInventoryDragAndDrop();
        loadVendorInventory();

        panel.getChildren().addAll(title, vendorFilterCombo, vendorInventoryList);
        return panel;
    }

    // ==================== TRADE TABLE PANEL ====================

    private VBox createTradeTablePanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(300);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 10;");

        Label title = new Label("⚖️ TRADE TABLE");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        // Your items section
        Label yourItemsLabel = new Label("📤 Tu dai:");
        yourItemsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e67e22; -fx-font-weight: bold;");

        VBox yourItemsBox = new VBox(5);
        yourItemsBox.setPrefHeight(200);
        yourItemsBox.setStyle("-fx-background-color: #34495e; -fx-background-radius: 5; -fx-padding: 10;");
        setupTradeTableDropTarget(yourItemsBox, true);

        // Vendor items section
        Label vendorItemsLabel = new Label("📥 Vendor dă:");
        vendorItemsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");

        VBox vendorItemsBox = new VBox(5);
        vendorItemsBox.setPrefHeight(200);
        vendorItemsBox.setStyle("-fx-background-color: #2d5a3d; -fx-background-radius: 5; -fx-padding: 10;");
        setupTradeTableDropTarget(vendorItemsBox, false);

        tradeTable = new VBox(10);
        tradeTable.getChildren().addAll(yourItemsLabel, yourItemsBox, vendorItemsLabel, vendorItemsBox);

        // Gold difference
        goldDifferenceLabel = new Label("💰 Gold: +0");
        goldDifferenceLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1c40f; -fx-font-weight: bold;");
        goldDifferenceLabel.setAlignment(Pos.CENTER);
        goldDifferenceLabel.setMaxWidth(Double.MAX_VALUE);

        // Execute trade button
        executeTradeButton = new Button("✅ EXECUTE TRADE");
        executeTradeButton.setMaxWidth(Double.MAX_VALUE);
        executeTradeButton.setStyle("-fx-font-size: 16px; -fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        executeTradeButton.setOnAction(e -> executeTrade());
        executeTradeButton.setDisable(true);

        // Clear trade button
        Button clearTradeButton = new Button("🗑️ CLEAR");
        clearTradeButton.setMaxWidth(Double.MAX_VALUE);
        clearTradeButton.setStyle("-fx-font-size: 14px; -fx-background-color: #e74c3c; -fx-text-fill: white;");
        clearTradeButton.setOnAction(e -> clearTrade());

        panel.getChildren().addAll(title, tradeTable, goldDifferenceLabel, executeTradeButton, clearTradeButton);
        return panel;
    }

    private HBox createFooter() {
        HBox footer = new HBox(15);
        footer.setPadding(new Insets(15));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #16213e;");

        Button refreshBtn = new Button("🔄 Refresh Shop");
        styleButton(refreshBtn, "#3498db");
        refreshBtn.setOnAction(e -> {
            forceRestock();
            loadVendorInventory();
            loadPlayerInventory();
        });

        Button backBtn = new Button("🔙 Înapoi");
        styleButton(backBtn, "#e74c3c");
        backBtn.setOnAction(e -> {
            TownMenuController townController = new TownMenuController(stage, hero);
            stage.setScene(townController.createScene());
        });

        footer.getChildren().addAll(refreshBtn, backBtn);
        return footer;
    }

    // ==================== DRAG & DROP SETUP ====================

    private void setupPlayerInventoryDragAndDrop() {
        playerInventoryList.setCellFactory(lv -> {
            ListCell<TradeItem> cell = new ListCell<TradeItem>() {
                @Override
                protected void updateItem(TradeItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setTooltip(null);
                    } else {
                        setText(item.getDisplayName());
                        if (item.getEquipment() != null) {
                            setTooltip(createItemTooltip(item.getEquipment()));
                        }
                        setStyle("-fx-text-fill: white;");
                    }
                }
            };

            // Drag source
            cell.setOnDragDetected(event -> {
                if (cell.getItem() != null) {
                    Dragboard db = cell.startDragAndDrop(TransferMode.COPY);
                    ClipboardContent content = new ClipboardContent();
                    content.putString("player:" + System.identityHashCode(cell.getItem()));
                    db.setContent(content);
                    event.consume();
                }
            });

            // Double click to add to trade
            cell.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && cell.getItem() != null) {
                    addToTrade(cell.getItem(), true);
                }
            });

            return cell;
        });
    }

    private void setupVendorInventoryDragAndDrop() {
        vendorInventoryList.setCellFactory(lv -> {
            ListCell<TradeItem> cell = new ListCell<TradeItem>() {
                @Override
                protected void updateItem(TradeItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setTooltip(null);
                    } else {
                        setText(item.getDisplayName());
                        if (item.getEquipment() != null) {
                            setTooltip(createItemTooltip(item.getEquipment()));
                        }

                        // Color coding pentru buyback items
                        if (item.isBuyback()) {
                            setStyle("-fx-text-fill: #e67e22; -fx-font-style: italic;"); // Orange pentru buyback
                        } else {
                            setStyle("-fx-text-fill: white;");
                        }
                    }
                }
            };

            // Drag source
            cell.setOnDragDetected(event -> {
                if (cell.getItem() != null) {
                    Dragboard db = cell.startDragAndDrop(TransferMode.COPY);
                    ClipboardContent content = new ClipboardContent();
                    content.putString("vendor:" + System.identityHashCode(cell.getItem()));
                    db.setContent(content);
                    event.consume();
                }
            });

            // Double click to add to trade
            cell.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && cell.getItem() != null) {
                    addToTrade(cell.getItem(), false);
                }
            });

            return cell;
        });
    }

    private void setupTradeTableDropTarget(VBox targetBox, boolean isPlayerSide) {
        targetBox.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                String content = event.getDragboard().getString();
                boolean validSource = isPlayerSide ? content.startsWith("player:") : content.startsWith("vendor:");

                if (validSource) {
                    event.acceptTransferModes(TransferMode.COPY);
                }
            }
            event.consume();
        });

        targetBox.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                String content = db.getString();
                TradeItem item = findItemByHash(content);

                if (item != null) {
                    addToTrade(item, isPlayerSide);
                    success = true;
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    // ==================== TRADE LOGIC ====================

    private void addToTrade(TradeItem item, boolean isPlayerSide) {
        if (isPlayerSide) {
            if (!playerTradeItems.contains(item)) {
                playerTradeItems.add(item);
            }
        } else {
            if (!vendorTradeItems.contains(item)) {
                vendorTradeItems.add(item);
            }
        }
        updateTradeDisplay();
    }

    private void updateTradeDisplay() {
        // Update trade table visual
        VBox yourItemsBox = (VBox) ((VBox) tradeTable.getChildren().get(1));
        VBox vendorItemsBox = (VBox) ((VBox) tradeTable.getChildren().get(3));

        yourItemsBox.getChildren().clear();
        vendorItemsBox.getChildren().clear();

        // Add player items to display
        for (TradeItem item : playerTradeItems) {
            Label itemLabel = new Label("📤 " + item.getDisplayName() + " (+" + item.getPrice() + "g)");
            itemLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
            itemLabel.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    playerTradeItems.remove(item);
                    updateTradeDisplay();
                }
            });
            yourItemsBox.getChildren().add(itemLabel);
        }

        // Add vendor items to display
        for (TradeItem item : vendorTradeItems) {
            Label itemLabel = new Label("📥 " + item.getDisplayName() + " (-" + item.getPrice() + "g)");
            itemLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
            itemLabel.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    vendorTradeItems.remove(item);
                    updateTradeDisplay();
                }
            });
            vendorItemsBox.getChildren().add(itemLabel);
        }

        // Calculate gold difference
        int playerGoldValue = playerTradeItems.stream().mapToInt(TradeItem::getPrice).sum();
        int vendorGoldValue = vendorTradeItems.stream().mapToInt(TradeItem::getPrice).sum();
        int goldDifference = playerGoldValue - vendorGoldValue;

        String goldText = "💰 Gold: ";
        if (goldDifference > 0) {
            goldText += "+" + goldDifference;
            goldDifferenceLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } else if (goldDifference < 0) {
            goldText += goldDifference;
            goldDifferenceLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else {
            goldText += "0";
            goldDifferenceLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1c40f; -fx-font-weight: bold;");
        }
        goldDifferenceLabel.setText(goldText);

        // Enable/disable trade button
        boolean canTrade = !playerTradeItems.isEmpty() || !vendorTradeItems.isEmpty();
        boolean hasEnoughGold = hero.getGold() >= Math.abs(Math.min(0, goldDifference));

        executeTradeButton.setDisable(!canTrade || !hasEnoughGold);

        if (!hasEnoughGold && goldDifference < 0) {
            executeTradeButton.setText("❌ INSUFFICIENT GOLD");
        } else {
            executeTradeButton.setText("✅ EXECUTE TRADE");
        }
    }

    private void executeTrade() {
        if (playerTradeItems.isEmpty() && vendorTradeItems.isEmpty()) {
            return;
        }

        // Calculate final gold change
        int playerGoldValue = playerTradeItems.stream().mapToInt(TradeItem::getPrice).sum();
        int vendorGoldValue = vendorTradeItems.stream().mapToInt(TradeItem::getPrice).sum();
        int goldDifference = playerGoldValue - vendorGoldValue;

        // Check if player has enough gold
        if (goldDifference < 0 && hero.getGold() < Math.abs(goldDifference)) {
            DialogHelper.showError("Insufficient Gold", "You need " + Math.abs(goldDifference) + " gold for this trade!");
            return;
        }

        // Execute the trade
        StringBuilder tradeDetails = new StringBuilder();
        tradeDetails.append("TRADE SUMMARY:\n\n");

        // Remove items from player, add to buyback
        for (TradeItem item : playerTradeItems) {
            if (item.getEquipment() != null) {
                hero.getInventar().removeItem(item.getEquipment());

                // Add to buyback with markup
                BuybackItem buybackItem = new BuybackItem(item.getEquipment(),
                        (int)(item.getPrice() * 1.2), dungeonEntriesCount); // 20% markup
                buybackItems.add(buybackItem);

                tradeDetails.append("📤 Sold: ").append(item.getDisplayName())
                        .append(" (+").append(item.getPrice()).append("g)\n");
            }
        }

        // Add items to player inventory
        for (TradeItem item : vendorTradeItems) {
            if (item.getEquipment() != null) {
                hero.getInventar().addItem(item.getEquipment());
                tradeDetails.append("📥 Bought: ").append(item.getDisplayName())
                        .append(" (-").append(item.getPrice()).append("g)\n");
            }
        }

        // Update gold
        hero.adaugaGold(goldDifference);

        tradeDetails.append("\n💰 Gold change: ");
        if (goldDifference > 0) {
            tradeDetails.append("+").append(goldDifference);
        } else {
            tradeDetails.append(goldDifference);
        }
        tradeDetails.append("\n💰 New balance: ").append(hero.getGold());

        DialogHelper.showSuccess("Trade Complete!", tradeDetails.toString());

        // Clear trade and refresh
        clearTrade();
        loadPlayerInventory();
        loadVendorInventory();

        // Update header gold display
        ((Label) ((VBox) stage.getScene().getRoot().getChildrenUnmodifiable().get(0))
                .getChildren().get(1)).setText("💰 Gold: " + hero.getGold());
    }

    private void clearTrade() {
        playerTradeItems.clear();
        vendorTradeItems.clear();
        updateTradeDisplay();
    }

    // ==================== DATA LOADING ====================

    private void loadPlayerInventory() {
        ItemFilter filter = playerFilterCombo.getValue();
        List<TradeItem> items = new ArrayList<>();

        for (ObiectEchipament equipment : hero.getInventar().getItems()) {
            if (filter == ItemFilter.ALL || matchesFilter(equipment, filter)) {
                items.add(new TradeItem(equipment, equipment.getPret(), false, false));
            }
        }

        playerInventoryList.getItems().clear();
        playerInventoryList.getItems().addAll(items);
    }

    private void loadVendorInventory() {
        VendorFilter filter = vendorFilterCombo.getValue();
        List<TradeItem> items = new ArrayList<>();

        if (filter == VendorFilter.ALL || filter != VendorFilter.BUYBACK) {
            // Add regular shop items
            List<ShopItemDTO> shopItems = shopService.getShopItems(hero.getNivel());

            for (ShopItemDTO shopItem : shopItems) {
                if (shopItem.getCategory() == ShopServiceFX.ShopCategory.ECHIPAMENT) {
                    ObiectEchipament equipment = shopService.createEquipmentFromShopItem(shopItem);
                    if (filter == VendorFilter.ALL || matchesVendorFilter(equipment, filter)) {
                        items.add(new TradeItem(equipment, shopItem.getPrice(), false, false));
                    }
                }
            }
        }

        if (filter == VendorFilter.ALL || filter == VendorFilter.BUYBACK) {
            // Add buyback items
            for (BuybackItem buybackItem : buybackItems) {
                if (buybackItem.getDungeonEntriesWhenSold() + 3 > dungeonEntriesCount) {
                    items.add(new TradeItem(buybackItem.getItem(),
                            buybackItem.getBuybackPrice(), true, false));
                }
            }
        }

        vendorInventoryList.getItems().clear();
        vendorInventoryList.getItems().addAll(items);
    }

    // ==================== HELPER METHODS ====================

    private TradeItem findItemByHash(String hashString) {
        String[] parts = hashString.split(":");
        if (parts.length != 2) return null;

        String source = parts[0];
        int hash = Integer.parseInt(parts[1]);

        List<TradeItem> sourceList = source.equals("player") ?
                new ArrayList<>(playerInventoryList.getItems()) :
                new ArrayList<>(vendorInventoryList.getItems());

        return sourceList.stream()
                .filter(item -> System.identityHashCode(item) == hash)
                .findFirst()
                .orElse(null);
    }

    private boolean matchesFilter(ObiectEchipament equipment, ItemFilter filter) {
        return switch (filter) {
            case ALL -> true;
            case WEAPONS -> equipment.isWeapon();
            case ARMOR -> equipment.isArmor();
            case ACCESSORIES -> equipment.isAccessory();
            case SHIELDS -> equipment.getTip() == ObiectEchipament.TipEchipament.SHIELD;
        };
    }

    private boolean matchesVendorFilter(ObiectEchipament equipment, VendorFilter filter) {
        return switch (filter) {
            case ALL -> true;
            case WEAPONS -> equipment.isWeapon();
            case ARMOR -> equipment.isArmor();
            case ACCESSORIES -> equipment.isAccessory();
            case SHIELDS -> equipment.getTip() == ObiectEchipament.TipEchipament.SHIELD;
            case BUYBACK -> false; // Handled separately
        };
    }

    private Tooltip createItemTooltip(ObiectEchipament item) {
        StringBuilder tooltipText = new StringBuilder();

        tooltipText.append("📦 ").append(item.getNume()).append("\n");
        tooltipText.append("═══════════════════════════\n");
        tooltipText.append("🎯 Tip: ").append(item.getTip().getDisplayName()).append("\n");
        tooltipText.append("⭐ Raritate: ").append(item.getRaritate().getDisplayName()).append("\n");
        tooltipText.append("📊 Nivel: ").append(item.getNivelNecesar()).append("\n");

        tooltipText.append("\n📊 STATISTICI:\n");
        Map<String, Integer> bonuses = item.getTotalBonuses();

        if (bonuses.isEmpty()) {
            tooltipText.append("  • Fără bonusuri");
        } else {
            bonuses.forEach((stat, bonus) -> {
                tooltipText.append("  ✨ +").append(bonus).append(" ").append(stat).append("\n");
            });
        }

        tooltipText.append("\n💰 Preț: ").append(item.getPret()).append(" gold");

        Tooltip tooltip = new Tooltip(tooltipText.toString());
        tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: #2c3e50; -fx-text-fill: white;");
        tooltip.setShowDuration(javafx.util.Duration.INDEFINITE);

        return tooltip;
    }

    private void forceRestock() {
        dungeonEntriesCount += 3; // Force restock
        buybackItems.removeIf(item ->
                item.getDungeonEntriesWhenSold() + 3 <= dungeonEntriesCount);
        shopService.forceRestock();
    }

    private void loadBuybackItems() {
        // Load buyback items from save or initialize empty
        buybackItems = new ArrayList<>();
    }

    private void styleButton(Button btn, String color) {
        btn.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 8px 16px; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"
        );
    }

    // ==================== DATA CLASSES ====================

    public static class TradeItem {
        private ObiectEchipament equipment;
        private int price;
        private boolean isBuyback;
        private boolean isBasicItem;

        public TradeItem(ObiectEchipament equipment, int price, boolean isBuyback, boolean isBasicItem) {
            this.equipment = equipment;
            this.price = price;
            this.isBuyback = isBuyback;
            this.isBasicItem = isBasicItem;
        }

        public String getDisplayName() {
            String prefix = isBuyback ? "🔄 " : "";
            return prefix + equipment.getNume() + " (" + price + "g)";
        }

        // Getters
        public ObiectEchipament getEquipment() { return equipment; }
        public int getPrice() { return price; }
        public boolean isBuyback() { return isBuyback; }
        public boolean isBasicItem() { return isBasicItem; }
    }

    public static class BuybackItem {
        private ObiectEchipament item;
        private int buybackPrice;
        private int dungeonEntriesWhenSold;

        public BuybackItem(ObiectEchipament item, int buybackPrice, int dungeonEntriesWhenSold) {
            this.item = item;
            this.buybackPrice = buybackPrice;
            this.dungeonEntriesWhenSold = dungeonEntriesWhenSold;
        }

        // Getters
        public ObiectEchipament getItem() { return item; }
        public int getBuybackPrice() { return buybackPrice; }
        public int getDungeonEntriesWhenSold() { return dungeonEntriesWhenSold; }
    }

    public enum ItemFilter {
        ALL("Toate"),
        WEAPONS("Arme"),
        ARMOR("Armuri"),
        SHIELDS("Scuturi"),
        ACCESSORIES("Accesorii");

        private final String displayName;

        ItemFilter(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum VendorFilter {
        ALL("Toate"),
        WEAPONS("Arme"),
        ARMOR("Armuri"),
        SHIELDS("Scuturi"),
        ACCESSORIES("Accesorii"),
        BUYBACK("Buyback");

        private final String displayName;

        VendorFilter(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
