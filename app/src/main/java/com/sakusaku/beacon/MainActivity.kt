package com.sakusaku.beacon

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    companion object {
        private val PERMISSION_LOCATION: Array<String> = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        private const val REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showSplash()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // アラート表示中に画面回転すると length ０でコールバックされるのでガードする
        if (requestCode == REQUEST_CODE && grantResults.isNotEmpty()) {
            if (!RuntimePermission.checkGrantResults(*grantResults)) {
                // 「今後は確認しない」にチェックされているかどうか
                if (RuntimePermission.shouldShowRequestPermissionRationale(this, PERMISSION_LOCATION[0])) {
                    Toast.makeText(this, "ビーコンの取得には位置情報の許可が必要です", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Handler().post { RuntimePermission.showAlertDialog(supportFragmentManager, "位置情報") }
                }
            } else {
                startBeaconActivity()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            showSplash()
        }
    }

    private fun showSplash() {
        if (FirebaseAuthUtils.isSignIn()) {
            // ユーザーデータの事前読み込み
            FirestoreUtils.getUserData {}

            // パーミッションチェック
            if (RuntimePermission.hasSelfPermissions(this, *PERMISSION_LOCATION)) {
                startBeaconActivity()
            } else {
                requestPermissions(PERMISSION_LOCATION, REQUEST_CODE)
            }
        } else {
            Handler().postDelayed({
                val intent = Intent(application, OnBoardingActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }, 800)
        }
    }

    private fun startBeaconActivity() {
        val intent = Intent(application, BeaconActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
}