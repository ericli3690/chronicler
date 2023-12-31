package com.example.chronicler;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chronicler.databinding.FragmentHomeBinding;
import com.example.chronicler.databinding.SubDeckRowBinding;

import java.util.List;

public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder> {

    // instance vars
    private final List<Deck> decks;
    private final Context context;
    private final Activity activity;

    // constructor
    public HomeRecyclerViewAdapter(List<Deck> decks, Context context, Activity activity) {
        // vital information about contents
        this.decks = decks;
        // important enclosing information
        this.context = context;
        this.activity = activity;
    }

    // getter for external use
    public List<Deck> getDecks() {
        return decks;
    }

    // inflate and set onclicks
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate the binding and link it to a viewholder
        SubDeckRowBinding binding = SubDeckRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        ViewHolder viewHolder = new ViewHolder(binding);
        // set onclicks
        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // which deck was clicked
                Deck deck = decks.get(viewHolder.getBindingAdapterPosition());
                Log.d("clicked_deck", deck.getName());
            }
        });
        // can also listen to other things that change here
        // anything that is a child of the binding works
        // finally, return the viewholder
        return viewHolder;
    }

    // transfer information from decks to viewholder (and thus to the ui)
    // casting between the two
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        // position is the position in the list this specific viewholder is at
        viewHolder.nameTv.setText(decks.get(position).getName()); // set name
    }

    // required method implementation
    @Override
    public int getItemCount() {
        return decks.size();
    }

    // define viewholder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView nameTv;

        public ViewHolder(SubDeckRowBinding binding) {
            super(binding.getRoot());
            // transfer all xml id bindings into viewholder instance vars
            // allows viewholder to act as a proxy for the xml
            this.nameTv = binding.deckNameTv;
        }
    }
}