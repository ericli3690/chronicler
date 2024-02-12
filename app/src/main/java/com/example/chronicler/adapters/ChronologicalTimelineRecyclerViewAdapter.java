package com.example.chronicler.adapters;

import android.view.View;

import androidx.annotation.NonNull;

import com.example.chronicler.R;
import com.example.chronicler.datatypes.Card;
import com.example.chronicler.datatypes.CardChronologicalList;

import java.util.ArrayList;
import java.util.List;

public abstract class ChronologicalTimelineRecyclerViewAdapter extends TimelineRecyclerViewAdapter {
    protected CardChronologicalList chronologicalCards;
    protected CardChronologicalList renderedChronologicalCards;
    private List<Card> flippedCards; // not chronological
    public List<Integer> checkedCardIndices;

    public ChronologicalTimelineRecyclerViewAdapter() {
        super();
    }

    public ChronologicalTimelineRecyclerViewAdapter(CardChronologicalList chronologicalCards) {
        super();
        this.chronologicalCards = chronologicalCards;
        this.renderedChronologicalCards = new CardChronologicalList(this.chronologicalCards);
        // flips
        this.flippedCards = new ArrayList<Card>();
    }

    @Override
    public int getItemCount() {
        return renderedChronologicalCards.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        // get card instance associated with this viewholder
        Card card = renderedChronologicalCards.get(position);
        // set text
        viewHolder.eventTv.setText(card.event);
        viewHolder.dateTv.setText(card.date.toString());
        viewHolder.infoTv.setText(card.info);
        // hide checkboxes; child classes can toggle back on
        viewHolder.checkBox.setVisibility(View.INVISIBLE);
        // handle if flip
        // toggle visibilities
        if (this.flippedCards.contains(card)) {
            viewHolder.eventTv.setVisibility(View.GONE);
            viewHolder.dateTv.setVisibility(View.GONE);
            viewHolder.infoTv.setVisibility(View.VISIBLE);
            viewHolder.background.setBackgroundResource(R.drawable.sub_card_row_backing_dark);
        } else {
            viewHolder.eventTv.setVisibility(View.VISIBLE);
            viewHolder.dateTv.setVisibility(View.VISIBLE);
            viewHolder.infoTv.setVisibility(View.GONE);
            viewHolder.background.setBackgroundResource(R.drawable.sub_card_row_backing);
        }

        // set on click to result in flip
        viewHolder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // flip flipped status
                if (flippedCards.contains(card)) {
                    flippedCards.remove(card);
                } else {
                    flippedCards.add(card);
                }
                // update on screen
                notifyItemChanged(renderedChronologicalCards.indexOf(card));
            }
        });
    }

    public void hideAllWithoutSearchTerm(String searchTerm) {
        // reset rendered list
        this.renderedChronologicalCards = new CardChronologicalList();
        // go through rendered list, culling as necessary
        // either the event must contain the search term
        // or a tag exactly matches the search term
        for (Card card : this.chronologicalCards) {
            boolean thisContainsSearchTerm = false;
            if (card.event.toUpperCase().contains(searchTerm.toUpperCase())) {
                thisContainsSearchTerm = true;
            } else {
                for (String tag : card.tags) {
                    if (tag.equalsIgnoreCase(searchTerm)) {
                        thisContainsSearchTerm = true;
                    }
                }
            }
            if (thisContainsSearchTerm) {
                // contains; keep it
                this.renderedChronologicalCards.add(card);
            }
        }
        // renderedflattenedcards now contains the list of cards that match
        // notify the adapter that the cards it contains are now stale and must be updated
        notifyDataSetChanged();
    }
}
