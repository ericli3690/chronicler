package com.example.chronicler.functions;

import android.content.Context;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.chronicler.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class DeleteConfirmation implements View.OnClickListener {

    View.OnClickListener andThen;
    Context context;

    public DeleteConfirmation(View.OnClickListener andThen, Context context) {
        this.andThen = andThen;
        this.context = context;
    }

    @Override
    public void onClick(View view) {
        // double check with confirmation message
        Snackbar deleteConfirmation = Snackbar.make(
                view,
                "Are you sure?",
                BaseTransientBottomBar.LENGTH_SHORT
        );
        deleteConfirmation.setAction("Confirm Deletion", this.andThen);
        deleteConfirmation.setActionTextColor(ContextCompat.getColor(this.context, R.color.red));
        deleteConfirmation.show();
    }
}
