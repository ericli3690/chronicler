package com.example.chronicler.adapters;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;

import com.example.chronicler.datatypes.Card;
import com.example.chronicler.datatypes.CardChronologicalList;
import com.example.chronicler.datatypes.SettingsFile;

import java.util.ArrayList;

// main timeline adapter, is chronological
// shows up in the timeline screen
public class FullTimelineRecyclerViewAdapter extends ChronologicalTimelineRecyclerViewAdapter {

    // quick constructor that relies on super
    public FullTimelineRecyclerViewAdapter(CardChronologicalList chronologicalCards, Context context, SettingsFile settingsFile) {
        super(chronologicalCards, context, settingsFile);
        this.checkedCardIndices = new ArrayList<Integer>();
    }

    // main:
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);

        // handle checkboxes
        Card card = renderedChronologicalCards.get(position);
        viewHolder.checkBox.setVisibility(View.VISIBLE);
        // check for checks
        // first, make sure android doesnt beat us to the chase and screw up the checkbox listener
        viewHolder.checkBox.setOnCheckedChangeListener(null);
        // then grab the index
        Integer cardIndexInChronologicalList = chronologicalCards.indexOf(card);
        // set it to be checked or unchecked
        viewHolder.checkBox.setChecked(
                this.checkedCardIndices.contains(cardIndexInChronologicalList)
        );

        // handle listener for when a card is selected
        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                // update checked cards
                if (isChecked) {
                    checkedCardIndices.add(cardIndexInChronologicalList);
                } else {
                    checkedCardIndices.remove(cardIndexInChronologicalList);
                }
            }
        });
    }

}
