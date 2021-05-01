package com.sakusaku.beacon.ui.account

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceScreen
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.sakusaku.beacon.*


class AccountFragment : PreferenceFragmentCompat() {
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_gray))

        // 名前の表示
        val account = findPreference<AccountPreference>("preference_account")!!
        account.title = FirebaseAuthUtils.getUserProfile()["name"].toString()

        // ログアウト押下時の処理
        account.setOnLogoutClickListener(object : AccountPreference.OnLogoutClickListener {
            override fun onLogoutClickListener(view: View) {
                FirebaseAuthUtils.signOut()

                val restartIntent = Intent(Intent.ACTION_MAIN)
                restartIntent.setClassName(requireContext().packageName, MainActivity::class.java.name)
                restartIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                requireContext().startActivity(restartIntent)

                requireActivity().finish()
            }
        })

        // 先生or生徒の表示
        FirestoreUtils.getUserData { user ->
            account.summary = user["position"]
        }

        return view
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_app_setting, rootKey)

        val account = findPreference<AccountPreference>("preference_account")
        account?.intent = Intent(requireActivity(), AccountSettingActivity::class.java)

        // ビーコンスキャン中の場合「プライバシー設定」のクリック無効化
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val disclosureRange = findPreference<ListPreference>("preference_disclosure_range")
        disclosureRange?.isEnabled = !pref.getBoolean("isBeaconScan", false)

        // OSSライセンス表示画面へのIntentを設定
        val oss = findPreference<PreferenceScreen>("preference_oss_license")
        oss?.intent = Intent(requireActivity(), OssLicensesMenuActivity::class.java)
    }
}