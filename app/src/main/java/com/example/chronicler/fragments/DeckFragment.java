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
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

// deck fragment
public class DeckFragment extends Fragment {

    // ui control
    private FragmentDeckBinding binding;
    // which deck is this screen open for?
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
        Deck deck = masterDeck.getFlattenedList().get(this.deckIndex);
        ((Toolbar) requireActivity().findViewById(R.id.activity_main_toolbar)).setTitle(
                "Deck: " + deck.name
        );
        binding.fragmentDeckName.setText(deck.name);

        //// onclicks
        // back button functionality: return to home
        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavHostFragment.findNavController(DeckFragment.this).navigate(
                        DeckFragmentDirections.actionDeckFragmentToHomeFragment(false)
                );
                this.setEnabled(false); // disable this back function so the next one can take over
            }
        });
        // edit this deck
        binding.fragmentDeckEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(DeckFragment.this).navigate(
                        DeckFragmentDirections.actionDeckFragmentToAddEditDeckFragment(false, deckIndex, parentIndex)
                );
            }
        });
        // add a new card
        binding.fragmentDeckAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send no card indices, thus telling it that we are adding, not editing
                NavHostFragment.findNavController(DeckFragment.this).navigate(
                        DeckFragmentDirections.actionDeckFragmentToAddEditCardFragment(deckIndex, parentIndex, "")
                );
            }
        });
        // look at the timeline or edit cards
        binding.fragmentDeckTimeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(DeckFragment.this).navigate(
                        DeckFragmentDirections.actionDeckFragmentToTimelineFragment(deckIndex, parentIndex, true, "", 0, 0, 0)
                );
            }
        });
        // play the game
        binding.fragmentDeckGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // dont allow if there arent enough cards
                if (deck.getAllCards().size() > 1) {
                    NavHostFragment.findNavController(DeckFragment.this).navigate(
                            DeckFragmentDirections.actionDeckFragmentToGameFragment(deckIndex, parentIndex, "", 0, 0, 0)
                    );
                } else {
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