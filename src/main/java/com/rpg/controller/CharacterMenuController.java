package com.rpg.controller;

import com.rpg.model.characters.Erou;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.service.InventoryServiceFX;
import com.rpg.service.dto.EquipResult;
import com.rpg.utils.DialogHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consolidated Character Menu with tabs for:
 * - Character Sheet
 * - Inventory
 * - Talent Tree
 * - Statistics
 */
public class CharacterMenuController {

    private Stage stage;
    private Erou hero;
    private Runnable onBackCallback;
    private int selectedTabIndex = 0;
    private InventoryServiceFX inventoryService;

    // UI components that need to be refreshed
    private VBox equipmentSection;
    private ListView<ObiectEchipament> characterSheetInventoryList;
    private ListView<ObiectEchipament> inventoryTabList;
    private TextArea statsArea;
    private TextArea itemDetailsArea;
    private TextArea inventoryItemDetailsArea;

    // Track equipment slots for drag-and-drop highlighting
    private Map<String, VBox> equipmentSlots = new HashMap<>();
    private ObiectEchipament draggedItem = null;

    // Persistent tooltip that follows mouse
    private Tooltip persistentTooltip;
    private ObiectEchipament selectedItem = null;
    private ObiectEchipament selectedInventoryItem = null;

    // Inventory filtering and pockets
    private ComboBox<String> inventoryFilterComboBox;
    private ComboBox<String> characterSheetFilterComboBox;
    private com.rpg.model.inventory.ItemPocket currentPocket = null;
    private com.rpg.model.inventory.ItemPocket currentCharacterSheetPocket = null;

    // Pocket editor components
    private ComboBox<com.rpg.model.inventory.ItemPocket> pocketEditorComboBox;
    private ListView<ObiectEchipament> pocketEditorList;
    private TextField pocketNameField;
    private ComboBox<String> pocketColorComboBox;
    private com.rpg.model.inventory.ItemPocket editingPocket = null;

    public CharacterMenuController(Stage stage, Erou hero, Runnable onBackCallback) {
        this.stage = stage;
        this.hero = hero;
        this.onBackCallback = onBackCallback;
        this.inventoryService = new InventoryServiceFX();

        // Initialize persistent tooltip
        persistentTooltip = new Tooltip();
        persistentTooltip.setStyle("-fx-font-size: 12px; -fx-background-color: #1a1a2e; -fx-text-fill: white; -fx-padding: 10;");
        persistentTooltip.setShowDelay(javafx.util.Duration.ZERO);
        persistentTooltip.setHideDelay(javafx.util.Duration.INDEFINITE);
        persistentTooltip.setAutoHide(false);
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        root.setTop(createHeader());
        root.setCenter(createMainContent());
        root.setBottom(createFooter());

        return new Scene(root, 1900, 1080);
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #16213e;");

        Label title = new Label("👤 CHARACTER MENU");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #f1c40f;");

        Label subtitle = new Label(hero.getNume() + " - Level " + hero.getNivel());
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #95a5a6;");

        header.getChildren().addAll(title, subtitle);
        return header;
    }

    private VBox createMainContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #1a1a2e;");

        Tab characterSheetTab = new Tab("📋 Character Sheet", createCharacterSheetPanel());
        characterSheetTab.setClosable(false);

        Tab inventoryTab = new Tab("🎒 Inventory", createInventoryPanel());
        inventoryTab.setClosable(false);

        Tab talentTreeTab = new Tab("🌳 Talent Tree", createTalentTreePanel());
        talentTreeTab.setClosable(false);

        Tab statisticsTab = new Tab("📊 Statistics", createStatisticsPanel());
        statisticsTab.setClosable(false);

        tabPane.getTabs().addAll(characterSheetTab, inventoryTab, talentTreeTab, statisticsTab);

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

    private VBox createCharacterSheetPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #0f0f1e;");

        Label titleLabel = new Label("📋 CHARACTER SHEET - Drag & Drop or Double-Click to Equip | Click item for details");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f1c40f;");

        // Create layout: Equipment | Inventory | Stats & Item Details
        HBox contentBox = new HBox(15);
        VBox.setVgrow(contentBox, Priority.ALWAYS);

        // Left side - Equipment Slots
        equipmentSection = new VBox(10);
        equipmentSection.setPadding(new Insets(15));
        equipmentSection.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");
        equipmentSection.setPrefWidth(280);

        refreshEquipmentDisplay();

        // Middle - Inventory List
        VBox inventorySection = new VBox(10);
        inventorySection.setPadding(new Insets(15));
        inventorySection.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");
        inventorySection.setPrefWidth(300);
        VBox.setVgrow(inventorySection, Priority.ALWAYS);

        Label invTitle = new Label("🎒 INVENTORY");
        invTitle.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        // Add filter for character sheet inventory
        HBox characterSheetFilterBar = createCharacterSheetFilterBar();

        characterSheetInventoryList = new ListView<>();
        characterSheetInventoryList.setStyle("-fx-background-color: #0f0f1e; -fx-control-inner-background: #0f0f1e;");
        VBox.setVgrow(characterSheetInventoryList, Priority.ALWAYS);

        refreshInventoryDisplay();

        inventorySection.getChildren().addAll(invTitle, characterSheetFilterBar, characterSheetInventoryList);

        // Right side - Stats & Item Details
        VBox rightSection = new VBox(10);
        rightSection.setPadding(new Insets(15));
        rightSection.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");
        rightSection.setPrefWidth(320);
        HBox.setHgrow(rightSection, Priority.ALWAYS);

        Label statsTitle = new Label("📊 STATS");
        statsTitle.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        statsArea = new TextArea();
        statsArea.setEditable(false);
        statsArea.setWrapText(true);
        statsArea.setStyle("-fx-control-inner-background: #0f0f1e; -fx-text-fill: white; -fx-font-size: 12px;");
        statsArea.setPrefHeight(250);

        refreshStatsDisplay();

        Label itemDetailsTitle = new Label("🔍 ITEM DETAILS");
        itemDetailsTitle.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        itemDetailsArea = new TextArea();
        itemDetailsArea.setEditable(false);
        itemDetailsArea.setWrapText(true);
        itemDetailsArea.setStyle("-fx-control-inner-background: #0f0f1e; -fx-text-fill: white; -fx-font-size: 12px;");
        itemDetailsArea.setText("Click on an item to see details and comparison...");
        VBox.setVgrow(itemDetailsArea, Priority.ALWAYS);

        rightSection.getChildren().addAll(statsTitle, statsArea, itemDetailsTitle, itemDetailsArea);

        contentBox.getChildren().addAll(equipmentSection, inventorySection, rightSection);
        panel.getChildren().addAll(titleLabel, contentBox);
        return panel;
    }

    private void refreshEquipmentDisplay() {
        equipmentSection.getChildren().clear();

        Label equipTitle = new Label("⚔️ EQUIPMENT");
        equipTitle.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        GridPane equipmentGrid = new GridPane();
        equipmentGrid.setHgap(8);
        equipmentGrid.setVgap(8);
        equipmentGrid.setPadding(new Insets(10, 0, 0, 0));

        // Add clickable equipment slots
        addClickableEquipmentSlot(equipmentGrid, "⛑️ Helmet", hero.getHelmetEquipped(), "HELMET", 0, 0);
        addClickableEquipmentSlot(equipmentGrid, "💍 Ring 1", hero.getRing1Equipped(), "RING1", 0, 1);
        addClickableEquipmentSlot(equipmentGrid, "⚔️ Main Hand", hero.getMainHandWeapon(), "MAIN_HAND", 0, 2);
        addClickableEquipmentSlot(equipmentGrid, "📿 Necklace", hero.getNecklaceEquipped(), "NECKLACE", 0, 3);

        addClickableEquipmentSlot(equipmentGrid, "🛡️ Armor", hero.getArmorEquipped(), "ARMOR", 1, 0);
        addClickableEquipmentSlot(equipmentGrid, "💍 Ring 2", hero.getRing2Equipped(), "RING2", 1, 1);
        addClickableEquipmentSlot(equipmentGrid, "🛡️ Off Hand", hero.getOffHandItem(), "OFF_HAND", 1, 2);
        addClickableEquipmentSlot(equipmentGrid, "🥾 Boots", hero.getBootsEquipped(), "BOOTS", 1, 3);

        addClickableEquipmentSlot(equipmentGrid, "🧤 Gloves", hero.getGlovesEquipped(), "GLOVES", 2, 0);

        equipmentSection.getChildren().addAll(equipTitle, equipmentGrid);
    }

    private void refreshStatsDisplay() {
        StringBuilder stats = new StringBuilder();
        stats.append("⚔️ CORE ATTRIBUTES:\n");
        stats.append(String.format("💪 Strength: %d (%d)\n", hero.getStrength(), hero.getStrengthTotal()));
        stats.append(String.format("🏃 Dexterity: %d (%d)\n", hero.getDexterity(), hero.getDexterityTotal()));
        stats.append(String.format("🧠 Intelligence: %d (%d)\n", hero.getIntelligence(), hero.getIntelligenceTotal()));
        stats.append(String.format("🛡️ Defense: %d (%d)\n", hero.getDefense(), hero.getDefenseTotal()));
        stats.append("\n💚 VITALS:\n");
        stats.append(String.format("❤️ HP: %d / %d\n", hero.getViata(), hero.getViataMaxima()));
        stats.append(String.format("💙 %s: %d / %d\n", hero.getTipResursa(), hero.getResursaCurenta(), hero.getResursaMaxima()));
        stats.append("\n⚔️ COMBAT:\n");
        stats.append(String.format("⚔️ Damage: %d\n", hero.getStrengthTotal() * 2));
        stats.append(String.format("🎯 Hit: %.1f%%\n", hero.getHitChance()));
        stats.append(String.format("💥 Crit: %.1f%%\n", hero.getCritChanceTotal()));
        stats.append(String.format("💨 Dodge: %.1f%%\n", hero.getDodgeChanceTotal()));

        statsArea.setText(stats.toString());
    }

    private void refreshInventoryDisplay() {
        characterSheetInventoryList.getItems().clear();

        // Apply filtering for character sheet
        List<ObiectEchipament> filteredItems = new ArrayList<>();
        String filterSelection = characterSheetFilterComboBox != null ? characterSheetFilterComboBox.getValue() : "All Items";

        System.out.println("DEBUG refreshInventoryDisplay: currentCharacterSheetPocket = " +
                (currentCharacterSheetPocket != null ? currentCharacterSheetPocket.getName() : "null"));
        System.out.println("DEBUG refreshInventoryDisplay: filterSelection = " + filterSelection);

        if (currentCharacterSheetPocket != null) {
            // Show only items in the selected pocket
            filteredItems.addAll(currentCharacterSheetPocket.getItems());
            System.out.println("DEBUG: Showing pocket items, count = " + filteredItems.size());
        } else if (filterSelection != null && !filterSelection.equals("All Items") && !filterSelection.equals("─────────")) {
            // Filter by item type
            for (ObiectEchipament item : hero.getInventar().getItems()) {
                if (matchesFilter(item, filterSelection)) {
                    filteredItems.add(item);
                }
            }
            System.out.println("DEBUG: Showing filtered items, count = " + filteredItems.size());
        } else {
            // Show all items
            filteredItems.addAll(hero.getInventar().getItems());
            System.out.println("DEBUG: Showing all items, count = " + filteredItems.size());
        }

        characterSheetInventoryList.getItems().addAll(filteredItems);

        characterSheetInventoryList.setCellFactory(lv -> {
            ListCell<ObiectEchipament> cell = new ListCell<ObiectEchipament>() {
                @Override
                protected void updateItem(ObiectEchipament item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        String rarityColor = switch (item.getRaritate()) {
                            case COMMON -> "#9d9d9d";
                            case UNCOMMON -> "#1eff00";
                            case RARE -> "#0070dd";
                            case EPIC -> "#a335ee";
                            case LEGENDARY -> "#ff8000";
                        };

                        String displayText = item.getNume();
                        if (item.getEnhancementLevel() > 0) {
                            displayText += " +" + item.getEnhancementLevel();
                        }
                        if (item.isEquipped()) {
                            displayText += " [EQUIPPED]";
                            setStyle("-fx-text-fill: " + rarityColor + "; -fx-background-color: #2c3e50; -fx-font-size: 12px;");
                        } else {
                            setStyle("-fx-text-fill: " + rarityColor + "; -fx-background-color: transparent; -fx-font-size: 12px;");
                        }

                        setText(displayText);
                    }
                }
            };

            // DRAG SOURCE - Start dragging item from inventory
            cell.setOnDragDetected(event -> {
                ObiectEchipament item = cell.getItem();
                if (item != null && !item.isEquipped()) {
                    draggedItem = item;
                    highlightCompatibleSlots(item);

                    Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString("equipment:" + System.identityHashCode(item));
                    db.setContent(content);
                    event.consume();
                }
            });

            cell.setOnDragDone(event -> {
                draggedItem = null;
                resetSlotHighlighting();
                event.consume();
            });

            // CLICK - Show item details in panel / DOUBLE-CLICK - Equip item
            cell.setOnMouseClicked(event -> {
                ObiectEchipament item = cell.getItem();
                if (item != null) {
                    if (event.getClickCount() == 2 && !item.isEquipped()) {
                        // Double-click to equip
                        handleEquipItem(item);
                    } else if (event.getClickCount() == 1) {
                        // Single click to show details
                        selectedItem = item;
                        showItemComparison(item);
                    }
                }
            });

            // HOVER - Show persistent tooltip that follows mouse
            cell.setOnMouseEntered(event -> {
                ObiectEchipament item = cell.getItem();
                if (item != null) {
                    showTooltipForItem(item, cell);
                }
            });

            cell.setOnMouseMoved(event -> {
                if (persistentTooltip.isShowing()) {
                    // Tooltip will auto-follow mouse due to being installed on the cell
                }
            });

            cell.setOnMouseExited(event -> {
                hideTooltip();
            });

            return cell;
        });
    }

    private void addClickableEquipmentSlot(GridPane grid, String slotName, ObiectEchipament item, String slotType, int col, int row) {
        VBox slotBox = new VBox(5);
        slotBox.setPadding(new Insets(8));
        slotBox.setStyle("-fx-background-color: #0f0f1e; -fx-background-radius: 5; -fx-border-color: #34495e; -fx-border-radius: 5; -fx-cursor: hand;");
        slotBox.setPrefWidth(130);
        slotBox.setMinHeight(65);
        slotBox.setUserData(slotType); // Store slot type for drag-and-drop

        // Store slot for highlighting
        equipmentSlots.put(slotType, slotBox);

        Label slotLabel = new Label(slotName);
        slotLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 10px; -fx-font-weight: bold;");

        if (item != null) {
            Label itemLabel = new Label(item.getNume());
            String rarityColor = switch (item.getRaritate()) {
                case COMMON -> "#9d9d9d";
                case UNCOMMON -> "#1eff00";
                case RARE -> "#0070dd";
                case EPIC -> "#a335ee";
                case LEGENDARY -> "#ff8000";
            };
            itemLabel.setStyle("-fx-text-fill: " + rarityColor + "; -fx-font-size: 11px;");
            itemLabel.setWrapText(true);

            if (item.getEnhancementLevel() > 0) {
                Label enhanceLabel = new Label("+" + item.getEnhancementLevel());
                enhanceLabel.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 9px;");
                slotBox.getChildren().addAll(slotLabel, itemLabel, enhanceLabel);
            } else {
                slotBox.getChildren().addAll(slotLabel, itemLabel);
            }

            // Click to unequip or show details
            slotBox.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    // Double-click to unequip
                    handleUnequipItem(item, slotType);
                } else if (e.getClickCount() == 1) {
                    // Single click to show details
                    selectedItem = item;
                    showItemDetails(item, null);
                }
            });

            // Show tooltip on hover
            slotBox.setOnMouseEntered(e -> {
                slotBox.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 5; -fx-border-color: #e74c3c; -fx-border-radius: 5; -fx-cursor: hand;");
                showTooltipForItem(item, slotBox);
            });
            slotBox.setOnMouseExited(e -> {
                slotBox.setStyle("-fx-background-color: #0f0f1e; -fx-background-radius: 5; -fx-border-color: #34495e; -fx-border-radius: 5; -fx-cursor: hand;");
                hideTooltip();
            });
        } else {
            Label emptyLabel = new Label("[Empty]");
            emptyLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 10px; -fx-font-style: italic;");
            slotBox.getChildren().addAll(slotLabel, emptyLabel);
        }

        // DRAG TARGET - Accept drops
        slotBox.setOnDragOver(event -> {
            if (event.getGestureSource() != slotBox && event.getDragboard().hasString()) {
                if (draggedItem != null && canEquipToSlot(draggedItem, slotType)) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
            }
            event.consume();
        });

        slotBox.setOnDragEntered(event -> {
            if (draggedItem != null && canEquipToSlot(draggedItem, slotType)) {
                slotBox.setStyle("-fx-background-color: #27ae60; -fx-background-radius: 5; -fx-border-color: #2ecc71; -fx-border-width: 2; -fx-border-radius: 5;");
            }
            event.consume();
        });

        slotBox.setOnDragExited(event -> {
            String borderColor = equipmentSlots.get(slotType).getStyle().contains("#3498db") ? "#3498db" : "#34495e";
            slotBox.setStyle("-fx-background-color: #0f0f1e; -fx-background-radius: 5; -fx-border-color: " + borderColor + "; -fx-border-radius: 5;");
            event.consume();
        });

        slotBox.setOnDragDropped(event -> {
            if (draggedItem != null) {
                handleEquipItem(draggedItem);
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });

        grid.add(slotBox, col, row);
    }

    private void handleEquipItem(ObiectEchipament item) {
        EquipResult result = inventoryService.equipItem(hero, item);

        if (result.isSuccess()) {
            DialogHelper.showSuccess("Item Equipped", result.getMessage());
            refreshEquipmentDisplay();
            refreshStatsDisplay();
            refreshInventoryDisplay();
        } else {
            DialogHelper.showError("Cannot Equip", result.getMessage());
        }
    }

    private void handleUnequipItem(ObiectEchipament item, String slotType) {
        EquipResult result = inventoryService.unequipItem(hero, item);

        if (result.isSuccess()) {
            DialogHelper.showInfo("Item Unequipped", result.getMessage());
            refreshEquipmentDisplay();
            refreshStatsDisplay();
            refreshInventoryDisplay();
        } else {
            DialogHelper.showError("Cannot Unequip", result.getMessage());
        }
    }

    private void highlightCompatibleSlots(ObiectEchipament item) {
        List<String> compatibleSlots = getCompatibleSlots(item);
        for (String slotType : compatibleSlots) {
            VBox slot = equipmentSlots.get(slotType);
            if (slot != null) {
                slot.setStyle("-fx-background-color: #0f0f1e; -fx-background-radius: 5; -fx-border-color: #3498db; -fx-border-width: 2; -fx-border-radius: 5;");
            }
        }
    }

    private void resetSlotHighlighting() {
        for (VBox slot : equipmentSlots.values()) {
            slot.setStyle("-fx-background-color: #0f0f1e; -fx-background-radius: 5; -fx-border-color: #34495e; -fx-border-radius: 5;");
        }
    }

    private List<String> getCompatibleSlots(ObiectEchipament item) {
        List<String> slots = new ArrayList<>();
        ObiectEchipament.TipEchipament type = item.getTip();

        switch (type) {
            case WEAPON_ONE_HANDED, WEAPON_TWO_HANDED -> slots.add("MAIN_HAND");
            case SHIELD, OFF_HAND_WEAPON, OFF_HAND_MAGIC -> slots.add("OFF_HAND");
            case ARMOR -> slots.add("ARMOR");
            case HELMET -> slots.add("HELMET");
            case GLOVES -> slots.add("GLOVES");
            case BOOTS -> slots.add("BOOTS");
            case RING -> {
                slots.add("RING1");
                slots.add("RING2");
            }
            case NECKLACE -> slots.add("NECKLACE");
        }
        return slots;
    }

    private boolean canEquipToSlot(ObiectEchipament item, String slotType) {
        return getCompatibleSlots(item).contains(slotType);
    }

    private void showItemComparison(ObiectEchipament newItem) {
        // Find currently equipped item in same slot
        ObiectEchipament currentItem = findEquippedItemForSlot(newItem);
        showItemDetails(newItem, currentItem);
    }

    private ObiectEchipament findEquippedItemForSlot(ObiectEchipament item) {
        List<String> compatibleSlots = getCompatibleSlots(item);

        for (String slot : compatibleSlots) {
            ObiectEchipament equipped = switch (slot) {
                case "MAIN_HAND" -> hero.getMainHandWeapon();
                case "OFF_HAND" -> hero.getOffHandItem();
                case "ARMOR" -> hero.getArmorEquipped();
                case "HELMET" -> hero.getHelmetEquipped();
                case "GLOVES" -> hero.getGlovesEquipped();
                case "BOOTS" -> hero.getBootsEquipped();
                case "RING1" -> hero.getRing1Equipped();
                case "RING2" -> hero.getRing2Equipped();
                case "NECKLACE" -> hero.getNecklaceEquipped();
                default -> null;
            };
            if (equipped != null) return equipped;
        }
        return null;
    }

    private ObiectEchipament findEquippedItemForComparison(ObiectEchipament item) {
        List<String> compatibleSlots = getCompatibleSlots(item);

        for (String slot : compatibleSlots) {
            ObiectEchipament equipped = switch (slot) {
                case "MAIN_HAND" -> hero.getMainHandWeapon();
                case "OFF_HAND" -> hero.getOffHandItem();
                case "ARMOR" -> hero.getArmorEquipped();
                case "HELMET" -> hero.getHelmetEquipped();
                case "GLOVES" -> hero.getGlovesEquipped();
                case "BOOTS" -> hero.getBootsEquipped();
                case "RING1" -> hero.getRing1Equipped();
                case "RING2" -> hero.getRing2Equipped();
                case "NECKLACE" -> hero.getNecklaceEquipped();
                default -> null;
            };
            if (equipped != null && !equipped.equals(item)) return equipped;
        }
        return null;
    }

    private void showItemDetails(ObiectEchipament item, ObiectEchipament comparison) {
        StringBuilder details = new StringBuilder();

        // Item header
        String rarityColor = switch (item.getRaritate()) {
            case COMMON -> "COMMON";
            case UNCOMMON -> "UNCOMMON";
            case RARE -> "RARE";
            case EPIC -> "EPIC";
            case LEGENDARY -> "LEGENDARY";
        };

        details.append("═══════════════════════════════════\n");
        details.append(item.getNume());
        if (item.getEnhancementLevel() > 0) {
            details.append(" +").append(item.getEnhancementLevel());
        }
        details.append("\n");
        details.append(rarityColor).append(" | ").append(item.getTip()).append("\n");
        details.append("Level Requirement: ").append(item.getNivelNecesar()).append("\n");
        details.append("═══════════════════════════════════\n\n");

        // Item bonuses
        if (item.getBonuses() != null && !item.getBonuses().isEmpty()) {
            details.append("BONUSES:\n");
            item.getBonuses().forEach((stat, value) -> {
                details.append("  +").append(value).append(" ").append(formatStatName(stat));

                // Show comparison if available
                if (comparison != null && comparison.getBonuses().containsKey(stat)) {
                    int diff = value - comparison.getBonuses().get(stat);
                    if (diff > 0) {
                        details.append(" (▲+").append(diff).append(")");
                    } else if (diff < 0) {
                        details.append(" (▼").append(diff).append(")");
                    }
                }
                details.append("\n");
            });
        }

        // Enhancement bonuses
        if (item.getEnhancementLevel() > 0 && item.getEnhancementBonuses() != null && !item.getEnhancementBonuses().isEmpty()) {
            details.append("\nENHANCEMENT BONUSES:\n");
            item.getEnhancementBonuses().forEach((stat, value) ->
                details.append("  +").append(value).append(" ").append(formatStatName(stat)).append("\n")
            );
        }

        // Show comparison item
        if (comparison != null) {
            details.append("\n═══ CURRENTLY EQUIPPED ═══\n");
            details.append(comparison.getNume());
            if (comparison.getEnhancementLevel() > 0) {
                details.append(" +").append(comparison.getEnhancementLevel());
            }
            details.append("\n\n");

            if (comparison.getBonuses() != null && !comparison.getBonuses().isEmpty()) {
                details.append("BONUSES:\n");
                comparison.getBonuses().forEach((stat, value) ->
                    details.append("  +").append(value).append(" ").append(formatStatName(stat)).append("\n")
                );
            }
        }

        itemDetailsArea.setText(details.toString());
    }

    private String formatStatName(String stat) {
        return switch (stat.toLowerCase()) {
            case "damage" -> "Damage";
            case "defense" -> "Defense";
            case "health", "viata" -> "Health";
            case "strength" -> "Strength";
            case "dexterity" -> "Dexterity";
            case "intelligence" -> "Intelligence";
            case "crit_chance" -> "Crit Chance";
            case "hit_chance" -> "Hit Chance";
            case "dodge_chance" -> "Dodge Chance";
            default -> stat;
        };
    }

    private void showTooltipForItem(ObiectEchipament item, javafx.scene.Node node) {
        if (item == null) return;

        // Build tooltip text
        StringBuilder tooltipText = new StringBuilder();

        String rarityColor = switch (item.getRaritate()) {
            case COMMON -> "COMMON";
            case UNCOMMON -> "UNCOMMON";
            case RARE -> "RARE";
            case EPIC -> "EPIC";
            case LEGENDARY -> "LEGENDARY";
        };

        tooltipText.append(item.getNume());
        if (item.getEnhancementLevel() > 0) {
            tooltipText.append(" +").append(item.getEnhancementLevel());
        }
        tooltipText.append("\n");
        tooltipText.append(rarityColor).append(" | Lvl ").append(item.getNivelNecesar());
        tooltipText.append("\n─────────────────────\n");

        // Add bonuses
        if (item.getBonuses() != null && !item.getBonuses().isEmpty()) {
            item.getBonuses().forEach((stat, value) -> {
                tooltipText.append("+").append(value).append(" ").append(formatStatName(stat)).append("\n");
            });
        }

        // Add enhancement bonuses
        if (item.getEnhancementLevel() > 0 && item.getEnhancementBonuses() != null && !item.getEnhancementBonuses().isEmpty()) {
            tooltipText.append("\nEnhancement:\n");
            item.getEnhancementBonuses().forEach((stat, value) -> {
                tooltipText.append("+").append(value).append(" ").append(formatStatName(stat)).append("\n");
            });
        }

        if (item.isEquipped()) {
            tooltipText.append("\n[EQUIPPED]");
        }

        persistentTooltip.setText(tooltipText.toString());

        // Install tooltip on the node
        Tooltip.install(node, persistentTooltip);
    }

    private void hideTooltip() {
        if (persistentTooltip != null) {
            persistentTooltip.hide();
        }
    }

    private void addEquipmentSlot(GridPane grid, String slotName, com.rpg.model.items.ObiectEchipament item, int col, int row) {
        VBox slotBox = new VBox(5);
        slotBox.setPadding(new Insets(8));
        slotBox.setStyle("-fx-background-color: #0f0f1e; -fx-background-radius: 5; -fx-border-color: #34495e; -fx-border-radius: 5;");
        slotBox.setPrefWidth(140);
        slotBox.setMinHeight(60);

        Label slotLabel = new Label(slotName);
        slotLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px; -fx-font-weight: bold;");

        if (item != null) {
            Label itemLabel = new Label(item.getNume());
            String rarityColor = switch (item.getRaritate()) {
                case COMMON -> "#9d9d9d";
                case UNCOMMON -> "#1eff00";
                case RARE -> "#0070dd";
                case EPIC -> "#a335ee";
                case LEGENDARY -> "#ff8000";
            };
            itemLabel.setStyle("-fx-text-fill: " + rarityColor + "; -fx-font-size: 12px;");
            itemLabel.setWrapText(true);

            if (item.getEnhancementLevel() > 0) {
                Label enhanceLabel = new Label("+" + item.getEnhancementLevel());
                enhanceLabel.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 10px;");
                slotBox.getChildren().addAll(slotLabel, itemLabel, enhanceLabel);
            } else {
                slotBox.getChildren().addAll(slotLabel, itemLabel);
            }
        } else {
            Label emptyLabel = new Label("[Empty]");
            emptyLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 11px; -fx-font-style: italic;");
            slotBox.getChildren().addAll(slotLabel, emptyLabel);
        }

        grid.add(slotBox, col, row);
    }

    private VBox createInventoryPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #0f0f1e;");

        Label titleLabel = new Label("🎒 INVENTORY - Filter & Organize");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #f1c40f;");

        // Resource summary bar
        HBox resourceBar = new HBox(15);
        resourceBar.setPadding(new Insets(10));
        resourceBar.setStyle("-fx-background-color: #16213e; -fx-background-radius: 5;");
        resourceBar.setAlignment(Pos.CENTER);

        Label goldLabel = new Label("💰 Gold: " + hero.getGold());
        goldLabel.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label scrapLabel = new Label("🔧 Scrap: " + hero.getScrap());
        scrapLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label healthPotLabel = new Label("🧪 HP Potions: " + hero.getHealthPotions());
        healthPotLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px;");

        Label manaPotLabel = new Label("💙 Mana Potions: " + hero.getManaPotions());
        manaPotLabel.setStyle("-fx-text-fill: #3498db; -fx-font-size: 14px;");

        resourceBar.getChildren().addAll(goldLabel, scrapLabel, healthPotLabel, manaPotLabel);

        // Filter Bar (only for filtering all items, not for pockets)
        HBox filterBar = createSimpleFilterBar();

        // Main content: All Items | Pocket Editor | Item Details
        HBox contentBox = new HBox(15);
        VBox.setVgrow(contentBox, Priority.ALWAYS);

        // LEFT - All Items list with filtering
        VBox itemsSection = new VBox(10);
        itemsSection.setPadding(new Insets(15));
        itemsSection.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");
        itemsSection.setPrefWidth(350);
        VBox.setVgrow(itemsSection, Priority.ALWAYS);

        Label itemsTitle = new Label("📦 ALL ITEMS");
        itemsTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        inventoryTabList = new ListView<>();
        inventoryTabList.setStyle("-fx-background-color: #0f0f1e; -fx-control-inner-background: #0f0f1e;");
        VBox.setVgrow(inventoryTabList, Priority.ALWAYS);

        refreshInventoryTabList();

        itemsSection.getChildren().addAll(itemsTitle, inventoryTabList);

        // MIDDLE - Pocket Editor
        VBox pocketSection = createPocketEditorSection();

        // RIGHT - Item Details
        VBox detailsSection = new VBox(10);
        detailsSection.setPadding(new Insets(15));
        detailsSection.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");
        detailsSection.setPrefWidth(350);
        HBox.setHgrow(detailsSection, Priority.ALWAYS);

        Label detailsTitle = new Label("🔍 ITEM DETAILS");
        detailsTitle.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        inventoryItemDetailsArea = new TextArea();
        inventoryItemDetailsArea.setEditable(false);
        inventoryItemDetailsArea.setWrapText(true);
        inventoryItemDetailsArea.setStyle("-fx-control-inner-background: #0f0f1e; -fx-text-fill: white; -fx-font-size: 12px;");
        inventoryItemDetailsArea.setText("Click on an item to see details and comparison...");
        VBox.setVgrow(inventoryItemDetailsArea, Priority.ALWAYS);

        detailsSection.getChildren().addAll(detailsTitle, inventoryItemDetailsArea);

        contentBox.getChildren().addAll(itemsSection, pocketSection, detailsSection);

        panel.getChildren().addAll(titleLabel, resourceBar, filterBar, contentBox);
        return panel;
    }

    private void refreshInventoryTabList() {
        inventoryTabList.getItems().clear();

        // Apply filtering (only type filters, no pockets here)
        List<ObiectEchipament> filteredItems = new ArrayList<>();
        String filterSelection = inventoryFilterComboBox != null ? inventoryFilterComboBox.getValue() : "All Items";

        if (filterSelection != null && !filterSelection.equals("All Items")) {
            // Filter by item type only
            for (ObiectEchipament item : hero.getInventar().getItems()) {
                if (matchesFilter(item, filterSelection)) {
                    filteredItems.add(item);
                }
            }
        } else {
            // Show all items
            filteredItems.addAll(hero.getInventar().getItems());
        }

        inventoryTabList.getItems().addAll(filteredItems);

        inventoryTabList.setCellFactory(lv -> {
            ListCell<ObiectEchipament> cell = new ListCell<ObiectEchipament>() {
                @Override
                protected void updateItem(ObiectEchipament item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        String rarityColor = switch (item.getRaritate()) {
                            case COMMON -> "#9d9d9d";
                            case UNCOMMON -> "#1eff00";
                            case RARE -> "#0070dd";
                            case EPIC -> "#a335ee";
                            case LEGENDARY -> "#ff8000";
                        };

                        String displayText = item.getNume();
                        if (item.getEnhancementLevel() > 0) {
                            displayText += " +" + item.getEnhancementLevel();
                        }
                        if (item.isEquipped()) {
                            displayText += " [EQUIPPED]";
                            setStyle("-fx-text-fill: " + rarityColor + "; -fx-background-color: #2c3e50; -fx-font-size: 13px;");
                        } else {
                            setStyle("-fx-text-fill: " + rarityColor + "; -fx-background-color: transparent; -fx-font-size: 13px;");
                        }

                        setText(displayText);
                    }
                }
            };

            // DRAG SOURCE - Start dragging item to pocket
            cell.setOnDragDetected(event -> {
                ObiectEchipament item = cell.getItem();
                if (item != null) {
                    Dragboard db = cell.startDragAndDrop(TransferMode.COPY);
                    ClipboardContent content = new ClipboardContent();
                    content.putString("pocket_item:" + System.identityHashCode(item));
                    db.setContent(content);
                    event.consume();
                }
            });

            // CLICK - Show item details in panel
            cell.setOnMouseClicked(event -> {
                ObiectEchipament item = cell.getItem();
                if (item != null && event.getClickCount() == 1) {
                    selectedInventoryItem = item;
                    showInventoryItemDetails(item);
                }
            });

            // HOVER - Show persistent tooltip
            cell.setOnMouseEntered(event -> {
                ObiectEchipament item = cell.getItem();
                if (item != null) {
                    showTooltipForItem(item, cell);
                }
            });

            cell.setOnMouseExited(event -> {
                hideTooltip();
            });

            return cell;
        });
    }

    private void showInventoryItemDetails(ObiectEchipament item) {
        if (item == null) return;

        // Find currently equipped item in same slot for comparison
        final ObiectEchipament currentItem = findEquippedItemForComparison(item);

        // Build detailed display
        StringBuilder details = new StringBuilder();

        String rarityColor = switch (item.getRaritate()) {
            case COMMON -> "COMMON";
            case UNCOMMON -> "UNCOMMON";
            case RARE -> "RARE";
            case EPIC -> "EPIC";
            case LEGENDARY -> "LEGENDARY";
        };

        details.append("═══════════════════════════════════\n");
        details.append(item.getNume());
        if (item.getEnhancementLevel() > 0) {
            details.append(" +").append(item.getEnhancementLevel());
        }
        details.append("\n");
        details.append(rarityColor).append(" | ").append(item.getTip()).append("\n");
        details.append("Level Requirement: ").append(item.getNivelNecesar()).append("\n");
        details.append("═══════════════════════════════════\n\n");

        if (item.isEquipped()) {
            details.append("🟢 CURRENTLY EQUIPPED\n\n");
        }

        // Item bonuses
        if (item.getBonuses() != null && !item.getBonuses().isEmpty()) {
            details.append("BONUSES:\n");
            item.getBonuses().forEach((stat, value) -> {
                details.append("  +").append(value).append(" ").append(formatStatName(stat));

                // Show comparison if available
                if (currentItem != null && currentItem.getBonuses().containsKey(stat)) {
                    int diff = value - currentItem.getBonuses().get(stat);
                    if (diff > 0) {
                        details.append(" (▲+").append(diff).append(")");
                    } else if (diff < 0) {
                        details.append(" (▼").append(diff).append(")");
                    }
                }
                details.append("\n");
            });
        }

        // Enhancement bonuses
        if (item.getEnhancementLevel() > 0 && item.getEnhancementBonuses() != null && !item.getEnhancementBonuses().isEmpty()) {
            details.append("\nENHANCEMENT BONUSES:\n");
            item.getEnhancementBonuses().forEach((stat, value) ->
                details.append("  +").append(value).append(" ").append(formatStatName(stat)).append("\n")
            );
        }

        // Show comparison item
        if (currentItem != null && !item.isEquipped()) {
            details.append("\n═══ CURRENTLY EQUIPPED ═══\n");
            details.append(currentItem.getNume());
            if (currentItem.getEnhancementLevel() > 0) {
                details.append(" +").append(currentItem.getEnhancementLevel());
            }
            details.append("\n\n");

            if (currentItem.getBonuses() != null && !currentItem.getBonuses().isEmpty()) {
                details.append("BONUSES:\n");
                currentItem.getBonuses().forEach((stat, value) ->
                    details.append("  +").append(value).append(" ").append(formatStatName(stat)).append("\n")
                );
            }
        }

        inventoryItemDetailsArea.setText(details.toString());
    }

    private VBox createTalentTreePanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #0f0f1e;");

        Label titleLabel = new Label("🌳 TALENT TREE");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #f1c40f;");

        Button openTalentTreeButton = new Button("🌳 Open Talent Tree");
        openTalentTreeButton.setStyle(
                "-fx-background-color: #27ae60; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 10 20; " +
                "-fx-background-radius: 5;"
        );
        openTalentTreeButton.setOnAction(e -> {
            TalentTreeController talentController = new TalentTreeController(stage, hero, () -> {
                // Return to character menu when done
                stage.setScene(createScene());
            });
            stage.setScene(talentController.createScene());
        });

        // Talent summary
        TextArea talentSummary = new TextArea();
        talentSummary.setEditable(false);
        talentSummary.setWrapText(true);
        talentSummary.setStyle("-fx-control-inner-background: #16213e; -fx-text-fill: white; -fx-font-size: 14px;");
        VBox.setVgrow(talentSummary, Priority.ALWAYS);

        StringBuilder talents = new StringBuilder();
        talents.append("🌟 TALENT POINTS:\n\n");
        talents.append(String.format("Available Points: %d\n\n", hero.getPassivePoints()));
        talents.append("💡 Use the Talent Tree to unlock powerful passive bonuses!\n\n");
        talents.append("Benefits include:\n");
        talents.append("  • Increased HP and damage\n");
        talents.append("  • Critical hit improvements\n");
        talents.append("  • Resource regeneration\n");
        talents.append("  • Special combat bonuses\n");

        talentSummary.setText(talents.toString());

        panel.getChildren().addAll(titleLabel, openTalentTreeButton, talentSummary);
        return panel;
    }

    private VBox createStatisticsPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #0f0f1e;");

        Label titleLabel = new Label("📊 DETAILED STATISTICS");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #f1c40f;");

        TextArea statsArea = new TextArea();
        statsArea.setEditable(false);
        statsArea.setWrapText(true);
        statsArea.setStyle("-fx-control-inner-background: #16213e; -fx-text-fill: white; -fx-font-size: 13px;");
        VBox.setVgrow(statsArea, Priority.ALWAYS);

        StringBuilder fullStats = new StringBuilder();
        fullStats.append("═══════════════════════════════════════════\n");
        fullStats.append("           📊 COMPLETE STATISTICS\n");
        fullStats.append("═══════════════════════════════════════════\n\n");

        fullStats.append("👤 CHARACTER INFO:\n");
        fullStats.append(String.format("Name: %s\n", hero.getNume()));
        fullStats.append(String.format("Level: %d\n", hero.getNivel()));
        fullStats.append(String.format("XP: %d / %d\n", hero.getXp(), hero.getXpNecesarPentruUrmatoarelNivel()));

        fullStats.append("\n⚔️ CORE ATTRIBUTES:\n");
        fullStats.append(String.format("💪 Strength: %d (Total: %d)\n",
                hero.getStrength(), hero.getStrengthTotal()));
        fullStats.append(String.format("🏃 Dexterity: %d (Total: %d)\n",
                hero.getDexterity(), hero.getDexterityTotal()));
        fullStats.append(String.format("🧠 Intelligence: %d (Total: %d)\n",
                hero.getIntelligence(), hero.getIntelligenceTotal()));
        fullStats.append(String.format("🛡️ Defense: %d (Total: %d)\n",
                hero.getDefense(), hero.getDefenseTotal()));

        fullStats.append("\n💚 VITALS:\n");
        fullStats.append(String.format("❤️ HP: %d / %d\n", hero.getViata(), hero.getViataMaxima()));
        fullStats.append(String.format("💙 %s: %d / %d\n",
                hero.getTipResursa(), hero.getResursaCurenta(), hero.getResursaMaxima()));

        fullStats.append("\n⚔️ COMBAT STATS:\n");
        fullStats.append(String.format("⚔️ Damage: %d\n", hero.getStrengthTotal() * 2));
        fullStats.append(String.format("🎯 Hit Chance: %.1f%%\n", hero.getHitChance()));
        fullStats.append(String.format("💥 Critical Chance: %.1f%%\n", hero.getCritChanceTotal()));
        fullStats.append(String.format("💨 Dodge Chance: %.1f%%\n", hero.getDodgeChanceTotal()));

        fullStats.append("\n💰 RESOURCES:\n");
        fullStats.append(String.format("💰 Gold: %d\n", hero.getGold()));
        fullStats.append(String.format("🔧 Scrap: %d\n", hero.getScrap()));
        fullStats.append(String.format("🎫 Dungeon Tokens: %d\n", hero.getDungeonTickets()));

        if (hero.getStatPointsToAllocate() > 0) {
            fullStats.append(String.format("\n⭐ Stat Points Available: %d\n", hero.getStatPointsToAllocate()));
        }
        if (hero.getPassivePoints() > 0) {
            fullStats.append(String.format("🌟 Talent Points Available: %d\n", hero.getPassivePoints()));
        }

        fullStats.append("\n🧪 CONSUMABLES:\n");
        fullStats.append(String.format("🧪 Health Potions: %d\n", hero.getHealthPotions()));
        fullStats.append(String.format("💙 Mana Potions: %d\n", hero.getManaPotions()));
        fullStats.append(String.format("🌯 Shaorma Revivals: %d\n", hero.getShaormaRevival()));

        fullStats.append("\n🏆 EQUIPMENT:\n");
        fullStats.append(String.format("Equipped Items: %d\n",
                hero.getInventar().stream().filter(item -> item.isEquipped()).count()));
        fullStats.append(String.format("Total Items: %d\n", hero.getInventar().size()));

        statsArea.setText(fullStats.toString());

        panel.getChildren().addAll(titleLabel, statsArea);
        return panel;
    }

    private HBox createFooter() {
        HBox footer = new HBox(20);
        footer.setPadding(new Insets(15));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #16213e;");

        Button backButton = new Button("🔙 Back to Town");
        backButton.setStyle(
                "-fx-background-color: #e74c3c; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 16px; " +
                "-fx-padding: 10 30; " +
                "-fx-background-radius: 8;"
        );
        backButton.setOnAction(e -> {
            if (onBackCallback != null) {
                onBackCallback.run();
            }
        });

        footer.getChildren().add(backButton);
        return footer;
    }

    // ==================== FILTER AND POCKET MANAGEMENT ====================

    private HBox createSimpleFilterBar() {
        HBox filterBar = new HBox(15);
        filterBar.setPadding(new Insets(10));
        filterBar.setStyle("-fx-background-color: #16213e; -fx-background-radius: 5;");
        filterBar.setAlignment(Pos.CENTER_LEFT);

        // Filter Label
        Label filterLabel = new Label("Filter:");
        filterLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Item Type Filter ComboBox (no pockets, just types)
        inventoryFilterComboBox = new ComboBox<>();
        inventoryFilterComboBox.setPrefWidth(200);
        inventoryFilterComboBox.setStyle("-fx-font-size: 14px;");
        inventoryFilterComboBox.getItems().addAll("All Items", "Weapons", "Armor", "Accessories");
        inventoryFilterComboBox.setValue("All Items");
        inventoryFilterComboBox.setOnAction(e -> refreshInventoryTabList());

        filterBar.getChildren().addAll(filterLabel, inventoryFilterComboBox);
        return filterBar;
    }

    private VBox createPocketEditorSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");
        section.setPrefWidth(400);
        VBox.setVgrow(section, Priority.ALWAYS);

        Label sectionTitle = new Label("📁 POCKET EDITOR");
        sectionTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        // Pocket selector and management buttons
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label selectLabel = new Label("Select:");
        selectLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        pocketEditorComboBox = new ComboBox<>();
        pocketEditorComboBox.setPromptText("Choose a pocket...");
        pocketEditorComboBox.setPrefWidth(180);
        pocketEditorComboBox.setStyle("-fx-font-size: 14px;");
        updatePocketEditorComboBox();
        pocketEditorComboBox.setOnAction(e -> loadPocketForEditing());

        Button newPocketBtn = new Button("➕");
        styleFilterButton(newPocketBtn, "#27ae60");
        newPocketBtn.setTooltip(new Tooltip("Create New Pocket"));
        newPocketBtn.setOnAction(e -> createNewEmptyPocket());

        Button deletePocketBtn = new Button("🗑️");
        styleFilterButton(deletePocketBtn, "#e74c3c");
        deletePocketBtn.setTooltip(new Tooltip("Delete Pocket"));
        deletePocketBtn.setOnAction(e -> deleteCurrentPocket());

        topBar.getChildren().addAll(selectLabel, pocketEditorComboBox, newPocketBtn, deletePocketBtn);

        // Pocket name editor
        HBox nameBar = new HBox(10);
        nameBar.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("Name:");
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        nameLabel.setPrefWidth(50);

        pocketNameField = new TextField();
        pocketNameField.setPromptText("Pocket name");
        pocketNameField.setStyle("-fx-font-size: 14px;");
        pocketNameField.setDisable(true);
        HBox.setHgrow(pocketNameField, Priority.ALWAYS);

        nameBar.getChildren().addAll(nameLabel, pocketNameField);

        // Color selector
        HBox colorBar = new HBox(10);
        colorBar.setAlignment(Pos.CENTER_LEFT);

        Label colorLabel = new Label("Color:");
        colorLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        colorLabel.setPrefWidth(50);

        pocketColorComboBox = new ComboBox<>();
        pocketColorComboBox.getItems().addAll(
                "Blue (#3498db)",
                "Green (#27ae60)",
                "Red (#e74c3c)",
                "Purple (#9b59b6)",
                "Orange (#e67e22)",
                "Yellow (#f1c40f)"
        );
        pocketColorComboBox.setValue("Blue (#3498db)");
        pocketColorComboBox.setStyle("-fx-font-size: 14px;");
        pocketColorComboBox.setDisable(true);
        HBox.setHgrow(pocketColorComboBox, Priority.ALWAYS);

        colorBar.getChildren().addAll(colorLabel, pocketColorComboBox);

        // Items in pocket list
        Label itemsLabel = new Label("Items in Pocket (Drag from left):");
        itemsLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px;");

        pocketEditorList = new ListView<>();
        pocketEditorList.setStyle("-fx-background-color: #0f0f1e; -fx-control-inner-background: #0f0f1e;");
        pocketEditorList.setPlaceholder(new Label("No pocket selected or pocket is empty"));
        VBox.setVgrow(pocketEditorList, Priority.ALWAYS);

        setupPocketEditorListDragAndDrop();

        // Save button
        Button saveBtn = new Button("💾 Save Pocket");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        styleFilterButton(saveBtn, "#3498db");
        saveBtn.setOnAction(e -> savePocketChanges());

        section.getChildren().addAll(
                sectionTitle,
                new Separator(),
                topBar,
                nameBar,
                colorBar,
                new Separator(),
                itemsLabel,
                pocketEditorList,
                saveBtn
        );

        return section;
    }

    private void styleFilterButton(Button button, String color) {
        button.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 8 15; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;"
        );
        button.setOnMouseEntered(e -> button.setOpacity(0.8));
        button.setOnMouseExited(e -> button.setOpacity(1.0));
    }

    private String getColorNameFromHex(String hex) {
        return switch (hex) {
            case "#3498db" -> "Blue (#3498db)";
            case "#27ae60" -> "Green (#27ae60)";
            case "#e74c3c" -> "Red (#e74c3c)";
            case "#9b59b6" -> "Purple (#9b59b6)";
            case "#e67e22" -> "Orange (#e67e22)";
            case "#f1c40f" -> "Yellow (#f1c40f)";
            default -> "Blue (#3498db)";
        };
    }

    private boolean matchesFilter(ObiectEchipament item, String filter) {
        ObiectEchipament.TipEchipament itemType = item.getTip();

        return switch (filter) {
            case "Weapons" -> itemType == ObiectEchipament.TipEchipament.WEAPON_ONE_HANDED ||
                    itemType == ObiectEchipament.TipEchipament.WEAPON_TWO_HANDED ||
                    itemType == ObiectEchipament.TipEchipament.OFF_HAND_WEAPON;
            case "Armor" -> itemType == ObiectEchipament.TipEchipament.HELMET ||
                    itemType == ObiectEchipament.TipEchipament.ARMOR ||
                    itemType == ObiectEchipament.TipEchipament.BOOTS ||
                    itemType == ObiectEchipament.TipEchipament.GLOVES;
            case "Accessories" -> itemType == ObiectEchipament.TipEchipament.RING ||
                    itemType == ObiectEchipament.TipEchipament.NECKLACE ||
                    itemType == ObiectEchipament.TipEchipament.SHIELD ||
                    itemType == ObiectEchipament.TipEchipament.OFF_HAND_MAGIC;
            default -> true;
        };
    }

    private HBox createCharacterSheetFilterBar() {
        HBox filterBar = new HBox(10);
        filterBar.setPadding(new Insets(5));
        filterBar.setStyle("-fx-background-color: #0f0f1e; -fx-background-radius: 5;");
        filterBar.setAlignment(Pos.CENTER_LEFT);

        // Filter ComboBox (more compact than inventory tab)
        characterSheetFilterComboBox = new ComboBox<>();
        characterSheetFilterComboBox.setPrefWidth(150);
        characterSheetFilterComboBox.setStyle("-fx-font-size: 12px;");
        updateCharacterSheetFilterComboBox();
        characterSheetFilterComboBox.setValue("All Items");
        characterSheetFilterComboBox.setOnAction(e -> applyCharacterSheetFilter());

        filterBar.getChildren().add(characterSheetFilterComboBox);
        return filterBar;
    }

    private void updateCharacterSheetFilterComboBox() {
        String currentSelection = characterSheetFilterComboBox.getValue();
        characterSheetFilterComboBox.getItems().clear();

        // Add item type filters
        characterSheetFilterComboBox.getItems().addAll(
                "All Items",
                "Weapons",
                "Armor",
                "Accessories"
        );

        // Add separator if there are pockets
        if (!hero.getItemPockets().isEmpty()) {
            characterSheetFilterComboBox.getItems().add("─────────");

            // Add existing pockets to filter
            for (com.rpg.model.inventory.ItemPocket pocket : hero.getItemPockets()) {
                characterSheetFilterComboBox.getItems().add("📁 " + pocket.getName());
            }
        }

        // Restore selection or default to "All Items"
        if (currentSelection != null && characterSheetFilterComboBox.getItems().contains(currentSelection)) {
            characterSheetFilterComboBox.setValue(currentSelection);
        } else {
            characterSheetFilterComboBox.setValue("All Items");
        }
    }

    private void applyCharacterSheetFilter() {
        String selected = characterSheetFilterComboBox.getValue();
        if (selected == null || selected.equals("─────────")) {
            return;
        }

        if (selected.startsWith("📁 ")) {
            // Pocket filter selected
            String pocketName = selected.substring(2).trim(); // Remove "📁 " prefix and trim
            currentCharacterSheetPocket = hero.getItemPockets().stream()
                    .filter(p -> p.getName().equals(pocketName))
                    .findFirst()
                    .orElse(null);

            System.out.println("DEBUG: Selected pocket: " + pocketName);
            System.out.println("DEBUG: Found pocket: " + (currentCharacterSheetPocket != null ? currentCharacterSheetPocket.getName() : "null"));
            System.out.println("DEBUG: Pocket has items: " + (currentCharacterSheetPocket != null ? currentCharacterSheetPocket.size() : 0));
        } else {
            // Item type filter selected
            currentCharacterSheetPocket = null;
        }

        refreshInventoryDisplay();
    }

    // ==================== POCKET EDITOR METHODS ====================

    private void updatePocketEditorComboBox() {
        if (pocketEditorComboBox == null) return;

        com.rpg.model.inventory.ItemPocket currentSelection = pocketEditorComboBox.getValue();
        pocketEditorComboBox.getItems().clear();
        pocketEditorComboBox.getItems().addAll(hero.getItemPockets());

        // Restore selection if still valid
        if (currentSelection != null && hero.getItemPockets().contains(currentSelection)) {
            pocketEditorComboBox.setValue(currentSelection);
        }
    }

    private void loadPocketForEditing() {
        editingPocket = pocketEditorComboBox.getValue();

        if (editingPocket == null) {
            pocketNameField.setDisable(true);
            pocketNameField.clear();
            pocketColorComboBox.setDisable(true);
            pocketColorComboBox.setValue("Blue (#3498db)");
            pocketEditorList.getItems().clear();
            return;
        }

        // Enable editing
        pocketNameField.setDisable(false);
        pocketNameField.setText(editingPocket.getName());
        pocketColorComboBox.setDisable(false);
        pocketColorComboBox.setValue(getColorNameFromHex(editingPocket.getColor()));

        // Load items
        pocketEditorList.getItems().clear();
        pocketEditorList.getItems().addAll(editingPocket.getItems());
    }

    private void setupPocketEditorListDragAndDrop() {
        pocketEditorList.setCellFactory(lv -> {
            ListCell<ObiectEchipament> cell = new ListCell<>() {
                @Override
                protected void updateItem(ObiectEchipament item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        String rarityColor = switch (item.getRaritate()) {
                            case COMMON -> "#9d9d9d";
                            case UNCOMMON -> "#1eff00";
                            case RARE -> "#0070dd";
                            case EPIC -> "#a335ee";
                            case LEGENDARY -> "#ff8000";
                        };
                        setText(item.getNume() + (item.getEnhancementLevel() > 0 ? " +" + item.getEnhancementLevel() : ""));
                        setStyle("-fx-text-fill: " + rarityColor + "; -fx-font-size: 12px;");
                    }
                }
            };

            // Allow removing items by right-click
            cell.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && cell.getItem() != null) {
                    pocketEditorList.getItems().remove(cell.getItem());
                }
            });

            return cell;
        });

        // DRAG TARGET - Accept items dropped into the pocket
        pocketEditorList.setOnDragOver(event -> {
            if (event.getDragboard().hasString() &&
                event.getDragboard().getString().startsWith("pocket_item:") &&
                editingPocket != null) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        pocketEditorList.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString() && db.getString().startsWith("pocket_item:") && editingPocket != null) {
                // Find the dragged item in the inventory list
                String idStr = db.getString().substring("pocket_item:".length());
                int itemId = Integer.parseInt(idStr);

                for (ObiectEchipament item : hero.getInventar().getItems()) {
                    if (System.identityHashCode(item) == itemId) {
                        // Add to pocket editor list if not already there
                        if (!pocketEditorList.getItems().contains(item)) {
                            pocketEditorList.getItems().add(item);
                            success = true;
                        }
                        break;
                    }
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void createNewEmptyPocket() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Pocket");
        dialog.setHeaderText("Enter a name for the new pocket:");
        dialog.setContentText("Pocket name:");

        // Style the dialog
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1a1a2e;");
        dialogPane.lookupAll(".label").forEach(node -> {
            if (node instanceof Label) {
                ((Label) node).setStyle("-fx-text-fill: white;");
            }
        });

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                // Check for duplicate names
                boolean exists = hero.getItemPockets().stream()
                        .anyMatch(p -> p.getName().equalsIgnoreCase(name.trim()));

                if (exists) {
                    DialogHelper.showWarning("Duplicate Name",
                            "A pocket with this name already exists!");
                    return;
                }

                // Create empty pocket
                com.rpg.model.inventory.ItemPocket newPocket = new com.rpg.model.inventory.ItemPocket(name.trim());
                hero.addItemPocket(newPocket);

                // Update UI
                updatePocketEditorComboBox();
                if (characterSheetFilterComboBox != null) {
                    updateCharacterSheetFilterComboBox();
                }

                // Select the new pocket
                pocketEditorComboBox.setValue(newPocket);
                loadPocketForEditing();

                DialogHelper.showSuccess("Pocket Created",
                        "Empty pocket '" + name.trim() + "' has been created.\nDrag items from the left to add them.");
            }
        });
    }

    private void deleteCurrentPocket() {
        com.rpg.model.inventory.ItemPocket pocket = pocketEditorComboBox.getValue();
        if (pocket == null) {
            DialogHelper.showWarning("No Pocket Selected",
                    "Please select a pocket to delete.");
            return;
        }

        // Confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Pocket");
        alert.setHeaderText("Delete pocket: " + pocket.getName());
        alert.setContentText("Are you sure you want to delete this pocket?\nItems in the pocket will not be deleted, only the organization.");

        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1a1a2e;");
        dialogPane.lookupAll(".label").forEach(node -> {
            if (node instanceof Label) {
                ((Label) node).setStyle("-fx-text-fill: white;");
            }
        });

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                hero.removeItemPocket(pocket);

                // Update UI
                updatePocketEditorComboBox();
                if (characterSheetFilterComboBox != null) {
                    updateCharacterSheetFilterComboBox();
                }

                // Clear editor
                editingPocket = null;
                pocketEditorComboBox.setValue(null);
                loadPocketForEditing();

                DialogHelper.showSuccess("Pocket Deleted",
                        "Pocket '" + pocket.getName() + "' has been deleted.");
            }
        });
    }

    private void savePocketChanges() {
        if (editingPocket == null) {
            DialogHelper.showWarning("No Pocket Selected",
                    "Please select a pocket to save changes.");
            return;
        }

        String newName = pocketNameField.getText().trim();
        if (newName.isEmpty()) {
            DialogHelper.showWarning("Invalid Name",
                    "Pocket name cannot be empty!");
            return;
        }

        // Check for duplicate names (excluding current pocket)
        boolean exists = hero.getItemPockets().stream()
                .filter(p -> !p.equals(editingPocket))
                .anyMatch(p -> p.getName().equalsIgnoreCase(newName));

        if (exists) {
            DialogHelper.showWarning("Duplicate Name",
                    "A pocket with this name already exists!");
            return;
        }

        // Save changes
        editingPocket.setName(newName);

        String colorSelection = pocketColorComboBox.getValue();
        String colorHex = colorSelection.substring(colorSelection.indexOf("#"), colorSelection.length() - 1);
        editingPocket.setColor(colorHex);

        // Update items in pocket
        editingPocket.getItems().clear();
        editingPocket.getItems().addAll(pocketEditorList.getItems());

        // Update UI
        updatePocketEditorComboBox();
        if (characterSheetFilterComboBox != null) {
            updateCharacterSheetFilterComboBox();
        }
        pocketEditorComboBox.setValue(editingPocket);

        DialogHelper.showSuccess("Pocket Saved",
                "Pocket '" + newName + "' has been saved with " + editingPocket.size() + " items.");
    }
}
