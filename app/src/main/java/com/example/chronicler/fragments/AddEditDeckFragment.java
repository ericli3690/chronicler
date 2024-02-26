package com.example.chronicler.fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chronicler.MainActivity;
import com.example.chronicler.R;
import com.example.chronicler.databinding.FragmentAddEditDeckBinding;
import com.example.chronicler.datatypes.Deck;
import com.example.chronicler.functions.DeleteConfirmation;
import com.example.chronicler.functions.FileManager;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

// screen where you can either add or edit decks
// depending on which parameters this screen is opened with, both can happen
public class AddEditDeckFragment extends Fragment {

    // ui control
    private FragmentAddEditDeckBinding binding;
    // controls whether this is adddeck or editdeck
    private boolean isNew;
    // what deck is this
    private int deckIndex;
    private int parentIndex;
    // information about complete file system
    private FileManager<Deck> masterDeckManager;
    private Deck masterDeck;
    // which deck is currently checked by the user on the parent deck selection list
    private int checkedIndex;

    // android-required ui initialization method
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // handle binding
        binding = FragmentAddEditDeckBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // main:
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // retrieve arguments from bundle
        this.isNew = AddEditDeckFragmentArgs.fromBundle(getArguments()).getIsNew();
        this.deckIndex = AddEditDeckFragmentArgs.fromBundle(getArguments()).getDeckIndex();
        this.parentIndex = AddEditDeckFragmentArgs.fromBundle(getArguments()).getParentIndex();

        // grab data
        masterDeckManager = ((MainActivity) requireActivity()).masterDeckManager;
        masterDeck = ((MainActivity) requireActivity()).masterDeck;
        List<Deck> flattenedList = masterDeck.getFlattenedList();

        // set back button functionality
        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavDirections action;
                // where it goes back to depends on whether this is adddeck or editdeck
                if (isNew) {
                    action = AddEditDeckFragmentDirections.actionAddEditDeckFragmentToHomeFragment(false);
                } else {
                    action = AddEditDeckFragmentDirections.actionAddEditDeckFragmentToDeckFragment(deckIndex, parentIndex);
                }
                NavHostFragment.findNavController(AddEditDeckFragment.this).navigate(action);
                this.setEnabled(false); // disable this back function so the next one can take over
            }
        });

        // set delete button visibility
        if (isNew) {
            binding.fragmentAddEditDeckDelete.setVisibility(View.GONE);
        }

        //// create parent deck radio list
        // generate programmatically from flattened list
        // and put in the radiolistfragment
        Bundle radioListBundle = new Bundle();

        // get names of all the decks
        // and give it to the radiolist
        ArrayList<String> names = new ArrayList<String>();
        for (Deck deck : flattenedList) {
            names.add(deck.name);
        }
        radioListBundle.putStringArrayList("names", names);

        // get selected radio; essentially, what is the current parent deck
        if (isNew) { // we are creating
            // save it and show it in the radio list
            radioListBundle.putInt("checked", 0);
            this.checkedIndex = 0;
        } else { // we are editing
            // set the title to be the name of hte deck we are editing
            binding.fragmentAddEditDeckName.setText(flattenedList.get(this.deckIndex).name);
            // likewise
            radioListBundle.putInt("checked", this.parentIndex);
            this.checkedIndex = this.parentIndex;
        }

        // activate the subfragment
        requireActivity().getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragment_add_edit_deck_radio_list, RadioListFragment.class, radioListBundle)
                .commit();

        // listen to if it replies with a change in which was checked
        // if so, save it, to be used when done is pressed
        requireActivity().getSupportFragmentManager().setFragmentResultListener("key", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                checkedIndex = result.getInt("checked");
            }
        });

        // set toolbar text
        ((Toolbar) requireActivity().findViewById(R.id.activity_main_toolbar)).setTitle(
                this.isNew ? "Add Deck" : "Edit Deck: " + flattenedList.get(this.deckIndex).name);

        //// delete button onclick
        // include a confirmation message
        binding.fragmentAddEditDeckDelete.setOnClickListener(new DeleteConfirmation(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // sever the child from the tree
                    Deck parent = flattenedList.get(parentIndex);
                    Deck child = flattenedList.get(deckIndex);
                    parent.children.remove(child);
                    // sort all decks alphabetically by name
                    masterDeck.doSortChildren();
                    // write to file
                    masterDeckManager.writeSingleObjectToFile(masterDeck);
                    // navigate back two screens
                    NavHostFragment.findNavController(AddEditDeckFragment.this).navigate(
                            AddEditDeckFragmentDirections.actionAddEditDeckFragmentToHomeFragment(false)
                    );
                }
            },
            requireContext() // needs some other android information
        ));

        //// done button onclick
        binding.fragmentAddEditDeckDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get name that was chosen
                String name = binding.fragmentAddEditDeckName.getText().toString();
                // get clicked parent deck
                Deck parentDeck = flattenedList.get(checkedIndex);
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
                masterDeck.doSortChildren();
                // sort decks alphabetically by name
                masterDeckManager.writeSingleObjectToFile(masterDeck);
                // get indices of new / edited deck
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