package com.sakusaku.beacon.ui.location;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sakusaku.beacon.BeaconService;
import com.sakusaku.beacon.R;

import java.util.Objects;

public class LocationFragment extends Fragment {

    private LocationViewModel locationViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        locationViewModel =
                new ViewModelProvider(this).get(LocationViewModel.class);
        View root = inflater.inflate(R.layout.fragment_location, container, false);

        FloatingActionButton fab = root.findViewById(R.id.fab);
        FloatingActionButton fabPause = root.findViewById(R.id.fab_pauce);

        SharedPreferences pref = requireActivity().getSharedPreferences("preference_data", Context.MODE_PRIVATE);
        if (pref.getInt("isForeground", 0) == 1) {
            fabPause.setVisibility(View.VISIBLE);
            fab.setVisibility(View.GONE);
        }

        fab.setOnClickListener(v -> {
            fabPause.setVisibility(View.VISIBLE);
            fab.setVisibility(View.GONE);

            SharedPreferences.Editor editor = pref.edit();
            editor.putInt("isForeground", 1);
            editor.apply();

            requireActivity().startForegroundService(new Intent(getActivity(), BeaconService.class));
        });
        fabPause.setOnClickListener(v -> {
            fab.setVisibility(View.VISIBLE);
            fabPause.setVisibility(View.GONE);

            SharedPreferences.Editor editor = pref.edit();
            editor.putInt("isForeground", 0);
            editor.apply();

            requireActivity().stopService(new Intent(getActivity(), BeaconService.class));
        });

        final TextView textView = root.findViewById(R.id.text_location);
        locationViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}