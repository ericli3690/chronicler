package com.example.chronicler.datatypes;

import java.util.Set;

// card data type
public class Card {
    // instance vars
    public String event;
    public CardDate date;
    public String info;
    public Set<String> tags; // set filters out duplicates, ensures all tags are unique

    // constructor
    public Card(String event, CardDate date, String info, Set<String> tags) {
        this.event = event;
        this.date = date;
        this.info = info;
        this.tags = tags;
    }
}
