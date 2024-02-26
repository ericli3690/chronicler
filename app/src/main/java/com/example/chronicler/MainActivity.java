package com.example.chronicler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;

import com.example.chronicler.databinding.ActivityMainBinding;
import com.example.chronicler.datatypes.Deck;
import com.example.chronicler.datatypes.SettingsFile;
import com.example.chronicler.functions.FileManager;

// main entry point of app
public class MainActivity extends AppCompatActivity {

    // files and file managers for public use
    public FileManager<Deck> masterDeckManager;
    public Deck masterDeck;
    public FileManager<SettingsFile> settingsFileManager;
    public SettingsFile settingsFile;

    // when the app first runs, this is called
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // initialize activity
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // get toolbar that appears at the top
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);
        
        // retrieve deck list
        masterDeckManager = new FileManager<Deck>("decks.txt", Deck.class, MainActivity.this, this);
        if (!masterDeckManager.wasCreatedAlready()) {
            // does the master deck NOT exist?
            // if so create it and write it to file
            masterDeck = new Deck("(none)");
            masterDeckManager.writeSingleObjectToFile(masterDeck);
        } else {
            // already exists, just read it
            masterDeck = masterDeckManager.readObjectsFromFile().get(0);
        }

        // retrieve settings
        // storing these at the root activity will allow game fragment to access them easily
        settingsFileManager = new FileManager<SettingsFile>("settings.txt", SettingsFile.class, MainActivity.this, this);
        if (!settingsFileManager.wasCreatedAlready()) {
            // does the settings file NOT exist?
            // if so create it and write it to file
            // use the default settings
            settingsFile = new SettingsFile(100, 0);
            settingsFileManager.writeSingleObjectToFile(settingsFile);
        } else {
            // already exists, just read it
            settingsFile = settingsFileManager.readObjectsFromFile().get(0);
        }
        
        // set navigation between fragments
        // this class contains the navhost that controls all navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.activity_main_navigator_container);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController);
    }

    // when the up button in the top left corner is pressed, move back one screen
    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}