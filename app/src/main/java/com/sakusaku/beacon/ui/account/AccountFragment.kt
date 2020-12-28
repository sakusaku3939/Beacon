package com.sakusaku.beacon.ui.account

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceFragmentCompat
import com.sakusaku.beacon.R

class AccountFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_app_setting, rootKey)
    }
//
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.setBackgroundColor(Color.parseColor("#F5F5F5"))
//        val test = view?.findViewById<PreferenceCategory>(R.)
//        val accountViewModel = ViewModelProvider(this).get(AccountViewModel::class.java)
//        val root = inflater.inflate(inflater, container, false)
//        val textView = root.findViewById<TextView?>(R.id.text_account)
//        accountViewModel.getText()?.observe(viewLifecycleOwner, {
//            textView.text = FirebaseAuthUtils.getUserProfile()["name"].toString()
//        })
        return view
    }
}