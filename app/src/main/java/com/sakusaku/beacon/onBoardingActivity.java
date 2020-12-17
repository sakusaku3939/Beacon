package com.sakusaku.beacon;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.AuthResult;
import com.sakusaku.beacon.onBoarding.GetStartedFragment;
import com.sakusaku.beacon.onBoarding.NameEntryFragment;

public class onBoardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding);

        Intent intent = getIntent();
        if (intent.getData() != null) FirebaseUtils.verifySignInLink(this, intent, (task) -> {
            if (task.isSuccessful()) {
                AuthResult result = task.getResult();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.fragment_close_enter, R.anim.fragment_close_exit);
                transaction.addToBackStack(null);
                transaction.replace(R.id.onboarding_fragment, new NameEntryFragment());
                transaction.commit();
            } else {
                Toast.makeText(this, "メールリンクが無効です", Toast.LENGTH_LONG).show();
            }
        });

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.onboarding_fragment, new GetStartedFragment());
        transaction.commit();
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
        getSupportFragmentManager().popBackStack();
    }
}
