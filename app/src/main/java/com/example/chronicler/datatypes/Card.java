package com.example.chronicler.datatypes;

import java.util.List;

public class Card {
    // instance vars
    public String event;
    public CardDate date;
    public String info;
    public List<String> tags;
    // TODO: include parent deck as instance var?

    // constructor
    public Card(String event, CardDate date, String info, List<String> tags) {
        this.event = event;
        this.date = date;
        this.info = info;
        this.tags = tags;
    }

    // TODO: getters and setters? a single consolidated one?
}
