package com.sakusaku.beacon.ui.account

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.*
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.sakusaku.beacon.FirebaseAuthUtils
import com.sakusaku.beacon.FirestoreUtils
import com.sakusaku.beacon.R

class AccountFragment : PreferenceFragmentCompat() {
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.setBackgroundColor(Color.parseColor("#F5F5F5"))

        // 名前の表示
        val account = findPreference<PreferenceScreen>("preference_account")!!
        account.title = FirebaseAuthUtils.getUserProfile()["name"].toString()

        // 先生or生徒の表示
        PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("position", null)?.let {
            account.summary = it
        } ?: run {
            FirestoreUtils.getUserData { data ->
                data?.let {
                    account.summary = it["position"].toString()
                    PreferenceManager.getDefaultSharedPreferences(requireContext())
                            .edit()
                            .putString("position", it["position"].toString())
                            .apply()
                }
            }
        }

        return view
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_app_setting, rootKey)

        val preference = findPreference<PreferenceScreen>("preference_oss_license")
        val intent = Intent(requireActivity(), OssLicensesMenuActivity::class.java)
        preference?.intent = intent
    }
}