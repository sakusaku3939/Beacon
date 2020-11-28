package com.sakusaku.beacon;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.jgabrielfreitas.core.BlurImageView;
import com.sakusaku.beacon.onBoarding.FirstFragment;
import com.sakusaku.beacon.onBoarding.SecondFragment;

public class onBoardingActivity extends AppCompatActivity {

    private int fragment_state = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding);
        FirstFragment firstFragment = new FirstFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.onboarding_fragment, firstFragment);
        transaction.commit();
        fragment_state = 1;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        onClickBack(null);
    }

    private void fragmentBack() {

    }

    public void onClickGetStarted(View view) {
        BlurImageView blurImage = findViewById(R.id.appCompatImageView);
        new Handler().postDelayed(() -> {
            blurImage.setBlur(5);
        }, 100);
        SecondFragment secondFragment = new SecondFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fragment_close_enter, R.anim.fragment_close_exit);
        transaction.addToBackStack(null);
        transaction.replace(R.id.onboarding_fragment, secondFragment);
        transaction.commit();
        fragment_state = 2;
    }


    public void onClickBack(View view) {
        getSupportFragmentManager().popBackStack();
        fragment_state--;
        if (fragment_state == 1) {
            BlurImageView blurImage = findViewById(R.id.appCompatImageView);
            blurImage.setBlur(0);
        }
    }

    public void onClickNext(View view) {
    }
}
