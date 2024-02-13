package com.example.chronicler.adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;

import com.example.chronicler.R;
import com.example.chronicler.datatypes.Card;
import com.example.chronicler.datatypes.SettingsFile;

import java.util.ArrayList;
import java.util.List;

public class GameTimelineRecyclerViewAdapter extends TimelineRecyclerViewAdapter {

    public List<Card> cards;
    public List<Card> flippedCards;
    public boolean obscure;
    private Context context;
    private SettingsFile settingsFile;

    public GameTimelineRecyclerViewAdapter(List<Card> cards, Context context, SettingsFile settingsFile) {
        super(context);
        this.cards = cards;
        this.obscure = true;
        this.flippedCards = new ArrayList<Card>();
        this.context = context;
        this.settingsFile = settingsFile;
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        // get card instance associated with this viewholder
        Card card = cards.get(position);
        // set text
        viewHolder.eventTv.setText(card.event);
        if (this.obscure && position == 1) {
            viewHolder.dateTv.setText("██████████");
        } else {
            viewHolder.dateTv.setText(card.date.toString());
        }
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
                notifyItemChanged(cards.indexOf(card));
                // play sound
                MediaPlayer player = MediaPlayer.create(context, R.raw.flip);
                // set volume
                // must do it logarithmically
                float logVolume = (float) (1 - Math.log(100-settingsFile.volume)/Math.log(100));
                player.setVolume(logVolume, logVolume);
                player.start();
            }
        });
    }

}
