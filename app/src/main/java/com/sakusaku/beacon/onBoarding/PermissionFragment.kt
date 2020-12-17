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
            // 失敗した場合
            if (!RuntimePermission.checkGrantResults(*grantResults)) {
                if (RuntimePermission.shouldShowRequestPermissionRationale(requireActivity(), PERMISSION_LOCATION.get(0))) {
                    Toast.makeText(requireContext(), "ビーコンの取得には位置情報の許可が必要です", Toast.LENGTH_SHORT).show()
                } else {
                    Handler().post { RuntimePermission.showAlertDialog(requireActivity().supportFragmentManager, "位置情報") }
                }
                // 成功した場合
            } else {
                requireActivity().setResult(Activity.RESULT_OK, Intent())
                requireActivity().finish()
            }
        }
    }
}