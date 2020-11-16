package com.sakusaku.beacon.ui.facility;

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

import com.sakusaku.beacon.R;

public class FacilityFragment extends Fragment {

    private FacilityViewModel facilityViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        facilityViewModel =
                new ViewModelProvider(this).get(FacilityViewModel.class);
        View root = inflater.inflate(R.layout.fragment_facility, container, false);
        final TextView textView = root.findViewById(R.id.text_facility);
        facilityViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}