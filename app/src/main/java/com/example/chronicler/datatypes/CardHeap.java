package com.example.chronicler.datatypes;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// a binary min-heap implemenetation used for storing cards efficiently
public class CardHeap extends ArrayList<Card> {

    // allows cards to be sorted efficiently (heapsort)
    // maximum time complexity of O(nlogn)
    // is especially good on larger datasets, which cards most certainly will be
    // also permits fast insert operations, with time complexity O(logn)
    // this will allow cards to be added on the fly to a cardheap during a game
    // this is a min-heap of sorts
    // a node will always have the EARLIEST date when compared to its descendants
    // this class will also break with the conventional use of "this" that i maintain in most other classes
    // this is because the use of "this" in this class will make the code hard to read and bloated

    // sources used:
    // https://www.geeksforgeeks.org/introduction-to-heap-data-structure-and-algorithm-tutorials/
    // https://www.programiz.com/dsa/heap-data-structure

    // constructor
    public CardHeap() {
        super();
    }

    // for cloning
    public CardHeap(CardHeap cardHeap) {
        super(cardHeap);
    }

    // alternative constructor for putting multiple cardheaps together into a single cardheap
    public CardHeap(List<CardHeap> subHeaps) {
        super();
        // put them into a single buffer list
        List<Card> cardsInList = new ArrayList<Card>();
        for (CardHeap subHeap : subHeaps) {
            cardsInList.addAll(subHeap);
        }
        addAll(cardsInList);
        // then push and rebalance them all at once
    }

    private int getLeftChildIndex(int parentIndex) {
        return 2 * parentIndex + 1;
    }

    private int getRightChildIndex(int parentIndex) {
        return 2 * parentIndex + 2;
    }

    private int getParentIndex(int childIndex) {
        return (childIndex - 1)/2; // integer rounding will floor the result
    }

    // swaps two indices
    private void swap(int index1, int index2) {
        Card temp = get(index1);
        set(index1, get(index2));
        set(index2, temp);
    }

    // move a card upward until it is in the right place
    private void siftUp(int index) {
        // has a time complexity of O(logn)

        if (index == 0) {
            // base case
            // this is the root
            return;
        } // else we may be sure that the parent exists

        // get cards
        Card childCard = get(index);
        int parentIndex = getParentIndex(index);
        Card parentCard = get(parentIndex);

        // compare child with parent
        if (childCard.date.isLaterThan(parentCard.date) == -1) {
            // a return value of -1 means "no": the childCard is NOT later than the parentCard
            // ie, something is wrong, the minheap property is not preserved
            // we must swap parent and child
            swap(index, parentIndex);
            // then recur upwards
            siftUp(parentIndex);
        } // if this while loop is NOT the case, then we are done; do nothing
    }

    // move a card downward until it is in the right place
    private void siftDown(int index) {
        // has a time complexity of O(logn)

        // temporarily state this index as the earliest one we have found thus far
        int earliestIndex = index;
        // compare to left
        int leftIndex = getLeftChildIndex(index);
        if (leftIndex < size()) { // this is a valid index
            Card leftCard = get(leftIndex);
            int doesLeftSideNeedSwap = get(earliestIndex).date.isLaterThan(leftCard.date);
            if (doesLeftSideNeedSwap == 1) {
                // a return value of 1 means "yes": the parent card IS later than the left card
                // ie, something is wrong, the minheap property is not preserved
                // we may need to swap the parent and left
                earliestIndex = leftIndex;
            } else if (doesLeftSideNeedSwap == 0) {
                // they are the same year at the very least
                // put the least specific (ie the one without the day or the month) further down

                // possible cases:
                //
                //  parent  child
                //  ymd     ymd
                //  ymd     ym
                //  ymd     y
                //  ym      ymd         SWAP
                //  ym      ym
                //  ym      y
                //  y       ymd         SWAP
                //  y       ym          SWAP
                //  y       y

                if (get(earliestIndex).date.month == -1 && leftCard.date.month != -1) {
                    // it has a month while we dont, swap
                    earliestIndex = leftIndex;
                } else if (get(earliestIndex).date.day == -1 && leftCard.date.day != -1) {
                    // it has a day while we dont, swap
                    earliestIndex = leftIndex;
                }
            }
        }
        // unless right is EVEN earlier...
        // compare to right
        int rightIndex = getRightChildIndex(index);
        if (rightIndex < size()) {
            Card rightCard = get(rightIndex);
            int doesRightNeedSwap = get(earliestIndex).date.isLaterThan(rightCard.date);
            if (doesRightNeedSwap == 1) {
                // by similar logic to that stated above:
                earliestIndex = rightIndex;
            } else if (doesRightNeedSwap == 0) {
                if (get(earliestIndex).date.month == -1 && rightCard.date.month != -1) {
                    // it has a month while we dont, swap
                    earliestIndex = rightIndex;
                } else if (get(earliestIndex).date.day == -1 && rightCard.date.day != -1) {
                    // it has a day while we dont, swap
                    earliestIndex = rightIndex;
                }
            }
        }
        // if earliestIndex is still equal to index, then this card is fine where it is
        if (earliestIndex == index) {
            return;
        }
        // else, swap and recur downwards
        swap(index, earliestIndex);
        siftDown(earliestIndex);
    }

    private void buildHeap() {
        // start from the end and go to the beginning
        // has a time complexity of O(n)
        // despite seeming like it should be O(nlogn) (n elements, each being inserted and costing logn time), thanks to cool math
        // source: https://www.geeksforgeeks.org/time-complexity-of-building-a-heap/
        for (int siftIndex = size()-1; siftIndex >= 0; siftIndex--) {
            siftDown(siftIndex);
        }
    }

    // get the top element and remove it
    // need to rebalance too
    private Card popRoot() {
        Card toReturn = get(0); // read root
        set(0, get(size()-1)); // overwrite root
        remove(size()-1); // remove last leaf, which was copied to root
        if (size() > 0) { // if theres still a tree to rebalance
            siftDown(0); // rebalance the tree
        }

        // test code to print out heap as it is popped
//        Log.d("A", Integer.toString(size()));
//        int lineLength = 1;
//        int lineCounter = 0;
//        StringBuilder output = new StringBuilder();
//        for (int i = 0; i < size(); i++) {
//            output.append(get(i).date.toString()).append("|");
//            lineCounter++;
//            if (lineCounter == lineLength) {
//                lineLength *= 2;
//                lineCounter = 0;
//                output.append("*").append("\n");
//            }
//        }
//        Log.d("B", output.toString());

        return toReturn; // return popped value
    }

    // override add so that it when cards are added it is done in a heap way
    @Override
    public boolean add(Card card) {
        super.add(card); // add at end
        siftUp(size()-1); // put it in its proper place
        return true; // overridden method always returns true
    }

    // override addall
    // similar to above: make sure that adding is done in a heap way
    @Override
    public boolean addAll(@NonNull Collection<? extends Card> cards) {
        super.addAll(cards); // add all at end
        buildHeap(); // rebalance
        return true; // overridden method always returns true
    }

    // return list of cards in chronological order
    // uses the custon cardchronologicallist datatype i created
    // which allows for a binary search to be performed
    // for indexOf(), remove(), and contains()
    // runs in O(nlogn), since popRoot is O(logn) and happens n times
    public CardChronologicalList getChronologicalList() {
        CardChronologicalList toReturn = new CardChronologicalList(); // will create a chronological list
        CardHeap temp = new CardHeap(this); // clone so the original is not deleted
        while (temp.size() > 0) { // while cards remain in it
            toReturn.add(temp.popRoot()); // pop everything out
        }
        return toReturn; // return it
    }
}
