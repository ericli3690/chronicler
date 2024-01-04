package com.example.chronicler.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chronicler.databinding.SubDeckRowBinding;
import com.example.chronicler.datatypes.Deck;
import com.example.chronicler.fragments.HomeFragment;
import com.example.chronicler.fragments.HomeFragmentDirections;
import com.example.chronicler.functions.FileManager;

import java.util.List;

public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder> {

    // instance vars
    private SubDeckRowBinding binding;
    private final Deck rootDeck;
    private final Deck masterDeck;
    private final Context context;
    private final Activity activity;
    private final HomeFragment fragment;

    // constructor
    public HomeRecyclerViewAdapter(Deck rootDeck, Deck masterDeck, Context context, Activity activity, HomeFragment fragment) {
        // vital information about contents
        this.rootDeck = rootDeck;
        this.masterDeck = masterDeck;
        // important enclosing information
        this.context = context;
        this.activity = activity;
        this.fragment = fragment;
    }

    // inflate and set onclicks
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate the binding and link it to a viewholder
        binding = SubDeckRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    // transfer information from decks to viewholder (and thus to the ui)
    // casting between the two
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        // set onclicks
        viewHolder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // which deck was clicked; get its position in the flattenedlist
                Deck deck = rootDeck.children.get(viewHolder.getBindingAdapterPosition());
                List<Deck> flattenedList = masterDeck.getFlattenedList();
                int deckIndex = flattenedList.indexOf(deck);
                // find the clicked deck's parent; get its position in the flattened list
                List<Integer> parentPointers = masterDeck.getHierarchy();
                Integer parentIndex = parentPointers.get(deckIndex); // returns an Integer
                // transition to edit screen with this info
                HomeFragmentDirections.ActionHomeFragmentToDeckFragment action = HomeFragmentDirections.actionHomeFragmentToDeckFragment(deckIndex, parentIndex);
                NavHostFragment.findNavController(fragment).navigate(action);
            }
        });
        // position is the position in the list this specific viewholder is at
        viewHolder.nameTv.setText(rootDeck.children.get(position).name); // set name

        // slot in children
        Deck childDeck = rootDeck.children.get(viewHolder.getBindingAdapterPosition());

        // get recyclerview
        RecyclerView childrenRv = binding.subDeckRowRv;
        // set layout
        childrenRv.setLayoutManager(new LinearLayoutManager(context));
        // set divider
        childrenRv.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        // set adapter
        childrenRv.setAdapter(new HomeRecyclerViewAdapter(childDeck, masterDeck, context, activity, fragment));
    }

    // required method implementation
    @Override
    public int getItemCount() {
        return rootDeck.children.size();
    }

    // define viewholder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final LinearLayout root;
        public final TextView nameTv;

        public ViewHolder(SubDeckRowBinding binding) {
            super(binding.getRoot());
            // transfer all xml id bindings into viewholder instance vars
            // allows viewholder to act as a proxy for the xml
            // allows for better caching, more efficient app
            this.root = binding.getRoot();
            this.nameTv = binding.subDeckRowNameTv;
        }
    }
}