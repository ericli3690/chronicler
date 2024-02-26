package com.example.chronicler.fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chronicler.MainActivity;
import com.example.chronicler.R;
import com.example.chronicler.databinding.FragmentAddEditCardBinding;
import com.example.chronicler.datatypes.Card;
import com.example.chronicler.datatypes.CardChronologicalList;
import com.example.chronicler.datatypes.CardDate;
import com.example.chronicler.datatypes.Deck;
import com.example.chronicler.functions.DeleteConfirmation;
import com.example.chronicler.functions.FileManager;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// screen for either adding or editings cards
// depends on which parameters it is provided on creation
// if it is supplied with information about a card, it will allow the user to edit it
// otherwise, it will let the user create a new card
// can also allow the user to edit multiple cards at once: bulk edit
public class AddEditCardFragment extends Fragment {

    // ui control
    private FragmentAddEditCardBinding binding;
    // which deck does this belong to?
    private int deckIndex;
    private int parentIndex;
    // which cards are we editing
    private String cardIndices;
    // what is the NEW parent deck index?
    private int checkedIndex;
    // get all decks and cards
    private Deck masterDeck;
    private FileManager<Deck> masterDeckManager;

    // android-required ui initialization method
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // handle binding
        binding = FragmentAddEditCardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // main:
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // back button functionality: return to deck
        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavHostFragment.findNavController(AddEditCardFragment.this).navigate(
                        AddEditCardFragmentDirections.actionAddEditCardFragmentToDeckFragment(deckIndex, parentIndex)
                );
                this.setEnabled(false); // disable this back function so the next one can take over
            }
        });

        // retrieve arguments from bundle
        this.deckIndex = AddEditCardFragmentArgs.fromBundle(getArguments()).getDeckIndex();
        this.parentIndex = AddEditCardFragmentArgs.fromBundle(getArguments()).getParentIndex();
        this.cardIndices = AddEditCardFragmentArgs.fromBundle(getArguments()).getCardIndices();
        // grab data
        masterDeckManager = ((MainActivity) requireActivity()).masterDeckManager;
        masterDeck = ((MainActivity) requireActivity()).masterDeck;
        List<Deck> flattenedList = masterDeck.getFlattenedList();
        // get the cards of this deck
        Deck deck = flattenedList.get(deckIndex);
        CardChronologicalList deckCards = deck.getAllCards().getChronologicalList();

        //// if is add card
        if (cardIndices.length() == 0) {
            // set toolbar as such
            ((Toolbar) requireActivity().findViewById(R.id.activity_main_toolbar)).setTitle("Add Card");

            // back button functionality: will need to return to deck fragment
            requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    NavHostFragment.findNavController(AddEditCardFragment.this).navigate(
                            AddEditCardFragmentDirections.actionAddEditCardFragmentToDeckFragment(deckIndex, parentIndex)
                    );
                    this.setEnabled(false); // disable this back function so the next one can take over
                }
            });
            // hide delete button
            binding.fragmentAddEditCardDelete.setVisibility(View.GONE);

            // no need to worry about the radiolist, hide it
            binding.fragmentAddEditCardLinearEdit.setVisibility(View.GONE);

            // done button onclick
            binding.fragmentAddEditCardDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // try to create a new card
                    Card newCard;
                    try {
                        newCard = getCardFromInputs(); // abstracted away so that edit (single) can also use it
                    } catch (NumberFormatException e) {
                        // the date was entered incorrectly
                        Snackbar.make(
                                requireActivity().findViewById(android.R.id.content), // get root
                                "The dates entered are invalid.",
                                BaseTransientBottomBar.LENGTH_SHORT
                        ).show(); // immediately show
                        return; // break out
                    }
                    // otherwise, successful! add the card
                    deck.cards.add(newCard);
                    // write to file
                    masterDeckManager.writeSingleObjectToFile(masterDeck);
                    // clear all textviews
                    // this allows the user to add another card instantly if they so choose
                    binding.fragmentAddEditCardEvent.setText("");
                    binding.fragmentAddEditCardDay.setText("");
                    binding.fragmentAddEditCardMonth.setText("");
                    binding.fragmentAddEditCardYear.setText("");
                    binding.fragmentAddEditCardInfo.setText("");
                    binding.fragmentAddEditCardTags.setText("");
                    // send success message
                    Snackbar.make(
                            requireActivity().findViewById(android.R.id.content), // get root
                            "Card created!",
                            BaseTransientBottomBar.LENGTH_SHORT
                    ).show(); // immediately show
                }
            });

        } else {
            //// else, is edit card

            // back button functionality
            // need to return to timeline
            requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    NavHostFragment.findNavController(AddEditCardFragment.this).navigate(
                            AddEditCardFragmentDirections.actionAddEditCardFragmentToTimelineFragment(deckIndex, parentIndex, true, "", 0, 0, 0)
                    );
                    this.setEnabled(false); // disable this back function so the next one can take over
                }
            });

            //// get cards list
            // split it apart
            String[] stringArrayCardIndices = cardIndices.split(" ");
            // convert to ints
            int[] intArrayCardIndices = new int[stringArrayCardIndices.length];
            for (int convertIndex = 0; convertIndex < stringArrayCardIndices.length; convertIndex++) {
                intArrayCardIndices[convertIndex] = Integer.parseInt(stringArrayCardIndices[convertIndex]);
            }
            // convert to cards
            List<Card> cards = new ArrayList<Card>();
            for (int cardIndex : intArrayCardIndices) {
                cards.add(deckCards.get(cardIndex));
            }
            // also prepare names of decks to be filled into the deck radio list
            Bundle radioListBundle = new Bundle();
            ArrayList<String> names = new ArrayList<String>();
            for (Deck deckInList : flattenedList) {
                names.add(deckInList.name);
            }
            radioListBundle.putStringArrayList("names", names);
            //// if there is only one, we are only editing ONE card
            if (cards.size() == 1) {
                //// single edit
                Card card = cards.get(0);
                // set toolbar accordingly
                ((Toolbar) requireActivity().findViewById(R.id.activity_main_toolbar)).setTitle(
                        "Edit Card: " + card.event
                );
                // set radio list
                Deck cardDirectOwner = deck.getDeckContainingCard(card);
                this.checkedIndex = flattenedList.indexOf(cardDirectOwner);
                radioListBundle.putInt("checked", this.checkedIndex);

                // set text, including name, date, etc.
                binding.fragmentAddEditCardEvent.setText(card.event);

                int intDay = card.date.day;
                int intMonth = card.date.month;
                int intYear = card.date.year;

                // if there is no day or month, show a blank
                binding.fragmentAddEditCardDay.setText(intDay != -1 ? String.valueOf(intDay) : "");
                binding.fragmentAddEditCardMonth.setText(intMonth != -1 ? String.valueOf(intMonth) : "");
                binding.fragmentAddEditCardYear.setText(intYear != -1 ? String.valueOf(intYear) : "");

                binding.fragmentAddEditCardInfo.setText(card.info);

                binding.fragmentAddEditCardTags.setText(
                        String.join(" ", card.tags)
                );

                // done button onclick functionality
                binding.fragmentAddEditCardDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // first of all, dont let them put cards in the masterdeck
                        if (checkedIndex == 0) {
                            Snackbar.make(
                                    requireActivity().findViewById(android.R.id.content), // get root
                                    "Cards must have a parent deck.",
                                    BaseTransientBottomBar.LENGTH_SHORT
                            ).show(); // immediately show
                            return; // break out
                        }
                        // otherwise, edit the card!
                        Card newCard;
                        try {
                            newCard = getCardFromInputs(); // abstracted away so that add can also use it
                        } catch (NumberFormatException e) {
                            // the date was entered incorrectly
                            Snackbar.make(
                                    requireActivity().findViewById(android.R.id.content), // get root
                                    "The date entered is invalid.",
                                    BaseTransientBottomBar.LENGTH_SHORT
                            ).show(); // immediately show
                            return; // break out
                        }
                        // delete old
                        cardDirectOwner.cards.remove(card);
                        // add new to the correct deck
                        flattenedList.get(checkedIndex).cards.add(newCard);
                        // commit
                        writeAndReturnToTimeline();
                    }
                });

            } else {
                //// else, we are bulk editing
                // set toolbar appropriately
                ((Toolbar) requireActivity().findViewById(R.id.activity_main_toolbar)).setTitle("Edit Cards: (multiple)");
                // set radio list to show nothing checked (in reality, multiple are selected)
                radioListBundle.putInt("checked", -1);
                this.checkedIndex = -1;
                // hide event, date, and info
                binding.fragmentAddEditCardLinearAdd.setVisibility(View.GONE);
                // then handle tags
                // show all the tags that all the cards selected have in common
                Set<String> commonTags = new HashSet<String>();
                // for every tag the first card has
                for (String tag : cards.get(0).tags) {
                    // check every other card
                    for (int cardIndex = 0; cardIndex < cards.size(); cardIndex++) {
                        // if they ALL contain this tag
                        if (cards.get(cardIndex).tags.contains(tag)) {
                            if (cardIndex == cards.size()-1) {
                                // then add it to commonTags
                                commonTags.add(tag);
                            } else {
                                continue;
                            }
                        } else {
                            // this tag is not in common across all selected cards
                            // proceed to next tag contained by the first card
                            break;
                        }
                    }
                }

                // yes, this is a nested loop, which is inefficient
                // but on average it should perform ok, as
                //      1. cards will generally have few tags, making the first loop short, and
                //      2. the moment a tag is NOT found, we break, making the second loop short
                // further optimization here is probably not worth the added codebase complexity

                // show the tags on the ui
                binding.fragmentAddEditCardTags.setText(
                        String.join(" ", commonTags)
                );

                // done button onclick functionality
                binding.fragmentAddEditCardDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // first of all, dont let them put cards in the masterdeck
                        if (checkedIndex == 0) {
                            Snackbar.make(
                                    requireActivity().findViewById(android.R.id.content), // get root
                                    "Cards must have a parent deck.",
                                    BaseTransientBottomBar.LENGTH_SHORT
                            ).show(); // immediately show
                            return; // break out
                        }
                        // get tags
                        String[] stringArrayTags = binding.fragmentAddEditCardTags.getText().toString().split(" ");
                        // put tags into a hashset
                        Set<String> potentiallyNewTags = new HashSet<String>();
                        Collections.addAll(potentiallyNewTags, stringArrayTags);
                        for (Card card : cards) {
                            // add them all; this will eliminate duplicates automatically due to card.tags being a set datastructure
                            card.tags.addAll(potentiallyNewTags);
                        }
                        if (checkedIndex == -1) {
                            // the user did not edit the parent deck
                            // we are done
                        } else {
                            // the user edited the parent deck
                            // delete all cards from their existing deck
                            locateAndDeleteAll(cards, deck);
                            // then readd them to the new deck
                            flattenedList.get(checkedIndex).cards.addAll(cards);
                        }
                        // commit
                        writeAndReturnToTimeline();
                    }
                });

            }
            // setup radio list
            // activate the subfragment
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_add_edit_card_radio_list, RadioListFragment.class, radioListBundle)
                    .commit();

            // listen to if it replies with a change in which was checked
            // if so, save it, to be used when done is pressed
            requireActivity().getSupportFragmentManager().setFragmentResultListener("key", this, new FragmentResultListener() {
                @Override
                public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                    checkedIndex = result.getInt("checked");
                }
            });

            // delete button onclick listener
            binding.fragmentAddEditCardDelete.setOnClickListener(new DeleteConfirmation(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // remove the cards that are being edited
                            locateAndDeleteAll(cards, deck);
                            // commit
                            writeAndReturnToTimeline();
                        }
                    },
                    requireContext()
            ));

            // code here will execute for edit (both cases)

            //

        }

        // code here will execute for all three

        //

    } // end of onViewCreated

    // grab the inputs
    // parse it and make sure its valid: validate it
    private Card getCardFromInputs() throws NumberFormatException {

        // prepare date variables
        int day;
        int month;
        int year;

        // get strings
        String stringDay = binding.fragmentAddEditCardDay.getText().toString();
        String stringMonth = binding.fragmentAddEditCardMonth.getText().toString();
        String stringYear = binding.fragmentAddEditCardYear.getText().toString();
        // year MUST be here, but month and day are optional
        if (stringYear.equals("")) {
            throw new NumberFormatException();
        }
        // try turning it into an integer
        year = Integer.parseInt(stringYear);
        // make sure its not in the future
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (year > currentYear) {
            throw new NumberFormatException();
        }
        // at this point, we can be sure year is valid
        // if the month is empty, end now
        if (stringMonth.equals("")) {
            month = -1;
            day = -1;
        } else {
            // else, make sure month works
            month = Integer.parseInt(stringMonth);
            if (CardDate.isInvalidMonth(month)) {
                throw new NumberFormatException();
            }
            // make sure its not in the future
            int currentMonth = Calendar.getInstance().get(Calendar.MONTH)+1; // counts from zero
            if (year == currentYear && month > currentMonth) {
                throw new NumberFormatException();
            }
            // at this point, we can be sure month is valid
            // if the day is empty, end now
            if (stringDay.equals("")) {
                day = -1;
            } else {
                // else, make sure day works
                day = Integer.parseInt(stringDay);
                // check that it is actually a day of this month and year
                if (CardDate.isInvalidDay(day, month, year)) {
                    throw new NumberFormatException();
                }
                // make sure its not in the future
                int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                if (year == currentYear && month == currentMonth && day > currentDay) {
                    throw new NumberFormatException();
                }
                // at this point, we can be sure day is valid
            }
        }

        // otherwise, dates are all valid!
        // turn it into a carddate
        CardDate date = new CardDate(day, month, year);

        // get other data
        String event = binding.fragmentAddEditCardEvent.getText().toString();
        String info = binding.fragmentAddEditCardInfo.getText().toString();
        String[] stringArrayTags = binding.fragmentAddEditCardTags.getText().toString().split(" ");

        // put tags into a hashset
        Set<String> tags = new HashSet<String>();
        Collections.addAll(tags, stringArrayTags);
        // make it into a new card and add it to the cards of the deck currently opened
        // voila! a new card
        return new Card(event, date, info, tags);
    }

    // commit method: writes to file and returns
    // this is used multiple times and is thus abstracted away
    private void writeAndReturnToTimeline() {
        // write to file
        masterDeckManager.writeSingleObjectToFile(masterDeck);
        // navigate back to the timeline
        NavHostFragment.findNavController(AddEditCardFragment.this).navigate(
                AddEditCardFragmentDirections.actionAddEditCardFragmentToTimelineFragment(deckIndex, parentIndex, true, "", 0, 0, 0)
        );
    }

    // search through all cards and untether all the cards from their decks
    private void locateAndDeleteAll(List<Card> cards, Deck deck) {
        for (Card card : cards) {
            Deck cardDirectOwner = deck.getDeckContainingCard(card);
            cardDirectOwner.cards.remove(card);
        }
    }
}