package com.example.chronicler.datatypes;

public class SettingsFile {
    // data structure for holding settings information when writing to the file

    // instance vars
    public int volume;
    public int percentDifficulty;

    // constructor
    public SettingsFile(int volume, int percentDifficulty) {
        this.volume = volume;
        this.percentDifficulty = percentDifficulty;
    }

}
