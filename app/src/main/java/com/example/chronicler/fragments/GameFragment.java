package com.example.chronicler.fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chronicler.MainActivity;
import com.example.chronicler.R;
import com.example.chronicler.adapters.GameTimelineRecyclerViewAdapter;
import com.example.chronicler.adapters.TimelineRecyclerViewAdapter;
import com.example.chronicler.databinding.FragmentGameBinding;
import com.example.chronicler.datatypes.Card;
import com.example.chronicler.datatypes.CardChronologicalList;
import com.example.chronicler.datatypes.CardHeap;
import com.example.chronicler.datatypes.Deck;
import com.example.chronicler.datatypes.SettingsFile;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameFragment extends Fragment {

    private FragmentGameBinding binding;
    private int deckIndex;
    private int parentIndex;
    private List<Integer> gameOrder;
    private int currentObscured;
    private GameTimelineRecyclerViewAdapter adapter;
    private CardChronologicalList chronologicalList;
    private boolean deactivateButtons;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // handle binding
        binding = FragmentGameBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // retrieve arguments from bundle
        this.deckIndex = GameFragmentArgs.fromBundle(getArguments()).getDeckIndex();
        this.parentIndex = GameFragmentArgs.fromBundle(getArguments()).getParentIndex();
        // grab data
        Deck masterDeck = ((MainActivity) requireActivity()).masterDeck;
        SettingsFile settingsFile = ((MainActivity) requireActivity()).settingsFile;
        // activate buttons
        this.deactivateButtons = false;

        //// get deck name
        // get deck
        Deck deck = masterDeck.getFlattenedList().get(this.deckIndex);
        // set toolbar
        ((Toolbar) requireActivity().findViewById(R.id.activity_main_toolbar)).setTitle(
                "Game: " + deck.name
        );

        // back button
        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Snackbar exitConfirmation = Snackbar.make(
                        view,
                        "Are you sure?",
                        BaseTransientBottomBar.LENGTH_SHORT
                );
                exitConfirmation.setAction("End Game", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // have to abstract this away so that this.setEnabled() still refers to the enclosing this in the goBack method
                        goBack();
                    }
                });
                exitConfirmation.show();

            }
            private void goBack() {
                NavHostFragment.findNavController(GameFragment.this).navigate(
                        GameFragmentDirections.actionGameFragmentToDeckFragment(deckIndex, parentIndex)
                );
                this.setEnabled(false);
            }
        });

        // get cards
        // create one chronological list for reference
        CardHeap allCards = deck.getAllCards();
        chronologicalList = allCards.getChronologicalList();

        // THE DIFFICULTY ALGORITHM
        // create another chronological list for working and whittling down
        CardChronologicalList workingList = new CardChronologicalList(chronologicalList);
        // get difficulty
        float percentDifficulty = settingsFile.percentDifficulty;
        // change it to its proper form for use in the algorithm
        // dont allow values lower than 20%, or else the difficulty algorithm makes the game no longer random
        float fixedPercentDifficulty = 100.0f - (percentDifficulty * 0.8f);
        // turn it into percent (ex. 0.66) then multiply by amount of cards
        float unroundedDifficulty = ((float) chronologicalList.size()) * fixedPercentDifficulty / 100.0f;
        // round it down: now difficulty stores an integer equal to approximately [percentDifficulty] of the cards
        int difficulty = (int) Math.floor(unroundedDifficulty);
        // output
        gameOrder = new ArrayList<Integer>();
        // generate a random number from 0 to # of cards
        Random random = new Random();
        int nextCardIndex = random.nextInt(chronologicalList.size());
        // then generate the cards on each game step, destroying the working list as we go
        while (workingList.size() > 0) {
            gameOrder.add(nextCardIndex);
            workingList.remove(nextCardIndex);
            int left = Math.max(0, nextCardIndex-difficulty);
            int right = Math.max(nextCardIndex+difficulty, workingList.size());

            // finally, select a card that is at most [difficulty] cards on either side of the current index
            // INCLUDES handling the fact that the central index has been removed and that the list is shifted
            nextCardIndex = left + random.nextInt(right-left); // do not add one to the range
        }
        // game order is now a list of integers that obey the difficulty rule
        // and which includes every index in the chronological list

        // set current
        List<Card> initialTwo = new ArrayList<Card>();
        // use just the first two
        // deckFragment guarantees these both exist
        initialTwo.add(chronologicalList.get(0));
        initialTwo.add(chronologicalList.get(1));
        currentObscured = 1;

        // TODO when we activate next card thing, deactivate all buttons on screen using a CURRENTLYDOINGSTUFF flag

        // TODO if we go to the timeline screen and back, this screen will reload
        // TODO undoing the gameorder algorithm and restarting the game
        // TODO we must make the game's state persist

        // TODO handle game logic here or in gametimelinerecyclerviewadapter

        // display first two
        // get recyclerview
        RecyclerView cardRv = binding.fragmentGameRv;
        // set layout
        cardRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        // set adapter
        adapter = new GameTimelineRecyclerViewAdapter(
                initialTwo
        );
        cardRv.setAdapter(adapter);

        // other buttons

    }

    private void goToNextCardInGame() { // TODO handle x or check symbol in center; include boolean arg
        // show the obscured card for two seconds, prevent user from doing anything
        adapter.obscure = false;
        this.deactivateButtons = true;
        adapter.notifyItemChanged(1);
        // then unshow it and move on
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // next card?
                currentObscured++;
                // if too high...
                if (currentObscured >= gameOrder.size()) {
                    // ending
                    // TODO on end
                } else {
                    // going to next card
                    // therefore rehide
                    adapter.obscure = true;
                    // scoot 1 to 0
                    adapter.cards.set(
                            0,
                            adapter.cards.get(1)
                    );
                    // add next to 1
                    adapter.cards.set(
                            1,
                            chronologicalList.get(gameOrder.get(currentObscured))
                    );
                    // reload
                    adapter.notifyItemChanged(0);
                    adapter.notifyItemChanged(1);
                    // reactivate buttons
                    deactivateButtons = false;
                }
            }
        }, 2000);
    }
}