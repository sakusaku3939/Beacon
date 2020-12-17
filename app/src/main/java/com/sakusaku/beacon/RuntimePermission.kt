package com.sakusaku.beacon

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

object RuntimePermission {
    fun hasSelfPermissions(context: Context, vararg permissions: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        for (permission in permissions) {
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    fun checkGrantResults(vararg grantResults: Int): Boolean {
        require(grantResults.isNotEmpty()) { "grantResults is empty" }
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    fun shouldShowRequestPermissionRationale(activity: Activity, permission: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.shouldShowRequestPermissionRationale(permission)
        } else true
    }

    // ダイアログ表示
    fun showAlertDialog(fragmentManager: FragmentManager, permission: String) {
        val dialog = RuntimePermissionAlertDialogFragment.newInstance(permission)
        dialog.show(fragmentManager, RuntimePermissionAlertDialogFragment.TAG)
    }

    // ダイアログ本体
    class RuntimePermissionAlertDialogFragment : DialogFragment() {
        companion object {
            const val TAG: String = "RuntimePermissionApplicationSettingsDialogFragment"
            private const val ARG_PERMISSION_NAME: String = "permissionName"
            fun newInstance(permission: String): RuntimePermissionAlertDialogFragment {
                val fragment = RuntimePermissionAlertDialogFragment()
                val args = Bundle()
                args.putString(ARG_PERMISSION_NAME, permission)
                fragment.arguments = args
                return fragment
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val permission = arguments?.getString(ARG_PERMISSION_NAME)
            val dialogBuilder = AlertDialog.Builder(activity)
                    .setMessage(permission + "の許可が必要です、アプリ情報から設定してください")
                    .setPositiveButton("アプリ情報") { _: DialogInterface, _: Int ->
                        dismiss()
                        // システムのアプリ設定画面
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + activity?.packageName))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        activity?.startActivity(intent)
                        activity?.finish()
                    }
                    .setNegativeButton("終了") { _: DialogInterface?, _: Int ->
                        dismiss()
                        activity?.finish()
                    }
            this.isCancelable = false
            return dialogBuilder.create()
        }
    }
}