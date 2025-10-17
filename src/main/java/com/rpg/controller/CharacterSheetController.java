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
    private Label statsLabel;
    private VBox equipmentSlotsPanel;
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

    /**
     * Header cu info basic
     */
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

    /**
     * Conținut principal - 3 paneluri
     */
    private HBox createMainContent() {
        HBox content = new HBox(15);
        content.setPadding(new Insets(20));

        // Panel stâng - Equipment slots
        VBox leftPanel = createEquipmentPanel();

        // Panel mijloc - Inventory
        VBox middlePanel = createInventoryPanel();

        // Panel drept - Stats în timp real
        VBox rightPanel = createRealTimeStatsPanel();

        content.getChildren().addAll(leftPanel, middlePanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.NEVER);
        HBox.setHgrow(middlePanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.NEVER);

        return content;
    }

    /**
     * 🎽 Panel cu slot-urile de echipament - REDESIGN COMPLET
     */
    private VBox createEquipmentPanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(320);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");

        Label title = new Label("⚔️ ECHIPAMENT");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        // Grid pentru slot-uri - Layout nou pentru dual-wielding
        GridPane slotsGrid = new GridPane();
        slotsGrid.setHgap(8);
        slotsGrid.setVgap(8);
        slotsGrid.setAlignment(Pos.CENTER);

        // Layout nou:
        //       [HELMET]
        //   [RING1] [ARMOR] [RING2]
        // [MAIN_HAND] [GLOVES] [OFF_HAND]
        //   [NECKLACE] [BOOTS]

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
    /**
     * 🎒 Panel cu inventarul
     */
    private VBox createInventoryPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");

        Label title = new Label("🎒 INVENTAR - Drag items to equip");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        // Filter pentru echipament doar
        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("Toate", "Arme", "Armuri", "Accesorii");
        filterCombo.setValue("Toate");
        filterCombo.setOnAction(e -> loadInventoryItems());

        inventoryListView = new ListView<>();
        inventoryListView.setStyle("-fx-font-size: 14px;");
        VBox.setVgrow(inventoryListView, Priority.ALWAYS);

        // Setup drag pentru inventory items
//        inventoryListView.setCellFactory(lv -> {
//            ListCell<InventoryItemDTO> cell = new ListCell<InventoryItemDTO>() {
//                @Override
//                protected void updateItem(InventoryItemDTO item, boolean empty) {
//                    super.updateItem(item, empty);
//                    if (empty || item == null) {
//                        setText(null);
//                        setGraphic(null);
//                    } else {
//                        setText(item.toString());
//                        if (item.isEquipped()) {
//                            setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
//                        } else {
//                            setStyle("");
//                        }
//                    }
//                }
//            };
//
//            // 🖱️ DRAG SOURCE
//            cell.setOnDragDetected(event -> {
//                if (cell.getItem() != null && cell.getItem().getEquipment() != null) {
//                    Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
//                    ClipboardContent content = new ClipboardContent();
//                    content.putString("equipment:" + System.identityHashCode(cell.getItem().getEquipment()));
//                    db.setContent(content);
//                    event.consume();
//                }
//            });
//
//            return cell;
//        });

        loadInventoryItems();

        panel.getChildren().addAll(title, filterCombo, inventoryListView);
        return panel;
    }

    /**
     * Actualizează setup-ul de drag and drop cu highlighting
     */
    private void setupDragAndDrop() {
        inventoryListView.setCellFactory(lv -> {
            ListCell<InventoryItemDTO> cell = new ListCell<InventoryItemDTO>() {
                @Override
                protected void updateItem(InventoryItemDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        setTooltip(null);
                    } else {
                        setText(item.getName());

                        // ✨ TOOLTIP cu statistici
                        if (item.getEquipment() != null) {
                            Tooltip tooltip = createItemTooltip(item.getEquipment());
                            setTooltip(tooltip);
                        }

                        if (item.isEquipped()) {
                            setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            };

            // 🖱️ DRAG SOURCE cu highlighting
            cell.setOnDragDetected(event -> {
                if (cell.getItem() != null && cell.getItem().getEquipment() != null) {
                    // Highlight compatible slots
                    highlightCompatibleSlots(cell.getItem().getEquipment());

                    Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString("equipment:" + System.identityHashCode(cell.getItem().getEquipment()));
                    db.setContent(content);
                    event.consume();
                }
            });

            // Reset highlighting when drag ends
            cell.setOnDragDone(event -> resetSlotHighlighting());

            return cell;
        });
    }

    /**
     * 📋 Creează tooltip cu statistici pentru item
     */
    private Tooltip createItemTooltip(ObiectEchipament item) {
        StringBuilder tooltipText = new StringBuilder();

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
        Map<String, Integer> bonuses = item.getTotalBonuses();
        if (bonuses.isEmpty()) {
            tooltipText.append("  • Fără bonusuri\n");
        } else {
            bonuses.forEach((stat, bonus) -> {
                String icon = getStatIcon(stat);
                String name = formatStatName(stat);
                tooltipText.append("  ").append(icon).append(" +").append(bonus).append(" ").append(name).append("\n");
            });
        }

        tooltipText.append("\n💰 Valoare: ").append(item.getPret()).append(" gold");

        Tooltip tooltip = new Tooltip(tooltipText.toString());
        tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: #2c3e50; -fx-text-fill: white;");
        tooltip.setShowDelay(javafx.util.Duration.millis(500));

        return tooltip;
    }

    /**
     * 📊 Panel cu stats în timp real
     */
    private VBox createRealTimeStatsPanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(280);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");

        Label title = new Label("📊 STATS LIVE");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        realTimeStatsPanel = new VBox(5);
        realTimeStatsPanel.setStyle("-fx-background-color: #0f1419; -fx-background-radius: 8; -fx-padding: 15;");

        updateRealTimeStats();

        ScrollPane scrollPane = new ScrollPane(realTimeStatsPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        panel.getChildren().addAll(title, scrollPane);
        return panel;
    }

    /**
     * Footer cu butoane
     */
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

    /**
     * Inițializează slot-urile pentru noul sistem
     */
    private void initializeEquipmentSlots() {
        equipmentSlots.put("MAIN_HAND", new EquipmentSlot("MAIN_HAND",
                java.util.Set.of(ObiectEchipament.TipEchipament.WEAPON_ONE_HANDED,
                        ObiectEchipament.TipEchipament.WEAPON_TWO_HANDED)));

        equipmentSlots.put("OFF_HAND", new EquipmentSlot("OFF_HAND",
                java.util.Set.of(ObiectEchipament.TipEchipament.SHIELD,
                        ObiectEchipament.TipEchipament.OFF_HAND_WEAPON,
                        ObiectEchipament.TipEchipament.OFF_HAND_MAGIC)));

        equipmentSlots.put("ARMOR", new EquipmentSlot("ARMOR",
                java.util.Set.of(ObiectEchipament.TipEchipament.ARMOR)));

        equipmentSlots.put("HELMET", new EquipmentSlot("HELMET",
                java.util.Set.of(ObiectEchipament.TipEchipament.HELMET)));

        equipmentSlots.put("GLOVES", new EquipmentSlot("GLOVES",
                java.util.Set.of(ObiectEchipament.TipEchipament.GLOVES)));

        equipmentSlots.put("BOOTS", new EquipmentSlot("BOOTS",
                java.util.Set.of(ObiectEchipament.TipEchipament.BOOTS)));

        equipmentSlots.put("RING1", new EquipmentSlot("RING1",
                java.util.Set.of(ObiectEchipament.TipEchipament.RING)));

        equipmentSlots.put("RING2", new EquipmentSlot("RING2",
                java.util.Set.of(ObiectEchipament.TipEchipament.RING)));

        equipmentSlots.put("NECKLACE", new EquipmentSlot("NECKLACE",
                java.util.Set.of(ObiectEchipament.TipEchipament.NECKLACE)));
    }

    /**
     * ✨ HIGHLIGHTING MAGIC - Calculează ce slot-uri sunt compatibile
     */
    private java.util.Set<String> getCompatibleSlots(ObiectEchipament item) {
        java.util.Set<String> compatibleSlots = new java.util.HashSet<>();

        if (item == null) return compatibleSlots;

        switch (item.getTip()) {
            case WEAPON_ONE_HANDED -> {
                compatibleSlots.add("MAIN_HAND");
                // Verifică dacă poate merge în off-hand
                if (item.isOffHandCompatible()) {
                    // Doar dacă nu avem two-handed în main hand
                    ObiectEchipament mainHand = hero.getMainHandWeapon();
                    if (mainHand == null || !mainHand.isTwoHanded()) {
                        compatibleSlots.add("OFF_HAND");
                    }
                }
            }
            case WEAPON_TWO_HANDED -> compatibleSlots.add("MAIN_HAND");
            case SHIELD -> {
                // Doar dacă nu avem two-handed weapon
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
            if (compatibleSlots.contains(slotId)) {
                // Highlight verde pentru slot-uri compatibile
                slotBox.setStyle("-fx-border-color: #27ae60; -fx-border-width: 3; " +
                        "-fx-background-color: #2d5a3d; -fx-background-radius: 8; " +
                        "-fx-effect: dropshadow(gaussian, #27ae60, 10, 0.7, 0, 0);");
            } else {
                // Highlight roșu pentru slot-uri incompatibile
                slotBox.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; " +
                        "-fx-background-color: #5a2d2d; -fx-background-radius: 8; " +
                        "-fx-effect: dropshadow(gaussian, #e74c3c, 5, 0.5, 0, 0);");
            }
        });
    }

    /**
     * 🎯 Resetează highlighting-ul slot-urilor
     */
    private void resetSlotHighlighting() {
        equipmentSlots.forEach((slotId, slot) -> updateSlotDisplay(slotId));
    }



    /**
     * Creează și adaugă un slot în grid
     */
    private void createAndAddSlot(GridPane grid, String slotId, String displayName, int col, int row) {
        EquipmentSlot slot = equipmentSlots.get(slotId);

        VBox slotBox = new VBox(5);
        slotBox.setAlignment(Pos.CENTER);
        slotBox.setPrefSize(80, 100);
        slotBox.setStyle("-fx-border-color: #34495e; -fx-border-width: 2; -fx-background-color: #2c3e50; -fx-background-radius: 8;");

        Label slotLabel = new Label(displayName);
        slotLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #bdc3c7; -fx-text-alignment: center;");
        slotLabel.setWrapText(true);

        Label itemLabel = new Label();
        itemLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-text-alignment: center;");
        itemLabel.setWrapText(true);
        itemLabel.setPrefHeight(60);

        slotBox.getChildren().addAll(slotLabel, itemLabel);

        // 🎯 DRAG TARGET
        slotBox.setOnDragOver(event -> {
            if (event.getGestureSource() != slotBox && event.getDragboard().hasString()) {
                String content = event.getDragboard().getString();
                if (content.startsWith("equipment:")) {
                    event.acceptTransferModes(TransferMode.MOVE);
                    slotBox.setStyle("-fx-border-color: #27ae60; -fx-border-width: 3; -fx-background-color: #34495e; -fx-background-radius: 8;");
                }
            }
            event.consume();
        });

        slotBox.setOnDragExited(event -> {
            slotBox.setStyle("-fx-border-color: #34495e; -fx-border-width: 2; -fx-background-color: #2c3e50; -fx-background-radius: 8;");
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

        // 🖱️ DOUBLE CLICK pentru deechipare
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

    // ==================== DRAG & DROP LOGIC ====================

    /**
     * Găsește item pe baza hash-ului
     */
    private ObiectEchipament findItemByHash(String hash) {
        for (ObiectEchipament item : hero.getInventar().getItems()) {
            if (String.valueOf(System.identityHashCode(item)).equals(hash)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Verifică dacă item-ul poate fi echipat în slot - ACTUALIZAT
     */
    private boolean canEquipInSlot(ObiectEchipament item, String slotId) {
        return getCompatibleSlots(item).contains(slotId);
    }

    /**
     * Echipează item în slot - ACTUALIZAT pentru noul sistem
     */
    private void equipItemInSlot(ObiectEchipament item, String slotId) {
        try {
            // Folosește noul sistem de echipare
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
            e.printStackTrace();
        }
    }
    /**
     * Deechipează din slot - ACTUALIZAT
     */
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
            e.printStackTrace();
        }
    }
    // ==================== HELPERS ====================

    /**
     * Încarcă items din inventar
     */
    private void loadInventoryItems() {
        var items = inventoryService.getItemsByCategory(hero, InventoryServiceFX.InventoryCategory.TOATE)
                .stream()
                .filter(item -> item.getType() == InventoryItemDTO.ItemType.EQUIPMENT ||
                        item.getType() == InventoryItemDTO.ItemType.EQUIPMENT_EQUIPPED)
                .toList();

        inventoryListView.getItems().clear();
        inventoryListView.getItems().addAll(items);
    }

    /**
     * Actualizează toate slot-urile
     */
    private void updateAllSlots() {
        for (String slotId : equipmentSlots.keySet()) {
            updateSlotDisplay(slotId);
        }
    }

    /**
     * Actualizează afișarea unui slot
     */
    private void updateSlotDisplay(String slotId) {
        EquipmentSlot slot = equipmentSlots.get(slotId);
        ObiectEchipament currentItem = getCurrentItemForSlot(slotId);

        slot.setCurrentItem(currentItem);

        if (currentItem != null) {
            String displayText = currentItem.getTip().getIcon() + "\n" +
                    currentItem.getNume().substring(0, Math.min(currentItem.getNume().length(), 15));
            if (currentItem.getNume().length() > 15) displayText += "...";

            slot.getItemLabel().setText(displayText);
            slot.getSlotBox().setStyle("-fx-border-color: #27ae60; -fx-border-width: 2; -fx-background-color: #2d5a3d; -fx-background-radius: 8;");
        } else {
            slot.getItemLabel().setText("Empty");
            slot.getSlotBox().setStyle("-fx-border-color: #34495e; -fx-border-width: 2; -fx-background-color: #2c3e50; -fx-background-radius: 8;");
        }
    }

    /**
     * Obține item-ul curent pentru un slot - ACTUALIZAT pentru noul sistem
     */
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

    /**
     * Actualizează stats-urile în timp real
     */
    private void updateRealTimeStats() {
        realTimeStatsPanel.getChildren().clear();

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

        // Combat Stats
        addStatRow("🎯 Hit Chance", String.format("%.1f%%", hero.getHitChance()));
        addStatRow("💥 Crit Chance", String.format("%.1f%%", hero.getCritChanceTotal()));
        addStatRow("💨 Dodge Chance", String.format("%.1f%%", hero.getDodgeChanceTotal()));

        // Equipment Bonuses
        Map<String, Integer> bonuses = getHeroAllBonuses();
        if (!bonuses.isEmpty()) {
            addSeparator();
            Label bonusTitle = new Label("✨ BONUSURI:");
            bonusTitle.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold; -fx-font-size: 12px;");
            realTimeStatsPanel.getChildren().add(bonusTitle);

            bonuses.forEach((stat, bonus) -> {
                if (!stat.equals("strength") && !stat.equals("dexterity") &&
                        !stat.equals("intelligence") && !stat.equals("defense")) {
                    String icon = getStatIcon(stat);
                    String name = formatStatName(stat);
                    addStatRow(icon + " " + name, "+" + bonus);
                }
            });
        }
    }

    /**
     * Adaugă un rând de stat WoW style
     */
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

    /**
     * Adaugă un rând de stat normal
     */
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

    /**
     * Adaugă separator
     */
    private void addSeparator() {
        Label sep = new Label("─────────────────");
        sep.setStyle("-fx-text-fill: #34495e; -fx-font-size: 10px;");
        realTimeStatsPanel.getChildren().add(sep);
    }

    // ==================== RING LOGIC ====================

    /**
     * Logică specială pentru ring-uri (2 slot-uri)
     */
    private void handleRingEquip(ObiectEchipament ring, String slotId) {
        // Pentru moment, folosește slot-ul unic existent
        // În viitor, poți extinde pentru 2 ring-uri separate
        hero.echipeazaAccesoriu(ring);
    }

    private void handleRingUnequip(String slotId) {
        hero.deechipeazaAccesoriu();
    }

    // ==================== HELPERS FINALE ====================

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
     * Actualizare EquipmentSlot class pentru multiple tipuri acceptate
     */
    private static class EquipmentSlot {
        private String slotId;
        private java.util.Set<ObiectEchipament.TipEchipament> acceptedTypes;
        private ObiectEchipament currentItem;
        private VBox slotBox;
        private Label itemLabel;

        public EquipmentSlot(String slotId, java.util.Set<ObiectEchipament.TipEchipament> acceptedTypes) {
            this.slotId = slotId;
            this.acceptedTypes = acceptedTypes;
        }

        // Getters & Setters
        public String getSlotId() { return slotId; }
        public java.util.Set<ObiectEchipament.TipEchipament> getAcceptedTypes() { return acceptedTypes; }
        public ObiectEchipament getCurrentItem() { return currentItem; }
        public void setCurrentItem(ObiectEchipament currentItem) { this.currentItem = currentItem; }
        public VBox getSlotBox() { return slotBox; }
        public void setSlotBox(VBox slotBox) { this.slotBox = slotBox; }
        public Label getItemLabel() { return itemLabel; }
        public void setItemLabel(Label itemLabel) { this.itemLabel = itemLabel; }
    }
}
