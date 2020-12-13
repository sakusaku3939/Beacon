package com.sakusaku.beacon.onBoarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.sakusaku.beacon.R;

public class EmailSendFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_email_send, container, false);

        String email = getArguments().getString("email");
        TextView description = view.findViewById(R.id.emailSendDescription);
        description.setText((email + " " + description.getText()));

        Button emailSendButton = view.findViewById(R.id.emailSendButton);
        emailSendButton.setOnClickListener(v -> {
            replaceFragment(new NameEntryFragment());
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
