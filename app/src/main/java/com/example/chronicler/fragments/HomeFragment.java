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

import com.example.chronicler.MainActivity;
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
        // handle binding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // grab data
        Deck masterDeck = ((MainActivity) requireActivity()).masterDeck;

        // set title
        ((Toolbar) requireActivity().findViewById(R.id.activity_main_toolbar)).setTitle("Chronicler");

        // get recyclerview
        RecyclerView deckRv = binding.fragmentHomeRv;
        // set layout
        deckRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        // set divider
        deckRv.addItemDecoration(new DividerItemDecoration(deckRv.getContext(), DividerItemDecoration.VERTICAL));
        // set adapter
        HomeRecyclerViewAdapter adapter = new HomeRecyclerViewAdapter(masterDeck, masterDeck, requireContext(), requireActivity(), this);
        deckRv.setAdapter(adapter);

        // set onclicks
        binding.fragmentHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActionHomeFragmentToAddEditDeckFragment action = HomeFragmentDirections.actionHomeFragmentToAddEditDeckFragment(true, 0, -1);
                NavHostFragment.findNavController(HomeFragment.this).navigate(action);
            }
        });
    }
}