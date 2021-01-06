package com.sakusaku.beacon

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity() {
    companion object {
        private val PERMISSION_LOCATION: Array<String> = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        private const val REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)
        showSplash()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // アラート表示中に画面回転すると length ０でコールバックされるのでガードする
        if (requestCode == REQUEST_CODE && grantResults.isNotEmpty()) {
            // 失敗した場合
            if (!RuntimePermission.checkGrantResults(*grantResults)) {
                // 「今後は確認しない」にチェックされているかどうか
                if (RuntimePermission.shouldShowRequestPermissionRationale(this, PERMISSION_LOCATION[0])) {
                    Toast.makeText(this, "ビーコンの取得には位置情報の許可が必要です", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Handler().post { RuntimePermission.showAlertDialog(supportFragmentManager, "位置情報") }
                }
            } else {
                // 権限が取れた場合は通常の処理を行う
                startBeaconActivity()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            showSplash()
        }
    }

    private fun showSplash() {
        if (FirebaseAuthUtils.isSignIn()) {
            Handler().postDelayed({
                if (RuntimePermission.hasSelfPermissions(this, *PERMISSION_LOCATION)) {
                    // 権限がある場合は、そのまま通常処理を行う
                    startBeaconActivity()
                } else {
                    // 権限がない場合は、パーミッション確認アラートを表示する
                    requestPermissions(PERMISSION_LOCATION, REQUEST_CODE)
                }
            }, 600)

            // ユーザーデータの読み込み
            FirestoreUtils.getUserData {}
        } else {
            Handler().postDelayed({
                val intent = Intent(application, OnBoardingActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }, 1200)
        }
    }

    private fun startBeaconActivity() {
        val intent = Intent(application, BeaconActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
}