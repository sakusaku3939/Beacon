package com.sakusaku.beacon.onBoarding;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.sakusaku.beacon.R;

public class NameEntryFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_name_entry, container, false);

        Button navigationNext = view.findViewById(R.id.navigationNext);
        navigationNext.setOnClickListener(v -> {
            // 入力された名前のチェック
            EditText text = view.findViewById(R.id.nameEntry);
            String name = text.getText().toString();
            if (!name.isEmpty()) {
                // 生徒か先生かチェック
                RadioGroup radioGroup = view.findViewById(R.id.onboardingRadio);
                int id = radioGroup.getCheckedRadioButtonId();
                RadioButton radioButton = view.findViewById(id);
                String position = radioButton.getText().toString();
                PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .edit()
                        .putString("name", name)
                        .putString("position", position)
                        .apply();
                replaceFragment(position.equals("先生") ? new RegionSelectFragment() : new PermissionFragment());
            } else {
                text.setError("文字を入力してください");
            }
        });

        Button navigationBack = view.findViewById(R.id.navigationBack);
        navigationBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        EditText edittext = requireActivity().findViewById(R.id.nameEntry);
        edittext.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void afterTextChanged(Editable s) {
                for (int i = s.length() - 1; i >= 0; i--) {
                    if (s.charAt(i) == '\n') {
                        s.delete(i, i + 1);
                        return;
                    }
                }
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fragment_close_enter, R.anim.fragment_close_exit);
        transaction.addToBackStack(null);
        transaction.replace(R.id.onboarding_fragment, fragment);
        transaction.commit();
    }
}