package com.example.chronicler.fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
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
    private String gameOrderString;
    private Deck deck;
    private List<Integer> gameOrder;
    private int currentObscured;
    private GameTimelineRecyclerViewAdapter adapter;
    private CardChronologicalList chronologicalList;
    private boolean deactivateButtons;

    // stats

    // Highest score ever achieved for the selected deck (see Deck)
    // Highest streak ever achieved for the selected deck (see Deck)
    // Current highest streak achieved during this game session
    // Current running streak
    // Current score during this game session

    private int sessionHighStreak;
    private int streak;
    private int score;

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
        this.gameOrderString = GameFragmentArgs.fromBundle(getArguments()).getGameOrderString();
        this.currentObscured = GameFragmentArgs.fromBundle(getArguments()).getCurrentObscured();
        // grab data
        Deck masterDeck = ((MainActivity) requireActivity()).masterDeck;
        SettingsFile settingsFile = ((MainActivity) requireActivity()).settingsFile;
        // activate buttons
        this.deactivateButtons = false;

        //// get deck name
        // get deck
        deck = masterDeck.getFlattenedList().get(this.deckIndex);
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

        // will set current
        List<Card> initialTwo = new ArrayList<Card>();
        // will use just the first two
        // deckFragment will guarantee these both exist

        if (gameOrderString.length() == 0) {
            // game order does not exist yet, this is a new game

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
            initialTwo.add(chronologicalList.get(gameOrder.get(0)));
            initialTwo.add(chronologicalList.get(gameOrder.get(1)));
            currentObscured = 1;
        } else {
            // game order already exists, read it
            String[] gameOrderStringList = gameOrderString.split(" ");
            gameOrder = new ArrayList<Integer>();
            for (int convertIndex = 0; convertIndex < gameOrderStringList.length; convertIndex++) {
                gameOrder.add(Integer.parseInt(gameOrderStringList[convertIndex]));
            }
            initialTwo.add(chronologicalList.get(gameOrder.get(currentObscured-1)));
            initialTwo.add(chronologicalList.get(gameOrder.get(currentObscured)));
        }

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
        binding.fragmentGameBefore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNextCardInGame(-1);
            }
        });
        binding.fragmentGameDuring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNextCardInGame(0);
            }
        });
        binding.fragmentGameAfter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNextCardInGame(1);
            }
        });
        binding.fragmentGameTimeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (deactivateButtons) {
                    return;
                }
                // package up game order
                List<String> gameOrderStringList = new ArrayList<String>();
                for (Integer gameIndex : gameOrder) {
                    gameOrderStringList.add(Integer.toString(gameIndex));
                }
                gameOrderString = String.join(" ", gameOrderStringList);
                // send it to timeline so state is saved
                NavHostFragment.findNavController(GameFragment.this).navigate(
                        DeckFragmentDirections.actionDeckFragmentToTimelineFragment(deckIndex, parentIndex, false, gameOrderString, currentObscured)
                );
            }
        });
    }

    public void goToNextCardInGame(int isBeforeDuringOrAfter) {
        if (deactivateButtons) {
            return;
        }
        // show check or x mark, do stats
        binding.fragmentGameVs.setVisibility(View.GONE);
        if (adapter.cards.get(0).date.isLaterThan(adapter.cards.get(1).date) == isBeforeDuringOrAfter) {
            // success
            binding.fragmentGameCheck.setVisibility(View.VISIBLE); // show check
            // stats
            streak++;
            score++;
            if (streak > sessionHighStreak) {
                sessionHighStreak++;
            }
            if (streak > deck.highStreak) {
                deck.highStreak++;
            }
            if (score > deck.highScore) {
                deck.highScore++;
            }
        } else {
            // failure
            binding.fragmentGameCross.setVisibility(View.VISIBLE); // show x
            // stats
            streak = 0;
        }
        // TODO display high score stats
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
                    // TODO on end; is game algorithm fully implemented?
                } else {
                    // going to next card
                    // therefore rehide and revert ui
                    adapter.obscure = true;
                    binding.fragmentGameVs.setVisibility(View.VISIBLE);
                    binding.fragmentGameCheck.setVisibility(View.GONE);
                    binding.fragmentGameCross.setVisibility(View.GONE);
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