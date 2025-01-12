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

    public FullTimelineRecyclerViewAdapter(CardChronologicalList chronologicalCards, Context context, SettingsFile settingsFile) {
        super(chronologicalCards, context, settingsFile);
        this.checkedCardIndices = new ArrayList<Integer>();
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);

        Card card = renderedChronologicalCards.get(position);
        viewHolder.checkBox.setVisibility(View.VISIBLE);
        // first, make sure android doesnt beat us to the chase and screw up the checkbox listener
        viewHolder.checkBox.setOnCheckedChangeListener(null);
        Integer cardIndexInChronologicalList = chronologicalCards.indexOf(card);
        viewHolder.checkBox.setChecked(
                this.checkedCardIndices.contains(cardIndexInChronologicalList)
        );

        // handle listener for when a card is selected
        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    checkedCardIndices.add(cardIndexInChronologicalList);
                } else {
                    checkedCardIndices.remove(cardIndexInChronologicalList);
                }
            }
        });
    }

}
