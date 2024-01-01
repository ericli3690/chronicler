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

        List<Deck> decks = new ArrayList<Deck>();
//        Deck deck1 = new Deck("One");
//        deck1.children.add(new Deck("One One"));
//        deck1.children.add(new Deck("One Two"));
//
////        FileManager fm = new FileManager("test", getContext(), getActivity());
////        fm.writeObjectToFile(deck1);
//
//        decks.add(deck1);
        decks.add(new Deck("Two"));
        decks.add(new Deck("Three"));

        // get recyclerview
        RecyclerView deckRv = binding.fragmentHomeRv;
        // set layout
        deckRv.setLayoutManager(new LinearLayoutManager(getContext()));
        // set divider
        deckRv.addItemDecoration(new DividerItemDecoration(deckRv.getContext(), DividerItemDecoration.VERTICAL));
        // set adapter
        deckRv.setAdapter(new HomeRecyclerViewAdapter(decks, getContext(), getActivity()));

        binding.fragmentHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_homeFragment_to_addEditDeckFragment);
            }
        });
    }
}