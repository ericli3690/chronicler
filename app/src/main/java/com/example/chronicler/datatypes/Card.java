package com.example.chronicler.datatypes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Card {
    // instance vars
    public String event;
    public CardDate date;
    public String info;
    public Set<String> tags; // set filters out duplicates

    // constructor
    public Card(String event, CardDate date, String info, Set<String> tags) {
        this.event = event;
        this.date = date;
        this.info = info;
        this.tags = tags;
    }
}
