package com.sakusaku.beacon.onBoarding

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.jgabrielfreitas.core.BlurImageView
import com.sakusaku.beacon.R

class GetStartedFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding_get_started, container, false)
        val blurImage: BlurImageView = requireActivity().findViewById(R.id.BlurImage)
        blurImage.setBlur(0)
        val getStarted = view.findViewById<Button?>(R.id.getStartedButton)
        getStarted.setOnClickListener {
            // 画像にぼかしを入れる
            Handler().postDelayed({ blurImage.setBlur(5) }, 100)
            if (PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("isEmailCheck", false)) {
                replaceFragment(NameEntryFragment())
            } else {
                replaceFragment(EmailEntryFragment())
            }
        }
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