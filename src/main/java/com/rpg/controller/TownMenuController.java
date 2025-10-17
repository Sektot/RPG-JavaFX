package com.rpg.controller;

import com.rpg.model.characters.Erou;
import com.rpg.model.characters.Inamic;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.service.DungeonServiceFX;
import com.rpg.service.EnemyGeneratorRomanesc;
import com.rpg.service.SaveLoadServiceFX;
import com.rpg.utils.DialogHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TownMenuController - Versiune Finală cu toate serviciile FX
 */
public class TownMenuController {

    private Stage stage;
    private Erou hero;
    private DungeonServiceFX dungeonService;
    private EnemyGeneratorRomanesc enemyGenerator;
    private SaveLoadServiceFX saveLoadService;

    public TownMenuController(Stage stage, Erou hero) {
        this.stage = stage;
        this.hero = hero;
        this.dungeonService = new DungeonServiceFX();
        this.enemyGenerator = new EnemyGeneratorRomanesc();
        this.saveLoadService = new SaveLoadServiceFX();
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setTop(createHeroInfo());
        root.setCenter(createMenu());
        root.setStyle("-fx-background-color: #2c3e50;");
        return new Scene(root, 900, 700);
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

        heroInfo.getChildren().addAll(heroLabel, levelLabel, hpLabel, goldLabel);

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

        // ⚔️ DUNGEON
        Button dungeonBtn = createMenuButton("⚔️ Gara de Nord (Dungeon)", "#e74c3c");
        dungeonBtn.setOnAction(e -> handleDungeon());

        // 🪙 SHOP
        Button shopBtn = createMenuButton("🪙 Magazin", "#27ae60");
        shopBtn.setOnAction(e -> {
            ShopController shopController = new ShopController(stage, hero);
            stage.setScene(shopController.createScene());
        });

        // 🔨 SMITH & TRAINER
        Button smithBtn = createMenuButton("🔨 Fierăria & Training", "#e67e22");
        smithBtn.setOnAction(e -> {
            SmithControllerFX smithController = new SmithControllerFX(stage, hero);
            stage.setScene(smithController.createScene());
        });

        // 🍺 TAVERN
        Button tavernBtn = createMenuButton("🍺 Taverna", "#8e44ad");
        tavernBtn.setOnAction(e -> {
            TavernControllerFX tavernController = new TavernControllerFX(stage, hero);
            stage.setScene(tavernController.createScene());
        });

        // 🎮 CHARACTER SHEET
        Button characterBtn = createMenuButton("🎮 Character Sheet", "#9b59b6");
        characterBtn.setOnAction(e -> {
            CharacterSheetController characterController = new CharacterSheetController(stage, hero);
            stage.setScene(characterController.createScene());
        });


        // 🎒 INVENTORY
        Button inventoryBtn = createMenuButton("🎒 Inventar", "#3498db");
        inventoryBtn.setOnAction(e -> {
            InventoryControllerFX inventoryController = new InventoryControllerFX(stage, hero);
            stage.setScene(inventoryController.createScene());
        });

        // 💾 SAVE
        Button saveBtn = createMenuButton("💾 Salvează Joc", "#16a085");
        saveBtn.setOnAction(e -> handleSave());

        // 📊 STATS
        Button statsBtn = createMenuButton("📊 Statistici Complete", "#95a5a6");
        statsBtn.setOnAction(e -> {
            // TODO: Creează un StatsController dedicat sau afișează în dialog
            showStatsDialog();
        });

        // 🔙 EXIT TO MAIN MENU
        Button exitBtn = createMenuButton("🔙 Meniu Principal", "#c0392b");
        exitBtn.setOnAction(e -> handleExit());

        menu.getChildren().addAll(
                menuTitle,
                dungeonBtn, shopBtn, smithBtn, tavernBtn,
                inventoryBtn, characterBtn, saveBtn, statsBtn, exitBtn  // ✅ Adaugă characterBtn
        );

        return menu;
    }

    private Button createMenuButton(String text, String color) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(50);
        btn.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e ->
                btn.setStyle(
                        "-fx-font-size: 16px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-color: derive(" + color + ", 20%); " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 10; " +
                                "-fx-cursor: hand;"
                )
        );

        btn.setOnMouseExited(e ->
                btn.setStyle(
                        "-fx-font-size: 16px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-color: " + color + "; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 10; " +
                                "-fx-cursor: hand;"
                )
        );

        return btn;
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
            BattleControllerFX battleController = new BattleControllerFX(stage, hero, null, true, 1);
            stage.setScene(battleController.createScene());
        }
    }


    private void handleDungeon() {
        // ✅ ADAUGĂ GENERAREA DE INAMICI:
        EnemyGeneratorRomanesc generator = new EnemyGeneratorRomanesc();

        // Generează inamici pentru nivelul eroului
        List<Inamic> enemies = generator.genereazaInamici(hero.getNivel());

        if (enemies.isEmpty()) {
            DialogHelper.showWarning("Nu s-au putut genera inamici!","hopa");
            return;
        }

        // Selectează primul inamic pentru luptă
        Inamic currentEnemy = enemies.get(0);

        // Lansează interfața de luptă
        BattleControllerFX battleController = new BattleControllerFX(stage, hero, currentEnemy,true,1);
        stage.setScene(battleController.createScene());
    }

    private void handleSave() {
        SaveLoadControllerFX saveController = new SaveLoadControllerFX(stage, hero);
        stage.setScene(saveController.createScene());
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
        stats.append("🔮 Shards: ").append(hero.getShards()).append("\n");

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


    private void handleExit() {
        if (DialogHelper.showConfirmation("Confirmare Ieșire",
                "Vrei să te întorci la meniul principal?\n\n" +
                        "Asigură-te că ai salvat progresul!")) {

            MainMenuController mainMenu = new MainMenuController(stage);
            stage.setScene(mainMenu.createScene());
        }
    }
}