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

// the main fragment that displays on the home page
// shows all the decks
public class HomeFragment extends Fragment {

    // ui control
    private FragmentHomeBinding binding;

    // android-required method for initialization
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // handle binding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // main:
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // grab data from activity
        Deck masterDeck = ((MainActivity) requireActivity()).masterDeck;

        // set title
        ((Toolbar) requireActivity().findViewById(R.id.activity_main_toolbar)).setTitle("Chronicler");

        // get recyclerview for list
        RecyclerView deckRv = binding.fragmentHomeRv;
        // set layout as linear, top to bottom
        deckRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        // show a divider
        deckRv.addItemDecoration(new DividerItemDecoration(deckRv.getContext(), DividerItemDecoration.VERTICAL));
        // set adapter, which shows a list of decks
        HomeRecyclerViewAdapter adapter = new HomeRecyclerViewAdapter(masterDeck, masterDeck, requireContext(), requireActivity(), this);
        deckRv.setAdapter(adapter);

        // set onclicks for the add and settings buttons
        // each navigates to their respective screen using navhost
        binding.fragmentHomeAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(HomeFragment.this).navigate(
                        HomeFragmentDirections.actionHomeFragmentToAddEditDeckFragment(true, 0, -1)
                );
            }
        });
        binding.fragmentHomeSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(HomeFragment.this).navigate(
                        HomeFragmentDirections.actionHomeFragmentToSettingsFragment(false)
                );
            }
        });
    }
}