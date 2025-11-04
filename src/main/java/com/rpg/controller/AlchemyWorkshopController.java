package com.rpg.controller;

import com.rpg.model.characters.Erou;
import com.rpg.model.items.EnchantScroll;
import com.rpg.model.items.FlaskPiece;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.service.PotionUpgradeService;
import com.rpg.utils.DialogHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

/**
 * Controller pentru Alchemy Workshop - upgrade poțiuni cu flask pieces
 */
public class AlchemyWorkshopController {

    private Stage stage;
    private Erou hero;
    private Runnable onExitCallback;

    private Label healthTierLabel;
    private Label manaTierLabel;
    private Label flaskPiecesLabel;

    public AlchemyWorkshopController(Stage stage, Erou hero, Runnable onExitCallback) {
        this.stage = stage;
        this.hero = hero;
        this.onExitCallback = onExitCallback;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        root.setTop(createHeader());
        root.setCenter(createMainContent());
        root.setBottom(createFooter());

        return new Scene(root, 1900, 1080);
    }

    private VBox createMainContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #1a1a2e;");

        Tab potionsTab = new Tab("🧪 Potion Upgrades", createContent());
        potionsTab.setClosable(false);

        Tab enchantmentsTab = new Tab("✨ Enchantments", createEnchantmentPanel());
        enchantmentsTab.setClosable(false);

        tabPane.getTabs().addAll(potionsTab, enchantmentsTab);

        VBox.setVgrow(tabPane, Priority.ALWAYS);
        content.getChildren().add(tabPane);

        return content;
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #16213e;");

        Label title = new Label("🧙‍♂️ ALCHEMY WORKSHOP");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #9b59b6;");

        Label subtitle = new Label("Upgrade Potion Quality with Flask Pieces");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #f1c40f;");

        flaskPiecesLabel = new Label();
        updateFlaskPiecesLabel();
        flaskPiecesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");

        header.getChildren().addAll(title, subtitle, flaskPiecesLabel);
        return header;
    }

    private VBox createContent() {
        VBox content = new VBox(30);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.TOP_CENTER);

        // Current tiers info
        VBox tiersInfo = createTiersInfo();

        // Upgrade options
        HBox upgradeOptions = createUpgradeOptions();

        // Info box
        VBox infoBox = createInfoBox();

        content.getChildren().addAll(tiersInfo, upgradeOptions, infoBox);
        return content;
    }

    private VBox createTiersInfo() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #0f0f1e; -fx-background-radius: 10;");
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(700);

        Label title = new Label("📊 CURRENT POTION TIERS");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f1c40f;");

        HBox stats = new HBox(50);
        stats.setAlignment(Pos.CENTER);

        // Health potion info
        VBox healthInfo = new VBox(5);
        healthInfo.setAlignment(Pos.CENTER);
        Label healthLabel = new Label("🧪 Health Potions");
        healthLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c;");
        healthTierLabel = new Label();
        updateHealthTierLabel();
        healthTierLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        healthInfo.getChildren().addAll(healthLabel, healthTierLabel);

        // Mana potion info
        VBox manaInfo = new VBox(5);
        manaInfo.setAlignment(Pos.CENTER);
        Label manaLabel = new Label("💙 Mana Potions");
        manaLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #3498db;");
        manaTierLabel = new Label();
        updateManaTierLabel();
        manaTierLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        manaInfo.getChildren().addAll(manaLabel, manaTierLabel);

        stats.getChildren().addAll(healthInfo, manaInfo);

        box.getChildren().addAll(title, stats);
        return box;
    }

    private HBox createUpgradeOptions() {
        HBox options = new HBox(30);
        options.setAlignment(Pos.CENTER);

        // Upgrade Health Potions button
        VBox healthUpgrade = createUpgradeCard(
            "🧪 UPGRADE HEALTH POTIONS",
            FlaskPiece.FlaskType.HEALTH,
            () -> upgradeHealthPotions()
        );

        // Upgrade Mana Potions button
        VBox manaUpgrade = createUpgradeCard(
            "💙 UPGRADE MANA POTIONS",
            FlaskPiece.FlaskType.MANA,
            () -> upgradeManaPotions()
        );

        options.getChildren().addAll(healthUpgrade, manaUpgrade);
        return options;
    }

    private VBox createUpgradeCard(String title, FlaskPiece.FlaskType type, Runnable action) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(350);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(300);

        PotionUpgradeService.PotionTier currentTier = getCurrentTier(type);
        PotionUpgradeService.PotionTier nextTier = currentTier.getNext();

        Label currentLabel = new Label("Current: " + currentTier.getIcon() + " " + currentTier.getDisplayName());
        currentLabel.setStyle("-fx-text-fill: #95a5a6;");

        Label nextLabel = new Label("Next: " + nextTier.getIcon() + " " + nextTier.getDisplayName());
        nextLabel.setStyle("-fx-text-fill: #f1c40f;");

        int flaskPiecesNeeded = getFlaskPiecesNeeded(currentTier);

        Label costLabel = new Label(String.format("Cost: %d x %s Flask Pieces",
            flaskPiecesNeeded, type.getIcon()));
        costLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        Button upgradeButton = new Button("⬆️ UPGRADE");
        upgradeButton.setStyle(
            "-fx-background-color: #9b59b6; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10 30;"
        );
        upgradeButton.setOnAction(e -> {
            action.run();
            refreshUI();
        });

        // Disable if at max tier or not enough resources
        if (currentTier == PotionUpgradeService.PotionTier.MASTER) {
            upgradeButton.setDisable(true);
            upgradeButton.setText("MAX LEVEL");
            nextLabel.setText("Already at max!");
        } else if (!canAffordUpgrade(type, flaskPiecesNeeded)) {
            upgradeButton.setStyle(upgradeButton.getStyle() + "-fx-opacity: 0.5;");
        }

        card.getChildren().addAll(titleLabel, currentLabel, nextLabel, costLabel, upgradeButton);
        return card;
    }

    private VBox createInfoBox() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: #0f0f1e; -fx-background-radius: 10; -fx-border-color: #9b59b6; -fx-border-width: 2; -fx-border-radius: 10;");
        box.setMaxWidth(700);

        Label title = new Label("ℹ️ POTION TIER SYSTEM");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f1c40f;");

        Label info = new Label(
            "🧪 BASIC (1.0x) - Default potions\n" +
            "✨ ENHANCED (1.5x) - 50% more effective | Cost: 3 flask pieces\n" +
            "💎 SUPERIOR (2.0x) - 2x more effective | Cost: 5 flask pieces\n" +
            "🌟 MASTER (3.0x) - 3x more effective | Cost: 10 flask pieces\n\n" +
            "💡 Flask Pieces drop from bosses!\n" +
            "✨ Universal Flask Pieces can be used for any potion type!"
        );
        info.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        info.setWrapText(true);

        box.getChildren().addAll(title, info);
        return box;
    }

    private HBox createFooter() {
        HBox footer = new HBox(20);
        footer.setPadding(new Insets(15));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #16213e;");

        Button backButton = new Button("🔙 Back to Town");
        backButton.setStyle(
            "-fx-background-color: #34495e; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 16px; " +
            "-fx-padding: 10 30;"
        );
        backButton.setOnAction(e -> {
            if (onExitCallback != null) {
                onExitCallback.run();
            }
        });

        footer.getChildren().add(backButton);
        return footer;
    }

    // Helper methods

    private PotionUpgradeService.PotionTier getCurrentTier(FlaskPiece.FlaskType type) {
        if (type == FlaskPiece.FlaskType.HEALTH) {
            return hero.getHealthPotionTier();
        } else {
            return hero.getManaPotionTier();
        }
    }

    private int getFlaskPiecesNeeded(PotionUpgradeService.PotionTier currentTier) {
        return switch (currentTier) {
            case BASIC -> 3;
            case ENHANCED -> 5;
            case SUPERIOR -> 10;
            case MASTER -> 0; // Max level
        };
    }

    private boolean canAffordUpgrade(FlaskPiece.FlaskType type, int flaskPiecesNeeded) {
        int availableFlasks = hero.getFlaskPiecesQuantity(type) +
                             hero.getFlaskPiecesQuantity(FlaskPiece.FlaskType.UNIVERSAL);
        return availableFlasks >= flaskPiecesNeeded;
    }

    private void upgradeHealthPotions() {
        PotionUpgradeService.PotionTier currentTier = hero.getHealthPotionTier();

        if (currentTier == PotionUpgradeService.PotionTier.MASTER) {
            DialogHelper.showError("Max Level", "Health potions are already at maximum tier!");
            return;
        }

        int flaskPiecesNeeded = getFlaskPiecesNeeded(currentTier);

        if (!canAffordUpgrade(FlaskPiece.FlaskType.HEALTH, flaskPiecesNeeded)) {
            DialogHelper.showError("Insufficient Resources",
                String.format("You need %d flask pieces!", flaskPiecesNeeded));
            return;
        }

        // Consume resources
        if (!consumeFlaskPieces(FlaskPiece.FlaskType.HEALTH, flaskPiecesNeeded)) {
            DialogHelper.showError("Error", "Failed to consume flask pieces!");
            return;
        }

        // Upgrade
        hero.upgradeHealthPotionTier();

        PotionUpgradeService.PotionTier newTier = hero.getHealthPotionTier();
        DialogHelper.showSuccess("Upgrade Successful!",
            String.format("Health Potions upgraded to %s %s!\n\nNew healing: %.1fx multiplier",
                newTier.getIcon(), newTier.getDisplayName(), newTier.getMultiplier()));
    }

    private void upgradeManaPotions() {
        PotionUpgradeService.PotionTier currentTier = hero.getManaPotionTier();

        if (currentTier == PotionUpgradeService.PotionTier.MASTER) {
            DialogHelper.showError("Max Level", "Mana potions are already at maximum tier!");
            return;
        }

        int flaskPiecesNeeded = getFlaskPiecesNeeded(currentTier);

        if (!canAffordUpgrade(FlaskPiece.FlaskType.MANA, flaskPiecesNeeded)) {
            DialogHelper.showError("Insufficient Resources",
                String.format("You need %d flask pieces!", flaskPiecesNeeded));
            return;
        }

        // Consume resources
        if (!consumeFlaskPieces(FlaskPiece.FlaskType.MANA, flaskPiecesNeeded)) {
            DialogHelper.showError("Error", "Failed to consume flask pieces!");
            return;
        }

        // Upgrade
        hero.upgradeManaPotionTier();

        PotionUpgradeService.PotionTier newTier = hero.getManaPotionTier();
        DialogHelper.showSuccess("Upgrade Successful!",
            String.format("Mana Potions upgraded to %s %s!\n\nNew restore: %.1fx multiplier",
                newTier.getIcon(), newTier.getDisplayName(), newTier.getMultiplier()));
    }

    private boolean consumeFlaskPieces(FlaskPiece.FlaskType type, int amount) {
        int specificPieces = hero.getFlaskPiecesQuantity(type);

        if (specificPieces >= amount) {
            // Use specific type first
            hero.consumeFlaskPieces(type, amount);
            return true;
        } else {
            // Use specific + universal
            int needed = amount - specificPieces;
            int universalPieces = hero.getFlaskPiecesQuantity(FlaskPiece.FlaskType.UNIVERSAL);

            if (universalPieces >= needed) {
                hero.consumeFlaskPieces(type, specificPieces);
                hero.consumeFlaskPieces(FlaskPiece.FlaskType.UNIVERSAL, needed);
                return true;
            }
        }

        return false;
    }

    private void updateFlaskPiecesLabel() {
        int healthPieces = hero.getFlaskPiecesQuantity(FlaskPiece.FlaskType.HEALTH);
        int manaPieces = hero.getFlaskPiecesQuantity(FlaskPiece.FlaskType.MANA);
        int universalPieces = hero.getFlaskPiecesQuantity(FlaskPiece.FlaskType.UNIVERSAL);

        String text = String.format("Flask Pieces: 🧪 %d | 💙 %d | ✨ %d | 🔮 Shards: %d",
            healthPieces, manaPieces, universalPieces, hero.getScrap());

        flaskPiecesLabel.setText(text);
    }

    private void updateHealthTierLabel() {
        PotionUpgradeService.PotionTier tier = hero.getHealthPotionTier();
        int healing = hero.getHealthPotionHealing();
        healthTierLabel.setText(String.format("%s %s (Heals %d HP)",
            tier.getIcon(), tier.getDisplayName(), healing));
    }

    private void updateManaTierLabel() {
        PotionUpgradeService.PotionTier tier = hero.getManaPotionTier();
        int restore = hero.getManaPotionRestore();
        manaTierLabel.setText(String.format("%s %s (Restores %d %s)",
            tier.getIcon(), tier.getDisplayName(), restore, hero.getTipResursa()));
    }

    // ==================== ENCHANTMENT PANEL ====================

    private VBox createEnchantmentPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));

        Label titleLabel = new Label("✨ WEAPON ENCHANTMENT");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #f1c40f;");

        Label infoLabel = new Label(
                "Apply elemental enchantments to your weapons!\n" +
                "Each weapon can have ONE type of enchantment.\n" +
                "Applying the same type multiple times will increase its power."
        );
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #b0b0b0;");
        infoLabel.setWrapText(true);

        // Get weapons that can be enchanted
        List<ObiectEchipament> weapons = hero.getInventar().stream()
                .filter(ObiectEchipament::isWeapon)
                .toList();

        if (weapons.isEmpty()) {
            Label noWeaponsLabel = new Label("⚔️ Nu ai arme în inventar!");
            noWeaponsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #ff9800;");
            panel.getChildren().addAll(titleLabel, infoLabel, noWeaponsLabel);
            return panel;
        }

        // Weapon selection
        ComboBox<ObiectEchipament> weaponCombo = new ComboBox<>();
        weaponCombo.getItems().addAll(weapons);
        weaponCombo.setPromptText("Select a weapon...");
        weaponCombo.setStyle("-fx-font-size: 14px;");
        weaponCombo.setMaxWidth(Double.MAX_VALUE);

        weaponCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ObiectEchipament weapon, boolean empty) {
                super.updateItem(weapon, empty);
                if (empty || weapon == null) {
                    setText(null);
                } else {
                    setText(weapon.getNume() + " " + getWeaponEnchantmentInfo(weapon));
                }
            }
        });

        weaponCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ObiectEchipament weapon, boolean empty) {
                super.updateItem(weapon, empty);
                if (empty || weapon == null) {
                    setText("Select a weapon...");
                } else {
                    setText(weapon.getNume() + " " + getWeaponEnchantmentInfo(weapon));
                }
            }
        });

        // Enchantment scrolls display
        VBox scrollsBox = new VBox(10);
        scrollsBox.setPadding(new Insets(10));
        scrollsBox.setStyle("-fx-background-color: #0f0f1e; -fx-background-radius: 10;");

        Label scrollsTitle = new Label("📜 AVAILABLE ENCHANTMENT SCROLLS");
        scrollsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f1c40f;");

        VBox scrollsList = new VBox(5);
        updateScrollsList(scrollsList, weaponCombo);

        scrollsBox.getChildren().addAll(scrollsTitle, scrollsList);

        // Listen for weapon selection changes
        weaponCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateScrollsList(scrollsList, weaponCombo);
        });

        panel.getChildren().addAll(titleLabel, infoLabel, weaponCombo, scrollsBox);
        return panel;
    }

    private void updateScrollsList(VBox scrollsList, ComboBox<ObiectEchipament> weaponCombo) {
        scrollsList.getChildren().clear();

        // Use hero's method which returns Map
        Map<EnchantScroll.EnchantType, EnchantScroll> scrolls = hero.getAllEnchantScrolls();

        if (scrolls.isEmpty()) {
            Label noScrollsLabel = new Label("📜 Nu ai scrolluri de enchantment!");
            noScrollsLabel.setStyle("-fx-text-fill: #95a5a6;");
            scrollsList.getChildren().add(noScrollsLabel);
            return;
        }

        for (Map.Entry<EnchantScroll.EnchantType, EnchantScroll> entry : scrolls.entrySet()) {
            EnchantScroll scroll = entry.getValue();
            if (scroll.getQuantity() > 0) {
                HBox scrollCard = createEnchantScrollCard(scroll, weaponCombo);
                scrollsList.getChildren().add(scrollCard);
            }
        }
    }

    private HBox createEnchantScrollCard(EnchantScroll scroll, ComboBox<ObiectEchipament> weaponCombo) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #16213e; -fx-background-radius: 8;");

        VBox infoBox = new VBox(5);

        Label nameLabel = new Label(String.format("%s %s (x%d)",
                scroll.getType().getIcon(),
                scroll.getType().getDisplayName(),
                scroll.getQuantity()));
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label damageLabel = new Label(String.format("+%d %s damage | Level %d",
                scroll.getEnchantDamage(),
                scroll.getType().getDamageType(),
                scroll.getEnchantLevel()));
        damageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4caf50;");

        Label effectLabel = new Label("Effect: " + scroll.getType().getSpecialEffect());
        effectLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9b59b6;");

        Label costLabel = new Label("Cost: " + scroll.getApplicationCost() + " gold");
        costLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ffd54f;");

        infoBox.getChildren().addAll(nameLabel, damageLabel, effectLabel, costLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button applyButton = new Button("✨ Apply");
        applyButton.setStyle(
                "-fx-background-color: #9b59b6; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 12px; " +
                "-fx-padding: 8px 15px; " +
                "-fx-background-radius: 5;"
        );

        applyButton.setOnAction(e -> {
            ObiectEchipament selectedWeapon = weaponCombo.getValue();
            if (selectedWeapon == null) {
                DialogHelper.showError("No Weapon Selected", "Please select a weapon first!");
                return;
            }
            handleApplyEnchantment(scroll, selectedWeapon, weaponCombo);
        });

        card.getChildren().addAll(infoBox, spacer, applyButton);
        return card;
    }

    private void handleApplyEnchantment(EnchantScroll scroll, ObiectEchipament weapon,
                                       ComboBox<ObiectEchipament> weaponCombo) {
        // Check if weapon already has a different enchantment
        Map<String, Integer> currentEnchants = weapon.getAllEnchantments();
        String newEnchantType = scroll.getType().getDamageType();

        for (String existingType : currentEnchants.keySet()) {
            if (!existingType.equals(newEnchantType)) {
                String warning = String.format(
                        "⚠️ This weapon already has a %s enchantment!\n\n" +
                        "You can only have ONE enchantment type per weapon.\n" +
                        "Applying a different enchantment will REPLACE the existing one.\n\n" +
                        "Continue anyway?",
                        existingType.toUpperCase()
                );
                if (!DialogHelper.showConfirmation("Replace Enchantment?", warning)) {
                    return;
                }
            }
        }

        // Call the hero's enchantment method
        if (hero.useEnchantScroll(scroll.getType(), weapon)) {
            DialogHelper.showSuccess("Enchantment Applied!",
                    String.format("Successfully enchanted %s with %s %s!\n\n" +
                            "+%d %s damage\n" +
                            "Effect: %s",
                            weapon.getNume(),
                            scroll.getType().getIcon(),
                            scroll.getType().getDisplayName(),
                            scroll.getEnchantDamage(),
                            scroll.getType().getDamageType(),
                            scroll.getType().getSpecialEffect()));

            // Refresh the UI
            refreshScene();
        }
    }

    private String getWeaponEnchantmentInfo(ObiectEchipament weapon) {
        Map<String, Integer> enchantments = weapon.getAllEnchantments();
        if (enchantments.isEmpty()) {
            return "(No enchantment)";
        }

        StringBuilder info = new StringBuilder("(");
        enchantments.forEach((type, damage) -> {
            info.append(String.format("+%d %s ", damage, type));
        });
        info.append(")");
        return info.toString();
    }

    // ==================== REFRESH ====================

    private void refreshUI() {
        updateFlaskPiecesLabel();
        updateHealthTierLabel();
        updateManaTierLabel();
        // Recreate the scene to update all buttons
        stage.setScene(createScene());
    }

    private void refreshScene() {
        stage.setScene(createScene());
    }
}
