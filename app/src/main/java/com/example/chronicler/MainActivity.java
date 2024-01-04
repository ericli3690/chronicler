package com.example.chronicler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;

import com.example.chronicler.databinding.ActivityMainBinding;
import com.example.chronicler.datatypes.Deck;
import com.example.chronicler.functions.FileManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public FileManager<Deck> masterDeckManager;
    public Deck masterDeck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // initialize activity
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // get toolbar
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
        
        // set navigation between fragments
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.activity_main_navigator_container);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController);
    }

    // when the up button is pressed, move back one screen
    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}