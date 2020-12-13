package com.sakusaku.beacon.onBoarding;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sakusaku.beacon.GridRadioGroup;
import com.sakusaku.beacon.R;
import com.sakusaku.beacon.RuntimePermission;

public class PermissionFragment extends Fragment {

    private final static String[] PERMISSION_LOCATION = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
    private final static int PERMISSION_REQUEST_CODE = 1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_permission, container, false);

        Button requestPermissionButton = view.findViewById(R.id.requestPermissionButton);
        requestPermissionButton.setOnClickListener(v -> {
            requestPermissions(PERMISSION_LOCATION, PERMISSION_REQUEST_CODE);
        });

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0) {
            if (!RuntimePermission.checkGrantResults(grantResults)) {
                if (RuntimePermission.shouldShowRequestPermissionRationale(requireActivity(), PERMISSION_LOCATION[0])) {
                    Toast.makeText(requireContext(), "ビーコンの取得には位置情報の許可が必要です", Toast.LENGTH_SHORT).show();
                } else {
                    new Handler().post(() -> {
                        RuntimePermission.showAlertDialog(requireActivity().getSupportFragmentManager(), "位置情報");
                    });
                }
            } else {
                requireActivity().setResult(Activity.RESULT_OK, new Intent());
                requireActivity().finish();
            }
        }
    }
}