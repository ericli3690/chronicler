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

    // in the future this could be improved to a black-red or avl tree
    // to prevent degenerate trees

    // sources used:
    // https://www.geeksforgeeks.org/introduction-to-heap-data-structure-and-algorithm-tutorials/
    // https://www.programiz.com/dsa/heap-data-structure

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

    private void swap(int index1, int index2) {
        Card temp = get(index1);
        set(index1, get(index2));
        set(index2, temp);
    }

    private void siftUp(int index) {
        // O(logn)

        if (index == 0) {
            // this is the root
            return;
        } // else we may be sure that the parent exists

        Card childCard = get(index);
        int parentIndex = getParentIndex(index);
        Card parentCard = get(parentIndex);

        if (childCard.date.isLaterThan(parentCard.date) == -1) {
            // a return value of -1 means "no": the childCard is NOT later than the parentCard
            // ie, something is wrong, the minheap property is not preserved
            swap(index, parentIndex);
            // recur upwards
            siftUp(parentIndex);
        }
    }

    // move a card downward until it is in the right place
    private void siftDown(int index) {
        // O(logn)

        // temporarily state this index as the earliest one we have found thus far
        int earliestIndex = index;
        // compare
        int leftIndex = getLeftChildIndex(index);
        if (leftIndex < size()) {
            Card leftCard = get(leftIndex);
            int doesLeftSideNeedSwap = get(earliestIndex).date.isLaterThan(leftCard.date);
            if (doesLeftSideNeedSwap == 1) {
                // a return value of 1 means "yes": the parent card IS later than the left card
                // ie, something is wrong, the minheap property is not preserved
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
                    earliestIndex = leftIndex;
                } else if (get(earliestIndex).date.day == -1 && leftCard.date.day != -1) {
                    earliestIndex = leftIndex;
                }
            }
        }

        int rightIndex = getRightChildIndex(index);
        if (rightIndex < size()) {
            Card rightCard = get(rightIndex);
            int doesRightNeedSwap = get(earliestIndex).date.isLaterThan(rightCard.date);
            if (doesRightNeedSwap == 1) {
                earliestIndex = rightIndex;
            } else if (doesRightNeedSwap == 0) {
                if (get(earliestIndex).date.month == -1 && rightCard.date.month != -1) {
                    earliestIndex = rightIndex;
                } else if (get(earliestIndex).date.day == -1 && rightCard.date.day != -1) {
                    earliestIndex = rightIndex;
                }
            }
        }

        if (earliestIndex == index) {
            return; // this card is fine where it is
        }
        // else, swap and recur downwards
        swap(index, earliestIndex);
        siftDown(earliestIndex);
    }

    private void buildHeap() {
        // O(n)
        for (int siftIndex = size()-1; siftIndex >= 0; siftIndex--) {
            siftDown(siftIndex);
        }
    }

    private Card popRoot() {
        Card toReturn = get(0);
        set(0, get(size()-1));
        remove(size()-1);
        if (size() > 0) {
            siftDown(0);
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

        return toReturn;
    }

    // override add so that it when cards are added it is done in a heap way
    @Override
    public boolean add(Card card) {
        super.add(card);
        siftUp(size()-1);
        return true; // overridden method always returns true
    }

    // similar to above
    @Override
    public boolean addAll(@NonNull Collection<? extends Card> cards) {
        super.addAll(cards);
        buildHeap();
        return true; // overridden method always returns true
    }

    // return list of cards in chronological order
    // uses the custon cardchronologicallist datatype i created
    // which allows for a binary search to be performed
    // for indexOf(), remove(), and contains()
    // runs in O(nlogn), since popRoot is O(logn) and happens n times
    public CardChronologicalList getChronologicalList() {
        CardChronologicalList toReturn = new CardChronologicalList();
        CardHeap temp = new CardHeap(this); // clone so the original is not deleted
        while (temp.size() > 0) {
            toReturn.add(temp.popRoot());
        }
        return toReturn;
    }
}
