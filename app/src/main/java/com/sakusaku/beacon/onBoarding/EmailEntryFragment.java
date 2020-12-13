package com.sakusaku.beacon.onBoarding;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.sakusaku.beacon.R;

public class EmailEntryFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_email_entry, container, false);

        Button navigationNext = view.findViewById(R.id.navigationNext);
        navigationNext.setOnClickListener(v -> {
            EditText text = view.findViewById(R.id.emailEntry);
            String email = text.getText().toString();
            if (email.isEmpty()) {
                text.setError("文字を入力してください");
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                text.setError("正しいメールアドレスを入力してください");
            } else {
                Fragment fragment = new EmailSendFragment();
                Bundle bundle = new Bundle();
                bundle.putString("email", email);
                fragment.setArguments(bundle);
                replaceFragment(fragment);
            }
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
