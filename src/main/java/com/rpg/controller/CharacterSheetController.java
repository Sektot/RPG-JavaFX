package com.rpg.controller;

import com.rpg.model.characters.Erou;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.service.InventoryServiceFX;
import com.rpg.service.dto.EquipResult;
import com.rpg.service.dto.InventoryItemDTO;
import com.rpg.utils.DialogHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * 🎮 CHARACTER SHEET cu Drag & Drop și Stats în timp real
 */
public class CharacterSheetController {

    private Stage stage;
    private Erou hero;
    private InventoryServiceFX inventoryService;

    // UI Components
    private ListView<InventoryItemDTO> inventoryListView;
    private VBox realTimeStatsPanel;

    // Equipment slots (drag targets)
    private Map<String, EquipmentSlot> equipmentSlots = new HashMap<>();

    public CharacterSheetController(Stage stage, Erou hero) {
        this.stage = stage;
        this.hero = hero;
        this.inventoryService = new InventoryServiceFX();
        initializeEquipmentSlots();
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setTop(createHeader());
        root.setCenter(createMainContent());
        root.setBottom(createFooter());
        root.setStyle("-fx-background-color: #1a1a2e;");

        return new Scene(root, 1200, 800);
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #16213e;");

        Label title = new Label("🎮 CHARACTER SHEET - " + hero.getNume().toUpperCase());
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        Label levelInfo = new Label("⭐ Level " + hero.getNivel() + " | 💰 " + hero.getGold() + " gold");
        levelInfo.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1f1f1;");

        header.getChildren().addAll(title, levelInfo);
        return header;
    }

    private HBox createMainContent() {
        HBox content = new HBox(15);
        content.setPadding(new Insets(20));

        VBox leftPanel = createEquipmentPanel();
        VBox middlePanel = createInventoryPanel();
        VBox rightPanel = createRealTimeStatsPanel();

        content.getChildren().addAll(leftPanel, middlePanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.NEVER);
        HBox.setHgrow(middlePanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.NEVER);

        return content;
    }

    private VBox createEquipmentPanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(320);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");

        Label title = new Label("⚔️ ECHIPAMENT");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        GridPane slotsGrid = new GridPane();
        slotsGrid.setHgap(8);
        slotsGrid.setVgap(8);
        slotsGrid.setAlignment(Pos.CENTER);

        createAndAddSlot(slotsGrid, "HELMET", "⛑️ Cască", 1, 0);
        createAndAddSlot(slotsGrid, "RING1", "💍 Ring 1", 0, 1);
        createAndAddSlot(slotsGrid, "ARMOR", "🛡️ Armură", 1, 1);
        createAndAddSlot(slotsGrid, "RING2", "💍 Ring 2", 2, 1);
        createAndAddSlot(slotsGrid, "MAIN_HAND", "⚔️ Main Hand", 0, 2);
        createAndAddSlot(slotsGrid, "GLOVES", "🧤 Mănuși", 1, 2);
        createAndAddSlot(slotsGrid, "OFF_HAND", "🛡️ Off Hand", 2, 2);
        createAndAddSlot(slotsGrid, "NECKLACE", "📿 Colier", 0, 3);
        createAndAddSlot(slotsGrid, "BOOTS", "🥾 Bocanci", 1, 3);

        panel.getChildren().addAll(title, slotsGrid);
        return panel;
    }

    private VBox createInventoryPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");

        Label title = new Label("🎒 INVENTAR - Drag items to equip");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        inventoryListView = new ListView<>();
        inventoryListView.setStyle("-fx-font-size: 14px; " +
                "-fx-background-color: #2c3e50; " +  // ✅ Fundal gri închis
                "-fx-control-inner-background: #2c3e50;"); // ✅ Fundal intern

        VBox.setVgrow(inventoryListView, Priority.ALWAYS);

        // ✅ DRAG & DROP SETUP
        inventoryListView.setCellFactory(lv -> {
            ListCell<InventoryItemDTO> cell = new ListCell<InventoryItemDTO>() {
                @Override
                protected void updateItem(InventoryItemDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        setTooltip(null);
                        setStyle("");
                    } else {
                        setText(null); // Clear text, we'll use graphic instead

                        String textColor = "#FFFFFF"; // Default white

                        if (item.getEquipment() != null) {
                            ObiectEchipament equipment = item.getEquipment();

                            // Check level requirement
                            boolean canEquip = hero.getNivel() >= equipment.getNivelNecesar();

                            // Set color: red if level too low, otherwise rarity color
                            if (!canEquip) {
                                textColor = "#FF4444"; // Red for insufficient level
                            } else {
                                textColor = getRarityColor(equipment.getRaritate());
                            }

                            // Create VBox for item display
                            VBox itemBox = new VBox(2);

                            // First line: Icon + Name
                            Label nameLabel = new Label(equipment.getTip().getIcon() + " " + item.getName());
                            nameLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 13px; -fx-font-weight: bold;");

                            // Second line: Type | Rarity (or level requirement if too low)
                            String secondLine;
                            if (!canEquip) {
                                secondLine = "   ⚠ Requires Level " + equipment.getNivelNecesar() +
                                           " | " + equipment.getTip().getDisplayName();
                            } else {
                                secondLine = "   " + equipment.getTip().getDisplayName() +
                                           " | " + equipment.getRaritate().getDisplayName();
                            }

                            Label infoLabel = new Label(secondLine);
                            infoLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 11px;");

                            itemBox.getChildren().addAll(nameLabel, infoLabel);
                            setGraphic(itemBox);

                            // Create tooltip
                            Tooltip tooltip = createItemTooltip(equipment);
                            setTooltip(tooltip);
                        } else {
                            // Non-equipment item (potions, etc.)
                            Label simpleLabel = new Label(item.getName());
                            simpleLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 13px;");
                            setGraphic(simpleLabel);
                        }

                        // ✅ Apply background style
                        setStyle("-fx-background-color: transparent;");
                    }
                }
            };

            // DRAG SOURCE
            cell.setOnDragDetected(event -> {
                if (cell.getItem() != null && cell.getItem().getEquipment() != null) {
                    highlightCompatibleSlots(cell.getItem().getEquipment());

                    Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString("equipment:" + System.identityHashCode(cell.getItem().getEquipment()));
                    db.setContent(content);
                    event.consume();
                }
            });

            cell.setOnDragDone(event -> resetSlotHighlighting());

            // ✅ HOVER EFFECTS cu culori corecte
            cell.setOnMouseEntered(e -> {
                if (cell.getItem() != null) {
                    // Fundal gri închis on hover
                    cell.setStyle("-fx-background-color: #34495e;");
                }
            });

            cell.setOnMouseExited(e -> {
                if (cell.getItem() != null) {
                    // Înapoi la normal - fundal transparent
                    cell.setStyle("-fx-background-color: transparent;");
                }
            });

            return cell;
        });



        loadInventoryItems();
        panel.getChildren().addAll(title, inventoryListView);
        return panel;
    }

    private VBox createRealTimeStatsPanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(320); // ✅ Era 280, acum mai lat
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");

        Label title = new Label("📊 STATS LIVE");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        realTimeStatsPanel = new VBox(3); // ✅ Spacing mai mic pentru mai multe stats
        realTimeStatsPanel.setStyle("-fx-background-color: #0f1419; -fx-background-radius: 8; -fx-padding: 12;");
        updateRealTimeStats();

        ScrollPane scrollPane = new ScrollPane(realTimeStatsPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        panel.getChildren().addAll(title, scrollPane);
        return panel;
    }


    private HBox createFooter() {
        HBox footer = new HBox(15);
        footer.setPadding(new Insets(15));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #16213e;");

        Button refreshBtn = new Button("🔄 Refresh");
        styleButton(refreshBtn, "#3498db");
        refreshBtn.setOnAction(e -> {
            loadInventoryItems();
            updateAllSlots();
            updateRealTimeStats();
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

    // ==================== EQUIPMENT SLOTS ====================

    private void initializeEquipmentSlots() {
        equipmentSlots.put("MAIN_HAND", new EquipmentSlot("MAIN_HAND"));
        equipmentSlots.put("OFF_HAND", new EquipmentSlot("OFF_HAND"));
        equipmentSlots.put("ARMOR", new EquipmentSlot("ARMOR"));
        equipmentSlots.put("HELMET", new EquipmentSlot("HELMET"));
        equipmentSlots.put("GLOVES", new EquipmentSlot("GLOVES"));
        equipmentSlots.put("BOOTS", new EquipmentSlot("BOOTS"));
        equipmentSlots.put("RING1", new EquipmentSlot("RING1"));
        equipmentSlots.put("RING2", new EquipmentSlot("RING2"));
        equipmentSlots.put("NECKLACE", new EquipmentSlot("NECKLACE"));
    }

    private void createAndAddSlot(GridPane grid, String slotId, String displayName, int col, int row) {
        EquipmentSlot slot = equipmentSlots.get(slotId);

        VBox slotBox = new VBox(5);
        slotBox.setAlignment(Pos.CENTER);
        slotBox.setPrefSize(80, 100);
        slotBox.setStyle("-fx-border-color: #34495e; -fx-border-width: 2; -fx-background-color: #2c3e50; -fx-background-radius: 8;");

        Label slotLabel = new Label(displayName);
        slotLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #bdc3c7; -fx-text-alignment: center;");
        slotLabel.setWrapText(true);

        Label itemLabel = new Label("Empty");
        itemLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-text-alignment: center;");
        itemLabel.setWrapText(true);
        itemLabel.setPrefHeight(60);

        // ✅ TOOLTIP pentru slot-uri echipate
        slotBox.setOnMouseEntered(event -> {
            ObiectEchipament equippedItem = getCurrentItemForSlot(slotId);
            if (equippedItem != null) {
                Tooltip slotTooltip = createEquippedItemTooltip(equippedItem);
                Tooltip.install(slotBox, slotTooltip);
            }
        });

        slotBox.setOnMouseExited(event -> {
            Tooltip.uninstall(slotBox, null); // ✅ Șterge tooltip când pleci
        });


        slotBox.getChildren().addAll(slotLabel, itemLabel);

        // ✅ DRAG TARGET
        slotBox.setOnDragOver(event -> {
            if (event.getDragboard().hasString() && event.getDragboard().getString().startsWith("equipment:")) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        slotBox.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString() && db.getString().startsWith("equipment:")) {
                String itemHash = db.getString().substring("equipment:".length());
                ObiectEchipament item = findItemByHash(itemHash);

                if (item != null && canEquipInSlot(item, slotId)) {
                    equipItemInSlot(item, slotId);
                    success = true;
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });

        // ✅ DOUBLE CLICK pentru deechipare
        slotBox.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                unequipFromSlot(slotId);
            }
        });

        slot.setSlotBox(slotBox);
        slot.setItemLabel(itemLabel);

        grid.add(slotBox, col, row);
        updateSlotDisplay(slotId);
    }

    // ==================== HELPERS ====================

    private ObiectEchipament findItemByHash(String hash) {
        for (ObiectEchipament item : hero.getInventar().getItems()) {
            if (String.valueOf(System.identityHashCode(item)).equals(hash)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 📋 Tooltip pentru iteme echipate în slot-uri
     */
    private Tooltip createEquippedItemTooltip(ObiectEchipament item) {
        StringBuilder tooltipText = new StringBuilder();

        tooltipText.append("⚔️ ECHIPAT: ").append(item.getNume()).append("\n");
        tooltipText.append("═══════════════════════════\n");
        tooltipText.append("🎯 Tip: ").append(item.getTip().getDisplayName()).append("\n");
        tooltipText.append("⭐ Raritate: ").append(item.getRaritate().getDisplayName()).append("\n");
        tooltipText.append("📊 Nivel: ").append(item.getNivelNecesar()).append("\n");

        if (item.canEquipInMainHand() || item.canEquipInOffHand()) {
            tooltipText.append("🤲 Handedness: ").append(item.getHandedness().getDisplayName()).append("\n");
            if (!item.getWeaponClass().isEmpty()) {
                tooltipText.append("⚔️ Class: ").append(item.getWeaponClass()).append("\n");
            }
        }

        if (item.getEnhancementLevel() > 0) {
            tooltipText.append("⚡ Enhancement: +").append(item.getEnhancementLevel()).append("\n");
        }

        tooltipText.append("\n📊 BONUSURI ACTIVE:\n");
        Map<String, Integer> bonuses = item.getTotalBonuses();

        if (bonuses.isEmpty()) {
            tooltipText.append("  • Fără bonusuri");
        } else {
            bonuses.forEach((stat, bonus) -> {
                String icon = getStatIcon(stat);
                String name = formatStatName(stat);
                tooltipText.append("  ").append(icon).append(" +").append(bonus).append(" ").append(name).append("\n");
            });
        }

        tooltipText.append("\n💰 Valoare: ").append(item.getPret()).append(" gold");
        tooltipText.append("\n\n💡 Dublu-click pentru deechipare");

        Tooltip tooltip = new Tooltip(tooltipText.toString());
        tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: #2d5a3d; -fx-text-fill: white; -fx-max-width: 350px; -fx-wrap-text: true;");
        tooltip.setShowDelay(javafx.util.Duration.millis(300));
        tooltip.setHideDelay(javafx.util.Duration.millis(100));
        tooltip.setShowDuration(javafx.util.Duration.INDEFINITE); // ✅ Stă permanent

        return tooltip;
    }


    private java.util.Set<String> getCompatibleSlots(ObiectEchipament item) {
        java.util.Set<String> compatibleSlots = new java.util.HashSet<>();

        if (item == null) return compatibleSlots;

        switch (item.getTip()) {
            case WEAPON_ONE_HANDED -> {
                compatibleSlots.add("MAIN_HAND");
                if (item.isOffHandCompatible()) {
                    ObiectEchipament mainHand = hero.getMainHandWeapon();
                    if (mainHand == null || !mainHand.isTwoHanded()) {
                        compatibleSlots.add("OFF_HAND");
                    }
                }
            }
            case WEAPON_TWO_HANDED -> compatibleSlots.add("MAIN_HAND");
            case SHIELD -> {
                ObiectEchipament mainHand = hero.getMainHandWeapon();
                if (mainHand == null || !mainHand.isTwoHanded()) {
                    compatibleSlots.add("OFF_HAND");
                }
            }
            case OFF_HAND_WEAPON, OFF_HAND_MAGIC -> {
                ObiectEchipament mainHand = hero.getMainHandWeapon();
                if (mainHand == null || !mainHand.isTwoHanded()) {
                    compatibleSlots.add("OFF_HAND");
                }
            }
            case ARMOR -> compatibleSlots.add("ARMOR");
            case HELMET -> compatibleSlots.add("HELMET");
            case GLOVES -> compatibleSlots.add("GLOVES");
            case BOOTS -> compatibleSlots.add("BOOTS");
            case RING -> {
                compatibleSlots.add("RING1");
                compatibleSlots.add("RING2");
            }
            case NECKLACE -> compatibleSlots.add("NECKLACE");
        }

        return compatibleSlots;
    }

    /**
     * 🎨 Actualizează highlighting-ul slot-urilor pe baza item-ului dragged
     */
    private void highlightCompatibleSlots(ObiectEchipament item) {
        java.util.Set<String> compatibleSlots = getCompatibleSlots(item);

        equipmentSlots.forEach((slotId, slot) -> {
            VBox slotBox = slot.getSlotBox();
            ObiectEchipament currentItem = getCurrentItemForSlot(slotId);

            if (compatibleSlots.contains(slotId)) {
                // ✅ Highlight VERDE pentru slot-uri compatibile
                slotBox.setStyle("-fx-border-color: #27ae60; -fx-border-width: 4; " +
                        "-fx-background-color: #2d5a3d; -fx-background-radius: 8; " +
                        "-fx-effect: dropshadow(gaussian, #27ae60, 15, 0.8, 0, 0);");
            } else {
                // ✅ Păstrează stilul NORMAL pentru slot-urile incompatibile
                if (currentItem != null) {
                    // Slot ocupat - stil normal ocupat
                    slotBox.setStyle("-fx-border-color: #34495e; -fx-border-width: 2; " +
                            "-fx-background-color: #2d5a3d; -fx-background-radius: 8; " +
                            "-fx-effect: null;");
                } else {
                    // Slot gol - stil normal gol
                    slotBox.setStyle("-fx-border-color: #34495e; -fx-border-width: 2; " +
                            "-fx-background-color: #2c3e50; -fx-background-radius: 8; " +
                            "-fx-effect: null;");
                }
            }
        });
    }


    /**
     * 🎯 Resetează highlighting-ul slot-urilor - FIX pentru bare verzi
     */
    private void resetSlotHighlighting() {
        equipmentSlots.forEach((slotId, slot) -> {
            // ✅ FORȚEAZĂ resetarea completă a stilului
            VBox slotBox = slot.getSlotBox();
            ObiectEchipament currentItem = getCurrentItemForSlot(slotId);

            if (currentItem != null) {
                // Slot ocupat - verde normal
                slotBox.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2; " +
                        "-fx-background-color: #2d5a3d; -fx-background-radius: 8; " +
                        "-fx-effect: null;"); // ✅ Elimină efectele
            } else {
                // Slot gol - stil normal
                slotBox.setStyle("-fx-border-color: #34495e; -fx-border-width: 2; " +
                        "-fx-background-color: #2c3e50; -fx-background-radius: 8; " +
                        "-fx-effect: null;"); // ✅ Elimină efectele
            }
        });
    }


    private boolean canEquipInSlot(ObiectEchipament item, String slotId) {
        return getCompatibleSlots(item).contains(slotId);
    }

    private void equipItemInSlot(ObiectEchipament item, String slotId) {
        try {
            EquipResult result = hero.equipItem(item);

            if (result.isSuccess()) {
                updateAllSlots();
                updateRealTimeStats();
                loadInventoryItems();
                DialogHelper.showSuccess("Echipat!", result.getMessage());
            } else {
                DialogHelper.showError("Nu se poate echipa", result.getMessage());
            }
        } catch (Exception e) {
            DialogHelper.showError("Eroare", "Nu s-a putut echipa itemul: " + e.getMessage());
        }
    }

    private void unequipFromSlot(String slotId) {
        try {
            EquipResult result = hero.unequipFromSlot(slotId);

            if (result.isSuccess()) {
                updateAllSlots();
                updateRealTimeStats();
                loadInventoryItems();
                DialogHelper.showSuccess("Deechipat!", result.getMessage());
            } else {
                DialogHelper.showWarning("Nu se poate deechipa", result.getMessage());
            }
        } catch (Exception e) {
            DialogHelper.showError("Eroare", "Nu s-a putut deechipa itemul: " + e.getMessage());
        }
    }

    /**
     * Încarcă items din inventar - ASCUNDE itemele echipate
     */
    private void loadInventoryItems() {
        var items = inventoryService.getItemsByCategory(hero, InventoryServiceFX.InventoryCategory.TOATE)
                .stream()
                .filter(item -> item.getType() == InventoryItemDTO.ItemType.EQUIPMENT) // ✅ Doar NEECHIPATE
                // ✅ Nu mai includem EQUIPMENT_EQUIPPED - vor dispărea din inventar când sunt echipate
                .toList();

        inventoryListView.getItems().clear();
        inventoryListView.getItems().addAll(items);
    }


    private void updateAllSlots() {
        for (String slotId : equipmentSlots.keySet()) {
            updateSlotDisplay(slotId);
        }
    }

    /**
     * Actualizează afișarea unui slot - CONSISTENT STYLING
     */
    private void updateSlotDisplay(String slotId) {
        EquipmentSlot slot = equipmentSlots.get(slotId);
        ObiectEchipament currentItem = getCurrentItemForSlot(slotId);

        slot.setCurrentItem(currentItem);

        if (currentItem != null) {
            // ✅ SLOT OCUPAT
            String displayText = currentItem.getTip().getIcon() + "\n" +
                    currentItem.getNume().substring(0, Math.min(currentItem.getNume().length(), 12));
            if (currentItem.getNume().length() > 12) displayText += "...";

            slot.getItemLabel().setText(displayText);
            slot.getItemLabel().setStyle("-fx-font-size: 10px; -fx-text-fill: white; -fx-text-alignment: center;");

            // Stil consistent pentru slot-uri ocupate
            slot.getSlotBox().setStyle("-fx-border-color: #27ae60; -fx-border-width: 2; " +
                    "-fx-background-color: #2d5a3d; -fx-background-radius: 8; " +
                    "-fx-effect: null;");
        } else {
            // ✅ SLOT GOL
            slot.getItemLabel().setText("Empty");
            slot.getItemLabel().setStyle("-fx-font-size: 10px; -fx-text-fill: #7f8c8d; -fx-text-alignment: center;");

            // Stil consistent pentru slot-uri goale
            slot.getSlotBox().setStyle("-fx-border-color: #34495e; -fx-border-width: 2; " +
                    "-fx-background-color: #2c3e50; -fx-background-radius: 8; " +
                    "-fx-effect: null;");
        }
    }


    private ObiectEchipament getCurrentItemForSlot(String slotId) {
        return switch (slotId) {
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
    }

    // ✅ TOOLTIP creator cu toate stat-urile
    /**
     * 📋 Creează tooltip cu statistici și COMPARAȚIE pentru item
     */
    /**
     * 🎨 Get color for item rarity
     */
    private String getRarityColor(ObiectEchipament.Raritate rarity) {
        return switch (rarity) {
            case COMMON -> "#FFFFFF";      // White
            case UNCOMMON -> "#1EFF00";    // Green
            case RARE -> "#0070DD";        // Blue
            case EPIC -> "#A335EE";        // Purple
            case LEGENDARY -> "#FF8000";   // Orange
        };
    }

    /**
     * 📋 Creează tooltip cu statistici și COMPARAȚIE pentru item
     */
    private Tooltip createItemTooltip(ObiectEchipament item) {
        StringBuilder tooltipText = new StringBuilder();

        // HEADER
        tooltipText.append("📦 ").append(item.getNume()).append("\n");
        tooltipText.append("═══════════════════════════\n");
        tooltipText.append("🎯 Tip: ").append(item.getTip().getDisplayName()).append("\n");
        tooltipText.append("⭐ Raritate: ").append(item.getRaritate().getDisplayName()).append("\n");
        tooltipText.append("📊 Nivel: ").append(item.getNivelNecesar()).append("\n");

        if (item.canEquipInMainHand() || item.canEquipInOffHand()) {
            tooltipText.append("🤲 Handedness: ").append(item.getHandedness().getDisplayName()).append("\n");
            if (!item.getWeaponClass().isEmpty()) {
                tooltipText.append("⚔️ Class: ").append(item.getWeaponClass()).append("\n");
            }
        }

        if (item.getEnhancementLevel() > 0) {
            tooltipText.append("⚡ Enhancement: +").append(item.getEnhancementLevel()).append("\n");
        }

        tooltipText.append("\n📊 STATISTICI:\n");

        // ✅ GĂSEȘTE ITEM-UL ECHIPAT PENTRU COMPARAȚIE
        ObiectEchipament equippedItem = findEquippedItemForComparison(item);

        Map<String, Integer> newBonuses = item.getTotalBonuses();
        Map<String, Integer> equippedBonuses = equippedItem != null ?
                equippedItem.getTotalBonuses() :
                new HashMap<>();

        if (newBonuses.isEmpty()) {
            tooltipText.append("  • Fără bonusuri\n");
        } else {
            // Sortează stats pentru afișare consistentă
            String[] statOrder = {"Damage", "strength", "dexterity", "intelligence", "defense",
                    "health", "mana", "crit_chance", "hit_chance", "dodge_chance",
                    "damage_bonus", "attack_bonus", "lifesteal", "gold_find"};

            // ✅ AFIȘEAZĂ STATS ÎN ORDINE CU COMPARAȚIE
            for (String stat : statOrder) {
                if (newBonuses.containsKey(stat)) {
                    addStatToTooltip(tooltipText, stat, newBonuses.get(stat),
                            equippedBonuses.getOrDefault(stat, 0), equippedItem != null);
                }
            }

            // Stats rămase (cele care nu sunt în ordine)
            newBonuses.forEach((stat, newValue) -> {
                boolean inOrder = java.util.Arrays.asList(statOrder).contains(stat);
                if (!inOrder) {
                    addStatToTooltip(tooltipText, stat, newValue,
                            equippedBonuses.getOrDefault(stat, 0), equippedItem != null);
                }
            });

            // ✅ STATS PE CARE LE PIERZI
            if (equippedItem != null) {
                equippedBonuses.forEach((stat, equippedValue) -> {
                    if (!newBonuses.containsKey(stat) && equippedValue > 0) {
                        String icon = getStatIcon(stat);
                        String name = formatStatName(stat);
                        tooltipText.append("  ").append(icon).append(" 0 ").append(name)
                                .append(" ❌(-").append(equippedValue).append(")\n");
                    }
                });
            }
        }

        // ✅ SUMAR COMPARAȚIE
        if (equippedItem != null) {
            tooltipText.append("\n⚔️ VS ECHIPAT:\n");
            tooltipText.append("📦 ").append(equippedItem.getNume()).append("\n");

            int betterStats = 0, worseStats = 0, equalStats = 0;

            for (String stat : getAllUniqueStats(newBonuses, equippedBonuses)) {
                int newVal = newBonuses.getOrDefault(stat, 0);
                int equippedVal = equippedBonuses.getOrDefault(stat, 0);

                if (newVal > equippedVal) betterStats++;
                else if (newVal < equippedVal) worseStats++;
                else if (newVal > 0) equalStats++;
            }

            if (betterStats > 0) {
                tooltipText.append("✅ ").append(betterStats).append(" îmbunătățiri\n");
            }
            if (worseStats > 0) {
                tooltipText.append("❌ ").append(worseStats).append(" scăderi\n");
            }
            if (equalStats > 0) {
                tooltipText.append("⚖️ ").append(equalStats).append(" identici\n");
            }
        }



        tooltipText.append("\n💰 Valoare: ").append(item.getPret()).append(" gold");

        Tooltip tooltip = new Tooltip(tooltipText.toString());
        tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: #2c3e50; -fx-text-fill: white; -fx-max-width: 350px; -fx-wrap-text: true;");

        // ✅ TOOLTIP SETTINGS pentru hover permanent
        tooltip.setShowDelay(javafx.util.Duration.millis(300));    // Apare repede
        tooltip.setHideDelay(javafx.util.Duration.millis(100));    // Se ascunde repede când pleci
        tooltip.setShowDuration(javafx.util.Duration.INDEFINITE); // ✅ Stă cât faci hover!

        return tooltip;
    }

    private ObiectEchipament findEquippedItemForComparison(ObiectEchipament newItem) {
        return switch (newItem.getTip()) {
            case WEAPON_ONE_HANDED, WEAPON_TWO_HANDED -> hero.getMainHandWeapon();
            case SHIELD, OFF_HAND_WEAPON, OFF_HAND_MAGIC -> hero.getOffHandItem();
            case ARMOR -> hero.getArmorEquipped();
            case HELMET -> hero.getHelmetEquipped();
            case GLOVES -> hero.getGlovesEquipped();
            case BOOTS -> hero.getBootsEquipped();
            case NECKLACE -> hero.getNecklaceEquipped();
            case RING -> {
                // Pentru ring-uri, compară cu cel mai slab ring echipat (pentru a înlocui)
                ObiectEchipament ring1 = hero.getRing1Equipped();
                ObiectEchipament ring2 = hero.getRing2Equipped();

                if (ring1 == null && ring2 == null) yield null;
                if (ring1 == null) yield ring2;
                if (ring2 == null) yield ring1;

                // Compară care ring e mai slab (pentru înlocuire)
                int ring1Value = ring1.getTotalBonuses().values().stream().mapToInt(Integer::intValue).sum();
                int ring2Value = ring2.getTotalBonuses().values().stream().mapToInt(Integer::intValue).sum();

                yield ring1Value <= ring2Value ? ring1 : ring2; // Returnează cel mai slab
            }
            default -> null;
        };
    }

    /**
     * ✨ Helper pentru adăugarea unui stat cu comparație în tooltip
     */
    private void addStatToTooltip(StringBuilder tooltip, String stat, int newValue, int equippedValue, boolean hasEquipped) {
        String icon = getStatIcon(stat);
        String name = formatStatName(stat);

        if (!hasEquipped || equippedValue == 0) {
            // Niciun item echipat sau stat nou
            tooltip.append("  ").append(icon).append(" +").append(newValue).append(" ").append(name).append("\n");
        } else {
            int difference = newValue - equippedValue;

            if (difference > 0) {
                // ✅ Verde pentru îmbunătățire
                tooltip.append("  ").append(icon).append(" +").append(newValue).append(" ").append(name)
                        .append(" ✅(+").append(difference).append(")\n");
            } else if (difference < 0) {
                // ❌ Roșu pentru scădere
                tooltip.append("  ").append(icon).append(" +").append(newValue).append(" ").append(name)
                        .append(" ❌(").append(difference).append(")\n");
            } else {
                // ⚖️ Același
                tooltip.append("  ").append(icon).append(" +").append(newValue).append(" ").append(name).append(" ⚖️\n");
            }
        }
    }
    /**
     * 📝 Obține toate statisticile unice din ambele iteme
     */
    private java.util.Set<String> getAllUniqueStats(Map<String, Integer> bonuses1, Map<String, Integer> bonuses2) {
        java.util.Set<String> allStats = new java.util.HashSet<>(bonuses1.keySet());
        allStats.addAll(bonuses2.keySet());
        return allStats;
    }

    /**
     * Actualizează stats-urile în timp real - EXTENDED VERSION
     */
    private void updateRealTimeStats() {
        realTimeStatsPanel.getChildren().clear();

        // ✅ NIVEL ȘI PROGRES XP
        addStatRow("⭐ Level", String.valueOf(hero.getNivel()));

// Calculează procentul XP
        double xpPercent = ((double) hero.getExperienta() / hero.getExpNecesara()) * 100;
        String xpDisplay = hero.getExperienta() + "/" + hero.getExpNecesara() +
                String.format(" (%.1f%%)", xpPercent);
        addStatRow("📊 XP", xpDisplay);

// Stat points dacă există
        if (hero.getStatPointsToAllocate() > 0) {
            addStatRow("🎯 Stat Points", String.valueOf(hero.getStatPointsToAllocate()));
        }

        // Passive points for talent tree
        if (hero.getPassivePoints() > 0) {
            addStatRow("🌳 Passive Points", String.valueOf(hero.getPassivePoints()));
        }

        addSeparator();

        // HP & Resources
        addStatRow("❤️ HP", hero.getViata() + "/" + hero.getViataMaxima());
        addStatRow("💙 " + hero.getTipResursa(), hero.getResursaCurenta() + "/" + hero.getResursaMaxima());

        addSeparator();

        // Main Stats - WoW Style
        addWoWStatRow("💪 Strength", hero.getStrength(), hero.getStrengthTotal());
        addWoWStatRow("🎯 Dexterity", hero.getDexterity(), hero.getDexterityTotal());
        addWoWStatRow("🧠 Intelligence", hero.getIntelligence(), hero.getIntelligenceTotal());
        addWoWStatRow("🛡️ Defense", hero.getDefense(), hero.getDefenseTotal());

        addSeparator();

        // Combat Stats cu breakdown
        Map<String, Integer> allBonuses = getHeroAllBonuses();

        double baseHit = getBaseHitChance();
        int hitEquipBonus = allBonuses.getOrDefault("hit_chance", 0);
        addWoWPercentStatRow("🎯 Hit Chance", baseHit, hitEquipBonus);

        double baseCrit = getBaseCritChance();
        int critEquipBonus = allBonuses.getOrDefault("crit_chance", 0);
        addWoWPercentStatRow("💥 Crit Chance", baseCrit, critEquipBonus);

        double baseDodge = getBaseDodgeChance();
        int dodgeEquipBonus = allBonuses.getOrDefault("dodge_chance", 0);
        addWoWPercentStatRow("💨 Dodge Chance", baseDodge, dodgeEquipBonus);

        // ✅ DAMAGE SECTION - ÎNTOTDEAUNA VIZIBILĂ
        addSeparator();
        Label damageTitle = new Label("⚔️ DAMAGE:");
        damageTitle.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 12px;");
        realTimeStatsPanel.getChildren().add(damageTitle);

        int weaponDamage = allBonuses.getOrDefault("Damage", 0);
        int damageBonus = allBonuses.getOrDefault("damage_bonus", 0);
        int attackBonus = allBonuses.getOrDefault("attack_bonus", 0);

        addStatRow("⚔️ Weapon Damage", weaponDamage > 0 ? String.valueOf(weaponDamage) : "0");
        if (damageBonus > 0) {
            addStatRow("⚔️ Damage Bonus", "+" + damageBonus);
        }
        if (attackBonus > 0) {
            addStatRow("⚔️ Attack Bonus", "+" + attackBonus);
        }

        // Damage total estimativ
        int totalWeaponDamage = weaponDamage + damageBonus + attackBonus;
        int estimatedDamage = hero.getStrengthTotal() * 2 + totalWeaponDamage;
        addStatRow("💀 Est. Total", String.valueOf(estimatedDamage));

        // ✅ DEFENSE SECTION - ÎNTOTDEAUNA VIZIBILĂ
        addSeparator();
        Label defenseTitle = new Label("🛡️ DEFENSE:");
        defenseTitle.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold; -fx-font-size: 12px;");
        realTimeStatsPanel.getChildren().add(defenseTitle);

        int equipHealth = allBonuses.getOrDefault("health", 0);
        int damageReduction = allBonuses.getOrDefault("damage_reduction", 0);
        int blockChance = allBonuses.getOrDefault("block_chance", 0);

        if (equipHealth > 0) {
            addStatRow("❤️ Bonus HP", "+" + equipHealth);
        } else {
            addStatRow("❤️ Bonus HP", "0");
        }

        addStatRow("🛡️ Damage Reduction", damageReduction > 0 ? damageReduction + "%" : "0%");
        addStatRow("🛡️ Block Chance", blockChance > 0 ? blockChance + "%" : "0%");

        // ✅ UTILITY SECTION - ÎNTOTDEAUNA VIZIBILĂ
        addSeparator();
        Label utilityTitle = new Label("✨ UTILITY:");
        utilityTitle.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold; -fx-font-size: 12px;");
        realTimeStatsPanel.getChildren().add(utilityTitle);

        int lifesteal = allBonuses.getOrDefault("lifesteal", 0);
        int goldFind = allBonuses.getOrDefault("gold_find", 0);
        int fireRes = allBonuses.getOrDefault("fire_resistance", 0);
        int iceRes = allBonuses.getOrDefault("ice_resistance", 0);
        int manaSteal = allBonuses.getOrDefault("mana_steal", 0);
        int elementalDmg = allBonuses.getOrDefault("elemental_damage", 0);

        addStatRow("🩸 Lifesteal", lifesteal > 0 ? lifesteal + "%" : "0%");
        addStatRow("💰 Gold Find", goldFind > 0 ? "+" + goldFind + "%" : "0%");
        addStatRow("💙 Mana Steal", manaSteal > 0 ? manaSteal + "%" : "0%");
        addStatRow("🌈 Elemental", elementalDmg > 0 ? "+" + elementalDmg : "0");
        addStatRow("🔥 Fire Res", fireRes > 0 ? fireRes + "%" : "0%");
        addStatRow("❄️ Ice Res", iceRes > 0 ? iceRes + "%" : "0%");

        addSeparator();
        int equippedCount = (int) equipmentSlots.values().stream()
                .filter(slot -> slot.getCurrentItem() != null)
                .count();
        addStatRow("📦 Equipped", equippedCount + "/" + equipmentSlots.size());
    }

    /**
     * ✅ Adaugă un rând pentru percent stats cu breakdown WoW style
     */
    private void addWoWPercentStatRow(String name, double base, int equipmentBonus) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(name + ":");
        nameLabel.setPrefWidth(120);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        // Format: "75.0 + 5 = 80.0%"
        String valueText;
        if (equipmentBonus > 0) {
            double total = base + equipmentBonus;
            valueText = String.format("%.1f + %d = %.1f%%", base, equipmentBonus, total);
        } else {
            valueText = String.format("%.1f%%", base);
        }

        Label valueLabel = new Label(valueText);
        String color = equipmentBonus > 0 ? "#27ae60" : "#bdc3c7";
        valueLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        row.getChildren().addAll(nameLabel, valueLabel);
        realTimeStatsPanel.getChildren().add(row);
    }


    /**
     * ✅ Adaugă un rând pentru combat stats cu equipment bonus
     */
    private void addCombatStatWithEquipment(String name, double baseValue, int equipmentBonus) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(name + ":");
        nameLabel.setPrefWidth(120);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        double totalValue = baseValue + equipmentBonus;
        String valueText = String.format("%.1f%%", totalValue);

        if (equipmentBonus > 0) {
            valueText += String.format(" (+%d)", equipmentBonus);
        }

        Label valueLabel = new Label(valueText);
        String color = equipmentBonus > 0 ? "#27ae60" : "#bdc3c7";
        valueLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        row.getChildren().addAll(nameLabel, valueLabel);
        realTimeStatsPanel.getChildren().add(row);
    }


    private void addWoWStatRow(String name, int base, int total) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(name + ":");
        nameLabel.setPrefWidth(100);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        int bonus = total - base;
        String valueText = base + (bonus > 0 ? " + " + bonus + " = " + total : "");

        Label valueLabel = new Label(valueText);
        valueLabel.setStyle("-fx-text-fill: " + (bonus > 0 ? "#27ae60" : "#bdc3c7") + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        row.getChildren().addAll(nameLabel, valueLabel);
        realTimeStatsPanel.getChildren().add(row);
    }

    private void addStatRow(String name, String value) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(name + ":");
        nameLabel.setPrefWidth(120);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12px; -fx-font-weight: bold;");

        row.getChildren().addAll(nameLabel, valueLabel);
        realTimeStatsPanel.getChildren().add(row);
    }

    private void addSeparator() {
        Label sep = new Label("─────────────────");
        sep.setStyle("-fx-text-fill: #34495e; -fx-font-size: 10px;");
        realTimeStatsPanel.getChildren().add(sep);
    }

    private Map<String, Integer> getHeroAllBonuses() {
        Map<String, Integer> allBonuses = new HashMap<>();

        Map<String, ObiectEchipament> echipat = hero.getEchipat();
        for (ObiectEchipament item : echipat.values()) {
            if (item != null) {
                Map<String, Integer> itemBonuses = item.getTotalBonuses();
                itemBonuses.forEach((stat, bonus) ->
                        allBonuses.merge(stat, bonus, Integer::sum)
                );
            }
        }

        return allBonuses;
    }

    private String getStatIcon(String stat) {
        return switch (stat.toLowerCase()) {
            case "damage" -> "⚔️";
            case "defense" -> "🛡️";
            case "health", "viata" -> "❤️";
            case "crit_chance" -> "💥";
            case "hit_chance" -> "🎯";
            case "dodge_chance" -> "💨";
            case "damage_reduction" -> "🛡️";
            case "gold_find" -> "💰";
            case "lifesteal" -> "🩸";
            case "mana_steal" -> "💙";
            case "elemental_damage" -> "🌈";
            case "fire_resistance" -> "🔥";
            case "ice_resistance" -> "❄️";
            case "damage_bonus" -> "⚔️";
            case "attack_bonus" -> "⚔️";
            case "mana" -> "💙";
            case "block_chance" -> "🛡️";
            default -> "✨";
        };
    }

    private String formatStatName(String stat) {
        return switch (stat.toLowerCase()) {
            case "damage" -> "Damage";
            case "defense" -> "Defense";
            case "health", "viata" -> "Health";
            case "crit_chance" -> "Crit %";
            case "hit_chance" -> "Hit %";
            case "dodge_chance" -> "Dodge %";
            case "damage_reduction" -> "Dmg Red %";
            case "gold_find" -> "Gold %";
            case "lifesteal" -> "Lifesteal %";
            case "mana_steal" -> "Mana Steal %";
            case "elemental_damage" -> "Elem Dmg";
            case "fire_resistance" -> "Fire Res %";
            case "ice_resistance" -> "Ice Res %";
            case "damage_bonus" -> "Dmg Bonus";
            case "attack_bonus" -> "Attack";
            case "mana" -> "Mana";
            case "block_chance" -> "Block %";
            default -> stat;
        };
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

    /**
     * ✅ Calculează base crit chance fără echipament
     */
    private double getBaseCritChance() {
        double baseCritChance = 5.0; // GameConstants.BASE_CRIT_CHANCE
        double dexBonus = hero.getDexterity() * 0.1; // GameConstants.CRIT_CHANCE_PER_DEX
        return baseCritChance + dexBonus;
    }

    /**
     * ✅ Calculează base hit chance fără echipament
     */
    private double getBaseHitChance() {
        double baseHitChance = 75.0; // GameConstants.BASE_HIT_CHANCE
        double dexBonus = hero.getDexterity() * 0.2; // GameConstants.HIT_CHANCE_PER_DEX
        double levelBonus = hero.getNivel() * 0.5; // GameConstants.HIT_CHANCE_PER_LEVEL
        return baseHitChance + dexBonus + levelBonus;
    }

    /**
     * ✅ Calculează base dodge chance fără echipament
     */
    private double getBaseDodgeChance() {
        double baseDodgeChance = 5.0; // GameConstants.BASE_DODGE_CHANCE
        double dexBonus = hero.getDexterity() * 0.15; // GameConstants.DODGE_CHANCE_PER_DEX
        return baseDodgeChance + dexBonus;
    }


    private static class EquipmentSlot {
        private String slotId;
        private ObiectEchipament currentItem;
        private VBox slotBox;
        private Label itemLabel;

        public EquipmentSlot(String slotId) {
            this.slotId = slotId;
        }

        public String getSlotId() { return slotId; }
        public ObiectEchipament getCurrentItem() { return currentItem; }
        public void setCurrentItem(ObiectEchipament currentItem) { this.currentItem = currentItem; }
        public VBox getSlotBox() { return slotBox; }
        public void setSlotBox(VBox slotBox) { this.slotBox = slotBox; }
        public Label getItemLabel() { return itemLabel; }
        public void setItemLabel(Label itemLabel) { this.itemLabel = itemLabel; }
    }
}
