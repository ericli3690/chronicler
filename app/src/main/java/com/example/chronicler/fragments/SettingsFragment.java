package com.example.chronicler.fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chronicler.MainActivity;
import com.example.chronicler.R;
import com.example.chronicler.databinding.FragmentSettingsBinding;
import com.example.chronicler.datatypes.SettingsFile;
import com.example.chronicler.functions.FileManager;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // handle binding
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // grab data
        FileManager<SettingsFile> settingsFileManager = ((MainActivity) requireActivity()).settingsFileManager;
        SettingsFile settingsFile = ((MainActivity) requireActivity()).settingsFile;

        // set toolbar
        ((Toolbar) requireActivity().findViewById(R.id.activity_main_toolbar)).setTitle("Settings");

        // back button
        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavHostFragment.findNavController(SettingsFragment.this).navigate(
                        SettingsFragmentDirections.actionSettingsFragmentToHomeFragment(false)
                );
                this.setEnabled(false);
            }
        });

        // set progress
        binding.fragmentSettingsSfx.setProgress(settingsFile.volume);
        binding.fragmentSettingsDifficulty.setProgress(settingsFile.percentDifficulty);

        // onclick
        binding.fragmentSettingsDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // save the new settings
                // write it to file
                settingsFile.volume = binding.fragmentSettingsSfx.getProgress();
                settingsFile.percentDifficulty = binding.fragmentSettingsDifficulty.getProgress();
                settingsFileManager.writeSingleObjectToFile(settingsFile);
                // navigate back
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }
}