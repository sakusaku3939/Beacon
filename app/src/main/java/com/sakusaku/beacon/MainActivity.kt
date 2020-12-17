package com.sakusaku.beacon;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.AppLaunchChecker;

public class MainActivity extends AppCompatActivity {

    private final static String[] PERMISSION_LOCATION = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
    private final static int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        showSplash();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // アラート表示中に画面回転すると length ０でコールバックされるのでガードする
        if (requestCode == REQUEST_CODE && grantResults.length > 0) {
            // 失敗した場合
            if (!RuntimePermission.checkGrantResults(grantResults)) {
                // 「今後は確認しない」にチェックされているかどうか
                if (RuntimePermission.shouldShowRequestPermissionRationale(this, PERMISSION_LOCATION[0])) {
                    Toast.makeText(this, "ビーコンの取得には位置情報の許可が必要です", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    new Handler().post(() -> {
                        RuntimePermission.showAlertDialog(getSupportFragmentManager(), "位置情報");
                    });
                }
            } else {
                // 権限が取れた場合は通常の処理を行う
                startBeaconActivity();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            AppLaunchChecker.onActivityCreate(this);
            showSplash();
        }
    }

    private void showSplash() {
        if (!AppLaunchChecker.hasStartedFromLauncher(this)) {
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(getApplication(), onBoardingActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }, 1200);
        } else {
            new Handler().postDelayed(() -> {
                if (RuntimePermission.hasSelfPermissions(this, PERMISSION_LOCATION)) {
                    // 権限がある場合は、そのまま通常処理を行う
                    startBeaconActivity();
                } else {
                    // 権限がない場合は、パーミッション確認アラートを表示する
                    requestPermissions(PERMISSION_LOCATION, REQUEST_CODE);
                }
            }, 600);
        }
    }

    private void startBeaconActivity() {
        Intent intent = new Intent(getApplication(), BeaconActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}