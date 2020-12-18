package com.sakusaku.beacon.onBoarding

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.sakusaku.beacon.FirebaseAuthUtils
import com.sakusaku.beacon.FirestoreUtils
import com.sakusaku.beacon.R
import com.sakusaku.beacon.RuntimePermission

class PermissionFragment : Fragment() {
    companion object {
        private val PERMISSION_LOCATION: Array<String> = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        private const val PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding_permission, container, false)
        val requestPermissionButton = view.findViewById<Button?>(R.id.requestPermissionButton)
        requestPermissionButton.setOnClickListener {
            // 位置情報のリクエスト
            requestPermissions(PERMISSION_LOCATION, PERMISSION_REQUEST_CODE)
        }
        return view
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // アラート表示中に画面回転すると length 0でコールバックされるのでガードする
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty()) {
            if (!RuntimePermission.checkGrantResults(*grantResults)) {
                // 「今後は確認しない」にチェックされているかどうか
                if (RuntimePermission.shouldShowRequestPermissionRationale(requireActivity(), PERMISSION_LOCATION[0])) {
                    Toast.makeText(requireContext(), "ビーコンの取得には位置情報の許可が必要です", Toast.LENGTH_SHORT).show()
                } else {
                    Handler().post { RuntimePermission.showAlertDialog(requireActivity().supportFragmentManager, "位置情報") }
                }
            } else {
                // ユーザー情報の登録
                val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
                val name = pref.getString("name", null)
                val position = pref.getString("position", null)
                val region = pref.getString("region", null)
                val subject = pref.getString("subject", null)

                FirebaseAuthUtils.updateProfile(name)
                FirestoreUtils.updateUser(position, region, subject) { isSuccess ->
                    if (!isSuccess) {
                        Toast.makeText(requireContext(), "ユーザー情報の登録に失敗しました", Toast.LENGTH_SHORT).show()
                        activity?.finish()
                    }
                }

                // 仮保存データの削除
                pref.edit()
                        .remove("name")
                        .remove("email")
                        .remove("position")
                        .remove("region")
                        .remove("subject")
                        .apply()

                requireActivity().setResult(Activity.RESULT_OK, Intent())
                requireActivity().finish()
            }
        }
    }
}