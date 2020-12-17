package com.sakusaku.beacon.onBoarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.sakusaku.beacon.R;

public class EmailSendFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_email_send, container, false);

        // メール確認を促す説明に、さっき入力したメールアドレスを反映
        String email = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("email", "");
        TextView description = view.findViewById(R.id.emailSendDescription);
        description.setText((email + " " + description.getText()));

        Button emailSendButton = view.findViewById(R.id.emailSendButton);
        emailSendButton.setOnClickListener(v -> {
            // メールアプリへの遷移
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            startActivity(intent);
        });

        return view;
    }
}
