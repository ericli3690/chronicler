package com.example.chronicler.adapters;

import com.example.chronicler.datatypes.CardChronologicalList;

public class PartialTimelineRecyclerViewAdapter extends ChronologicalTimelineRecyclerViewAdapter {

    public PartialTimelineRecyclerViewAdapter(CardChronologicalList chronologicalCards, String cardIndicesInString) {
        // convert the string of indices into a proper array of ints
        String[] stringCardIndices = cardIndicesInString.split(" ");
        int[] cardIndices = new int[stringCardIndices.length];
        for (int convertIndex = 0; convertIndex < stringCardIndices.length; convertIndex++) {
            cardIndices[convertIndex] = Integer.parseInt(stringCardIndices[convertIndex]);
        }
        // iterate through them and add them to the chronological cards
        this.chronologicalCards = new CardChronologicalList();
        for (int index : cardIndices) {
            this.chronologicalCards.add(
                    chronologicalCards.get(index)
            );
        }
        // the chronological list now only contains those listed in cardIndicesInString, stored chronologically
        this.renderedChronologicalCards = new CardChronologicalList(this.chronologicalCards);
    }

}
