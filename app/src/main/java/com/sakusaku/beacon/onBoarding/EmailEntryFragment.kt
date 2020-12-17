package com.sakusaku.beacon.onBoarding

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.sakusaku.beacon.FirebaseUtils
import com.sakusaku.beacon.R

class EmailEntryFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding_email_entry, container, false)
        val navigationNext = view.findViewById<Button?>(R.id.navigationNext)
        navigationNext.setOnClickListener {
            // 有効なメールアドレスかチェック
            val text = view.findViewById<EditText?>(R.id.emailEntry)
            val email = text.text.toString()
            if (email.isEmpty()) {
                text.error = "文字を入力してください"
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                text.error = "正しいメールアドレスを入力してください"
            } else {
                FirebaseUtils.sendSignInLink(email, requireActivity(), FirebaseUtils.buildActionCodeSettings())
                PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .edit()
                        .putString("email", email)
                        .apply()
                replaceFragment(EmailSendFragment())
            }
        }
        val navigationBack = view.findViewById<Button?>(R.id.navigationBack)
        navigationBack.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }
        return view
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.fragment_close_enter, R.anim.fragment_close_exit)
        transaction.addToBackStack(null)
        transaction.replace(R.id.onboarding_fragment, fragment)
        transaction.commit()
    }
}