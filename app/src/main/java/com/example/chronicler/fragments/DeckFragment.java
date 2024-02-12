package com.example.chronicler.fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chronicler.MainActivity;
import com.example.chronicler.R;
import com.example.chronicler.databinding.FragmentDeckBinding;
import com.example.chronicler.datatypes.Deck;
import com.example.chronicler.functions.FileManager;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class DeckFragment extends Fragment {

    private FragmentDeckBinding binding;
    private int deckIndex;
    private int parentIndex;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // handle binding
        binding = FragmentDeckBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // retrieve arguments from bundle
        this.deckIndex = DeckFragmentArgs.fromBundle(getArguments()).getDeckIndex();
        this.parentIndex = DeckFragmentArgs.fromBundle(getArguments()).getParentIndex();
        // grab data
        Deck masterDeck = ((MainActivity) requireActivity()).masterDeck;

        //// get deck name
        // get the deck
        Deck deck = masterDeck.getFlattenedList().get(this.deckIndex);
        // set toolbar
        ((Toolbar) requireActivity().findViewById(R.id.activity_main_toolbar)).setTitle(
                "Deck: " + deck.name
        );
        // set name
        binding.fragmentDeckName.setText(deck.name);

        //// onclicks
        // back button
        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavHostFragment.findNavController(DeckFragment.this).navigate(
                        DeckFragmentDirections.actionDeckFragmentToHomeFragment(false)
                );
                this.setEnabled(false);
            }
        });
        // all other buttons
        binding.fragmentDeckEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(DeckFragment.this).navigate(
                        DeckFragmentDirections.actionDeckFragmentToAddEditDeckFragment(false, deckIndex, parentIndex)
                );
            }
        });
        binding.fragmentDeckAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send no card indices, thus telling it that we are adding, not editing
                NavHostFragment.findNavController(DeckFragment.this).navigate(
                        DeckFragmentDirections.actionDeckFragmentToAddEditCardFragment(deckIndex, parentIndex, "")
                );
            }
        });
        binding.fragmentDeckTimeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(DeckFragment.this).navigate(
                        DeckFragmentDirections.actionDeckFragmentToTimelineFragment(deckIndex, parentIndex, true, "", 0)
                );
            }
        });
        binding.fragmentDeckGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // dont allow if there arent enough cards
                if (deck.cards.size() > 1) {
                    NavHostFragment.findNavController(DeckFragment.this).navigate(
                            DeckFragmentDirections.actionDeckFragmentToGameFragment(deckIndex, parentIndex, "", 0)
                    );
                } else {
                    // there is only one card or no cards
                    // you cant play the game, log an error
                    Snackbar.make(
                            requireActivity().findViewById(android.R.id.content), // get root
                            "This deck doesn't have enough cards to play the game. Add more!",
                            BaseTransientBottomBar.LENGTH_SHORT
                    ).show();
                }
            }
        });
    }
}