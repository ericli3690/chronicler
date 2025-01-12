package com.example.chronicler.datatypes;

import com.example.chronicler.functions.Sorter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Deck {
    public String name;
    // the decks it contains
    public List<Deck> children;
    // the cards it immediately contains
    public CardHeap cards;
    // leaderboard statistics
    public int highStreak;
    public int highScore;

    // defines how to compare two decks by name, alphabetically
    private final static Sorter<Deck> sorter = new Sorter<Deck>(new Comparator<Deck>() {
        @Override
        public int compare(Deck deck1, Deck deck2) {
            return deck1.name.compareTo(deck2.name);
            // will return a positive number if deck1 should be put after deck2 in normal alphabetical order
            // will return a negative number if deck2 should be put after deck1 in normal alphabetical order
            // will return 0 if deck1's name is the same as deck2's name
        }
    });;

    public Deck(String name) {
        this.name = name;
        this.children = new ArrayList<Deck>();
        this.cards = new CardHeap();
        this.highScore = 0;
        this.highStreak = 0;
    }

    public void doSortChildren() {
        this.children = this.sortChildren();
    }

    private List<Deck> sortChildren() {
        List<Deck> toSort = new ArrayList<Deck>();
        for (Deck child : this.children) {
            if (child.children.size() == 0) {
                // therefore do nothing
            } else {
                child.children = child.sortChildren();
            }
            // add this deck to the tosort buffer
            toSort.add(child);
        }
        sorter.doSort(toSort);
        return toSort;
    }

    // turn the multi-level hierarchy of decks into a single linear list
    // sort of like "flattening" it down
    public List<Deck> getFlattenedList() {
        // a depth-first search of the deck tree
        List<Deck> toReturn = new ArrayList<Deck>();
        toReturn.add(this);
        if (this.children.size() == 0) {
            // therefore do nothing
        } else {
            // flatten them
            for (Deck child : this.children) {
                toReturn.addAll(child.getFlattenedList());
            }
        }
        return toReturn;
    }

    public List<Integer> getHierarchy() {
        List<Integer> toReturn = new ArrayList<Integer>();
        this.hierarchy(-1, toReturn);
        return toReturn;
    }

    // get a list of pointers to parents, SUCH THAT:
    //      masterDeck.getFlattenedList().indexOf(masterDeck.getHierarchy().get(child)) = parent
    // basically the nth index of the list this returns is an index m, where the card at m is the parent of the card at n
    private void hierarchy(int parentLocation, List<Integer> toReturn) {
        // a depth first search of the deck tree
        toReturn.add(parentLocation); // at this deck's position, insert an index pointing to this card's parent location
        if (this.children.size() == 0) {
            // therefore do nothing
        } else {
            int parentOfAllTheseChildren = toReturn.size()-1;
            for (Deck child : this.children) {
                // tell all the children that its parent is this current object
                child.hierarchy(parentOfAllTheseChildren, toReturn);
            }
        }
    }

    public CardHeap getAllCards() {
        return new CardHeap(this.collectCardHeaps());
    }

    // recursively grabs all the cards held by the decks this deck is a parent to
    // and puts them into a single heap
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

    // given a card, find the deck that contains it
    public Deck getDeckContainingCard(Card cardToFind) {
        // assumes the card is located somewhere in this deck's children
        for (Card card : cards) {
            if (card == cardToFind) {
                return this;
            }
        }
        for (Deck child : children) {
            Deck childReturn = child.getDeckContainingCard((cardToFind));
            if (childReturn != null) {
                return childReturn;
            }
        }
        // a parallel process must contain it, this is a dead end of the tree
        return null;
    }

}
