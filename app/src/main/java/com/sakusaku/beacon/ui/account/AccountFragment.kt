package com.sakusaku.beacon.ui.account

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceFragmentCompat
import com.sakusaku.beacon.FirebaseAuthUtils
import com.sakusaku.beacon.R

class AccountFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_app_setting, rootKey)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        super.onCreateView(inflater, container, savedInstanceState)?.apply {
//            setBackgroundColor(Color.parseColor("#f9f9f9"))
//        }
//        val accountViewModel = ViewModelProvider(this).get(AccountViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_account, container, false)
//        val textView = root.findViewById<TextView?>(R.id.text_account)
//        accountViewModel.getText()?.observe(viewLifecycleOwner, {
//            textView.text = FirebaseAuthUtils.getUserProfile()["name"].toString()
//        })
        return root
    }
}