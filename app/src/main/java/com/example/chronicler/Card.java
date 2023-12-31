package com.example.chronicler;

import java.util.List;

public class Card {
    // instance vars
    private String event;
    private String date;
    private String info;
    private List<String> tags;
    // TODO: include parent deck as instance var?

    // constructor
    public Card(String event, String date, String info, List<String> tags) {
        this.event = event;
        this.date = date;
        this.info = info;
        this.tags = tags;
    }

    // TODO: getters and setters? a single consolidated one?
}
