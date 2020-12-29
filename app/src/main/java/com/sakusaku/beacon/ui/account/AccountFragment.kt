package com.sakusaku.beacon.ui.account

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceScreen
import com.sakusaku.beacon.FirebaseAuthUtils
import com.sakusaku.beacon.FirestoreUtils
import com.sakusaku.beacon.R


class AccountFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_app_setting, rootKey)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.setBackgroundColor(Color.parseColor("#F5F5F5"))

        val account = findPreference<PreferenceScreen>("preference_account")!!
        account.title = FirebaseAuthUtils.getUserProfile()["name"].toString()

        PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("position", null)?.let {
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
}