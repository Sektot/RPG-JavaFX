package com.rpg.service;

import com.rpg.model.characters.Erou;
import com.rpg.utils.Validator;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Service îmbunătățit pentru salvarea și încărcarea jocului.
 * VERSIUNEA ÎMBUNĂTĂȚITĂ cu salvări multiple și autosave personalizat.
 */
public class SaveLoadService {
    private static final String SAVE_DIR = "saves/";
    private static final String AUTOSAVE_DIR = "autosaves/";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public SaveLoadService() {
        createDirectories();
    }

    /**
     * Creează directoarele necesare.
     */
    private void createDirectories() {
        new File(SAVE_DIR).mkdirs();
        new File(AUTOSAVE_DIR).mkdirs();
    }

    /**
     * Salvează jocul cu un nume personalizat - INTERFAȚA ÎMBUNĂTĂȚITĂ.
     */
    public void saveGame(Erou erou, Scanner scanner) {
        clearScreen();
       // GameService.getCurrentDungeonService() = GameService.getDungeonService; // Din GameService curent
        System.out.println("\n" + "=".repeat(50));
        System.out.println(" 💾 SALVARE JOC");
        System.out.println("=".repeat(50));

        // Afișează salvările existente
        displayExistingSaves();

        System.out.println("\n🎯 Opțiuni de salvare:");
        System.out.println("1. 💾 Salvare nouă cu nume personalizat");
        System.out.println("2. 🔄 Suprascrie o salvare existentă");
        System.out.println("3. ❌ Anulează");

        int choice = Validator.readValidChoice(scanner, 1, 3);

        switch (choice) {
            case 1 -> saveGameWithCustomName(erou, scanner);
            case 2 -> overwriteExistingSave(erou, scanner);
            case 3 -> System.out.println("\n❌ Salvarea a fost anulată.");
        }
    }

    /**
     * Salvează jocul cu un nume personalizat.
     */
    private void saveGameWithCustomName(Erou erou, Scanner scanner) {
        System.out.print("\n📝 Introdu numele pentru salvare (fără extensie): ");
        //scanner.nextLine(); // Clear buffer
        String saveName = scanner.nextLine().trim();

        if (saveName.isEmpty()) {
            saveName = erou.getNume() + "_" + erou.getNivel();
        }

        // Validează numele fișierului
        saveName = sanitizeFileName(saveName);
        String fileName = saveName + ".sav";

        if (saveGameToFile(erou, SAVE_DIR + fileName)) {
            System.out.println("\n✅ Jocul a fost salvat cu succes!");
            System.out.println("📁 Nume salvare: " + saveName);
            System.out.println("📍 Locația: " + SAVE_DIR + fileName);
        }

        waitForEnter();
    }

    /**
     * Suprascrie o salvare existentă.
     */
    private void overwriteExistingSave(Erou erou, Scanner scanner) {
        List<File> saveFiles = getSaveFiles();

        if (saveFiles.isEmpty()) {
            System.out.println("\n❌ Nu există salvări pentru a fi suprascrise!");
            waitForEnter();
            return;
        }

        System.out.println("\n🔄 Alege salvarea de suprascris:");
        for (int i = 0; i < saveFiles.size(); i++) {
            System.out.println((i + 1) + ". " + getSaveDisplayName(saveFiles.get(i)));
        }
        System.out.println((saveFiles.size() + 1) + ". ❌ Anulează");

        int choice = Validator.readValidChoice(scanner, 1, saveFiles.size() + 1);

        if (choice <= saveFiles.size()) {
            File selectedFile = saveFiles.get(choice - 1);
            if (saveGameToFile(erou, selectedFile.getPath())) {
                System.out.println("\n✅ Salvarea a fost suprascrisă cu succes!");
                System.out.println("📁 Fișier: " + selectedFile.getName());
            }
        } else {
            System.out.println("\n❌ Operațiunea a fost anulată.");
        }

        waitForEnter();
    }

    /**
     * Încarcă jocul cu interfață îmbunătățită.
     */
    public Erou loadGame(Scanner scanner) {
        clearScreen();
        System.out.println("\n" + "=".repeat(50));
        System.out.println(" 📂 ÎNCĂRCARE JOC");
        System.out.println("=".repeat(50));

        List<File> saveFiles = getSaveFiles();
        List<File> autoSaveFiles = getAutoSaveFiles();

        if (saveFiles.isEmpty() && autoSaveFiles.isEmpty()) {
            System.out.println("\n❌ Nu au fost găsite salvări!");
            System.out.println("💡 Creează un erou nou pentru a începe.");
            waitForEnter();
            return null;
        }

        System.out.println("\n💾 SALVĂRI MANUALE:");
        if (saveFiles.isEmpty()) {
            System.out.println("   (Niciuna găsită)");
        } else {
            for (int i = 0; i < saveFiles.size(); i++) {
                System.out.println((i + 1) + ". " + getSaveDisplayName(saveFiles.get(i)));
            }
        }

        System.out.println("\n🤖 AUTO-SALVĂRI:");
        if (autoSaveFiles.isEmpty()) {
            System.out.println("   (Niciuna găsită)");
        } else {
            for (int i = 0; i < autoSaveFiles.size(); i++) {
                int displayNum = saveFiles.size() + i + 1;
                System.out.println(displayNum + ". " + getAutoSaveDisplayName(autoSaveFiles.get(i)));
            }
        }

        int totalOptions = saveFiles.size() + autoSaveFiles.size();
        System.out.println((totalOptions + 1) + ". ❌ Înapoi la meniul principal");

        int choice = Validator.readValidChoice(scanner, 1, totalOptions + 1);

        if (choice == totalOptions + 1) {
            return null; // Înapoi la meniu
        }

        File fileToLoad;
        if (choice <= saveFiles.size()) {
            fileToLoad = saveFiles.get(choice - 1);
        } else {
            fileToLoad = autoSaveFiles.get(choice - saveFiles.size() - 1);
        }

        Erou loadedErou = loadGameFromFile(fileToLoad.getPath());
        if (loadedErou != null) {
            System.out.println("\n📂 Jocul a fost încărcat cu succes!");
            System.out.printf("👤 Bine ai revenit, %s (Nivel %d)!\n",
                    loadedErou.getNume(), loadedErou.getNivel());
            waitForEnter();
        }

        return loadedErou;
    }

    /**
     * Auto-salvează jocul specific pentru personaj - LA ÎNTOARCEREA ÎN ORAȘ.
     */
    public void autoSaveOnTownReturn(Erou erou) {
        String autoSaveFile = AUTOSAVE_DIR + "autosave_" +
                sanitizeFileName(erou.getNume()) + "_lv" + erou.getNivel() + ".asv";

        if (saveGameToFile(erou, autoSaveFile)) {
            System.out.println("🏠 💾 Auto-save complet la întoarcerea în oraș!");
        }
    }

    /**
     * Auto-salvează la checkpoint-uri în dungeon.
     */
//    public void autoSave(Erou erou) {
//        String autoSaveFile = AUTOSAVE_DIR + "checkpoint_" +
//                sanitizeFileName(erou.getNume()) + "_lv" + erou.getNivel() + ".asv";
//
//        if (saveGameToFile(erou, autoSaveFile)) {
//            System.out.println("💾 Checkpoint salvat!");
//        }
//    }

    public void autoSave(Erou erou) {
        String fileName = AUTOSAVE_DIR + "/auto_" + erou.getNume() + ".asv";
        if (saveGameToFile(erou, fileName)) {
            System.out.println("✅ AUTO-SAVE COMPLET!");
        }
    }



    private boolean saveGameToFile(Erou erou, String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            // SALVEAZĂ DOAR EROUL - progresul dungeon e inclus!
            oos.writeObject(erou);                         // ✅ Primul obiect
            oos.writeObject(new Date());                   // ✅ Al doilea obiect
            return true;
        } catch (IOException e) {
            System.err.println("Eroare la salvarea jocului: " + e.getMessage());
            return false;
        }
    }



    /**
     * Încarcă dintr-un fișier specific.
     */
    private Erou loadGameFromFile(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            // CITEȘTE DOAR CE AI SALVAT!
            Erou erou = (Erou) ois.readObject();           // ✅ Primul obiect
            Date saveDate = (Date) ois.readObject();       // ✅ Al doilea obiect
            return erou;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ Eroare la încărcarea jocului: " + e.getMessage());
            return null;
        }
    }

    /**
     * Returnează lista fișierelor de salvare.
     */
    private List<File> getSaveFiles() {
        File saveDir = new File(SAVE_DIR);
        File[] files = saveDir.listFiles((dir, name) -> name.endsWith(".sav"));
        return files != null ? Arrays.asList(files) : new ArrayList<>();
    }

    /**
     * Returnează lista auto-salvărilor.
     */
    private List<File> getAutoSaveFiles() {
        File autoSaveDir = new File(AUTOSAVE_DIR);
        File[] files = autoSaveDir.listFiles((dir, name) -> name.endsWith(".asv"));
        return files != null ? Arrays.asList(files) : new ArrayList<>();
    }

    /**
     * Afișează salvările existente.
     */
    private void displayExistingSaves() {
        List<File> saveFiles = getSaveFiles();

        if (saveFiles.isEmpty()) {
            System.out.println("\n📁 Nu există salvări existente.");
        } else {
            System.out.println("\n📁 Salvări existente:");
            for (File file : saveFiles) {
                System.out.println("   • " + getSaveDisplayName(file));
            }
        }
    }

    /**
     * Returnează numele de afișare pentru o salvare.
     */
    private String getSaveDisplayName(File saveFile) {
        String name = saveFile.getName().replace(".sav", "");
        long fileSize = saveFile.length();
        String lastModified = dateFormat.format(new Date(saveFile.lastModified()));

        return String.format("%s (%.1f KB, %s)", name, fileSize / 1024.0, lastModified);
    }

    /**
     * Returnează numele de afișare pentru o auto-salvare.
     */
    private String getAutoSaveDisplayName(File autoSaveFile) {
        String name = autoSaveFile.getName().replace(".asv", "");
        String lastModified = dateFormat.format(new Date(autoSaveFile.lastModified()));

        return String.format("[AUTO] %s (%s)", name, lastModified);
    }

    /**
     * Curăță numele fișierului de caractere nevalide.
     */
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * Șterge o salvare specifică.
     */
    public void deleteSave(String fileName) {
        File file = new File(SAVE_DIR + fileName);
        if (file.exists() && file.delete()) {
            System.out.println("🗑️ Salvarea a fost ștearsă: " + fileName);
        }
    }

    /**
     * Curăță ecranul - FUNCȚIE UTILĂ PENTRU UI.
     */
    public static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            // Fallback pentru când comanda nu funcționează
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    /**
     * Așteaptă apăsarea tastei Enter.
     */
    private void waitForEnter() {
        System.out.println("\n📝 Apasă Enter pentru a continua...");
        try {
            System.in.read();
        } catch (IOException e) {
            // Ignore
        }
    }

    // ================== METODE PENTRU COMPATIBILITATE ==================

    /**
     * Salvează jocul simplu - pentru compatibilitate cu codul existent.
     */
    public void saveGame(Erou erou) {
        String fileName = SAVE_DIR + sanitizeFileName(erou.getNume()) + "_manual.sav";
        if (saveGameToFile(erou, fileName)) {
            System.out.println("\n💾 Jocul a fost salvat!");
            System.out.println("📁 Fișier: " + fileName);
        }
    }

    /**


    /**
     * Verifică dacă există salvări.
     */
    public boolean hasSavedGame() {
        return !getSaveFiles().isEmpty();
    }

    /**
     * Verifică dacă există auto-salvări.
     */
    public boolean hasAutoSave() {
        return !getAutoSaveFiles().isEmpty();
    }
}