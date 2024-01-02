package com.example.chronicler.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chronicler.adapters.HomeRecyclerViewAdapter;
import com.example.chronicler.R;
import com.example.chronicler.databinding.FragmentHomeBinding;
import com.example.chronicler.datatypes.Deck;
import com.example.chronicler.fragments.HomeFragmentDirections.ActionHomeFragmentToAddEditDeckFragment;
import com.example.chronicler.functions.FileManager;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((Toolbar) getActivity().findViewById(R.id.activity_main_toolbar)).setTitle("Chronicler"); // set title
        binding = FragmentHomeBinding.inflate(inflater, container, false); // get binding
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TESTING ONLY
        // TODO: delete later
//        resetDecks();

        // read decks from file
        FileManager<Deck> fileManager = new FileManager<Deck>("deck.txt", Deck.class, getContext(), getActivity());
        Deck MASTER_DECK = fileManager.readObjectsFromFile().get(0);
        MASTER_DECK.doSortChildren();

        // get recyclerview
        RecyclerView deckRv = binding.fragmentHomeRv;
        // set layout
        deckRv.setLayoutManager(new LinearLayoutManager(getContext()));
        // set divider
        deckRv.addItemDecoration(new DividerItemDecoration(deckRv.getContext(), DividerItemDecoration.VERTICAL));
        // set adapter
        deckRv.setAdapter(new HomeRecyclerViewAdapter(MASTER_DECK, MASTER_DECK, getContext(), getActivity(), this));

        // set onclicks
        binding.fragmentHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActionHomeFragmentToAddEditDeckFragment action = HomeFragmentDirections.actionHomeFragmentToAddEditDeckFragment(true, 0, -1);
                NavHostFragment.findNavController(HomeFragment.this).navigate(action);
            }
        });
    }

    // FOR TESTING ONLY
    // TODO: delete later
    private void resetDecks() {

        // create invisible master deck
        Deck MASTER_DECK = new Deck("(none)");

        // add children
        MASTER_DECK.children.add(new Deck("One"));
        MASTER_DECK.children.add(new Deck("Two"));
        MASTER_DECK.children.add(new Deck("Three"));
        MASTER_DECK.children.add(new Deck("Four"));

        // write to file
        MASTER_DECK.doSortChildren(); // sort alphabetically by name
        List<Deck> filePackage = new ArrayList<Deck>();
        filePackage.add(MASTER_DECK);
        FileManager<Deck> fileManager = new FileManager<Deck>("deck.txt", Deck.class, getContext(), getActivity());
        fileManager.writeObjectsToFile(filePackage);
    }
}