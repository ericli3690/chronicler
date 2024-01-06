package com.example.chronicler.adapters;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chronicler.R;
import com.example.chronicler.databinding.SubCardRowBinding;
import com.example.chronicler.datatypes.Card;
import com.example.chronicler.datatypes.CardChronologicalList;
import com.example.chronicler.datatypes.CardHeap;

import java.util.ArrayList;
import java.util.List;

public class TimelineRecyclerViewAdapter extends RecyclerView.Adapter<TimelineRecyclerViewAdapter.ViewHolder> {

    // instance vars
    private SubCardRowBinding binding;
    private final boolean showCheckboxes;
    private final CardChronologicalList chronologicalCards;
    private CardChronologicalList renderedChronologicalCards;
    private final boolean obscure;
    public List<Card> flippedCards;
    public List<Integer> checkedCardIndices;


    public TimelineRecyclerViewAdapter(CardHeap cards, boolean showCheckboxes, boolean obscure) {
        this.showCheckboxes = showCheckboxes;
        this.obscure = obscure;
        this.flippedCards = new ArrayList<Card>();
        this.checkedCardIndices = new ArrayList<Integer>();
        this.chronologicalCards = cards.getChronologicalList();
        // clone it
        this.renderedChronologicalCards = new CardChronologicalList(this.chronologicalCards);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate binding and link to viewholder
        binding = SubCardRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
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
        // check for checks
        // first, make sure android doesnt beat us to the chase and screw up the checkbox listener
        viewHolder.checkBox.setOnCheckedChangeListener(null);
        // then grab the index
        Integer cardIndexInChronologicalList = chronologicalCards.indexOf(card);
        // set it to be checked or unchecked
        viewHolder.checkBox.setChecked(
                this.checkedCardIndices.contains(cardIndexInChronologicalList)
        );
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
        // handle listener
        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                // update checked cards
                if (isChecked) {
                    checkedCardIndices.add(cardIndexInChronologicalList);
                } else {
                    checkedCardIndices.remove(cardIndexInChronologicalList);
                }
            }
        });
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
        public final RelativeLayout background;
        public final TextView eventTv;
        public final TextView dateTv;
        public final TextView infoTv;
        public final CheckBox checkBox;

        // constructor
        public ViewHolder(SubCardRowBinding binding) {
            super(binding.getRoot());

            this.root = binding.subCardRowRoot;
            this.background = binding.subCardRowBackground;
            this.eventTv = binding.subCardRowEvent;
            this.dateTv = binding.subCardRowDate;
            this.infoTv = binding.subCardRowInfo;
            this.checkBox = binding.subCardRowCheckbox;
        }
    }

}
