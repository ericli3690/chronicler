package com.example.chronicler.adapters;

import com.example.chronicler.datatypes.CardChronologicalList;
import com.example.chronicler.functions.Sorter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PartialTimelineRecyclerViewAdapter extends ChronologicalTimelineRecyclerViewAdapter {

    public PartialTimelineRecyclerViewAdapter(CardChronologicalList chronologicalCards, String gameOrderString, int currentObscured) {
        // get the game order
        String[] gameOrderStringList = gameOrderString.split(" ");
        List<Integer> gameOrder = new ArrayList<Integer>();
        for (int convertIndex = 0; convertIndex < gameOrderStringList.length; convertIndex++) {
            gameOrder.add(Integer.parseInt(gameOrderStringList[convertIndex]));
        }
        // the cards that have been shown so far are all the indices in gameorder up to currentObscured-1
        // chop gameOrder off at this index
        List<Integer> choppedGameOrder = gameOrder.subList(0,currentObscured); // will not include currentobscured0
        // sort them from lowest to highest (ie chronologically)
        Sorter<Integer> sorter = new Sorter<Integer>(new Comparator<Integer>() {
            @Override
            public int compare(Integer int1, Integer int2) {
                return int1-int2;
                // will return a positive number if int1 should be put after int2
                // will return a negative number if int1 should be kept before int2
                // will return 0 if they are the same
            }
        });
        sorter.doSort(choppedGameOrder);
        // then iterate through that list of indices, adding to a final list as we go
        // the fact that the gameorder list is now sorted chronologically means that the culledchronologicallist will also be sorted chronologically
        CardChronologicalList culledChronologicalList = new CardChronologicalList();
        for (Integer includedCardIndex : choppedGameOrder) {
            culledChronologicalList.add(chronologicalCards.get(includedCardIndex));
        }
        // the chronological list now only contains those listed in cardIndicesInString, stored chronologically
        this.renderedChronologicalCards = culledChronologicalList;
    }

}
