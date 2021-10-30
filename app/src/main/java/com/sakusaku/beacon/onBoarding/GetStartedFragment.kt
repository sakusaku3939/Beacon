package com.sakusaku.beacon.onBoarding

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.jgabrielfreitas.core.BlurImageView
import com.sakusaku.beacon.firebase.FirebaseAuthUtils
import com.sakusaku.beacon.FragmentUtil
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
            val isEmailVerified = FirebaseAuthUtils.emailVerified
            isEmailVerified?.let {
                FragmentUtil.existsUserData(requireActivity(), blurImage)
            } ?: FragmentUtil.replaceFragment(requireActivity(), EmailEntryFragment())
        }

        val getAnonymouslyStarted = view.findViewById<Button?>(R.id.getAnonymouslyStartedButton)
        getAnonymouslyStarted.setOnClickListener {
            FirebaseAuthUtils.signInAnonymously()
            FragmentUtil.existsUserData(requireActivity(), blurImage)
        }
        return view
    }
}