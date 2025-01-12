package com.example.chronicler.adapters;

import android.app.Activity;
import android.content.Context;
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

import java.util.List;

// home screen of the app, first opened after passing through mainactivity
public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder> {

    private SubDeckRowBinding binding;
    private final Deck rootDeck;
    private final Deck masterDeck;
    private final Context context;
    private final Activity activity;
    private final HomeFragment fragment;

    public HomeRecyclerViewAdapter(Deck rootDeck, Deck masterDeck, Context context, Activity activity, HomeFragment fragment) {
        this.rootDeck = rootDeck;
        this.masterDeck = masterDeck;
        this.context = context;
        this.activity = activity;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // handle binding
        binding = SubDeckRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
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
        viewHolder.nameTv.setText(rootDeck.children.get(position).name);
        Deck childDeck = rootDeck.children.get(viewHolder.getBindingAdapterPosition());

        RecyclerView childrenRv = binding.subDeckRowRv;
        childrenRv.setLayoutManager(new LinearLayoutManager(context));
        childrenRv.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        childrenRv.setAdapter(new HomeRecyclerViewAdapter(childDeck, masterDeck, context, activity, fragment));
    }

    // required, mandatory method implementation
    @Override
    public int getItemCount() {
        return rootDeck.children.size();
    }

    // define viewholder for the list
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