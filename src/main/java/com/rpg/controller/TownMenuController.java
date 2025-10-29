package com.rpg.controller;

import com.rpg.dungeon.controller.DungeonController;
import com.rpg.model.characters.Erou;
import com.rpg.model.characters.Inamic;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.service.DungeonServiceFX;
import com.rpg.service.EnemyGeneratorRomanesc;
import com.rpg.service.JewelTestUtility;
import com.rpg.service.SaveLoadServiceFX;
import com.rpg.utils.DialogHelper;
import com.rpg.utils.SpriteManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TownMenuController - Versiune Finală cu toate serviciile FX
 *
 * 🎨 SPRITE SUPPORT:
 * Place UI sprites in: resources/sprites/ui/town_menu/
 * - background.png - Town menu background
 * - header_bg.png - Header background
 * - menu_bg.png - Menu panel background
 * - button_dungeon.png / button_dungeon_hover.png
 * - button_shop.png / button_shop_hover.png
 * - button_smith.png / button_smith_hover.png
 * - button_alchemy.png / button_alchemy_hover.png
 * - button_tavern.png / button_tavern_hover.png
 * - button_character.png / button_character_hover.png
 * - button_save.png / button_save_hover.png
 * - button_options.png / button_options_hover.png
 * - button_exit.png / button_exit_hover.png
 * - panel_frame.png - Optional frame decoration
 */
public class TownMenuController {

    private Stage stage;
    private Erou hero;
    private DungeonServiceFX dungeonService;
    private EnemyGeneratorRomanesc enemyGenerator;
    private SaveLoadServiceFX saveLoadService;

    // 🎨 UI Textures
    private Image backgroundTexture;
    private Image headerBgTexture;
    private Image menuBgTexture;
    private Image panelFrameTexture;
    private Map<String, Image> buttonTextures;
    private Map<String, Image> buttonHoverTextures;

    public TownMenuController(Stage stage, Erou hero) {
        this.stage = stage;
        this.hero = hero;
        this.dungeonService = new DungeonServiceFX();
        this.enemyGenerator = new EnemyGeneratorRomanesc();
        this.saveLoadService = new SaveLoadServiceFX();
        loadTextures();
    }

    /**
     * 🎨 Load all UI textures
     */
    private void loadTextures() {
        backgroundTexture = SpriteManager.getSprite("ui/town_menu", "background");
        headerBgTexture = SpriteManager.getSprite("ui/town_menu", "header_bg");
        menuBgTexture = SpriteManager.getSprite("ui/town_menu", "menu_bg");
        panelFrameTexture = SpriteManager.getSprite("ui/town_menu", "panel_frame");

        // Load button textures
        buttonTextures = new HashMap<>();
        buttonHoverTextures = new HashMap<>();

        String[] buttonTypes = {
            "dungeon", "shop", "smith", "alchemy", "tavern",
            "character", "save", "options", "exit"
        };

        for (String type : buttonTypes) {
            buttonTextures.put(type, SpriteManager.getSprite("ui/town_menu", "button_" + type));
            buttonHoverTextures.put(type, SpriteManager.getSprite("ui/town_menu", "button_" + type + "_hover"));
        }
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
            Scene scene = new Scene(mainContainer, 900, 700);
            backgroundView.fitWidthProperty().bind(scene.widthProperty());
            backgroundView.fitHeightProperty().bind(scene.heightProperty());
        }

        BorderPane root = new BorderPane();
        root.setTop(createHeroInfo());
        root.setCenter(createMenu());

        // Only use solid color if no background texture
        if (backgroundTexture == null) {
            root.setStyle("-fx-background-color: #2c3e50;");
        } else {
            root.setStyle("-fx-background-color: transparent;");
        }

        mainContainer.getChildren().add(root);

        Scene scene = new Scene(mainContainer, 900, 700);
        return scene;
    }

    /**
     * Header cu informații erou
     */
    private VBox createHeroInfo() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(15));
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #1a1a2e;");

        Label titleLabel = new Label("🏛️ ORAȘUL BUCUREȘTI 🏛️");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #f39c12;");

        // Info erou
        HBox heroInfo = new HBox(20);
        heroInfo.setAlignment(Pos.CENTER);

        Label heroLabel = new Label("👤 " + hero.getNume());
        heroLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label levelLabel = new Label("⭐ Nivel " + hero.getNivel());
        levelLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        Label hpLabel = new Label("❤️ HP: " + hero.getViata() + "/" + hero.getViataMaxima());
        hpLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 16px;");

        Label goldLabel = new Label("💰 " + hero.getGold() + " gold");
        goldLabel.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 16px;");

        Label tokensLabel = new Label("🎫 " + hero.getDungeonTickets() + " Dungeon Tokens");
        tokensLabel.setStyle("-fx-text-fill: #3498db; -fx-font-size: 16px;");

        heroInfo.getChildren().addAll(heroLabel, levelLabel, hpLabel, goldLabel, tokensLabel);

        box.getChildren().addAll(titleLabel, heroInfo);
        return box;
    }

    /**
     * Meniul principal cu butoane
     */
    private VBox createMenu() {
        VBox menu = new VBox(12);
        menu.setPadding(new Insets(30));
        menu.setAlignment(Pos.CENTER);
        menu.setStyle("-fx-background-color: #34495e;");

        Label menuTitle = new Label("📍 Ce vrei să faci?");
        menuTitle.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");

        // 🗺️ DUNGEON (MAIN BATTLE SYSTEM)
        Button dungeonBtn = createMenuButton("🗺️ Enter the Dungeon", "#e74c3c");
        dungeonBtn.setOnAction(e -> handleDungeonMain());

        // 🏛️ DUNGEON UPGRADES SHOP
        Button dungeonUpgradesBtn = createMenuButton("🏛️ Dungeon Upgrades Shop", "#8e44ad");
        dungeonUpgradesBtn.setOnAction(e -> handleDungeonUpgrades());

        // 🛍️ ADVANCED SHOP
        Button shopBtn = createMenuButton("🛍️ Advanced Shop", "#f39c12");
        shopBtn.setOnAction(e -> {
            ShopAdvancedController shopController = new ShopAdvancedController(stage, hero);
            stage.setScene(shopController.createScene());
        });




        // 🔨 SMITH (Enhancement only)
        Button smithBtn = createMenuButton("🔨 Fierăria", "#e67e22");
        smithBtn.setOnAction(e -> {
            SmithControllerFX smithController = new SmithControllerFX(stage, hero);
            stage.setScene(smithController.createScene());
        });

        // 🧙‍♂️ ALCHEMY WORKSHOP
        Button alchemyBtn = createMenuButton("🧙‍♂️ Alchemy Workshop", "#9b59b6");
        alchemyBtn.setOnAction(e -> {
            AlchemyWorkshopController alchemyController = new AlchemyWorkshopController(stage, hero, () -> returnToTown());
            stage.setScene(alchemyController.createScene());
        });

        // 🍺 TAVERN
        Button tavernBtn = createMenuButton("🍺 Taverna", "#8e44ad");
        tavernBtn.setOnAction(e -> {
            TavernControllerFX tavernController = new TavernControllerFX(stage, hero);
            stage.setScene(tavernController.createScene());
        });

        // 👤 CHARACTER (Consolidated: Sheet, Inventory, Talents, Stats)
        Button characterBtn = createMenuButton("👤 Character", "#9b59b6");
        characterBtn.setOnAction(e -> {
            CharacterMenuController characterController = new CharacterMenuController(stage, hero, () -> returnToTown());
            stage.setScene(characterController.createScene());
        });

        // 💾 SAVE
        Button saveBtn = createMenuButton("💾 Salvează Joc", "#16a085");
        saveBtn.setOnAction(e -> handleSave());

        // ⚙️ OPTIONS
        Button optionsBtn = createMenuButton("⚙️ Opțiuni", "#7f8c8d");
        optionsBtn.setOnAction(e -> openOptions());

        // 💎 JEWEL TEST (DEBUG BUTTON)
        Button jewelTestBtn = createMenuButton("💎 [TEST] Add Test Jewels", "#f1c40f");
        jewelTestBtn.setOnAction(e -> handleJewelTest());

        // 🔙 EXIT TO MAIN MENU
        Button exitBtn = createMenuButton("🔙 Meniu Principal", "#c0392b");
        exitBtn.setOnAction(e -> handleExit());

        menu.getChildren().addAll(
                menuTitle,
                dungeonBtn, dungeonUpgradesBtn, shopBtn, smithBtn, alchemyBtn, tavernBtn,
                characterBtn, saveBtn, optionsBtn,
                jewelTestBtn, exitBtn
        );

        return menu;
    }

    /**
     * 🎨 Create menu button with texture support
     * @param text Button text
     * @param color Fallback color if no texture
     * @param textureKey Key for button texture (e.g., "dungeon", "shop")
     */
    private Button createMenuButton(String text, String color, String textureKey) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(50);

        // Check for texture
        Image normalTexture = buttonTextures.get(textureKey);
        Image hoverTexture = buttonHoverTextures.get(textureKey);

        if (normalTexture != null && hoverTexture != null) {
            // Use texture-based styling
            String normalStyle = String.format(
                    "-fx-font-size: 16px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-image: url('%s'); " +
                    "-fx-background-size: cover; " +
                    "-fx-background-repeat: no-repeat; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0; " +
                    "-fx-effect: dropshadow(gaussian, black, 3, 0, 0, 2);",
                    normalTexture.getUrl()
            );

            String hoverStyle = String.format(
                    "-fx-font-size: 16px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-image: url('%s'); " +
                    "-fx-background-size: cover; " +
                    "-fx-background-repeat: no-repeat; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0; " +
                    "-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.3), 5, 0, 0, 0);",
                    hoverTexture.getUrl()
            );

            btn.setStyle(normalStyle);
            btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
            btn.setOnMouseExited(e -> btn.setStyle(normalStyle));

        } else {
            // Fallback to color-based styling
            String normalStyle =
                    "-fx-font-size: 16px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-color: " + color + "; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 10; " +
                    "-fx-cursor: hand;";

            String hoverStyle =
                    "-fx-font-size: 16px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-color: derive(" + color + ", 20%); " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 10; " +
                    "-fx-cursor: hand;";

            btn.setStyle(normalStyle);
            btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
            btn.setOnMouseExited(e -> btn.setStyle(normalStyle));
        }

        return btn;
    }

    /**
     * Legacy method for backward compatibility
     */
    private Button createMenuButton(String text, String color) {
        // Extract button type from text for texture lookup
        String textureKey = extractTextureKey(text);
        return createMenuButton(text, color, textureKey);
    }

    /**
     * Extract texture key from button text
     */
    private String extractTextureKey(String text) {
        String lowerText = text.toLowerCase();
        if (lowerText.contains("dungeon")) return "dungeon";
        if (lowerText.contains("shop")) return "shop";
        if (lowerText.contains("fier") || lowerText.contains("smith")) return "smith";
        if (lowerText.contains("alchemy") || lowerText.contains("alchim")) return "alchemy";
        if (lowerText.contains("taver")) return "tavern";
        if (lowerText.contains("character") || lowerText.contains("personaj")) return "character";
        if (lowerText.contains("salv") || lowerText.contains("save")) return "save";
        if (lowerText.contains("op") || lowerText.contains("settings")) return "options";
        if (lowerText.contains("exit") || lowerText.contains("meniu") || lowerText.contains("înapoi")) return "exit";
        return "default";
    }

    // ==================== HANDLERS ====================

//    private void handleDungeon() {
//        // TODO: Creează un DungeonStartController pentru alegerea nivelului
//        // Pentru moment, generăm un inamic simplu
//        EnemyGeneratorRomanesc generator = new EnemyGeneratorRomanesc();
//        Inamic enemy = enemyGenerator.genereazaInamici(hero.getNivel()).get(1);
//        BattleControllerFX battleController = new BattleControllerFX(stage, hero, enemy);
//        stage.setScene(battleController.createScene());
//    }

    // 🆕 ADAUGĂ BUTONUL DUNGEON
    private Button createDungeonButton() {
        Button dungeonBtn = createMenuButton("🏰 EXPLOREAZĂ DUNGEON", "#8e44ad");
        dungeonBtn.setOnAction(e -> startDungeonExploration());
        return dungeonBtn;
    }

    // 🆕 ADAUGĂ METODA PENTRU DUNGEON
    private void startDungeonExploration() {
        if (hero.getViata() <= hero.getViataMaxima() * 0.2) {
            DialogHelper.showWarning("HP Scăzut",
                    "Ai prea puțin HP pentru dungeon!\\n" +
                            "Vindecă-te la taverna sau folosește poțiuni înainte să intri.");
            return;
        }

        boolean confirm = DialogHelper.showConfirmation(
                "Explorare Dungeon",
                "Vrei să intri în dungeon?\\n\\n" +
                        "🏰 Vei lupta cu inamici din ce în ce mai puternici\\n" +
                        "💰 Recompensele cresc cu depth-ul\\n" +
                        "👑 Boss la fiecare 5 nivele\\n" +
                        "⚠️  Poți ieși după fiecare victorie"
        );

        if (confirm) {
            // Pornește dungeon exploration cu primul inamic
            BattleControllerFX battleController = new BattleControllerFX(stage, hero, (com.rpg.model.characters.Inamic) null, true, 1);
            stage.setScene(battleController.createScene());
        }
    }


    /**
     * 🗺️ Main Dungeon Entry - Now the primary battle system
     */
    private void handleDungeonMain() {
        // Show depth selection dialog
        com.rpg.dungeon.model.DungeonProgression progression = hero.getDungeonProgression();
        final int maxDepth = Math.min(10, Math.max(1, progression.getDeepestDepthReached() + 1));

        // Build depth selection message
        StringBuilder message = new StringBuilder();
        message.append("Select dungeon depth to enter:\n\n");
        message.append("Deepest cleared: ").append(progression.getDeepestDepthReached()).append("\n");
        message.append("Available depths: 1-").append(maxDepth).append("\n\n");
        message.append("💡 Higher depths have:\n");
        message.append("  • Stronger enemies\n");
        message.append("  • Better loot\n");
        message.append("  • More tokens\n\n");
        message.append("Enter depth (1-").append(maxDepth).append("):");

        // Simple depth selection using TextInputDialog
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("1");
        dialog.setTitle("Select Dungeon Depth");
        dialog.setHeaderText("🗺️ Enter the Dungeon");
        dialog.setContentText(message.toString());

        dialog.showAndWait().ifPresent(input -> {
            try {
                int selectedDepth = Integer.parseInt(input.trim());
                if (selectedDepth < 1 || selectedDepth > maxDepth) {
                    DialogHelper.showError("Invalid Depth", "Please select a depth between 1 and " + maxDepth);
                    return;
                }

                // Launch dungeon at selected depth
                com.rpg.dungeon.controller.DungeonController dungeonController =
                    new com.rpg.dungeon.controller.DungeonController(
                        stage,
                        hero,
                        selectedDepth,
                        () -> returnToTown()
                    );
                stage.setScene(dungeonController.createScene());
            } catch (NumberFormatException e) {
                DialogHelper.showError("Invalid Input", "Please enter a valid number!");
            }
        });
    }

    private void handleSave() {
        SaveLoadControllerFX saveController = new SaveLoadControllerFX(stage, hero);
        stage.setScene(saveController.createScene());
    }

    private void openOptions() {
        OptionsController optionsController = new OptionsController(stage, () -> returnToTown());
        stage.setScene(optionsController.createScene());
    }

    private void showStatsDialog() {
        StringBuilder stats = new StringBuilder();
        stats.append("═══════════════════════════════\n");
        stats.append("   📊 STATISTICI COMPLETE\n");
        stats.append("═══════════════════════════════\n\n");

        stats.append("👤 Nume: ").append(hero.getNume()).append("\n");
        stats.append("⭐ Nivel: ").append(hero.getNivel()).append("\n");
        stats.append("📊 Experiență: ").append(hero.getExperienta()).append("/").append(hero.getExpNecesara()).append("\n\n");

        stats.append("❤️  HP: ").append(hero.getViata()).append("/").append(hero.getViataMaxima()).append("\n");
        stats.append("💙 ").append(hero.getTipResursa()).append(": ")
                .append(hero.getResursaCurenta()).append("/").append(hero.getResursaMaxima()).append("\n\n");

        // ✅ WOW STYLE - Base + Equipment = Total
        stats.append("🔥 STATISTICI PRINCIPALE:\n");
        stats.append(String.format("💪 Strength: %d + %d = %d\n",
                hero.getStrength(),
                hero.getStrengthTotal() - hero.getStrength(),
                hero.getStrengthTotal()));

        stats.append(String.format("🎯 Dexterity: %d + %d = %d\n",
                hero.getDexterity(),
                hero.getDexterityTotal() - hero.getDexterity(),
                hero.getDexterityTotal()));

        stats.append(String.format("🧠 Intelligence: %d + %d = %d\n",
                hero.getIntelligence(),
                hero.getIntelligenceTotal() - hero.getIntelligence(),
                hero.getIntelligenceTotal()));

        stats.append(String.format("🛡️ Defense: %d + %d = %d\n",
                hero.getDefense(),
                hero.getDefenseTotal() - hero.getDefense(),
                hero.getDefenseTotal()));

        // ✅ BONUSURI AVANSATE din echipament
        stats.append("\n✨ BONUSURI SPECIALE:\n");
        Map<String, Integer> allBonuses = getHeroAllBonuses(hero);

        if (allBonuses.isEmpty()) {
            stats.append("   • Niciun bonus special activ\n");
        } else {
            allBonuses.forEach((stat, bonus) -> {
                if (!stat.equals("strength") && !stat.equals("dexterity") &&
                        !stat.equals("intelligence") && !stat.equals("defense")) {
                    String icon = getStatIcon(stat);
                    String name = formatStatName(stat);
                    stats.append(String.format("   %s +%d %s\n", icon, bonus, name));
                }
            });
        }

        // ✅ ȘANSE DE COMBAT
        stats.append("\n⚔️ COMBAT STATS:\n");
        stats.append(String.format("🎯 Hit Chance: %.1f%%\n", hero.getHitChance()));
        stats.append(String.format("💥 Critical Chance: %.1f%%\n", hero.getCritChanceTotal()));
        stats.append(String.format("💨 Dodge Chance: %.1f%%\n", hero.getDodgeChanceTotal()));

        stats.append("\n💰 RESURSE:\n");
        stats.append("💰 Gold: ").append(hero.getGold()).append("\n");
        stats.append("🔧 Scrap: ").append(hero.getScrap()).append("\n");
        stats.append("🎫 Dungeon Tokens: ").append(hero.getDungeonTickets()).append("\n");

        if (hero.getStatPointsToAllocate() > 0) {
            stats.append("\n⭐ Stat Points disponibile: ").append(hero.getStatPointsToAllocate()).append("\n");
        }

        DialogHelper.showInfo("Statistici Complete", stats.toString());
    }

    /**
     * ✨ Helper pentru toate bonusurile active ale eroului
     */
    private Map<String, Integer> getHeroAllBonuses(Erou hero) {
        Map<String, Integer> allBonuses = new HashMap<>();

        // Bonusuri din echipament
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

    /**
     * 🎨 Iconițe pentru statistici
     */
    private String getStatIcon(String stat) {
        return switch (stat.toLowerCase()) {
            case "damage" -> "⚔️";
            case "defense" -> "🛡️";
            case "health" -> "❤️";
            case "strength" -> "💪";
            case "dexterity" -> "🎯";
            case "intelligence" -> "🧠";
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
            case "viata" -> "❤️";
            case "mana" -> "💙";
            case "block_chance" -> "🛡️";
            default -> "✨";
        };
    }

    /**
     * 🏷️ Nume formatate pentru statistici
     */
    private String formatStatName(String stat) {
        return switch (stat.toLowerCase()) {
            case "damage" -> "Damage";
            case "defense" -> "Defense";
            case "health", "viata" -> "Health";
            case "strength" -> "Strength";
            case "dexterity" -> "Dexterity";
            case "intelligence" -> "Intelligence";
            case "crit_chance" -> "Critical Chance %";
            case "hit_chance" -> "Hit Chance %";
            case "dodge_chance" -> "Dodge Chance %";
            case "damage_reduction" -> "Damage Reduction %";
            case "gold_find" -> "Gold Find %";
            case "lifesteal" -> "Lifesteal %";
            case "mana_steal" -> "Mana Steal %";
            case "elemental_damage" -> "Elemental Damage";
            case "fire_resistance" -> "Fire Resistance %";
            case "ice_resistance" -> "Ice Resistance %";
            case "damage_bonus" -> "Damage Bonus";
            case "attack_bonus" -> "Attack Bonus";
            case "mana" -> "Mana";
            case "block_chance" -> "Block Chance %";
            default -> stat;
        };
    }


    /**
     * 💎 DEBUG: Quick jewel testing
     */
    private void handleJewelTest() {
        StringBuilder testInfo = new StringBuilder();
        testInfo.append("═══════════════════════════════════\n");
        testInfo.append("   💎 JEWEL TEST UTILITY\n");
        testInfo.append("═══════════════════════════════════\n\n");
        testInfo.append("This will:\n");
        testInfo.append("✅ Add 5 test jewels to inventory\n");
        testInfo.append("✅ Add 5 passive points for socketing\n");
        testInfo.append("✅ Show jewel inventory stats\n\n");
        testInfo.append("Current jewels: ").append(hero.getJewelCount()).append("\n");
        testInfo.append("Passive points: ").append(hero.getPassivePoints()).append("\n\n");
        testInfo.append("Continue?");

        if (DialogHelper.showConfirmation("Jewel Test Utility", testInfo.toString())) {
            // Add test jewels
            JewelTestUtility.addTestJewelsToHero(hero);

            // Add passive points for testing talent tree
            hero.increasePassivePoints(5);

            // Build result message
            StringBuilder result = new StringBuilder();
            result.append("═══════════════════════════════════\n");
            result.append("   ✅ TEST JEWELS ADDED!\n");
            result.append("═══════════════════════════════════\n\n");
            result.append("📊 NEW STATS:\n");
            result.append("💎 Total Jewels: ").append(hero.getJewelCount()).append("\n");
            result.append("✅ Available (Unsocketed): ").append(hero.getAvailableJewels().size()).append("\n");
            result.append("💎 Socketed: ").append(hero.getSocketedJewels().size()).append("\n");
            result.append("🌳 Passive Points: ").append(hero.getPassivePoints()).append("\n\n");

            result.append("📝 JEWELS ADDED:\n");
            result.append("🔴 Test Crimson Jewel (STR)\n");
            result.append("🟢 Test Viridian Jewel (DEX)\n");
            result.append("🔵 Test Cobalt Jewel (INT)\n");
            result.append("⚪ Test Prismatic Jewel (Balanced)\n");
            result.append("🟡 Blood of the Immortal (Legendary)\n\n");

            result.append("💡 NEXT STEPS:\n");
            result.append("1. Open Talent Tree (🌳)\n");
            result.append("2. Allocate jewel sockets (purple nodes)\n");
            result.append("3. Right-click to insert jewels\n");
            result.append("4. Check stats with jewel bonuses\n");

            DialogHelper.showInfo("Test Complete", result.toString());

            // Refresh the town menu to show updated stats
            stage.setScene(createScene());
        }
    }

    private void handleExit() {
        if (DialogHelper.showConfirmation("Confirmare Ieșire",
                "Vrei să te întorci la meniul principal?\n\n" +
                        "Asigură-te că ai salvat progresul!")) {

            MainMenuController mainMenu = new MainMenuController(stage);
            stage.setScene(mainMenu.createScene());
        }
    }


    /**
     * Returns to town menu after dungeon
     */
    private void returnToTown() {
        stage.setScene(createScene());
    }

    /**
     * 🏛️ Handler pentru Dungeon Upgrades Shop
     */
    private void handleDungeonUpgrades() {
        com.rpg.dungeon.controller.DungeonUpgradeShopController upgradeShop =
            new com.rpg.dungeon.controller.DungeonUpgradeShopController(
                stage,
                hero,
                () -> returnToTown()
            );
        stage.setScene(upgradeShop.createScene());
    }
}