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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.isNew = AddEditDeckFragmentArgs.fromBundle(getArguments()).getIsNew(); // retrieve arguments
        ((Toolbar) getActivity().findViewById(R.id.activity_main_toolbar)).setTitle(this.isNew ? "Add Deck" : "Edit Deck"); // set title
        binding = FragmentAddEditDeckBinding.inflate(inflater, container, false); // get binding
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

        // finally, now that all are in the group, set (none) to be default checked
        radios.get(0).setChecked(true);

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
                Deck parentDeck;
                try {
                    parentDeck = flattenedList.get(radioIndex);
                } catch (Exception e) {
                    Snackbar.make(
                            getActivity().findViewById(android.R.id.content), // get root
                            "Deck Locator Error: Please try again later.",
                            BaseTransientBottomBar.LENGTH_SHORT
                    ).show(); // immediately show
                    return;
                }
                // create and attach
                Deck newDeck = new Deck(name);
                parentDeck.children.add(newDeck);
                // write to file
                MASTER_DECK.sortChildren(); // sort alphabetically by name
                List<Deck> filePackage = new ArrayList<Deck>();
                filePackage.add(MASTER_DECK);
                fileManager.writeObjectsToFile(filePackage);
                // navigate back
                ActionAddEditDeckFragmentToHomeFragment action = AddEditDeckFragmentDirections.actionAddEditDeckFragmentToHomeFragment(false);
                NavHostFragment.findNavController(AddEditDeckFragment.this).navigate(action);
            }
        });
    }
}