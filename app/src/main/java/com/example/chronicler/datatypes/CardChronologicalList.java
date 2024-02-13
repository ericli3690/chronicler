package com.example.chronicler.datatypes;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class CardChronologicalList extends ArrayList<Card> {

    // CardHeap.getChronologicalList() returns one of these
    // .indexOf(), .remove(), and .contains() are overriden to be more efficient
    // so that they use a binary search algorithm instead
    // allows the element to be indexed, removed, or determined if it is contained
    // in O(logn) time instead of O(n) time
    // binary search is implemented recursively rather than iteratively
    // as it is more elegant this way
    // additionally, because two cards can have the SAME date but be DIFFERENT,
    // this algorithm will have to be a little less efficient than a normal binary search
    // consider the following array: 2004 2005 2005 2005 2006
    // if we are binary searching for the first 2005 in this array,
    // then after the first call to binarySearch(), the code will not know whether to go left or right
    // thus it should do BOTH, and return whichever has a valid answer, and -1 if neither do
    // there should be no scenario where both return valid answers, as all cards are unique
    // in practice these double splits should be rare, as few cards in a deck will have many cards on the same date
    // so the time complexity will not be affected greately
    // in the worst case, if all the cards in a deck are on the same day,
    // then binarySearch() will act like a linear search, running once per element in the list
    // and each time, eliminating one card as a possibility for a match
    // this reverts the time complexity to O(n) -- but again, will be rare

    // constructor
    public CardChronologicalList() {
        super();
    }

    // constructor for cloning
    public CardChronologicalList(CardChronologicalList cardChronologicalList) {
        super(cardChronologicalList);
    }

    private int binarySearch(Card card, int first, int last) {
        if (last < first) {
            // this is the failure base case
            return -1;
        }
        // otherwise get the middle and continue
        int middle = first + (last-first)/2; // integer division ensures this is a floored int
        if (get(middle) == card) {
            // this is the success base case
            // match found, return and break out
            return middle;
        }
        // now do the recursive step
        int isCardLaterThanMiddle = card.date.isLaterThan(
                get(middle).date
        );
        // check it
        if (isCardLaterThanMiddle > 0) {
            // this means yes
            return binarySearch(card, middle+1, last);
        } else if (isCardLaterThanMiddle < 0) {
            // this means no
            return binarySearch(card, first, middle-1);
        } else {
            // this means they are equal
            // the answer is in the left, or the right, or neither, but it should not be in both
            // if it IS in both somehow due to an error, the first answer (the one in the left) will be returned
            // try left first
            int leftResult = binarySearch(card, first, middle-1);
            if (leftResult != -1) {
                return leftResult;
            }
            // then try right
            int rightResult = binarySearch(card, middle+1, last);
            if (rightResult != -1) {
                return rightResult;
            }
            // not in left or right; return failure
            return -1;
        }
    }

    @Override
    public int indexOf(@Nullable Object card) {
        // search and return the entire list
        return binarySearch((Card) card, 0, size()-1);
    }

    @Override
    public boolean remove(@Nullable Object card) {
        int indexToRemove = binarySearch((Card) card, 0, size()-1);
        if (indexToRemove == -1) {
            // remove fails
            return false;
        } else {
            super.remove(indexToRemove);
            return true;
        }
    }

    @Override
    public boolean contains(@Nullable Object card) {
        // simple oneliner
        // if search fails, return false; if search succeeds, return true
        return binarySearch((Card) card, 0, size() - 1) != -1;
    }
}
