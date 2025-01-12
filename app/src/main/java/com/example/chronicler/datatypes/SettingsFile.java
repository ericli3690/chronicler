package com.example.chronicler.datatypes;

// data structure for holding settings information when writing to the file
public class SettingsFile {

    public int volume;
    public int percentDifficulty;

    public SettingsFile(int volume, int percentDifficulty) {
        this.volume = volume;
        this.percentDifficulty = percentDifficulty;
    }

}
