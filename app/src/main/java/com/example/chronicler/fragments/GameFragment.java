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

// game fragment!
// the critical center of the entire app
// this is the purpose of the app: allowing users to play the history trivia game
// all the deck and card editing powers are in service of this
public class GameFragment extends Fragment {

    // ui control
    private FragmentGameBinding binding;
    // informationa bout which deck is being played
    private int deckIndex;
    private int parentIndex;
    private Deck deck;
    // information about how the game is progressing
    private String gameOrderString;
    private List<Card> gameOrder;
    private int currentObscured;
    // useful information and objects being used
    private GameTimelineRecyclerViewAdapter adapter;
    private CardChronologicalList chronologicalList;
    // important global variables
    private Deck masterDeck;
    private FileManager<Deck> masterDeckManager;
    private SettingsFile settingsFile;
    // controls whether the user can press buttons, to ensure they don't interrupt the game at inopportune moments
    private boolean deactivateButtons;

    // stats

    // Highest score ever achieved for the selected deck (see Deck)
    // Highest streak ever achieved for the selected deck (see Deck)
    // Current running streak
    // Current score during this game session

    private int streak;
    private int score;

    // the scoreboard currently being shown
    private int currentlyDisplayedScoreboard;

    // mandatory android startup method for binding
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // handle binding
        binding = FragmentGameBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // main:
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // retrieve arguments and information from bundle
        this.deckIndex = GameFragmentArgs.fromBundle(getArguments()).getDeckIndex();
        this.parentIndex = GameFragmentArgs.fromBundle(getArguments()).getParentIndex();
        this.gameOrderString = GameFragmentArgs.fromBundle(getArguments()).getGameOrderString();
        this.currentObscured = GameFragmentArgs.fromBundle(getArguments()).getCurrentObscured();
        this.score = GameFragmentArgs.fromBundle(getArguments()).getScore();
        this.streak = GameFragmentArgs.fromBundle(getArguments()).getStreak();
        // grab data
        masterDeck = ((MainActivity) requireActivity()).masterDeck;
        masterDeckManager = ((MainActivity) requireActivity()).masterDeckManager;
        settingsFile = ((MainActivity) requireActivity()).settingsFile;
        // activate buttons so the user can press them
        this.deactivateButtons = false;

        //// get deck name
        // get deck
        deck = masterDeck.getFlattenedList().get(this.deckIndex);
        // set toolbar
        ((Toolbar) requireActivity().findViewById(R.id.activity_main_toolbar)).setTitle(
                "Game: " + deck.name
        );

        // back button functionality
        // ensure the user does not accidentally end the game, show a confirmation message
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
                // return!
                NavHostFragment.findNavController(GameFragment.this).navigate(
                        GameFragmentDirections.actionGameFragmentToDeckFragment(deckIndex, parentIndex)
                );
                this.setEnabled(false); // disable this back function so the next one can take over
            }
        });

        // will set current cards being shown
        List<Card> initialTwo = new ArrayList<Card>();
        // will use just the first two
        // deckFragment guarantees these both exist, or else the GameFragment would not have opened at all

        // get cards from the deck
        // get the chronological list for use in the difficulty algorithm
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
            int difficulty = Math.max(1, (int) Math.floor(unroundedDifficulty));
            // prepare output order of cards
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
            // get the first two
            initialTwo.add(gameOrder.get(0));
            initialTwo.add(gameOrder.get(1));
            // the one on the bottom of the game is at index 1
            currentObscured = 1;
        } else {
            // otherwise, game order already exists, read it
            // no need to run the difficulty algorithm again
            String[] gameOrderStringList = gameOrderString.split(" ");
            gameOrder = new ArrayList<Card>();
            for (String stringToConvert : gameOrderStringList) {
                gameOrder.add( // add to the game order...
                        chronologicalList.get( // ...from the chronological list, the card at...
                                Integer.parseInt( // ...the integer...
                                        stringToConvert // ...index that is next
                                )
                        )
                );
            }
            // get the first two so they can be shown
            initialTwo.add(gameOrder.get(currentObscured-1));
            initialTwo.add(gameOrder.get(currentObscured));
        }
        // get recyclerview for list
        RecyclerView cardRv = binding.fragmentGameRv;
        // set layout to be linear
        cardRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        // set adapter, show the initial two
        adapter = new GameTimelineRecyclerViewAdapter(
                initialTwo, requireContext(), settingsFile
        );
        cardRv.setAdapter(adapter);

        // other buttons: before, during, after
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
        // the timeline button
        binding.fragmentGameTimeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (deactivateButtons) { // if something is in progress, stop
                    return;
                }
                // package up game order
                List<String> gameOrderStringList = new ArrayList<String>();
                for (Card card : gameOrder) {
                    gameOrderStringList.add( // add to the string list...
                            Integer.toString( // ...the integer...
                                    chronologicalList.indexOf( // ...location of the card... (this is mostly efficient since it is a binary search)
                                            card // ...that is next
                                    )
                            )
                    );
                }
                // turn it into a string
                gameOrderString = String.join(" ", gameOrderStringList);
                // send it to the timeline screen so that the progress of the game is saved
                NavHostFragment.findNavController(GameFragment.this).navigate(
                        GameFragmentDirections.actionGameFragmentToTimelineFragment(deckIndex, parentIndex, false, gameOrderString, currentObscured, score, streak)
                );
            }
        });

        // show high score ui
        Handler scoreboardHandler = new Handler(Looper.getMainLooper());
        currentlyDisplayedScoreboard = 0;
        // will run the following over and over:
        Runnable nextScoreboard = new Runnable() {
            @Override
            public void run() {
                // switch statement it keeps rotating through
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
                // loop back if at end
                if (currentlyDisplayedScoreboard == 4) {
                    currentlyDisplayedScoreboard = 0;
                }
                // delay by 2s
                // hardcoded delay, can add customization in the future
                scoreboardHandler.postDelayed(this, 2000);
            }
        };

        // start it!
        nextScoreboard.run();
    }

    // the user has provided an answer!
    public void goToNextCardInGame(int isBeforeDuringOrAfter) {
        if (deactivateButtons) { // if something is in progress, stop
            return;
        }
        // show check or x mark! first, though, hide the "vs" icon
        binding.fragmentGameVs.setVisibility(View.GONE);
        // prep sound
        MediaPlayer player;
        if (adapter.cards.get(1).date.isLaterThan(adapter.cards.get(0).date) == isBeforeDuringOrAfter) {
            // success: the user answered correctly!
            binding.fragmentGameCheck.setVisibility(View.VISIBLE); // show check
            // stats
            streak++;
            score++;
            // high streak
            if (streak > deck.highStreak) {
                deck.highStreak++;
                // write to file
                masterDeckManager.writeSingleObjectToFile(masterDeck);
            }
            // high score
            if (score > deck.highScore) {
                deck.highScore++;
                // write to file
                masterDeckManager.writeSingleObjectToFile(masterDeck);
            }
            // prepare victory sound
            player = MediaPlayer.create(requireContext(), R.raw.correct);
        } else {
            // failure: the user answered wrong.
            binding.fragmentGameCross.setVisibility(View.VISIBLE); // show x
            // stats
            streak = 0;
            // prepare buzzer sound
            player = MediaPlayer.create(requireContext(), R.raw.incorrect);
        }
        // set volume
        // must do it logarithmically
        float logVolume = (float) (1 - Math.log(100-settingsFile.volume)/Math.log(100));
        player.setVolume(logVolume, logVolume);
        // play sound
        player.start();
        // show the obscured card for two seconds, prevent user from doing anything by deactivating buttons
        adapter.obscure = false;
        this.deactivateButtons = true;
        // render on screen
        adapter.notifyItemChanged(1);
        // then unshow it and move on using a handler:
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // try to go to next card
                currentObscured++;
                // if too high...
                if (currentObscured >= gameOrder.size()) {
                    // we have reached the end of the game
                    Snackbar.make(
                            requireActivity().findViewById(android.R.id.content), // get root
                            "Congratulations! You finished the game! Press back to return to the deck screen.",
                            BaseTransientBottomBar.LENGTH_SHORT
                    ).show(); // immediately show
                } else {
                    // going to next card!
                    // therefore rehide and revert ui, ex. show "vs" icon again
                    adapter.obscure = true;
                    binding.fragmentGameVs.setVisibility(View.VISIBLE);
                    binding.fragmentGameCheck.setVisibility(View.GONE);
                    binding.fragmentGameCross.setVisibility(View.GONE);
                    // scoot the card on the bottom up to the top
                    adapter.cards.set(
                            0,
                            adapter.cards.get(1)
                    );
                    // add the next card in the queue to the bottom
                    adapter.cards.set(
                            1,
                            gameOrder.get(currentObscured)
                    );
                    // reload ui
                    adapter.notifyItemChanged(0);
                    adapter.notifyItemChanged(1);
                    // reactivate buttons so the user can use them again
                    deactivateButtons = false;
                    // play continues!
                }
            }
        }, 2000); // this all takes two seconds to kick in so that the answer is shown for a little bit
    }
}