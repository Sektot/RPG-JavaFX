package com.rpg.service;

import com.rpg.model.characters.Erou;
import com.rpg.service.dto.SaveFileDTO;
import com.rpg.service.dto.SaveResult;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * SaveLoadServiceFX - Refactorizat pentru JavaFX
 * Toate metodele returnează date în loc să folosească Scanner
 */
public class SaveLoadServiceFX {

    private static final String SAVE_DIR = "saves/";
    private static final String AUTOSAVE_DIR = "autosaves/";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public SaveLoadServiceFX() {
        createDirectories();
    }

    /**
     * Creează directoarele necesare
     */
    private void createDirectories() {
        new File(SAVE_DIR).mkdirs();
        new File(AUTOSAVE_DIR).mkdirs();
    }

    /**
     * Returnează lista tuturor salvărilor disponibile (manuale + auto)
     */
    public List<SaveFileDTO> getAvailableSaves() {
        List<SaveFileDTO> allSaves = new ArrayList<>();

        // Salvări manuale
        for (File file : getSaveFiles()) {
            allSaves.add(createSaveFileDTO(file, false));
        }

        // Auto-salvări
        for (File file : getAutoSaveFiles()) {
            allSaves.add(createSaveFileDTO(file, true));
        }

        // Sortează după data modificării (cele mai recente primele)
        allSaves.sort((a, b) -> b.getSaveDate().compareTo(a.getSaveDate()));

        return allSaves;
    }

    /**
     * Returnează doar salvările manuale
     */
    public List<SaveFileDTO> getManualSaves() {
        List<SaveFileDTO> saves = new ArrayList<>();

        for (File file : getSaveFiles()) {
            saves.add(createSaveFileDTO(file, false));
        }

        saves.sort((a, b) -> b.getSaveDate().compareTo(a.getSaveDate()));
        return saves;
    }

    /**
     * Returnează doar auto-salvările
     */
    public List<SaveFileDTO> getAutoSaves() {
        List<SaveFileDTO> saves = new ArrayList<>();

        for (File file : getAutoSaveFiles()) {
            saves.add(createSaveFileDTO(file, true));
        }

        saves.sort((a, b) -> b.getSaveDate().compareTo(a.getSaveDate()));
        return saves;
    }

    /**
     * Salvează jocul cu un nume custom
     */
    public SaveResult saveGame(Erou erou, String saveName) {
        if (saveName == null || saveName.trim().isEmpty()) {
            return new SaveResult(false, "Numele salvării nu poate fi gol!", null);
        }

        String sanitizedName = sanitizeFileName(saveName.trim());
        String fileName = SAVE_DIR + sanitizedName + ".sav";

        if (saveGameToFile(erou, fileName)) {
            return new SaveResult(
                    true,
                    "Jocul a fost salvat cu succes!",
                    sanitizedName + ".sav"
            );
        }

        return new SaveResult(false, "Eroare la salvarea jocului!", null);
    }

    /**
     * Salvează rapid (folosește numele eroului)
     */
    public SaveResult quickSave(Erou erou) {
        String fileName = SAVE_DIR + sanitizeFileName(erou.getNume()) + "_quick.sav";

        if (saveGameToFile(erou, fileName)) {
            return new SaveResult(
                    true,
                    "Quick Save complet!",
                    sanitizeFileName(erou.getNume()) + "_quick.sav"
            );
        }

        return new SaveResult(false, "Eroare la quick save!", null);
    }

    /**
     * Auto-salvare
     */
    public SaveResult autoSave(Erou erou) {
        String fileName = AUTOSAVE_DIR + "auto_" + sanitizeFileName(erou.getNume()) + ".asv";

        if (saveGameToFile(erou, fileName)) {
            return new SaveResult(
                    true,
                    "Auto-save complet!",
                    "auto_" + sanitizeFileName(erou.getNume()) + ".asv"
            );
        }

        return new SaveResult(false, "Eroare la auto-save!", null);
    }

    /**
     * Încarcă un joc din fișier
     */
    public Erou loadGame(String fileName) {
        String fullPath;

        // Verifică dacă e auto-save sau save manual
        if (fileName.endsWith(".asv")) {
            fullPath = AUTOSAVE_DIR + fileName;
        } else {
            fullPath = SAVE_DIR + fileName;
        }

        return loadGameFromFile(fullPath);
    }

    /**
     * Șterge o salvare
     */
    public boolean deleteSave(String fileName) {
        String fullPath;

        if (fileName.endsWith(".asv")) {
            fullPath = AUTOSAVE_DIR + fileName;
        } else {
            fullPath = SAVE_DIR + fileName;
        }

        File file = new File(fullPath);
        return file.exists() && file.delete();
    }

    /**
     * Verifică dacă există salvări
     */
    public boolean hasSaves() {
        return !getSaveFiles().isEmpty() || !getAutoSaveFiles().isEmpty();
    }

    /**
     * Obține informații despre o salvare fără a o încărca complet
     */
    public SaveFileDTO getSaveInfo(String fileName) {
        String fullPath;

        if (fileName.endsWith(".asv")) {
            fullPath = AUTOSAVE_DIR + fileName;
        } else {
            fullPath = SAVE_DIR + fileName;
        }

        File file = new File(fullPath);
        if (!file.exists()) {
            return null;
        }

        return createSaveFileDTO(file, fileName.endsWith(".asv"));
    }

    // ==================== HELPER METHODS ====================

    private SaveFileDTO createSaveFileDTO(File file, boolean isAutoSave) {
        String fileName = file.getName();
        long fileSize = file.length();
        Date saveDate = new Date(file.lastModified());

        // Încearcă să citească informații despre erou
        String heroName = "Unknown";
        int heroLevel = 0;
        int heroGold = 0;

        try {
            Erou erou = loadGameFromFile(file.getPath());
            if (erou != null) {
                heroName = erou.getNume();
                heroLevel = erou.getNivel();
                heroGold = erou.getGold();
            }
        } catch (Exception e) {
            // Dacă nu se poate citi, folosește valorile default
        }

        String displayName = isAutoSave
                ? "[AUTO] " + fileName.replace(".asv", "")
                : fileName.replace(".sav", "");

        return new SaveFileDTO(
                fileName,
                displayName,
                heroName,
                heroLevel,
                heroGold,
                saveDate,
                fileSize,
                isAutoSave
        );
    }

    private boolean saveGameToFile(Erou erou, String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(erou);
            oos.writeObject(new Date());
            return true;
        } catch (IOException e) {
            System.err.println("Eroare la salvarea jocului: " + e.getMessage());
            return false;
        }
    }

    private Erou loadGameFromFile(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            Erou erou = (Erou) ois.readObject();
            Date saveDate = (Date) ois.readObject();

            // Migrate old stat points to passive points (for talent tree)
            erou.migrateStatPointsToPassive();

            return erou;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Eroare la încărcarea jocului: " + e.getMessage());
            return null;
        }
    }

    private List<File> getSaveFiles() {
        File saveDir = new File(SAVE_DIR);
        File[] files = saveDir.listFiles((dir, name) -> name.endsWith(".sav"));
        return files != null ? Arrays.asList(files) : new ArrayList<>();
    }

    private List<File> getAutoSaveFiles() {
        File autoSaveDir = new File(AUTOSAVE_DIR);
        File[] files = autoSaveDir.listFiles((dir, name) -> name.endsWith(".asv"));
        return files != null ? Arrays.asList(files) : new ArrayList<>();
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * Export salvare pentru backup
     */
    public SaveResult exportSave(String fileName, String exportPath) {
        try {
            String sourcePath = fileName.endsWith(".asv")
                    ? AUTOSAVE_DIR + fileName
                    : SAVE_DIR + fileName;

            File source = new File(sourcePath);
            File dest = new File(exportPath);

            if (!source.exists()) {
                return new SaveResult(false, "Salvarea nu există!", null);
            }

            // Copiază fișierul
            try (FileInputStream fis = new FileInputStream(source);
                 FileOutputStream fos = new FileOutputStream(dest)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            }

            return new SaveResult(true, "Salvarea a fost exportată cu succes!", dest.getName());
        } catch (IOException e) {
            return new SaveResult(false, "Eroare la export: " + e.getMessage(), null);
        }
    }

    /**
     * Import salvare din backup
     */
    public SaveResult importSave(String importPath) {
        try {
            File source = new File(importPath);

            if (!source.exists()) {
                return new SaveResult(false, "Fișierul nu există!", null);
            }

            // Determină destinația
            String fileName = source.getName();
            String destPath = fileName.endsWith(".asv")
                    ? AUTOSAVE_DIR + fileName
                    : SAVE_DIR + fileName;

            File dest = new File(destPath);

            // Copiază fișierul
            try (FileInputStream fis = new FileInputStream(source);
                 FileOutputStream fos = new FileOutputStream(dest)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            }

            return new SaveResult(true, "Salvarea a fost importată cu succes!", fileName);
        } catch (IOException e) {
            return new SaveResult(false, "Eroare la import: " + e.getMessage(), null);
        }
    }

    /**
     * Curăță salvările vechi (păstrează doar ultimele N)
     */
    public int cleanOldAutoSaves(int keepCount) {
        List<File> autoSaves = getAutoSaveFiles();

        if (autoSaves.size() <= keepCount) {
            return 0;
        }

        // Sortează după data modificării
        autoSaves.sort((a, b) -> Long.compare(b.lastModified(), a.lastModified()));

        int deleted = 0;
        for (int i = keepCount; i < autoSaves.size(); i++) {
            if (autoSaves.get(i).delete()) {
                deleted++;
            }
        }

        return deleted;
    }
}