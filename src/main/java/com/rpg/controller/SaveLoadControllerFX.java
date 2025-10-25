package com.rpg.controller;

import com.rpg.model.characters.Erou;
import com.rpg.service.SaveLoadServiceFX;
import com.rpg.service.dto.SaveFileDTO;
import com.rpg.utils.DialogHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller pentru interfața de Save/Load
 */
public class SaveLoadControllerFX {

    private Stage stage;
    private SaveLoadServiceFX saveLoadService;
    private boolean isLoadMode; // true = load, false = save
    private Erou hero; // Pentru save mode

    private ListView<SaveFileDTO> savesListView;
    private TextArea detailsArea;

    /**
     * Constructor pentru LOAD mode
     */
    public SaveLoadControllerFX(Stage stage) {
        this.stage = stage;
        this.saveLoadService = new SaveLoadServiceFX();
        this.isLoadMode = true;
        this.hero = null;
    }

    /**
     * Constructor pentru SAVE mode
     */
    public SaveLoadControllerFX(Stage stage, Erou hero) {
        this.stage = stage;
        this.saveLoadService = new SaveLoadServiceFX();
        this.isLoadMode = false;
        this.hero = hero;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setTop(createHeader());
        root.setCenter(createMainContent());
        root.setBottom(createFooter());

        root.setStyle("-fx-background-color: #1a1a2e;");

        return new Scene(root, 900, 600);
    }

    /**
     * Header
     */
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #16213e;");

        Label title = new Label(isLoadMode ? "📂 ÎNCARCĂ JOC" : "💾 SALVEAZĂ JOC");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        if (!isLoadMode && hero != null) {
            Label heroInfo = new Label("Erou: " + hero.getNume() + " | Nivel: " + hero.getNivel());
            heroInfo.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1f1f1;");
            header.getChildren().addAll(title, heroInfo);
        } else {
            header.getChildren().add(title);
        }

        return header;
    }

    /**
     * Conținut principal
     */
    private HBox createMainContent() {
        HBox content = new HBox(15);
        content.setPadding(new Insets(20));

        VBox leftPanel = createSavesListPanel();
        VBox rightPanel = createDetailsPanel();

        content.getChildren().addAll(leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        return content;
    }

    /**
     * Panel cu lista salvărilor
     */
    private VBox createSavesListPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");

        Label listLabel = new Label("📋 Salvări Disponibile:");
        listLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

        // Tabs pentru manual saves vs auto saves
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Initialize the main ListView first
        savesListView = new ListView<>();

        Tab allTab = new Tab("Toate", createSavesList(saveLoadService.getAvailableSaves()));
        Tab manualTab = new Tab("Manuale", createSavesList(saveLoadService.getManualSaves()));
        Tab autoTab = new Tab("Auto-Save", createSavesList(saveLoadService.getAutoSaves()));

        tabPane.getTabs().addAll(allTab, manualTab, autoTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        panel.getChildren().addAll(listLabel, tabPane);
        return panel;
    }

    private ListView<SaveFileDTO> createSavesList(List<SaveFileDTO> saves) {
        ListView<SaveFileDTO> listView = new ListView<>();
        listView.getItems().addAll(saves);
        listView.setStyle("-fx-font-size: 14px; -fx-background-color: #1a1a2e;");

        // Custom cell factory pentru afișare frumoasă
        listView.setCellFactory(lv -> new ListCell<SaveFileDTO>() {
            @Override
            protected void updateItem(SaveFileDTO save, boolean empty) {
                super.updateItem(save, empty);
                if (empty || save == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(save.toString());
                    if (save.isAutoSave()) {
                        // Auto-save: dark background with green text
                        setStyle("-fx-background-color: #263238; -fx-text-fill: #81c784;");
                    } else {
                        // Manual save: normal background with white text
                        setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: #f1f1f1;");
                    }
                }
            }
        });

        // Add selection listener to update details and track selected save
        listView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    displaySaveDetails(newVal);
                    // Update the main reference when selection changes
                    if (newVal != null) {
                        savesListView = listView;
                    }
                }
        );

        return listView;
    }

    /**
     * Panel cu detalii salvare
     */
    private VBox createDetailsPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");

        Label detailsLabel = new Label("📋 Detalii Salvare:");
        detailsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

        detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setWrapText(true);
        detailsArea.setStyle("-fx-control-inner-background: #1a1a2e; -fx-text-fill: white; -fx-font-size: 14px;");
        detailsArea.setPrefHeight(300);
        VBox.setVgrow(detailsArea, Priority.ALWAYS);

        // Butoane de acțiune
        VBox actionsBox = new VBox(10);
        actionsBox.setAlignment(Pos.CENTER);

        if (isLoadMode) {
            Button loadButton = new Button("📂 ÎNCARCĂ");
            styleButton(loadButton, "#27ae60");
            loadButton.setMaxWidth(Double.MAX_VALUE);
            loadButton.setOnAction(e -> handleLoad());

            Button deleteButton = new Button("🗑️ ȘTERGE");
            styleButton(deleteButton, "#e74c3c");
            deleteButton.setMaxWidth(Double.MAX_VALUE);
            deleteButton.setOnAction(e -> handleDelete());

            actionsBox.getChildren().addAll(loadButton, deleteButton);
        } else {
            Button saveButton = new Button("💾 SALVEAZĂ");
            styleButton(saveButton, "#27ae60");
            saveButton.setMaxWidth(Double.MAX_VALUE);
            saveButton.setOnAction(e -> handleSave());

            actionsBox.getChildren().add(saveButton);
        }

        panel.getChildren().addAll(detailsLabel, detailsArea, actionsBox);
        return panel;
    }

    /**
     * Footer
     */
    private HBox createFooter() {
        HBox footer = new HBox(15);
        footer.setPadding(new Insets(15));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #16213e;");

        Button backButton = new Button("🔙 Înapoi");
        styleButton(backButton, "#e74c3c");
        backButton.setOnAction(e -> {
            MainMenuController mainMenu = new MainMenuController(stage);
            stage.setScene(mainMenu.createScene());
        });

        footer.getChildren().add(backButton);
        return footer;
    }

    // ==================== HANDLERS ====================

    private void displaySaveDetails(SaveFileDTO save) {
        if (save == null) {
            detailsArea.clear();
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("═══════════════════════════════════\n");
        details.append("        DETALII SALVARE\n");
        details.append("═══════════════════════════════════\n\n");

        details.append("📁 Fișier: ").append(save.getFileName()).append("\n");
        details.append("🏷️  Tip: ").append(save.isAutoSave() ? "Auto-Save" : "Manual Save").append("\n\n");

        details.append("👤 Erou: ").append(save.getHeroName()).append("\n");
        details.append("⭐ Nivel: ").append(save.getHeroLevel()).append("\n");
        details.append("💰 Gold: ").append(save.getHeroGold()).append("\n\n");

        details.append("📅 Data: ").append(save.getFormattedDate()).append("\n");
        details.append("💾 Mărime: ").append(save.getFormattedFileSize()).append("\n");

        detailsArea.setText(details.toString());
    }

    private void handleLoad() {
        SaveFileDTO selectedSave = savesListView.getSelectionModel().getSelectedItem();

        if (selectedSave == null) {
            DialogHelper.showWarning("Atenție", "Selectează o salvare pentru a încărca!");
            return;
        }

        if (DialogHelper.showConfirmation("Confirmare",
                "Vrei să încarci salvarea: " + selectedSave.getDisplayName() + "?")) {

            Erou loadedHero = saveLoadService.loadGame(selectedSave.getFileName());

            if (loadedHero != null) {
                DialogHelper.showSuccess("Succes",
                        "Joc încărcat cu succes!\n" +
                                "Bine ai revenit, " + loadedHero.getNume() + "!");

                // Navighează la TownMenu
                TownMenuController townController = new TownMenuController(stage, loadedHero);
                stage.setScene(townController.createScene());
            } else {
                DialogHelper.showError("Eroare", "Nu s-a putut încărca salvarea!");
            }
        }
    }

    private void handleDelete() {
        SaveFileDTO selectedSave = savesListView.getSelectionModel().getSelectedItem();

        if (selectedSave == null) {
            DialogHelper.showWarning("Atenție", "Selectează o salvare pentru a șterge!");
            return;
        }

        if (DialogHelper.showConfirmation("Confirmare Ștergere",
                "Ești SIGUR că vrei să ștergi salvarea:\n" +
                        selectedSave.getDisplayName() + "?\n\n" +
                        "Această acțiune este PERMANENTĂ!")) {

            boolean deleted = saveLoadService.deleteSave(selectedSave.getFileName());

            if (deleted) {
                DialogHelper.showSuccess("Șters", "Salvarea a fost ștearsă!");
                // Refresh lista
                stage.setScene(createScene());
            } else {
                DialogHelper.showError("Eroare", "Nu s-a putut șterge salvarea!");
            }
        }
    }

    private void handleSave() {
        if (hero == null) {
            DialogHelper.showError("Eroare", "Niciun erou pentru salvat!");
            return;
        }

        // Cere nume pentru salvare
        TextInputDialog dialog = new TextInputDialog(hero.getNume() + "_save");
        dialog.setTitle("Nume Salvare");
        dialog.setHeaderText("Introdu un nume pentru salvare:");
        dialog.setContentText("Nume:");

        dialog.showAndWait().ifPresent(saveName -> {
            if (saveName.trim().isEmpty()) {
                DialogHelper.showWarning("Atenție", "Numele nu poate fi gol!");
                return;
            }

            var result = saveLoadService.saveGame(hero, saveName);

            if (result.isSuccess()) {
                DialogHelper.showSuccess("Salvat", result.getMessage());
                // Înapoi la town
                TownMenuController townController = new TownMenuController(stage, hero);
                stage.setScene(townController.createScene());
            } else {
                DialogHelper.showError("Eroare", result.getMessage());
            }
        });
    }

    private void styleButton(Button btn, String color) {
        btn.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 12px 30px; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e ->
                btn.setStyle(
                        "-fx-font-size: 16px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-color: derive(" + color + ", 20%); " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 12px 30px; " +
                                "-fx-background-radius: 8; " +
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
                                "-fx-background-radius: 8; " +
                                "-fx-cursor: hand;"
                )
        );
    }
}