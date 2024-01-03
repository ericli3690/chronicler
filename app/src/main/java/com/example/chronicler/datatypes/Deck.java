package com.example.chronicler.datatypes;

import android.os.Parcelable;

import com.example.chronicler.functions.Sorter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Deck {
    // instance vars
    public String name;
    public List<Deck> children;
    public CardHeap cards;
    public int highScore;
    public int highStreak;

    // private utilities
    private final static Sorter<Deck> sorter = new Sorter<Deck>(new Comparator<Deck>() {
        @Override
        public int compare(Deck deck1, Deck deck2) {
            return deck1.name.compareTo(deck2.name); // compare names
            // will return a positive number if deck1 should be put after deck2 in normal alphabetical order
            // will return a negative number if deck2 should be put after deck1 in normal alphabetical order
            // will return 0 if deck1's name is the same as deck2's name
        }
    });;

    // constructor
    public Deck(String name) {
        this.name = name;
        this.children = new ArrayList<Deck>();
        this.cards = new CardHeap();
        this.highScore = 0;
        this.highStreak = 0;

        // TESTING CODE
        this.cards.add(new Card(
                "Event 1",
                new CardDate(4, 4, 1984),
                "Info info info.",
                new ArrayList<String>()
        ));
        this.cards.add(new Card(
                "Event 2",
                new CardDate(4, 4, 1988),
                "Info info info.",
                new ArrayList<String>()
        ));
        this.cards.add(new Card(
                "Event 3",
                new CardDate(4, 3, 1988),
                "Info info info.",
                new ArrayList<String>()
        ));
        this.cards.add(new Card(
                "Event 4",
                new CardDate(5, 3, 1988),
                "Info info info.",
                new ArrayList<String>()
        ));
        this.cards.add(new Card(
                "Event 5",
                new CardDate(5, 3, 1988),
                "Info info info.",
                new ArrayList<String>()
        ));
    }

    public void doSortChildren() {
        this.children = this.sortChildren();
    }

    // sort
    private List<Deck> sortChildren() {
        List<Deck> toSort = new ArrayList<Deck>();
        for (Deck child : this.children) {
            if (child.children.size() == 0) { // this specific child does not have any children
                // therefore do nothing
                // this is the base case
            } else {
                // else, it has children; repeat this algorithm for it
                // this is the recursive case
                child.children = child.sortChildren();
            }
            // add this deck to the tosort buffer
            toSort.add(child);
        }
        // sort this list!
        sorter.doSort(toSort);
        return toSort; // now sorted
    }

    public List<Deck> getFlattenedList() {
        // a depth-first search of the deck tree
        List<Deck> toReturn = new ArrayList<Deck>();
        toReturn.add(this); // add itself for one
        // then add the children, if there are any
        if (this.children.size() == 0) { // we have no children
            // therefore do nothing
            // this is the base case
        } else { // there are children
            // flatted them
            // add them to the flattened list
            // this is the recursive case
            for (Deck child : this.children) {
                toReturn.addAll(child.getFlattenedList());
            }
        }
        // finally return
        return toReturn;
    }

    // main call into hierarchy
    public List<Integer> getHierarchy() {
        List<Integer> toReturn = new ArrayList<Integer>();
        this.hierarchy(-1, toReturn);
        return toReturn;
    }

    // get a list of pointers to parents
    // masterDeck.getFlattenedList().indexOf(masterDeck.getHierarchy().get(child)) = parent
    private void hierarchy(int parentLocation, List<Integer> toReturn) {
        // a depth first search of the deck tree
        toReturn.add(parentLocation); // at this deck's position, insert data pointing to its parent location
        if (this.children.size() == 0) { // no children, nothing more to do
            // this is the base case
        } else { // there are children
            // for each, repeat
            int parentOfAllTheseChildren = toReturn.size()-1;
            for (Deck child : this.children) {
                // tell all the children that its parent is this current object
                // take what they return and append it
                child.hierarchy(parentOfAllTheseChildren, toReturn);
            }
        }
    }

    public CardHeap getAllCards() {
        return new CardHeap(this.collectCardHeaps());
    }

    private List<CardHeap> collectCardHeaps() {
        List<CardHeap> cardHeaps = new ArrayList<CardHeap>();
        cardHeaps.add(this.cards);
        for (Deck child : children) {
            cardHeaps.addAll(
                    child.collectCardHeaps()
            );
        }
        return cardHeaps;
    }

}
