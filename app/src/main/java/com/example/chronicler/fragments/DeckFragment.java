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

public class DeckFragment extends Fragment {

    private FragmentDeckBinding binding;
    private int deckIndex;
    private int parentIndex;
    private Deck masterDeck;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // retrieve arguments from bundle
        this.deckIndex = DeckFragmentArgs.fromBundle(getArguments()).getDeckIndex();
        this.parentIndex = DeckFragmentArgs.fromBundle(getArguments()).getParentIndex();
        // grab data
        masterDeck = ((MainActivity) requireActivity()).masterDeck;
        // set back button
        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                DeckFragmentDirections.ActionDeckFragmentToHomeFragment action = DeckFragmentDirections.actionDeckFragmentToHomeFragment(false);
                NavHostFragment.findNavController(DeckFragment.this).navigate(action);
                this.setEnabled(false);
            }
        });
        // handle binding
        binding = FragmentDeckBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //// get deck name
        // get the deck
        Deck deck = masterDeck.getFlattenedList().get(this.deckIndex);
        // set toolbar
        ((Toolbar) requireActivity().findViewById(R.id.activity_main_toolbar)).setTitle(deck.name);
        // set name
        binding.fragmentDeckName.setText(deck.name);

        //// onclicks
        binding.fragmentDeckEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeckFragmentDirections.ActionDeckFragmentToAddEditDeckFragment action = DeckFragmentDirections.actionDeckFragmentToAddEditDeckFragment(false, deckIndex, parentIndex);
                NavHostFragment.findNavController(DeckFragment.this).navigate(action);
            }
        });
    }
}