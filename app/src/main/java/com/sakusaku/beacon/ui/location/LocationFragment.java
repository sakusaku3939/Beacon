package com.sakusaku.beacon.ui.location;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sakusaku.beacon.BeaconService;
import com.sakusaku.beacon.R;

public class LocationFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LocationViewModel locationViewModel = new ViewModelProvider(this).get(LocationViewModel.class);
        View root = inflater.inflate(R.layout.fragment_location, container, false);

        FloatingActionButton fab = root.findViewById(R.id.fab);
        FloatingActionButton fabPause = root.findViewById(R.id.fab_pauce);

        // フォアグラウンド実行中か
        ActivityManager manager = (ActivityManager) requireActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (BeaconService.class.getName().equals(serviceInfo.service.getClassName())) {
                fabPause.setVisibility(View.VISIBLE);
                fab.setVisibility(View.GONE);
            }
        }

        fab.setOnClickListener(v -> {
            // デバイスのBLE対応チェック
            if (!requireActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                // 未対応の場合、Toast表示
                Toast.makeText(getActivity(), "このデバイスはBLE未対応です", Toast.LENGTH_LONG).show();
            } else {
                fabPause.setVisibility(View.VISIBLE);
                fab.setVisibility(View.GONE);
                requireActivity().startForegroundService(new Intent(getActivity(), BeaconService.class));
            }
        });
        fabPause.setOnClickListener(v -> {
            fab.setVisibility(View.VISIBLE);
            fabPause.setVisibility(View.GONE);
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