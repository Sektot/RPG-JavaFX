package com.rpg.controller;

import com.rpg.model.characters.Erou;
import com.rpg.model.characters.Inamic;
import com.rpg.service.BattleServiceFX;
import com.rpg.service.EnemyGeneratorRomanesc;
import com.rpg.service.dto.AbilityDTO;
import com.rpg.service.dto.BattleInitDTO;
import com.rpg.utils.DialogHelper;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BattleControllerFX - Controller îmbunătățit pentru lupte
 */
public class BattleControllerFX {

    private Stage stage;
    private Erou hero;
    private Inamic enemy;
    //private Inamic currentEnemy;
    private BattleServiceFX battleService;

    // 🆕 ADAUGĂ ACESTEA
    private final boolean inDungeon;
    private int dungeonDepth = 1;

    // UI Components - Hero
    private Label heroNameLabel;
    private Label heroHPLabel;
    private ProgressBar heroHPBar;
    private Label heroResourceLabel;
    private ProgressBar heroResourceBar;

    // UI Components - Enemy
    private Label enemyNameLabel;
    private Label enemyHPLabel;
    private ProgressBar enemyHPBar;

    // Battle Log
    private TextArea battleLog;

    // Action Buttons
    private Button attackButton;
    private Button fleeButton;
    private VBox abilityButtonsPanel;
    private VBox potionButtonsPanel;

    // 🆕 CONSTRUCTOR NOU cu support dungeon
    public BattleControllerFX(Stage stage, Erou hero, Inamic enemy, boolean inDungeon, int depth) {
        this.stage = stage;
        this.hero = hero;
        this.enemy = enemy;
        this.inDungeon = inDungeon;
        this.dungeonDepth = depth;
        this.battleService = new BattleServiceFX();
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setTop(createHeader());
        root.setCenter(createBattleArea());
        root.setBottom(createActionPanel());

        root.setStyle("-fx-background-color: #0f0f1e;");

        // Inițializează bătălia
        initializeBattle();

        return new Scene(root, 1200, 800);
    }

    /**
     * Header cu titlul
     */
    private VBox createHeader() {
        VBox header = new VBox(5);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #1a1a2e;");

        // 🔄 MODIFICĂ title să includă depth
        String titleText = "⚔️ LUPTĂ: " + hero.getNume() + " VS " + enemy.getNume();
        if (inDungeon) {
            titleText += " | 🏰 Depth: " + dungeonDepth;
        }
        titleText += " ⚔️";

        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        if (enemy.isBoss()) {
            Label bossLabel = new Label("💀 BOSS BATTLE 💀");
            bossLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ff6b6b;");
            header.getChildren().addAll(title, bossLabel);
        } else {
            header.getChildren().add(title);
        }

        return header;
    }


    /**
     * Zona de luptă - Hero, Log, Enemy
     */
    private HBox createBattleArea() {
        HBox battleArea = new HBox(20);
        battleArea.setPadding(new Insets(20));
        battleArea.setAlignment(Pos.CENTER);

        VBox heroPanel = createHeroPanel();
        VBox logPanel = createLogPanel();
        VBox enemyPanel = createEnemyPanel();

        battleArea.getChildren().addAll(heroPanel, logPanel, enemyPanel);

        return battleArea;
    }

    /**
     * Panel erou
     */
    private VBox createHeroPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setAlignment(Pos.CENTER);
        panel.setStyle(
                "-fx-background-color: #16213e; " +
                        "-fx-background-radius: 15; " +
                        "-fx-border-color: #27ae60; " +
                        "-fx-border-width: 3; " +
                        "-fx-border-radius: 15;"
        );
        panel.setPrefWidth(300);

        heroNameLabel = new Label(hero.getNume());
        heroNameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        Label hpTextLabel = new Label("❤️ HP");
        hpTextLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        heroHPBar = new ProgressBar(1.0);
        heroHPBar.setPrefWidth(250);
        heroHPBar.setPrefHeight(25);
        heroHPBar.setStyle("-fx-accent: #e74c3c;");

        heroHPLabel = new Label(hero.getViata() + " / " + hero.getViataMaxima());
        heroHPLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

        Label resourceTextLabel = new Label("💙 " + hero.getTipResursa());
        resourceTextLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        heroResourceBar = new ProgressBar(1.0);
        heroResourceBar.setPrefWidth(250);
        heroResourceBar.setPrefHeight(20);
        heroResourceBar.setStyle("-fx-accent: #3498db;");

        heroResourceLabel = new Label(hero.getResursaCurenta() + " / " + hero.getResursaMaxima());
        heroResourceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        panel.getChildren().addAll(
                heroNameLabel,
                hpTextLabel, heroHPBar, heroHPLabel,
                resourceTextLabel, heroResourceBar, heroResourceLabel
        );

        return panel;
    }

    /**
     * Panel inamic
     */
    private VBox createEnemyPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setAlignment(Pos.CENTER);
        panel.setStyle(
                "-fx-background-color: #16213e; " +
                        "-fx-background-radius: 15; " +
                        "-fx-border-color: #e74c3c; " +
                        "-fx-border-width: 3; " +
                        "-fx-border-radius: 15;"
        );
        panel.setPrefWidth(300);

        enemyNameLabel = new Label(enemy.getNume());
        enemyNameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        if (enemy.isBoss()) {
            Label bossIndicator = new Label("💀 BOSS");
            bossIndicator.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
            panel.getChildren().add(bossIndicator);
        }

        Label hpTextLabel = new Label("❤️ HP");
        hpTextLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        enemyHPBar = new ProgressBar(1.0);
        enemyHPBar.setPrefWidth(250);
        enemyHPBar.setPrefHeight(25);
        enemyHPBar.setStyle("-fx-accent: #e74c3c;");

        enemyHPLabel = new Label(enemy.getViata() + " / " + enemy.getViataMaxima());
        enemyHPLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

        panel.getChildren().addAll(
                enemyNameLabel,
                hpTextLabel, enemyHPBar, enemyHPLabel
        );

        return panel;
    }

    /**
     * Panel log
     */
    private VBox createLogPanel() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");

        Label logLabel = new Label("📜 Battle Log");
        logLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

        battleLog = new TextArea();
        battleLog.setEditable(false);
        battleLog.setWrapText(true);
        battleLog.setStyle(
                "-fx-control-inner-background: #0f0f1e; " +
                        "-fx-text-fill: #f1f1f1; " +
                        "-fx-font-family: 'Courier New'; " +
                        "-fx-font-size: 13px;"
        );
        battleLog.setPrefWidth(400);
        battleLog.setPrefHeight(400);
        VBox.setVgrow(battleLog, Priority.ALWAYS);

        panel.getChildren().addAll(logLabel, battleLog);
        return panel;
    }

    /**
     * Panel acțiuni
     */
    private VBox createActionPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #1a1a2e;");

        // Butoane principale
        HBox mainButtons = new HBox(10);
        mainButtons.setAlignment(Pos.CENTER);

        attackButton = createActionButton("⚔️ ATAC NORMAL", "#27ae60");
        attackButton.setOnAction(e -> handleNormalAttack());

        fleeButton = createActionButton("🏃 FUGI", "#e67e22");
        fleeButton.setOnAction(e -> handleFlee());

        mainButtons.getChildren().addAll(attackButton, fleeButton);

        // Panel abilități
        Label abilitiesLabel = new Label("✨ Abilități:");
        abilitiesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold;");

        abilityButtonsPanel = new VBox(5);
        abilityButtonsPanel.setAlignment(Pos.CENTER);

        // Panel poțiuni
        Label potionsLabel = new Label("🧪 Poțiuni:");
        potionsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold;");

        potionButtonsPanel = new VBox(5);
        potionButtonsPanel.setAlignment(Pos.CENTER);

        HBox bottomPanels = new HBox(30);
        bottomPanels.setAlignment(Pos.CENTER);

        VBox abilitiesContainer = new VBox(5, abilitiesLabel, abilityButtonsPanel);
        abilitiesContainer.setAlignment(Pos.CENTER);

        VBox potionsContainer = new VBox(5, potionsLabel, potionButtonsPanel);
        potionsContainer.setAlignment(Pos.CENTER);

        bottomPanels.getChildren().addAll(abilitiesContainer, potionsContainer);

        panel.getChildren().addAll(mainButtons, bottomPanels);
        return panel;
    }

    /**
     * Creează un buton de acțiune
     */
    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 12px 30px; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e ->
                btn.setStyle(
                        "-fx-font-size: 16px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-color: derive(" + color + ", 20%); " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 12px 30px; " +
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
                                "-fx-padding: 12px 30px; " +
                                "-fx-background-radius: 10; " +
                                "-fx-cursor: hand;"
                )
        );

        return btn;
    }

    // ==================== BATTLE LOGIC ====================

    private void initializeBattle() {
        // 🆕 GENEREAZĂ INAMIC DACĂ LIPSEȘTE (pentru dungeon)
        if (this.enemy == null) {
            EnemyGeneratorRomanesc generator = new EnemyGeneratorRomanesc();
            int scaledLevel = hero.getNivel() + Math.max(0, (dungeonDepth - 1) / 3);

            if (dungeonDepth % 5 == 0) {
                this.enemy = generator.genereazaBoss(scaledLevel);
            } else {
                this.enemy = generator.genereazaInamicNormal(scaledLevel);
            }

            System.out.printf("🏰 GENERATED ENEMY: Depth %d, Level %d -> %s \n",
                    dungeonDepth, scaledLevel, enemy.getNume());
        }

        // Restul codului rămâne la fel...
        BattleInitDTO initData = battleService.initializeBattle(hero, enemy);
        updateUI(new AbilityDTO.BattleStateDTO(
                initData.getHeroHP(),
                initData.getHeroMaxHP(),
                initData.getHeroResource(),
                initData.getHeroMaxResource(),
                initData.getEnemyHP(),
                initData.getEnemyMaxHP(),
                initData.getAbilities()
        ));

        addToLog("⚔️ Bătălia începe!");
        addToLog(hero.getNume() + " vs " + enemy.getNume());

        if (inDungeon) {
            addToLog("🏰 Dungeon Depth: " + dungeonDepth);
        }

        if (enemy.isBoss()) {
            addToLog("💀 BOSS BATTLE! Pregătește-te!");
        }
        addToLog("━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private void handleNormalAttack() {
        disableAllButtons();

        AbilityDTO.BattleTurnResultDTO result = battleService.executeNormalAttack(hero, enemy);

        addToLog(result.getLog());

        if (result.isBattleOver()) {
            handleBattleEnd(result);
        } else {
            updateUI(result.getCurrentState());
            enableAllButtons();
        }
    }

    private void handleFlee() {
        if (enemy.isBoss()) {
            addToLog("❌ Nu poți fugi de la un BOSS!");
            return;
        }

        if (DialogHelper.showConfirmation("Fugă", "Ești sigur că vrei să fugi din luptă?")) {
            disableAllButtons();

            AbilityDTO.BattleTurnResultDTO result = battleService.attemptFlee(hero, enemy);
            addToLog(result.getLog());

            if (result.hasFled()) {
                DialogHelper.showInfo("Fugă Reușită", "Ai scăpat din luptă!");
                returnToTown();
            } else if (result.isBattleOver()) {
                handleBattleEnd(result);
            } else {
                updateUI(result.getCurrentState());
                enableAllButtons();
            }
        }
    }

    private void handleAbilityUse(String abilityName) {
        disableAllButtons();

        AbilityDTO.BattleTurnResultDTO result = battleService.executeAbility(hero, enemy, abilityName);

        if (!result.isSuccess()) {
            // Abilitatea nu a putut fi folosită
            addToLog(result.getLog());
            enableAllButtons();
            return;
        }

        addToLog(result.getLog());

        if (result.isBattleOver()) {
            handleBattleEnd(result);
        } else {
            updateUI(result.getCurrentState());
            enableAllButtons();
        }
    }

    private void handlePotionUse(int healAmount) {
        // 🔄 MIGRARE AUTOMATĂ (păstrează din fix-ul anterior)
        if (hero.getInventar().getHealthPotions().isEmpty() && hero.getHealthPotions() > 0) {
            hero.getInventar().getHealthPotions().put(healAmount, hero.getHealthPotions());
            System.out.printf("🔄 AUTO-MIGRATED: %d poțiuni HP → Map\\n", hero.getHealthPotions());
        }

        // 🆕 DIALOG PENTRU ALEGEREA TIPULUI DE POȚIUNE
        if (hero.getHealthPotions() > 0 && hero.getManaPotions() > 0) {
            // Dacă ai și HP și resource potions, întreabă care vrea
            boolean useHealthPotion = DialogHelper.showConfirmation(
                    "Alegere Poțiune",
                    "Ce poțiune vrei să folosești?\\n\\n" +
                            "🧪 HP: Berice (+" + hero.getHealthPotionHealing() + " HP) x" + hero.getHealthPotions() + "\\n" +
                            "💙 " + hero.getTipResursa() + ": Energizant (+" + hero.getManaPotionRestore() + ") x" + hero.getManaPotions() + "\\n\\n" +
                            "✅ OK = HP Potion\\n" +
                            "❌ Cancel = " + hero.getTipResursa() + " Potion"
            );

            if (!useHealthPotion) {
                // Folosește resource potion în schimb
                handleResourcePotionUse(hero.getManaPotionRestore());
                return; // Exit early pentru a nu executa codul de health potion
            }
        } else if (hero.getHealthPotions() <= 0 && hero.getManaPotions() > 0) {
            // Dacă ai doar mana potions, folosește direct
            handleResourcePotionUse(hero.getManaPotionRestore());
            return;
        }

        // ✅ RESTUL CODULUI EXISTENT pentru health potions (NU schimba!)
        disableAllButtons();

        AbilityDTO.BattleTurnResultDTO result = battleService.usePotion(hero, enemy, healAmount);

        addToLog(result.getLog()); // ✅ Folosește getLog() ca în original

        if (result.isBattleOver()) { // ✅ Folosește isBattleOver() ca în original
            handleBattleEnd(result);
        } else {
            updateUI(result.getCurrentState());
            enableAllButtons();
        }
    }


    private void handleBattleEnd(AbilityDTO.BattleTurnResultDTO turnResult) {
        AbilityDTO.BattleResultDTO result = turnResult.getFinalResult();

        if (result == null) {
            // Fled
            returnToTown();
            return;
        }

        if (result.isVictory()) {
            showVictoryScreen(result);
        } else {
            showDefeatScreen();
        }
    }

    private void showVictoryScreen(AbilityDTO.BattleResultDTO result) {
        StringBuilder victoryMsg = new StringBuilder();
        victoryMsg.append("🎉 VICTORIE! 🎉 \n \n");
        victoryMsg.append("Recompense: \n");
        victoryMsg.append("💰 Gold: ").append(result.getGoldEarned()).append(" \n");
        victoryMsg.append("⭐ Experiență: ").append(result.getExperienceEarned()).append(" \n");

        if (result.getShaormaReward() > 0) {
            victoryMsg.append("🌯 Șaorma Revival: ").append(result.getShaormaReward()).append(" \n");
        }

        if (result.hasLoot()) {
            victoryMsg.append(" \n📦 Loot primit: \n");
            for (var item : result.getLoot()) {
                victoryMsg.append("  • ").append(item.getNume()).append(" \n");
            }
        }

        DialogHelper.showSuccess("Victorie!", victoryMsg.toString());

        // Aplică recompensele
        hero.adaugaGold(result.getGoldEarned());
        // Aplică recompensele
        hero.adaugaGold(result.getGoldEarned());

// 🆕 LEVEL-UP UI FEEDBACK
        int oldLevel = hero.getNivel(); // Salvează nivelul înainte de XP
        hero.adaugaXp(result.getExperienceEarned()); // Aici se declanșează level-up automat
        int newLevel = hero.getNivel(); // Nivelul după XP

// 🎉 DIALOG LEVEL-UP
        if (newLevel > oldLevel) {
            StringBuilder levelUpMsg = new StringBuilder();
            levelUpMsg.append("🎉 LEVEL UP! 🎉 \n \n");

            if (newLevel - oldLevel > 1) {
                levelUpMsg.append("🌟 MULTIPLE LEVEL UP! ").append(oldLevel).append(" → ").append(newLevel).append(" \n \n");
            } else {
                levelUpMsg.append("🌟 Noul nivel: ").append(newLevel).append(" \n \n");
            }

            levelUpMsg.append("📈 Îmbunătățiri: \n");
            levelUpMsg.append("💪 Stat Points noi: ").append(hero.getStatPoints()).append(" \n");
            levelUpMsg.append("❤️  HP Maxim: ").append(hero.getViataMaxima()).append(" \n");
            levelUpMsg.append("🔋 ").append(hero.getTipResursa()).append(" Maxim: ").append(hero.getResursaMaxima()).append(" \n \n");
            levelUpMsg.append("💡 Vizitează Trainer-ul pentru stat upgrades!");

            // 🎊 DIALOG SPECIAL PENTRU LEVEL-UP
            DialogHelper.showSuccess("🎉 LEVEL UP! 🎉", levelUpMsg.toString());
        }

// Restul reward-urilor...
        if (result.getShaormaReward() > 0) {
            hero.adaugaShaormaRevival(result.getShaormaReward());
        }

        if (result.getShaormaReward() > 0) {
            hero.adaugaShaormaRevival(result.getShaormaReward());
        }

        if (result.hasLoot()) {
            for (var item : result.getLoot()) {
                hero.getInventar().addItem(item);
            }
        }

        // 🆕 ALEGERI DUPĂ VICTORIE
        if (inDungeon) {
            String depthInfo = dungeonDepth > 1 ? " \n🏰 Depth actual: " + dungeonDepth : "";
            boolean continua = DialogHelper.showConfirmation(
                    "Continuă explorarea?",
                    "Ai învins " + enemy.getNume() + "!" + depthInfo + " \n \n" +
                            "Vrei să continui mai adânc în dungeon sau să te întorci în oraș? \n \n" +
                            "✅ OK = Continuă explorarea \n" +
                            "❌ Cancel = Întoarce-te în oraș"
            );

            if (continua) {
                // Creează următorul inamic cu dificultate crescută
                EnemyGeneratorRomanesc generator = new EnemyGeneratorRomanesc();

                // Crește dificultatea pe baza depth-ului
                int scaledLevel = hero.getNivel() + Math.max(0, dungeonDepth / 3);

                // La fiecare 5 depth-uri, boss battle
                Inamic nextEnemy;
                if ((dungeonDepth + 1) % 5 == 0) {
                    nextEnemy = generator.genereazaBoss(scaledLevel);
                    DialogHelper.showInfo("Boss Ahead!", "🔥 Un BOSS te așteaptă la depth " + (dungeonDepth + 1) + "!");
                } else {
                    nextEnemy = generator.genereazaInamicNormal(scaledLevel);
                }

                // Bonus healing între lupte (5% HP)
                int healing = Math.max(1, hero.getViataMaxima() / 20);
                hero.vindeca(healing);

                // Bonus mana/energy regen
                hero.regenereazaResursa(Math.max(5, hero.getResursaMaxima() / 10));

                System.out.printf("🏰 DUNGEON PROGRESS: Depth %d -> %d | Scaled Level: %d \n",
                        dungeonDepth, dungeonDepth + 1, scaledLevel);

                // Avansează în dungeon
                this.enemy = nextEnemy;
                this.dungeonDepth++;

                // Re-pornește lupta cu noul inamic
                stage.setScene(this.createScene());
                return;
            }
        }

        // Default sau alegerea "Cancel": întoarce-te în oraș
        returnToTown();
    }


    private void showDefeatScreen() {
        DialogHelper.showError("Înfrângere!", "Ai fost învins! \n💀 Game Over");
        // Aici poți implementa logica pentru moarte (ex: șaorma revival)
        returnToTown();
    }

    private void returnToTown() {
        TownMenuController townController = new TownMenuController(stage, hero);
        stage.setScene(townController.createScene());
    }

    // ==================== UI UPDATE ====================

    private void updateUI(AbilityDTO.BattleStateDTO state) {
        // 🛡️ GUARD împotriva null state
        if (state == null) {
            addToLog("⚠️ Eroare internă: starea luptei e nulă după acțiune.");
            enableAllButtons();
            return;
        }

        // 🛡️ NORMALIZE max values pentru a evita division by zero
        int heroMaxHP = Math.max(1, state.getHeroMaxHP());
        int heroMaxRes = Math.max(1, state.getHeroMaxResource());
        int enemyMaxHP = Math.max(1, state.getEnemyMaxHP());

        // 🔍 DEBUG OPTIONAL (decomentează dacă vrei să vezi valorile)
        // addToLog(String.format("DEBUG State: H %d/%d R %d/%d | E %d/%d",
        //         state.getHeroHP(), heroMaxHP,
        //         state.getHeroResource(), heroMaxRes,
        //         state.getEnemyHP(), enemyMaxHP));

        // ✅ UPDATE HERO cu protecție
        heroHPLabel.setText(state.getHeroHP() + " / " + heroMaxHP);
        double heroHpProgress = Math.min(1.0, Math.max(0.0, (double) state.getHeroHP() / heroMaxHP));
        heroHPBar.setProgress(heroHpProgress);
        animateHealthBar(heroHPBar);

        heroResourceLabel.setText(state.getHeroResource() + " / " + heroMaxRes);
        double heroResProgress = Math.min(1.0, Math.max(0.0, (double) state.getHeroResource() / heroMaxRes));
        heroResourceBar.setProgress(heroResProgress);

        // ✅ UPDATE ENEMY cu protecție
        enemyHPLabel.setText(state.getEnemyHP() + " / " + enemyMaxHP);
        double enemyHpProgress = Math.min(1.0, Math.max(0.0, (double) state.getEnemyHP() / enemyMaxHP));
        enemyHPBar.setProgress(enemyHpProgress);
        animateHealthBar(enemyHPBar);

        // ✅ UPDATE ABILITIES și POTIONS cu protecție
        if (state.getAbilities() != null) {
            updateAbilityButtons(state.getAbilities());
        }
        updatePotionButtons();
    }


    private void updateAbilityButtons(java.util.List<AbilityDTO> abilities) {
        abilityButtonsPanel.getChildren().clear();

        for (AbilityDTO ability : abilities) {
            Button btn = new Button(ability.getDisplayName());
            btn.setDisable(!ability.isAvailable());

            String color = ability.isAvailable() ? "#9b59b6" : "#7f8c8d";
            btn.setStyle(
                    "-fx-font-size: 14px; " +
                            "-fx-background-color: " + color + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 8px 20px; " +
                            "-fx-background-radius: 8; " +
                            "-fx-min-width: 200px;"
            );

            btn.setOnAction(e -> handleAbilityUse(ability.getName()));

            abilityButtonsPanel.getChildren().add(btn);
        }
    }

    private void updatePotionButtons() {
        // 🔍 GĂSEȘTE CONTAINERUL CORECT - înlocuiește cu numele real
        // Caută în createActionPanel() sau createUI() numele containerului pentru poțiuni
        VBox potionsContainer = potionButtonsPanel; // SAU orice se numește containerul tău

        if (potionsContainer != null) {
            potionsContainer.getChildren().clear();

            // 🧪 AFIȘEAZĂ POȚIUNI SIMPLE (sistemul vechi)
            int healthPotions = hero.getHealthPotions();
            if (healthPotions > 0) {
                int healAmount = hero.getHealthPotionHealing();

                Button potionBtn = new Button("🧪 Berice (" + healAmount + " HP) x" + healthPotions);
                potionBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
                potionBtn.setOnAction(e -> handlePotionUse(healAmount));
                potionsContainer.getChildren().add(potionBtn);
            } else {
                Label noLabel = new Label("❌ Nu ai poțiuni");
                noLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-style: italic;");
                potionsContainer.getChildren().add(noLabel);
            }

            // 🧪 AFIȘEAZĂ MANA POTIONS dacă există
            int manaPotions = hero.getManaPotions();
            if (manaPotions > 0) {
                int restoreAmount = hero.getManaPotionRestore();

                Button manaBtn = new Button("💙 " + hero.getTipResursa() + " (" + restoreAmount + ") x" + manaPotions);
                manaBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
                manaBtn.setOnAction(e -> handleResourcePotionUse(restoreAmount));
                potionsContainer.getChildren().add(manaBtn);
            }
        }
    }

    /**
     * Gestionează folosirea poțiunilor de resurse
     */
    private void handleResourcePotionUse(int restoreAmount) {
        disableAllButtons();

        AbilityDTO.BattleTurnResultDTO result = battleService.useResourcePotion(hero, enemy, restoreAmount);

        // ✅ FOLOSEȘTE getLog() ca în codul existent
        addToLog(result.getLog());

        // ✅ FOLOSEȘTE isBattleOver() ca în codul existent
        if (result.isBattleOver()) {
            // Verifică dacă ai șaorme pentru revival
            if (hero.areShaormaRevival()) {
                boolean useRevival = DialogHelper.showConfirmation(
                        "💀 AI MURIT! 💀",
                        "Vrei să folosești o Șaorma de Revival?\\n" +
                                "🌯 Șaorme disponibile: " + hero.getShaormaRevival() + "\\n\\n" +
                                "✅ Da = Reînvie cu 50% HP/Resources\\n" +
                                "❌ Nu = Game Over"
                );

                if (useRevival && hero.folosesteShaormaRevival()) {
                    addToLog("🌯✨ ȘAORMA DE REVIVAL ACTIVATĂ! ✨🌯");
                    addToLog("💚 Te-ai reîntors din tărâmul umbrelor!");

                    // Update UI după revival
                    updateUI(new AbilityDTO.BattleStateDTO(
                            hero.getViata(), hero.getViataMaxima(),
                            hero.getResursaCurenta(), hero.getResursaMaxima(),
                            enemy.getViata(), enemy.getViataMaxima(),
                            new ArrayList<>()
                    ));
                    enableAllButtons();
                    return;
                }
            }

            // Nu ai șaorme sau nu vrei să le folosești
            showDefeatScreen();
        } else {
            // Continuă lupta
            updateUI(result.getCurrentState());
            enableAllButtons();
        }
    }

    /**
     * Helper pentru a verifica dacă eroul a murit din BattleTurnResultDTO
     */
    private boolean isHeroDead(AbilityDTO.BattleTurnResultDTO result) {
        // Verifică din starea curentă dacă eroul are 0 HP
        return result.getCurrentState().getHeroHP() <= 0;
    }


    private void addToLog(String message) {
        battleLog.appendText(message + "\n");
        battleLog.setScrollTop(Double.MAX_VALUE);
    }

    private void disableAllButtons() {
        attackButton.setDisable(true);
        fleeButton.setDisable(true);
        abilityButtonsPanel.getChildren().forEach(node ->
                ((Button)node).setDisable(true)
        );
        potionButtonsPanel.getChildren().forEach(node ->
                ((Button)node).setDisable(true)
        );
    }

    private void enableAllButtons() {
        attackButton.setDisable(false);
        fleeButton.setDisable(false);
        // Abilities și potions vor fi update-ate prin updateUI
    }

    private void animateHealthBar(ProgressBar bar) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), bar);
        ft.setFromValue(0.5);
        ft.setToValue(1.0);
        ft.play();
    }
}