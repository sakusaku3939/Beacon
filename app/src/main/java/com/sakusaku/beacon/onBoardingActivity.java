package com.sakusaku.beacon;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.jgabrielfreitas.core.BlurImageView;
import com.sakusaku.beacon.onBoarding.FirstFragment;
import com.sakusaku.beacon.onBoarding.SecondFragment;
import com.sakusaku.beacon.onBoarding.ThirdFragment;

public class onBoardingActivity extends AppCompatActivity {

    private int fragment_state;

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

    public void onClickGetStarted(View view) {
        BlurImageView blurImage = findViewById(R.id.appCompatImageView);
        new Handler().postDelayed(() -> {
            blurImage.setBlur(5);
        }, 100);
        FragmentTransaction(new SecondFragment(), 2);
    }

    public void onClickBack(View view) {
        getSupportFragmentManager().popBackStack();
        fragment_state -= fragment_state > 0 ? 1 : 0;
        if (fragment_state == 1) {
            BlurImageView blurImage = findViewById(R.id.appCompatImageView);
            blurImage.setBlur(0);
        }
    }

    public void onClickNext(View view) {
        switch (fragment_state) {
            case 2:
                EditText text = findViewById(R.id.appCompatEditText);
                if (text.getText().toString().isEmpty()) {
                    text.setError("文字を入力してください");
                } else {
                    RadioGroup radioGroup = findViewById(R.id.onboardingRadio);
                    int id = radioGroup.getCheckedRadioButtonId();
                    RadioButton radioButton = findViewById(id);
                    if (radioButton.getText().toString().equals("先生")) {
                        FragmentTransaction(new ThirdFragment(), 3);
                    } else {
                    }
                }
                break;
            case 3:
                break;
        }
    }

    private void FragmentTransaction(Fragment fragment, int state) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fragment_close_enter, R.anim.fragment_close_exit);
        transaction.addToBackStack(null);
        transaction.replace(R.id.onboarding_fragment, fragment);
        transaction.commit();
        fragment_state = state;
    }
}
