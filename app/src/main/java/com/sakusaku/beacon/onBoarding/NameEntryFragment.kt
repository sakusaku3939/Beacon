package com.sakusaku.beacon.onBoarding

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.sakusaku.beacon.R

class NameEntryFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding_name_entry, container, false)

        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit()
                .putBoolean("isEmailCheck", true)
                .apply()

        val navigationNext = view.findViewById<Button?>(R.id.navigationNext)
        navigationNext.setOnClickListener {
            // 入力された名前のチェック
            val text = view.findViewById<EditText?>(R.id.nameEntry)
            val name = text.text.toString()
            if (name.isNotEmpty()) {
                // 生徒か先生かチェック
                val radioGroup = view.findViewById<RadioGroup?>(R.id.onboardingRadio)
                val id = radioGroup.checkedRadioButtonId
                val radioButton = view.findViewById<RadioButton?>(id)
                val position = radioButton.text.toString()
                PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .edit()
                        .putString("name", name)
                        .putString("position", position)
                        .apply()
                replaceFragment(if (position == "先生") RegionSelectFragment() else PermissionFragment())
            } else {
                text.error = "文字を入力してください"
            }
        }
        val navigationBack = view.findViewById<Button?>(R.id.navigationBack)
        navigationBack.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }
        return view
    }

    override fun onStart() {
        super.onStart()
        val editText = requireActivity().findViewById<EditText?>(R.id.nameEntry)
        editText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                for (i in s.length - 1 downTo 0) {
                    if (s[i] == '\n') {
                        s.delete(i, i + 1)
                        return
                    }
                }
            }
        })
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.fragment_close_enter, R.anim.fragment_close_exit)
        transaction.addToBackStack(null)
        transaction.replace(R.id.onboarding_fragment, fragment)
        transaction.commit()
    }
}