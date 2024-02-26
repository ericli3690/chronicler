package com.example.chronicler.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chronicler.databinding.FragmentRadioListBinding;

import java.util.ArrayList;
import java.util.List;

// a convenience class that is reused in multiple places
// displays the flattened list of all decks so that the user can easily select one
public class RadioListFragment extends Fragment {

    // ui control
    private FragmentRadioListBinding binding;

    // if the constructor is used, just use the inherited one
    public RadioListFragment() {
        super();
    }

    // android-required initialization method
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // handle binding
        binding = FragmentRadioListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // main:
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // get arguments from parent fragment that is hosting this radiolist
        List<String> names = requireArguments().getStringArrayList("names");
        int checked = requireArguments().getInt("checked");
        // create views
        List<RadioButton> radios = new ArrayList<RadioButton>();
        // name the decks and attach them all to the radio list
        for (String name : names) {
            RadioButton radioButton = new RadioButton(requireContext());
            radioButton.setText(name);
            radios.add(radioButton);
            binding.fragmentRadioListRadioGroup.addView(radioButton);
        }
        // set which ones are checked
        // checked being -1 means that addeditcardfragment is editing multiple cards, which may belong to multiple decks
        if (checked != -1) {
            radios.get(checked).setChecked(true);
        }
        // set listener for when the checked one changes
        binding.fragmentRadioListRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                // get the index of the one that was clicked
                View checkedRadioButton = binding.fragmentRadioListRadioGroup.findViewById(checkedId);
                int radioIndex = binding.fragmentRadioListRadioGroup.indexOfChild(checkedRadioButton);
                // put it in a bundle
                Bundle sendBack = new Bundle();
                sendBack.putInt("checked", radioIndex);
                // send it back to the parent fragment so they know which one the user picked
                requireActivity().getSupportFragmentManager().setFragmentResult("key", sendBack);
            }
        });
    }
}
