package com.example.chronicler.datatypes;

import com.example.chronicler.functions.Sorter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// one of the key elements of this solution
// the decks that contain cards
public class Deck {
    // instance vars
    public String name;
    // the decks it contains
    public List<Deck> children;
    // the cards it immediately contains
    public CardHeap cards;
    // leaderboard statistics
    public int highStreak;
    public int highScore;

    // private utilities
    // defines how to compare two decks by name, alphabetically
    private final static Sorter<Deck> sorter = new Sorter<Deck>(new Comparator<Deck>() {
        @Override
        public int compare(Deck deck1, Deck deck2) {
            return deck1.name.compareTo(deck2.name); // compare names
            // will return a positive number if deck1 should be put after deck2 in normal alphabetical order
            // will return a negative number if deck2 should be put after deck1 in normal alphabetical order
            // will return 0 if deck1's name is the same as deck2's name
        }
    });;

    // simple constructor
    public Deck(String name) {
        this.name = name;
        this.children = new ArrayList<Deck>();
        this.cards = new CardHeap();
        this.highScore = 0;
        this.highStreak = 0;
    }

    // quick public exposed utility function, protects private methods
    public void doSortChildren() {
        this.children = this.sortChildren();
    }

    // sort the children of this deck alphabetically
    // runs recursively, calls itself on the children
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

    // turn the multi-level hierarchy of decks into a single linear list
    // sort of like "flattening" it down
    public List<Deck> getFlattenedList() {
        // a depth-first search of the deck tree
        List<Deck> toReturn = new ArrayList<Deck>();
        toReturn.add(this); // add itself for one
        // then add the children, if there are any
        if (this.children.size() == 0) { // we have no children
            // therefore do nothing
            // this is the base case
        } else { // there are children
            // flatten them
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
    // publicly exposed for use, allows other methods to be hidden and kept safe
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

    // publicly exposed way to get all cards, hides the collectcardheaps method
    public CardHeap getAllCards() {
        return new CardHeap(this.collectCardHeaps());
    }

    // recursively grabs all the cards held by the decks this deck is a parent to
    // and puts them into a single heap
    private List<CardHeap> collectCardHeaps() {
        List<CardHeap> cardHeaps = new ArrayList<CardHeap>(); // prepare
        cardHeaps.add(this.cards); // add these cards
        for (Deck child : children) { // for each child
            cardHeaps.addAll( // add those heaps too!
                    child.collectCardHeaps() // recursive call
            );
        }
        return cardHeaps; // then return upward
    }

    // given a card, find the deck that contains it
    public Deck getDeckContainingCard(Card cardToFind) {
        // assumes the card is located somewhere in this deck's children
        // first, check itself
        for (Card card : cards) {
            if (card == cardToFind) {
                return this;
            }
        }
        // still here? then the children decks must contain it
        for (Deck child : children) {
            // does it contain it?
            Deck childReturn = child.getDeckContainingCard((cardToFind));
            if (childReturn != null) {
                // if so then pass it up!
                return childReturn;
            }
        }
        // did not contain it, nor did its children contain it
        // a parallel process must contain it, this is a dead end of the tree
        return null;
    }

}
