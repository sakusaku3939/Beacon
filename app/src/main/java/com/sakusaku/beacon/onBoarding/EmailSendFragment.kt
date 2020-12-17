package com.sakusaku.beacon.onBoarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.sakusaku.beacon.R
import java.lang.StringBuilder

class EmailSendFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding_email_send, container, false)

        // メール確認を促す説明に、さっき入力したメールアドレスを反映
        val email = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("email", "")
        val description = view.findViewById<TextView?>(R.id.emailSendDescription)

        val sb = StringBuilder(description.text)
        sb.insert(0, email)
        description.text = sb.toString()
        val emailSendButton = view.findViewById<Button?>(R.id.emailSendButton)
        emailSendButton.setOnClickListener {
            // メールアプリへの遷移
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_APP_EMAIL)
            startActivity(intent)
        }
        return view
    }
}