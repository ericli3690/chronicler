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
import com.example.chronicler.adapters.ChronologicalTimelineRecyclerViewAdapter;
import com.example.chronicler.adapters.FullTimelineRecyclerViewAdapter;
import com.example.chronicler.adapters.PartialTimelineRecyclerViewAdapter;
import com.example.chronicler.adapters.TimelineRecyclerViewAdapter;
import com.example.chronicler.databinding.FragmentTimelineBinding;
import com.example.chronicler.datatypes.Card;
import com.example.chronicler.datatypes.CardChronologicalList;
import com.example.chronicler.datatypes.Deck;
import com.example.chronicler.functions.Sorter;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TimelineFragment extends Fragment {

    private FragmentTimelineBinding binding;
    private int deckIndex;
    private int parentIndex;
    private String gameOrderString;
    private int currentObscured;
    private boolean allowEdit;

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
        this.allowEdit = TimelineFragmentArgs.fromBundle(getArguments()).getAllowEdit();
        this.gameOrderString = TimelineFragmentArgs.fromBundle(getArguments()).getGameOrderString();
        this.currentObscured = TimelineFragmentArgs.fromBundle(getArguments()).getCurrentObscured();
        // grab data
        Deck masterDeck = ((MainActivity) requireActivity()).masterDeck;

        //// get deck name
        // get deck
        Deck deck = masterDeck.getFlattenedList().get(this.deckIndex);
        // set toolbar
        ((Toolbar) requireActivity().findViewById(R.id.activity_main_toolbar)).setTitle(
                "Timeline: " + deck.name
        );

        // if edit is prohibited, hide the edit button
        if (!allowEdit) {
            binding.fragmentTimelineEdit.setVisibility(View.GONE);
        }

        // back button
        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (allowEdit) {
                    NavHostFragment.findNavController(TimelineFragment.this).navigate(
                            TimelineFragmentDirections.actionTimelineFragmentToDeckFragment(deckIndex, parentIndex)
                    );
                } else {
                    NavHostFragment.findNavController(TimelineFragment.this).navigate(
                            TimelineFragmentDirections.actionTimelineFragmentToGameFragment(deckIndex, parentIndex, gameOrderString, currentObscured)
                    );
                }
                this.setEnabled(false);
            }
        });

        // get recyclerview
        RecyclerView cardRv = binding.fragmentTimelineRv;
        // set layout
        cardRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        ChronologicalTimelineRecyclerViewAdapter adapter;
        // set adapter
        CardChronologicalList chronologicalList = deck.getAllCards().getChronologicalList();
        if (allowEdit) {
            adapter = new FullTimelineRecyclerViewAdapter(
                    chronologicalList, requireContext()
            );
        } else {
            adapter = new PartialTimelineRecyclerViewAdapter(
                    chronologicalList, gameOrderString, currentObscured
            );
        }

        cardRv.setAdapter(adapter);

        // other buttons
        binding.fragmentTimelineSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get search term
                String searchTerm = binding.fragmentTimelineSearchBar.getText().toString();
                // only show those that contain it
                adapter.hideAllWithoutSearchTerm(searchTerm);
            }
        });
        binding.fragmentTimelineEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get all that are selected
                if (gameOrderString.length() > 0) {
                    return;
                }
                // we can now be sure we are in full mode
                // ie this is not accessed from the game
                // and there are checkboxes visible
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