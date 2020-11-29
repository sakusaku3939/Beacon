package com.sakusaku.beacon.onBoarding;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sakusaku.beacon.R;

public class SecondFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_second, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        EditText edittext = requireActivity().findViewById(R.id.appCompatEditText);
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
}