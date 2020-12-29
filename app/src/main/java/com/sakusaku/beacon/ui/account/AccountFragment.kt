package com.sakusaku.beacon.ui.account

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.*
import com.sakusaku.beacon.FirebaseAuthUtils
import com.sakusaku.beacon.FirestoreUtils
import com.sakusaku.beacon.R


class AccountFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_app_setting, rootKey)

        rootKey?.let { onCreatePreference(it) }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.setBackgroundColor(Color.parseColor("#F5F5F5"))

        // 設定値の読み込み
        val preferenceStatus = findPreference<ListPreference>("preference_status")
        val preferenceScanPeriod = findPreference<ListPreference>("preference_scan_period")
        preferenceStatus?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        preferenceScanPeriod?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()

        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        pref.registerOnSharedPreferenceChangeListener(this)

        // 名前の表示
        val account = findPreference<PreferenceScreen>("preference_account")!!
        account.title = FirebaseAuthUtils.getUserProfile()["name"].toString()

        // 先生or生徒の表示
        pref.getString("position", null)?.let {
            account.summary = it
        } ?: run {
            FirestoreUtils.getUserData({ data ->
                account.summary = data?.get("position").toString()
                PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .edit()
                        .putString("position", account.summary.toString())
                        .apply()
            }, {})
        }

        return view
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val preference = findPreference<ListPreference>(key)
        preference?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
    }

    private fun onCreatePreference(key: String) {
        val preference = findPreference<ListPreference>(key)
        preference?.summary = preference?.entry
    }
}