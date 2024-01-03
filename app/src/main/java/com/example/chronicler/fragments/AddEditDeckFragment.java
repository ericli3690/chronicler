package com.example.chronicler.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.example.chronicler.MainActivity;
import com.example.chronicler.R;
import com.example.chronicler.databinding.FragmentAddEditDeckBinding;
import com.example.chronicler.datatypes.Deck;
import com.example.chronicler.functions.FileManager;
import com.example.chronicler.fragments.AddEditDeckFragmentDirections.ActionAddEditDeckFragmentToDeckFragment;
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
    private FileManager<Deck> masterDeckManager;
    private Deck masterDeck;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // retrieve arguments from bundle
        this.isNew = AddEditDeckFragmentArgs.fromBundle(getArguments()).getIsNew();
        this.deckIndex = AddEditDeckFragmentArgs.fromBundle(getArguments()).getDeckIndex();
        this.parentIndex = AddEditDeckFragmentArgs.fromBundle(getArguments()).getParentIndex();
        // grab data
        masterDeckManager = ((MainActivity) requireActivity()).masterDeckManager;
        masterDeck = ((MainActivity) requireActivity()).masterDeck;
        // set back button
        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavDirections action;
                if (isNew) {
                    action = AddEditDeckFragmentDirections.actionAddEditDeckFragmentToHomeFragment(false);
                } else {
                    action = AddEditDeckFragmentDirections.actionAddEditDeckFragmentToDeckFragment(deckIndex, parentIndex);
                }
                NavHostFragment.findNavController(AddEditDeckFragment.this).navigate(action);
                this.setEnabled(false);
            }
        });
        // handle binding
        binding = FragmentAddEditDeckBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // set delete button visibility
        if (isNew) {
            binding.fragmentAddEditDeckDelete.setVisibility(View.GONE);
        }

        //// create parent deck list
        // generate programmatically from flattened list
        List<RadioButton> radios = new ArrayList<RadioButton>();
        List<Deck> flattenedList = masterDeck.getFlattenedList();
        for (Deck deck : flattenedList) {
            RadioButton radioButton = new RadioButton(requireContext());
            radioButton.setText(deck.name);
            radios.add(radioButton);
            binding.fragmentAddEditDeckRadioGroup.addView(radioButton);
        }

        // set toolbar
        ((Toolbar) requireActivity().findViewById(R.id.activity_main_toolbar)).setTitle(this.isNew ? "Add Deck" : "Edit Deck");

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
                // double check with confirmation message
                Snackbar deleteConfirmation = Snackbar.make(
                        view,
                        "Are you sure?",
                        BaseTransientBottomBar.LENGTH_SHORT
                );
                deleteConfirmation.setAction("Confirm Deletion", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // sever the child from the tree
                        Deck parent = flattenedList.get(parentIndex);
                        Deck child = flattenedList.get(deckIndex);
                        parent.children.remove(child);
                        // write to file
                        masterDeck.doSortChildren(); // sort alphabetically by name
                        List<Deck> filePackage = new ArrayList<Deck>();
                        filePackage.add(masterDeck);
                        masterDeckManager.writeObjectsToFile(filePackage);
                        // navigate back two screens
                        ActionAddEditDeckFragmentToHomeFragment action = AddEditDeckFragmentDirections.actionAddEditDeckFragmentToHomeFragment(false);
                        NavHostFragment.findNavController(AddEditDeckFragment.this).navigate(action);
                    }
                });
                deleteConfirmation.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.red));
                deleteConfirmation.show();
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
                Deck childDeck;
                // add or edit?
                if (isNew) {
                    // create and attach
                    childDeck = new Deck(name);
                    parentDeck.children.add(childDeck);
                } else {
                    // get edited deck
                    childDeck = flattenedList.get(deckIndex);
                    // ensure did not set parent to self or any of self's children
                    // this sort of self-referential error will lead to the deck falling off from the tree: data loss
                    if (childDeck.getFlattenedList().contains(parentDeck)) {
                        Snackbar.make(
                                requireActivity().findViewById(android.R.id.content), // get root
                                "You can't make " + name + " be a child of itself or any of " + name + "'s children.",
                                BaseTransientBottomBar.LENGTH_SHORT
                        ).show(); // immediately show
                        return; // break out
                    }
                    // set new name
                    childDeck.name = name;
                    // add to new parent's children
                    parentDeck.children.add(childDeck);
                    // remove from old parent's children
                    flattenedList.get(parentIndex).children.remove(childDeck);
                }
                // write to file
                masterDeck.doSortChildren(); // sort alphabetically by name
                List<Deck> filePackage = new ArrayList<Deck>();
                filePackage.add(masterDeck);
                masterDeckManager.writeObjectsToFile(filePackage);
                // get indices
                List<Deck> flattenedList = masterDeck.getFlattenedList();
                deckIndex = flattenedList.indexOf(childDeck);
                // find the clicked deck's parent; get its position in the flattened list
                List<Integer> parentPointers = masterDeck.getHierarchy();
                parentIndex = parentPointers.get(deckIndex); // returns an Integer
                // navigate back
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }
}