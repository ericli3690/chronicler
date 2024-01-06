package com.example.chronicler.fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chronicler.MainActivity;
import com.example.chronicler.R;
import com.example.chronicler.adapters.TimelineRecyclerViewAdapter;
import com.example.chronicler.databinding.FragmentTimelineBinding;
import com.example.chronicler.datatypes.Card;
import com.example.chronicler.datatypes.Deck;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class TimelineFragment extends Fragment {

    private FragmentTimelineBinding binding;
    private int deckIndex;
    private int parentIndex;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // handle binding
        binding = FragmentTimelineBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // retrieve arguments from bundle
        this.deckIndex = TimelineFragmentArgs.fromBundle(getArguments()).getDeckIndex();
        this.parentIndex = TimelineFragmentArgs.fromBundle(getArguments()).getParentIndex();
        // grab data
        Deck masterDeck = ((MainActivity) requireActivity()).masterDeck;

        //// get deck name
        // get deck
        Deck deck = masterDeck.getFlattenedList().get(this.deckIndex);
        // set toolbar
        ((Toolbar) requireActivity().findViewById(R.id.activity_main_toolbar)).setTitle(
                "Timeline: " + deck.name
        );

        // back button
        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavHostFragment.findNavController(TimelineFragment.this).navigate(
                        TimelineFragmentDirections.actionTimelineFragmentToDeckFragment(deckIndex, parentIndex)
                );
                this.setEnabled(false);
            }
        });

        // get recyclerview
        RecyclerView cardRv = binding.fragmentTimelineRv;
        // set layout
        cardRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        // set adapter
        TimelineRecyclerViewAdapter adapter = new TimelineRecyclerViewAdapter(deck.getAllCards(), true, false);
        cardRv.setAdapter(adapter);

        // other buttons
        binding.fragmentTimelineSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get search term
                String searchTerm = binding.fragmentTimelineSearchBar.getText().toString();
//                // uncheck all checkboxes
//                for (int cardIndex = 0; cardIndex < cardRv.getChildCount(); cardIndex++) {
//                    // get each viewholder
//                    TimelineRecyclerViewAdapter.ViewHolder viewHolder =
//                            (TimelineRecyclerViewAdapter.ViewHolder)
//                                    cardRv.getChildViewHolder(
//                                            cardRv.getChildAt(cardIndex)
//                                    );
//                    // uncheck them
//                    viewHolder.checkBox.setChecked(false);
//                }
                // only show those that contain it
                adapter.hideAllWithoutSearchTerm(searchTerm);
            }
        });
        binding.fragmentTimelineEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get all that are selected
                List<Integer> checkedCardIndices = adapter.checkedCardIndices;
                // check that at least one is selected
                if (checkedCardIndices.size() == 0) {
                    // none were selected
                    Snackbar.make( // show error message
                            requireActivity().findViewById(android.R.id.content), // get root
                            "You have not selected any cards.",
                            BaseTransientBottomBar.LENGTH_SHORT
                    ).show(); // immediately show
                    return; // do not proceed
                }
                // else were are confident that there is at least one index selected
                // package the indices up as a string
                List<String> indicesStringList = new ArrayList<String>();
                for (Integer cardIndex : checkedCardIndices) {
                    indicesStringList.add(Integer.toString(cardIndex));
                }
                String indicesString = String.join(" ", indicesStringList);
                // then send it on to the edit tab
                NavHostFragment.findNavController(TimelineFragment.this).navigate(
                        TimelineFragmentDirections.actionTimelineFragmentToAddEditCardFragment(deckIndex, parentIndex, indicesString)
                );
            }
        });
    }
}