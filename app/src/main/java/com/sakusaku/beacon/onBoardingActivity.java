package com.sakusaku.beacon;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.jgabrielfreitas.core.BlurImageView;
import com.sakusaku.beacon.onBoarding.FirstFragment;
import com.sakusaku.beacon.onBoarding.FourthFragment;
import com.sakusaku.beacon.onBoarding.SecondFragment;
import com.sakusaku.beacon.onBoarding.ThirdFragment;

public class onBoardingActivity extends AppCompatActivity {

    private int fragment_state;
    private final static String[] PERMISSION_LOCATION = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
    private final static int PERMISSION_REQUEST_CODE = 1;

    private String name = "";
    private String position = "";
    private String region = "";
    private String subject = "";

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // アラート表示中に画面回転すると length ０でコールバックされるのでガードする
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0) {
            // 失敗した場合
            if (!RuntimePermission.checkGrantResults(grantResults)) {
                // 「今後は確認しない」にチェックされているかどうか
                if (RuntimePermission.shouldShowRequestPermissionRationale(this, PERMISSION_LOCATION[0])) {
                    Toast.makeText(this, "ビーコンの取得には位置情報の許可が必要です", Toast.LENGTH_SHORT).show();
                } else {
                    new Handler().post(() -> {
                        RuntimePermission.showAlertDialog(getSupportFragmentManager(), "位置情報");
                    });
                }
            } else {
                PreferenceManager.getDefaultSharedPreferences(this)
                        .edit()
                        .putString("name", name)
                        .putString("position", position)
                        .putString("region", region)
                        .putString("subject", subject)
                        .apply();
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    public void onClickGetStarted(View view) {
        switch (fragment_state) {
            case 1:
                BlurImageView blurImage = findViewById(R.id.appCompatImageView);
                new Handler().postDelayed(() -> {
                    blurImage.setBlur(5);
                }, 100);
                FragmentTransaction(new SecondFragment(), 2);
                break;
            case 4:
                requestPermissions(PERMISSION_LOCATION, PERMISSION_REQUEST_CODE);
                break;
        }

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
                name = text.getText().toString();
                if (name.isEmpty()) {
                    text.setError("文字を入力してください");
                } else {
                    RadioGroup radioGroup = findViewById(R.id.onboardingRadio);
                    int id = radioGroup.getCheckedRadioButtonId();
                    RadioButton radioButton = findViewById(id);
                    position = radioButton.getText().toString();
                    if (position.equals("先生")) {
                        FragmentTransaction(new ThirdFragment(), 3);
                        new Handler().postDelayed(() -> {
                            GridRadioGroup radioSubject = findViewById(R.id.radioSubject);
                            radioSubject.check(R.id.radioSubjectPhysics);
                        }, 100);
                    } else {
                        FragmentTransaction(new FourthFragment(), 4);
                    }
                }
                break;
            case 3:
                RadioGroup radioRegion = findViewById(R.id.radioRegion);
                RadioButton radioRegionSelect = findViewById((int)radioRegion.getCheckedRadioButtonId());
                GridRadioGroup radioSubject = findViewById(R.id.radioSubject);
                RadioButton radioSubjectSelect = findViewById((int)radioSubject.getCheckedRadioButtonId());
                region = radioRegionSelect.getText().toString();
                subject = radioSubjectSelect.getText().toString();
                FragmentTransaction(new FourthFragment(), 4);
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
