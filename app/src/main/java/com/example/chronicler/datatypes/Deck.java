package com.example.chronicler.datatypes;

import java.util.ArrayList;
import java.util.List;

public class Deck {
    // instance vars
    public String name;
    public List<Deck> children;
    public List<Card> cards;
    public int highScore;
    public int highStreak;

    // constructor
    public Deck(String name) {
        this.name = name;
        this.children = new ArrayList<Deck>();
        this.cards = new ArrayList<Card>();
        this.highScore = 0;
        this.highStreak = 0;
    }

    // TODO: addCard, deleteCard, getAllCards
    // TODO: make cards into a heap, implement Decks as children

}
