package com.example.chronicler.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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

public abstract class TimelineRecyclerViewAdapter extends RecyclerView.Adapter<TimelineRecyclerViewAdapter.ViewHolder> {

    // there are three different ways this class can be used
    // 1. the chronological list that appears after the deck menu
    //      permits searching
    //      permits checkboxes
    //      does not permit obscuring
    //      cards includes all cards in the deck
    //      renderedcards filters the cards by search
    // 2. the chronological incomplete list that can be viewed during the game
    //      permits searching
    //      does not permit checkboxes
    //      does not permit obscuring
    //      cards includes all cards in the deck that have shown up in the game so far
    //      renderedcards further filters the cards by search
    // 3. the nonchronological list, with a length of 2, that is used as the game's UI
    //      does not permit searching
    //      does not permit checkboxes
    //      permits obscuring
    //      cards is the gameorder list
    //      renderedcards shows two cards in the gameorder list at a time

    // these are implemented as this abstract class's children; see inheritance structure below
    // abstract classes are in uppercase
    //
    //          TIMELINE --- game
    //           |
    //          CHRONOLOGICAL --- partial
    //           |
    //          full
    //

    // instance vars
    private SubCardRowBinding binding;
    protected List<Card> flippedCards; // not chronological
    protected Context context;

    // empty dummy constructor
    // after all, this abstract class is mainly to keep viewholder stuff all together
    public TimelineRecyclerViewAdapter(Context context) {
        // flips
        this.flippedCards = new ArrayList<Card>();
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate binding and link to viewholder
        binding = SubCardRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
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
