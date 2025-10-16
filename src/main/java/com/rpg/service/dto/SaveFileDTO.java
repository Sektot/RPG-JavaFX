package com.rpg.service.dto;

import java.util.Date;

/**
 * DTO pentru informații despre un fișier de salvare
 */
public class SaveFileDTO {
    private final String fileName;
    private final String displayName;
    private final String heroName;
    private final int heroLevel;
    private final int heroGold;
    private final Date saveDate;
    private final long fileSize;
    private final boolean isAutoSave;

    public SaveFileDTO(String fileName, String displayName, String heroName,
                       int heroLevel, int heroGold, Date saveDate,
                       long fileSize, boolean isAutoSave) {
        this.fileName = fileName;
        this.displayName = displayName;
        this.heroName = heroName;
        this.heroLevel = heroLevel;
        this.heroGold = heroGold;
        this.saveDate = saveDate;
        this.fileSize = fileSize;
        this.isAutoSave = isAutoSave;
    }

    // Getters
    public String getFileName() { return fileName; }
    public String getDisplayName() { return displayName; }
    public String getHeroName() { return heroName; }
    public int getHeroLevel() { return heroLevel; }
    public int getHeroGold() { return heroGold; }
    public Date getSaveDate() { return saveDate; }
    public long getFileSize() { return fileSize; }
    public boolean isAutoSave() { return isAutoSave; }

    /**
     * Returnează mărimea fișierului în format citibil
     */
    public String getFormattedFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }

    /**
     * Returnează data în format citibil
     */
    public String getFormattedDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(saveDate);
    }

    @Override
    public String toString() {
        return displayName + " - " + heroName + " (Nivel " + heroLevel + ") - " + getFormattedDate();
    }
}


