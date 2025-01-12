package com.example.chronicler.functions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// class that runs mergesort
public class Sorter<T> {

    Comparator<T> comparator;

    public Sorter(Comparator<T> comparator) { // comparing anything
        this.comparator = comparator;
    }

    // need a fast, stable, dependable sort
    // hence, using recursive mergesort
    public void doSort(List<T> toSort) {
        this.sort(toSort, 0, toSort.size()-1);
    }

    private void sort(List<T> toSort, int left, int right) {
        // base case
        if (left >= right) {
            return;
        }
        // recursive case
        int middle = left + (right - left)/2; // int division, cuts off remainder
        this.sort(toSort, left, middle);
        this.sort(toSort, middle+1, right);
        this.merge(toSort, left, middle, middle+1, right);
    }

    // merge helper function
    private void merge(List<T> toSort, int leftStart, int leftEnd, int rightStart, int rightEnd) {
        // initialize pointers
        int left = leftStart;
        int right = rightStart;
        // initialize a temporary list
        List<T> temp = new ArrayList<T>();

        while (left <= leftEnd && right <= rightEnd) {
            // the comparator should return a positive number if a change needs to be made (ie right goes before left)
            // and a negative number if left goes before right, as it currently is
            if (comparator.compare(toSort.get(left), toSort.get(right)) >= 0) {
                temp.add(toSort.get(right));
                right++;
            } else {
                temp.add(toSort.get(left));
                left++;
            }
        }

        while (left <= leftEnd) {
            temp.add(toSort.get(left));
            left++;
        }
        while (right <= rightEnd) {
            temp.add(toSort.get(right));
            right++;
        }

        // now put it back
        for (int replaceIndex = leftStart; replaceIndex <= rightEnd; replaceIndex++) {
            toSort.set(replaceIndex, temp.get(replaceIndex-leftStart));
        }
        // done
    }
}
