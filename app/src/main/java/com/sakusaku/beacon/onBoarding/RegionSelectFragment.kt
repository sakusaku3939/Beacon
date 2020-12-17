package com.sakusaku.beacon.onBoarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.sakusaku.beacon.GridRadioGroup;
import com.sakusaku.beacon.R;

public class RegionSelectFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_region_select, container, false);

        GridRadioGroup radioSubject = view.findViewById(R.id.radioSubject);
        radioSubject.check(R.id.radioSubjectPhysics);

        Button navigationNext = view.findViewById(R.id.navigationNext);
        navigationNext.setOnClickListener(v -> {
            // 領域、教科のチェック
            RadioGroup radioRegion = view.findViewById(R.id.radioRegion);
            RadioButton radioRegionSelect = view.findViewById((int) radioRegion.getCheckedRadioButtonId());
            RadioButton radioSubjectSelect = view.findViewById((int) radioSubject.getCheckedRadioButtonId());
            String region = radioRegionSelect.getText().toString();
            String subject = radioSubjectSelect.getText().toString();
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .edit()
                    .putString("region", region)
                    .putString("subject", subject)
                    .apply();
            replaceFragment(new PermissionFragment());
        });

        Button navigationBack = view.findViewById(R.id.navigationBack);
        navigationBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fragment_close_enter, R.anim.fragment_close_exit);
        transaction.addToBackStack(null);
        transaction.replace(R.id.onboarding_fragment, fragment);
        transaction.commit();
    }
}