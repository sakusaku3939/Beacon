package com.sakusaku.beacon.onBoarding;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.jgabrielfreitas.core.BlurImageView;
import com.sakusaku.beacon.R;

public class GetStartedFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_get_started, container, false);
        BlurImageView blurImage = requireActivity().findViewById(R.id.BlurImage);
        blurImage.setBlur(0);

        Button getStarted = view.findViewById(R.id.getStartedButton);
        getStarted.setOnClickListener(v -> {
            // 画像にぼかしを入れる
            new Handler().postDelayed(() -> {
                blurImage.setBlur(5);
            }, 100);
            replaceFragment(new EmailEntryFragment());
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