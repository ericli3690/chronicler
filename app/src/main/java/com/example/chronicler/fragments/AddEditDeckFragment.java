package com.example.chronicler.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.example.chronicler.R;
import com.example.chronicler.databinding.FragmentAddEditDeckBinding;
import com.example.chronicler.datatypes.Deck;
import com.example.chronicler.functions.FileManager;
import com.example.chronicler.fragments.AddEditDeckFragmentDirections.ActionAddEditDeckFragmentToHomeFragment;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class AddEditDeckFragment extends Fragment {

    private FragmentAddEditDeckBinding binding;
    private boolean isNew; // controls whether this is adddeck or editdeck
    private int deckIndex;
    private int parentIndex;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // retrieve arguments from bundle
        this.isNew = AddEditDeckFragmentArgs.fromBundle(getArguments()).getIsNew();
        this.deckIndex = AddEditDeckFragmentArgs.fromBundle(getArguments()).getDeckIndex();
        this.parentIndex = AddEditDeckFragmentArgs.fromBundle(getArguments()).getParentIndex();
        // set toolbar
        ((Toolbar) getActivity().findViewById(R.id.activity_main_toolbar)).setTitle(this.isNew ? "Add Deck" : "Edit Deck");
        // handle binding
        binding = FragmentAddEditDeckBinding.inflate(inflater, container, false);
        // set delete button visibility
        if (isNew) {
            binding.fragmentAddEditDeckDelete.setVisibility(View.GONE);
        }
        // return
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //// create parent deck list
        // retrieve deck list
        FileManager<Deck> fileManager = new FileManager<Deck>("deck.txt", Deck.class, getContext(), getActivity());
        Deck MASTER_DECK = fileManager.readObjectsFromFile().get(0);

        // generate programmatically from flattened list
        List<RadioButton> radios = new ArrayList<RadioButton>();
        List<Deck> flattenedList = MASTER_DECK.getFlattenedList();
        for (Deck deck : flattenedList) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(deck.name);
            radios.add(radioButton);
            binding.fragmentAddEditDeckRadioGroup.addView(radioButton);
        }

        //// set text and selected
        if (isNew) {
            binding.fragmentAddEditDeckName.setText(R.string.new_deck);
            radios.get(0).setChecked(true);
        } else {
            binding.fragmentAddEditDeckName.setText(flattenedList.get(this.deckIndex).name);
            radios.get(this.parentIndex).setChecked(true);
        }

        //// delete button onclick
        binding.fragmentAddEditDeckDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // sever the child from the tree
                Deck parent = flattenedList.get(parentIndex);
                Deck child = flattenedList.get(deckIndex);
                parent.children.remove(child);
                // return
                pushAndReturn(MASTER_DECK, fileManager);
            }
        });

        //// done button onclick
        binding.fragmentAddEditDeckDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get name
                String name = binding.fragmentAddEditDeckName.getText().toString();
                // get clicked parent deck
                int radioId = binding.fragmentAddEditDeckRadioGroup.getCheckedRadioButtonId();
                View radioButton = binding.fragmentAddEditDeckRadioGroup.findViewById(radioId);
                int radioIndex = binding.fragmentAddEditDeckRadioGroup.indexOfChild(radioButton);
                Deck parentDeck = flattenedList.get(radioIndex);
                // add or edit?
                if (isNew) {
                    // create and attach
                    Deck newDeck = new Deck(name);
                    parentDeck.children.add(newDeck);
                } else {
                    // get edited deck
                    Deck editedDeck = flattenedList.get(deckIndex);
                    // ensure did not set parent to self or any of self's children
                    // this sort of self-referential error will lead to the deck falling off from the tree: data loss
                    if (editedDeck.getFlattenedList().contains(parentDeck)) {
                        Snackbar.make(
                                getActivity().findViewById(android.R.id.content), // get root
                                "You can't make " + name + " be a child of itself or any of its children.",
                                BaseTransientBottomBar.LENGTH_SHORT
                        ).show(); // immediately show
                        return; // break out
                    }
                    // set new name
                    editedDeck.name = name;
                    // add to new parent's children
                    parentDeck.children.add(editedDeck);
                    // remove from old parent's children
                    flattenedList.get(parentIndex).children.remove(editedDeck);
                }
                // finish
                pushAndReturn(MASTER_DECK, fileManager);
            }
        });
    }

    private void pushAndReturn(Deck MASTER_DECK, FileManager fileManager) {
        // write to file
        MASTER_DECK.doSortChildren(); // sort alphabetically by name
        List<Deck> filePackage = new ArrayList<Deck>();
        filePackage.add(MASTER_DECK);
        fileManager.writeObjectsToFile(filePackage);
        // navigate back
        ActionAddEditDeckFragmentToHomeFragment action = AddEditDeckFragmentDirections.actionAddEditDeckFragmentToHomeFragment(false);
        NavHostFragment.findNavController(AddEditDeckFragment.this).navigate(action);
    }
}