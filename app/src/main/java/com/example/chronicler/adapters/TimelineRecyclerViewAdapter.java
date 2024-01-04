package com.example.chronicler.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chronicler.databinding.SubCardRowBinding;
import com.example.chronicler.datatypes.Card;
import com.example.chronicler.datatypes.CardHeap;

import java.util.ArrayList;
import java.util.List;

public class TimelineRecyclerViewAdapter extends RecyclerView.Adapter<TimelineRecyclerViewAdapter.ViewHolder> {

    // instance vars
    private SubCardRowBinding binding;
    private CardHeap cards;
    private boolean showCheckboxes;
    private final List<Card> chronologicalCards;
    private List<Card> renderedChronologicalCards;
    private boolean obscure;


    public TimelineRecyclerViewAdapter(CardHeap cards, boolean showCheckboxes, boolean obscure) {
        this.cards = cards;
        this.showCheckboxes = showCheckboxes;
        this.obscure = obscure;
        this.chronologicalCards = cards.getChronologicalList();
        // clone it
        this.renderedChronologicalCards = new ArrayList<Card>(this.chronologicalCards);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate binding and link to viewholder
        binding = SubCardRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        // get card instance associated with this viewholder
        Card card = renderedChronologicalCards.get(position);
        // set text
        if (this.obscure && position == 1) {
            viewHolder.eventTv.setText("██████████");
        } else {
            viewHolder.eventTv.setText(card.event);
        }
        viewHolder.dateTv.setText(card.date.toString());
        viewHolder.infoTv.setText(card.info);
        // show / hide checkboxes
        viewHolder.checkBox.setVisibility(
                showCheckboxes ? View.VISIBLE : View.INVISIBLE
        );
        // set on click to be flip
        viewHolder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewHolder.flipped) {

                    viewHolder.flipped = false; // unflip
                    // toggle visibilities
                    viewHolder.eventWrapper.setVisibility(View.VISIBLE);
                    viewHolder.dateTv.setVisibility(View.VISIBLE);
                    viewHolder.infoWrapper.setVisibility(View.GONE);

                } else {

                    viewHolder.flipped = true; // flip
                    // toggle visibilities
                    viewHolder.eventWrapper.setVisibility(View.GONE);
                    viewHolder.dateTv.setVisibility(View.GONE);
                    viewHolder.infoWrapper.setVisibility(View.VISIBLE);

                }
            }
        });
    }

    public void hideAllWithoutSearchTerm(String searchTerm) {
        // reset rendered list
        this.renderedChronologicalCards = new ArrayList<Card>();
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
        // renderedflattenedcards now contains the list of cards  that match
        // notify the adapter that the cards it contains are now stale and must be updated
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return renderedChronologicalCards.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ConstraintLayout root;
        public final TextView eventTv;
        public final TextView dateTv;
        public final TextView infoTv;
        public final ScrollView eventWrapper;
        public final ScrollView infoWrapper;
        public final CheckBox checkBox;

        public boolean flipped;

        // constructor
        public ViewHolder(SubCardRowBinding binding) {
            super(binding.getRoot());

            this.root = binding.subCardRowRoot;
            this.eventTv = binding.subCardRowEvent;
            this.dateTv = binding.subCardRowDate;
            this.infoTv = binding.subCardRowInfo;
            this.eventWrapper = binding.subCardRowEventWrapper;
            this.infoWrapper = binding.subCardRowInfoWrapper;
            this.checkBox = binding.subCardRowCheckbox;

            this.flipped = false;
        }
    }

}
