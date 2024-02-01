package com.example.chronicler.adapters;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;

import com.example.chronicler.R;
import com.example.chronicler.datatypes.Card;

import java.util.ArrayList;
import java.util.List;

public class GameTimelineRecyclerViewAdapter extends TimelineRecyclerViewAdapter {

    private List<Card> cards;
    private List<Card> renderedCards;
    private boolean obscure;
    private int currentObscured;
    private List<Card> flippedCards;

    public GameTimelineRecyclerViewAdapter(List<Card> cards) {
        this.cards = cards;
        // use just the first two
        // deckFragment guarantees these both exist
        this.renderedCards = new ArrayList<Card>();
        this.renderedCards.add(this.cards.get(0));
        this.renderedCards.add(this.cards.get(1));
        // counter
        this.obscure = true;
        this.currentObscured = 1; // TODO maybe move this and game flow control to gamefragment
        // flips
        this.flippedCards = new ArrayList<Card>();
    }

    @Override
    public int getItemCount() {
        return renderedCards.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        // get card instance associated with this viewholder
        Card card = renderedCards.get(position);
        // set text
        if (this.obscure && position == 1) {
            viewHolder.eventTv.setText("██████████");
        } else {
            viewHolder.eventTv.setText(card.event);
        }
        viewHolder.dateTv.setText(card.date.toString());
        viewHolder.infoTv.setText(card.info);
        // hide checkboxes; child classes can toggle back on
        viewHolder.checkBox.setVisibility(View.INVISIBLE);
        // handle if flip
        // toggle visibilities
        if (this.flippedCards.contains(card)) {
            viewHolder.eventTv.setVisibility(View.GONE);
            viewHolder.dateTv.setVisibility(View.GONE);
            viewHolder.infoTv.setVisibility(View.VISIBLE);
            viewHolder.background.setBackgroundResource(R.drawable.sub_card_row_backing_dark);
        } else {
            viewHolder.eventTv.setVisibility(View.VISIBLE);
            viewHolder.dateTv.setVisibility(View.VISIBLE);
            viewHolder.infoTv.setVisibility(View.GONE);
            viewHolder.background.setBackgroundResource(R.drawable.sub_card_row_backing);
        }

        // set on click to result in flip
        viewHolder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // flip flipped status
                if (flippedCards.contains(card)) {
                    flippedCards.remove(card);
                } else {
                    flippedCards.add(card);
                }
                // update on screen
                notifyItemChanged(renderedCards.indexOf(card));
            }
        });
    }

    public void goToNextCardInGame() {
        // prepare variable to return
        boolean gameState;
        // show the obscured card for two seconds
        this.obscure = false;
        notifyItemChanged(1); // second item in UI should be rerendered
        // then unshow it
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // try to increment to next
                currentObscured++;
                // if too high...
                if (currentObscured >= cards.size()) {
                    // game is ending
                    return false;
                } else {
                    // going to next step
                    // therefore rehide
                    obscure = true;
                    // move 1 to 0
                    renderedCards.set(
                            0,
                            renderedCards.get(1)
                    );
                    // add next to 1
                    renderedCards.set(
                            1,
                            cards.get(currentObscured)
                    );
                    // reload
                    notifyItemChanged(0);
                    notifyItemChanged(1);
                    // we're still going
                    return true;
                }
            }
        }, 2000); // delay time
    }

}
