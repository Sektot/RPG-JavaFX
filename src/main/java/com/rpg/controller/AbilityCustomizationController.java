package com.rpg.controller;

import com.rpg.model.abilities.*;
import com.rpg.model.characters.Erou;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller for the Ability Customization screen.
 * Allows players to:
 * - Select variants for each ability
 * - Choose talents (3 tiers)
 * - View final stats
 */
public class AbilityCustomizationController {

    private Stage stage;
    private Erou hero;
    private Scene previousScene;

    // UI Components
    private ListView<String> abilityListView;
    private ConfiguredAbility selectedAbility;

    private Label abilityNameLabel;
    private ComboBox<AbilityVariant> variantComboBox;
    private TextArea variantDescArea;

    // Talent selection
    private VBox tier1TalentsBox;
    private VBox tier2TalentsBox;
    private VBox tier3TalentsBox;

    private ToggleGroup tier1Group;
    private ToggleGroup tier2Group;
    private ToggleGroup tier3Group;

    // Stats display (VBox containers with labels inside)
    private VBox finalDamageLabel;
    private VBox finalManaLabel;
    private VBox finalCooldownLabel;
    private VBox finalCritLabel;

    public AbilityCustomizationController(Stage stage, Erou hero, Scene previousScene) {
        this.stage = stage;
        this.hero = hero;
        this.previousScene = previousScene;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0f0f1e;");

        // Title
        Label titleLabel = new Label("⚙️ Ability Customization");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #e94560;");
        titleLabel.setPadding(new Insets(20));
        root.setTop(titleLabel);
        BorderPane.setAlignment(titleLabel, Pos.CENTER);

        // Main content: Left = ability list, Center = customization, Right = stats
        HBox mainContent = new HBox(20);
        mainContent.setPadding(new Insets(20));

        // Left: Ability List
        VBox leftPanel = createAbilityListPanel();
        leftPanel.setMinWidth(250);

        // Center: Customization Panel
        VBox centerPanel = createCustomizationPanel();
        HBox.setHgrow(centerPanel, Priority.ALWAYS);

        // Right: Stats Preview
        VBox rightPanel = createStatsPanel();
        rightPanel.setMinWidth(250);

        mainContent.getChildren().addAll(leftPanel, centerPanel, rightPanel);
        root.setCenter(mainContent);

        // Bottom: Save & Back buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20));

        Button saveButton = new Button("💾 Save Changes");
        styleButton(saveButton, "#27ae60");
        saveButton.setOnAction(e -> saveChanges());

        Button backButton = new Button("◀ Back to Town");
        styleButton(backButton, "#7f8c8d");
        backButton.setOnAction(e -> stage.setScene(previousScene));

        buttonBox.getChildren().addAll(saveButton, backButton);
        root.setBottom(buttonBox);

        // Load abilities
        loadAbilityList();

        return new Scene(root, 1400, 800);
    }

    private VBox createAbilityListPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 10;");

        Label header = new Label("📚 Unlocked Abilities");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        header.setStyle("-fx-text-fill: #ecf0f1;");

        Label countLabel = new Label(hero.getUnlockedAbilityCount() + " abilities unlocked");
        countLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px;");

        abilityListView = new ListView<>();
        abilityListView.setStyle(
                "-fx-background-color: #16213e; " +
                "-fx-control-inner-background: #16213e; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px;"
        );
        VBox.setVgrow(abilityListView, Priority.ALWAYS);

        abilityListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> onAbilitySelected(newVal)
        );

        panel.getChildren().addAll(header, countLabel, abilityListView);
        return panel;
    }

    private VBox createCustomizationPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 10;");

        // Ability Name
        abilityNameLabel = new Label("Select an ability");
        abilityNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        abilityNameLabel.setStyle("-fx-text-fill: #e94560;");

        // Variant Selection
        VBox variantSection = new VBox(10);
        Label variantLabel = new Label("🔀 Select Variant:");
        variantLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        variantLabel.setStyle("-fx-text-fill: #ecf0f1;");

        variantComboBox = new ComboBox<>();
        variantComboBox.setMaxWidth(Double.MAX_VALUE);
        variantComboBox.setStyle("-fx-font-size: 13px;");
        variantComboBox.setOnAction(e -> onVariantChanged());

        // Custom cell factory to display variant names
        variantComboBox.setCellFactory(param -> new ListCell<AbilityVariant>() {
            @Override
            protected void updateItem(AbilityVariant item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        // Button cell (selected item display)
        variantComboBox.setButtonCell(new ListCell<AbilityVariant>() {
            @Override
            protected void updateItem(AbilityVariant item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        variantDescArea = new TextArea();
        variantDescArea.setEditable(false);
        variantDescArea.setWrapText(true);
        variantDescArea.setPrefRowCount(4);
        variantDescArea.setStyle(
                "-fx-control-inner-background: #16213e; " +
                "-fx-text-fill: #ecf0f1; " +
                "-fx-font-size: 12px;"
        );

        variantSection.getChildren().addAll(variantLabel, variantComboBox, variantDescArea);

        // Talents Section
        Label talentsLabel = new Label("🌟 Select Talents:");
        talentsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        talentsLabel.setStyle("-fx-text-fill: #ecf0f1;");

        // Tier 1
        Label tier1Label = new Label("Tier 1:");
        tier1Label.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
        tier1TalentsBox = new VBox(5);
        tier1TalentsBox.setPadding(new Insets(5, 0, 10, 10));
        tier1Group = new ToggleGroup();

        // Tier 2
        Label tier2Label = new Label("Tier 2:");
        tier2Label.setStyle("-fx-text-fill: #9b59b6; -fx-font-weight: bold;");
        tier2TalentsBox = new VBox(5);
        tier2TalentsBox.setPadding(new Insets(5, 0, 10, 10));
        tier2Group = new ToggleGroup();

        // Tier 3
        Label tier3Label = new Label("Tier 3:");
        tier3Label.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
        tier3TalentsBox = new VBox(5);
        tier3TalentsBox.setPadding(new Insets(5, 0, 10, 10));
        tier3Group = new ToggleGroup();

        ScrollPane talentsScroll = new ScrollPane();
        VBox talentsContent = new VBox(15);
        talentsContent.getChildren().addAll(
                tier1Label, tier1TalentsBox,
                tier2Label, tier2TalentsBox,
                tier3Label, tier3TalentsBox
        );
        talentsScroll.setContent(talentsContent);
        talentsScroll.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");
        talentsScroll.setFitToWidth(true);
        VBox.setVgrow(talentsScroll, Priority.ALWAYS);

        panel.getChildren().addAll(
                abilityNameLabel,
                variantSection,
                new Separator(),
                talentsLabel,
                talentsScroll
        );

        return panel;
    }

    private VBox createStatsPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 10;");

        Label header = new Label("📊 Final Stats");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        header.setStyle("-fx-text-fill: #ecf0f1;");

        // Stats labels
        finalDamageLabel = createStatLabel("⚔️ Damage:", "--");
        finalManaLabel = createStatLabel("💧 Mana Cost:", "--");
        finalCooldownLabel = createStatLabel("⏱️ Cooldown:", "--");
        finalCritLabel = createStatLabel("💥 Crit Bonus:", "--");

        Label infoLabel = new Label("These stats include all\ntalent modifiers and\nvariant changes.");
        infoLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px;");
        infoLabel.setWrapText(true);

        panel.getChildren().addAll(
                header,
                new Separator(),
                finalDamageLabel,
                finalManaLabel,
                finalCooldownLabel,
                finalCritLabel,
                new Separator(),
                infoLabel
        );

        return panel;
    }

    private VBox createStatLabel(String label, String value) {
        VBox box = new VBox(3);
        Label labelText = new Label(label);
        labelText.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 13px;");

        Label valueText = new Label(value);
        valueText.setStyle("-fx-text-fill: #e94560; -fx-font-size: 18px; -fx-font-weight: bold;");

        box.getChildren().addAll(labelText, valueText);
        return box;
    }

    private void loadAbilityList() {
        abilityListView.getItems().clear();

        if (hero.getUnlockedAbilityCount() == 0) {
            abilityListView.getItems().add("(No abilities unlocked yet)");
            return;
        }

        for (ConfiguredAbility ability : hero.getAbilityLoadout().getAllUnlockedAbilities()) {
            abilityListView.getItems().add(ability.getBaseAbilityId());
        }

        // Select first ability by default
        if (!abilityListView.getItems().isEmpty()) {
            abilityListView.getSelectionModel().selectFirst();
        }
    }

    private void onAbilitySelected(String abilityId) {
        if (abilityId == null || abilityId.startsWith("(")) {
            return;
        }

        selectedAbility = hero.getConfiguredAbility(abilityId);
        if (selectedAbility == null) {
            return;
        }

        // Update ability name
        abilityNameLabel.setText(selectedAbility.getDisplayName());

        // Load variants
        loadVariants();

        // Load talents
        loadTalents();

        // Update stats
        updateStatsDisplay();
    }

    private void loadVariants() {
        variantComboBox.getItems().clear();

        String abilityName = selectedAbility.getBaseAbilityId();
        List<AbilityVariant> variants = AbilityDefinitions.getVariantsForAbility(abilityName);

        variantComboBox.getItems().addAll(variants);

        // Set current variant as selected
        AbilityVariant current = selectedAbility.getSelectedVariant();
        variantComboBox.getSelectionModel().select(current);

        // Show description
        if (current != null) {
            variantDescArea.setText(current.getFullDescription());
        }
    }

    private void loadTalents() {
        String abilityName = selectedAbility.getBaseAbilityId();

        // Load Tier 1
        tier1TalentsBox.getChildren().clear();
        List<AbilityTalent> tier1Talents = AbilityDefinitions.getTalentsForTier(abilityName, TalentTier.TIER_1);
        for (AbilityTalent talent : tier1Talents) {
            RadioButton rb = createTalentRadioButton(talent);
            rb.setToggleGroup(tier1Group);
            rb.setOnAction(e -> onTalentSelected(TalentTier.TIER_1, talent));

            // Select if currently chosen
            if (selectedAbility.getTier1Talent() != null &&
                selectedAbility.getTier1Talent().getId().equals(talent.getId())) {
                rb.setSelected(true);
            }

            tier1TalentsBox.getChildren().add(rb);
        }

        // Load Tier 2
        tier2TalentsBox.getChildren().clear();
        List<AbilityTalent> tier2Talents = AbilityDefinitions.getTalentsForTier(abilityName, TalentTier.TIER_2);
        for (AbilityTalent talent : tier2Talents) {
            RadioButton rb = createTalentRadioButton(talent);
            rb.setToggleGroup(tier2Group);
            rb.setOnAction(e -> onTalentSelected(TalentTier.TIER_2, talent));

            if (selectedAbility.getTier2Talent() != null &&
                selectedAbility.getTier2Talent().getId().equals(talent.getId())) {
                rb.setSelected(true);
            }

            tier2TalentsBox.getChildren().add(rb);
        }

        // Load Tier 3
        tier3TalentsBox.getChildren().clear();
        List<AbilityTalent> tier3Talents = AbilityDefinitions.getTalentsForTier(abilityName, TalentTier.TIER_3);
        for (AbilityTalent talent : tier3Talents) {
            RadioButton rb = createTalentRadioButton(talent);
            rb.setToggleGroup(tier3Group);
            rb.setOnAction(e -> onTalentSelected(TalentTier.TIER_3, talent));

            if (selectedAbility.getTier3Talent() != null &&
                selectedAbility.getTier3Talent().getId().equals(talent.getId())) {
                rb.setSelected(true);
            }

            tier3TalentsBox.getChildren().add(rb);
        }
    }

    private RadioButton createTalentRadioButton(AbilityTalent talent) {
        RadioButton rb = new RadioButton();
        rb.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 13px;");
        rb.setWrapText(true);

        // Format: Icon Name - Description
        String text = talent.getIcon() + " " + talent.getName() + "\n  " + talent.getDescription();
        rb.setText(text);

        return rb;
    }

    private void onVariantChanged() {
        if (selectedAbility == null) return;

        AbilityVariant newVariant = variantComboBox.getValue();
        if (newVariant != null) {
            selectedAbility.setSelectedVariant(newVariant);
            variantDescArea.setText(newVariant.getFullDescription());
            updateStatsDisplay();
        }
    }

    private void onTalentSelected(TalentTier tier, AbilityTalent talent) {
        if (selectedAbility == null) return;

        switch (tier) {
            case TIER_1 -> selectedAbility.setTier1Talent(talent);
            case TIER_2 -> selectedAbility.setTier2Talent(talent);
            case TIER_3 -> selectedAbility.setTier3Talent(talent);
        }

        updateStatsDisplay();
    }

    private void updateStatsDisplay() {
        if (selectedAbility == null) {
            finalDamageLabel.getChildren().get(1).setStyle("-fx-text-fill: #95a5a6;");
            ((Label) finalDamageLabel.getChildren().get(1)).setText("--");
            ((Label) finalManaLabel.getChildren().get(1)).setText("--");
            ((Label) finalCooldownLabel.getChildren().get(1)).setText("--");
            ((Label) finalCritLabel.getChildren().get(1)).setText("--");
            return;
        }

        // Update with final stats
        ((Label) finalDamageLabel.getChildren().get(1)).setText(String.valueOf(selectedAbility.getFinalDamage()));
        ((Label) finalManaLabel.getChildren().get(1)).setText(String.valueOf(selectedAbility.getFinalManaCost()));
        ((Label) finalCooldownLabel.getChildren().get(1)).setText(selectedAbility.getFinalCooldown() + " turns");

        double critBonus = selectedAbility.getFinalCritChanceBonus();
        if (critBonus > 0) {
            ((Label) finalCritLabel.getChildren().get(1)).setText("+" + (int)(critBonus * 100) + "%");
        } else {
            ((Label) finalCritLabel.getChildren().get(1)).setText("--");
        }
    }

    private void saveChanges() {
        // Changes are already applied to ConfiguredAbility objects
        // Just show confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Changes Saved");
        alert.setHeaderText(null);
        alert.setContentText("✅ Ability customizations have been saved!\nThey will be used in your next combat.");
        alert.showAndWait();
    }

    private void styleButton(Button button, String color) {
        button.setStyle(
                "-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 10px 30px; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;"
        );
        button.setMinWidth(180);
    }
}
