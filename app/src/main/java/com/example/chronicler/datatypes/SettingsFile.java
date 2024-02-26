package com.example.chronicler.datatypes;

// data structure for holding settings information when writing to the file
public class SettingsFile {

    // instance vars
    public int volume;
    public int percentDifficulty;

    // constructor
    public SettingsFile(int volume, int percentDifficulty) {
        this.volume = volume;
        this.percentDifficulty = percentDifficulty;
    }

}
