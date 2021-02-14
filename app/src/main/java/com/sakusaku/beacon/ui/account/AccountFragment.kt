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
import com.sakusaku.beacon.FirebaseAuthUtils
import com.sakusaku.beacon.FirestoreUtils
import com.sakusaku.beacon.MainActivity
import com.sakusaku.beacon.R


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

        // 保存値からステータス設定のクリック無効化のON/OFF
        val preferenceStatus = findPreference<ListPreference>("preference_status")
        preferenceStatus?.isEnabled = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("prefs.preferenceList.enabled", true)

        // プライバシー設定が「非公開」の場合クリック無効化
        val disclosureRange = findPreference<ListPreference>("preference_disclosure_range")
        disclosureRange?.setOnPreferenceChangeListener { _, newValue ->
            val isEnabled = when (newValue.toString()) {
                "非公開" -> false
                else -> true
            }
            preferenceStatus?.isEnabled = isEnabled
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putBoolean("prefs.preferenceList.enabled", isEnabled)
                    .apply()
            true
        }

        // OSSライセンス表示画面へのIntentを設定
        val oss = findPreference<PreferenceScreen>("preference_oss_license")
        val intent = Intent(requireActivity(), OssLicensesMenuActivity::class.java)
        oss?.intent = intent
    }
}