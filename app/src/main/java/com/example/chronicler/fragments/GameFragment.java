package com.example.chronicler.fragments;

import android.graphics.Typeface;
import android.media.MediaPlayer;
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
import android.util.Log;
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
import com.example.chronicler.functions.FileManager;
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
    private List<Card> gameOrder;
    private int currentObscured;
    private GameTimelineRecyclerViewAdapter adapter;
    private CardChronologicalList chronologicalList;
    private Deck masterDeck;
    private FileManager<Deck> masterDeckManager;
    private SettingsFile settingsFile;
    private boolean deactivateButtons;

    // stats

    // Highest score ever achieved for the selected deck (see Deck)
    // Highest streak ever achieved for the selected deck (see Deck)
    // Current running streak
    // Current score during this game session

    private int streak;
    private int score;
    private int currentlyDisplayedScoreboard;

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
        masterDeck = ((MainActivity) requireActivity()).masterDeck;
        masterDeckManager = ((MainActivity) requireActivity()).masterDeckManager;
        settingsFile = ((MainActivity) requireActivity()).settingsFile;
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

        // get cards
        // create one chronological list for reference
        CardHeap allCards = deck.getAllCards();
        chronologicalList = allCards.getChronologicalList();

        if (gameOrderString.length() == 0) {
            // game order does not exist yet, this is a new game
            // therefore run...
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
            gameOrder = new ArrayList<Card>();
            // generate a random number from 0 to # of cards
            Random random = new Random();
            int nextCardIndex = random.nextInt(chronologicalList.size());
            // then generate the cards on each game step, destroying the working list as we go
            while (true) {
                gameOrder.add(workingList.get(nextCardIndex));
                workingList.remove(nextCardIndex);
                int left = Math.max(0, nextCardIndex-difficulty);
                int right = Math.min(nextCardIndex+difficulty, workingList.size());

                if (workingList.size() == 0) {
                    break;
                }

                // finally, select a card that is at most [difficulty] cards on either side of the current index
                // INCLUDES handling the fact that the central index has been removed and that the list is shifted
                nextCardIndex = left + random.nextInt(right-left); // do not add one to the range
            }
            // game order is now a list of integers that obey the difficulty rule
            // and which includes every index in the chronological list
            initialTwo.add(gameOrder.get(0));
            initialTwo.add(gameOrder.get(1));
            currentObscured = 1;
        } else {
            // game order already exists, read it
            String[] gameOrderStringList = gameOrderString.split(" ");
            gameOrder = new ArrayList<Card>();
            for (String stringToConvert : gameOrderStringList) {
                gameOrder.add( // add to the game order...
                        chronologicalList.get( // from the chronological list...
                                Integer.parseInt( // the integer...
                                        stringToConvert // ...index that is next
                                )
                        )
                );
            }
            initialTwo.add(gameOrder.get(currentObscured-1));
            initialTwo.add(gameOrder.get(currentObscured));
        }

        // display first two
        // get recyclerview
        RecyclerView cardRv = binding.fragmentGameRv;
        // set layout
        cardRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        // set adapter
        adapter = new GameTimelineRecyclerViewAdapter(
                initialTwo, requireContext(), settingsFile
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
                for (Card card : gameOrder) {
                    gameOrderStringList.add( // add to the string list...
                            Integer.toString( // the integer...
                                    chronologicalList.indexOf( // ...location of the card... (this is mostly efficient since it is a binary search)
                                            card // ...that is next
                                    )
                            )
                    );
                }
                gameOrderString = String.join(" ", gameOrderStringList);
                // send it to timeline so state is saved
                NavHostFragment.findNavController(GameFragment.this).navigate(
                        GameFragmentDirections.actionGameFragmentToTimelineFragment(deckIndex, parentIndex, false, gameOrderString, currentObscured)
                );
            }
        });

        // show high score ui
        Handler scoreboardHandler = new Handler(Looper.getMainLooper());
        currentlyDisplayedScoreboard = 0;
        Runnable nextScoreboard = new Runnable() {
            @Override
            public void run() {
                switch (currentlyDisplayedScoreboard) {
                    case 0:
                        binding.fragmentGameScoreboard.setText("High Streak: " + deck.highStreak);
                        binding.fragmentGameScoreboard.setTypeface(null, Typeface.BOLD_ITALIC);
                        break;
                    case 1:
                        binding.fragmentGameScoreboard.setText("High Score: " + deck.highScore);
                        binding.fragmentGameScoreboard.setTypeface(null, Typeface.BOLD);
                        break;
                    case 2:
                        binding.fragmentGameScoreboard.setText("Current Streak: " + streak);
                        binding.fragmentGameScoreboard.setTypeface(null, Typeface.ITALIC);
                        break;
                    case 3:
                    default:
                        binding.fragmentGameScoreboard.setText("Current Score: " + score);
                        binding.fragmentGameScoreboard.setTypeface(null, Typeface.NORMAL);
                }
                // increment
                currentlyDisplayedScoreboard++;
                if (currentlyDisplayedScoreboard == 4) {
                    currentlyDisplayedScoreboard = 0;
                }
                // delay
                scoreboardHandler.postDelayed(this, 2000); // hardcoded delay, can add customization in the future
            }
        };

        // start it!
        nextScoreboard.run();
    }

    public void goToNextCardInGame(int isBeforeDuringOrAfter) {
        if (deactivateButtons) {
            return;
        }
        // show check or x mark, do stats
        binding.fragmentGameVs.setVisibility(View.GONE);
        // prep sound
        MediaPlayer player;
        if (adapter.cards.get(1).date.isLaterThan(adapter.cards.get(0).date) == isBeforeDuringOrAfter) {
            // success
            binding.fragmentGameCheck.setVisibility(View.VISIBLE); // show check
            // stats
            streak++;
            score++;
            if (streak > deck.highStreak) {
                deck.highStreak++;
                // write to file
                masterDeckManager.writeSingleObjectToFile(masterDeck);
            }
            if (score > deck.highScore) {
                deck.highScore++;
                // write to file
                masterDeckManager.writeSingleObjectToFile(masterDeck);
            }
            // sound
            player = MediaPlayer.create(requireContext(), R.raw.correct);
        } else {
            // failure
            binding.fragmentGameCross.setVisibility(View.VISIBLE); // show x
            // stats
            streak = 0;
            // sound
            player = MediaPlayer.create(requireContext(), R.raw.incorrect);
        }
        // set volume
        // must do it logarithmically
        float logVolume = (float) (1 - Math.log(100-settingsFile.volume)/Math.log(100));
        player.setVolume(logVolume, logVolume);
        // play sound
        player.start();
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
                    Snackbar.make(
                            requireActivity().findViewById(android.R.id.content), // get root
                            "Congratulations! You finished the game! Press back to return to the deck screen.",
                            BaseTransientBottomBar.LENGTH_SHORT
                    ).show(); // immediately show
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
                            gameOrder.get(currentObscured)
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