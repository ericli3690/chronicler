package com.example.chronicler;

import java.util.ArrayList;
import java.util.List;

public class Deck {
    // instance vars
    private String name;
    private List<Deck> children;
    private List<Card> cards;
    private int highScore;
    private int highStreak;

    // constructor
    public Deck(String name) {
        this.name = name;
        this.children = new ArrayList<Deck>();
        this.cards = new ArrayList<Card>();
        this.highScore = 0;
        this.highStreak = 0;
    }

    // getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public int getHighScore() {
        return highScore;
    }

    public void setHighScore(int highScore) {
        this.highScore = highScore;
    }

    public int getHighStreak() {
        return highStreak;
    }

    public void setHighStreak(int highStreak) {
        this.highStreak = highStreak;
    }

    // TODO: addCard, deleteCard, getAllCards
    // TODO: make cards into a heap, implement Decks as children

}
