package com.sakusaku.beacon.onBoarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.sakusaku.beacon.FragmentUtil
import com.sakusaku.beacon.GridRadioGroup
import com.sakusaku.beacon.R

class RegionSelectFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding_region_select, container, false)
        val radioSubject: GridRadioGroup = view.findViewById(R.id.radioSubject)
        radioSubject.check(R.id.radioSubjectPhysics)
        val navigationNext = view.findViewById<Button?>(R.id.navigationNext)
        navigationNext.setOnClickListener {
            // 領域、教科のチェック
            val radioRegion = view.findViewById<RadioGroup?>(R.id.radioRegion)
            val radioRegionSelect = view.findViewById<RadioButton?>(radioRegion.checkedRadioButtonId)
            val radioSubjectSelect = view.findViewById<RadioButton?>(radioSubject.checkedRadioButtonId)
            val region = radioRegionSelect.text.toString()
            val subject = radioSubjectSelect.text.toString()
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .edit()
                    .putString("region", region)
                    .putString("subject", subject)
                    .apply()
            FragmentUtil.replaceFragment(requireActivity(), PermissionFragment())
        }
        val navigationBack = view.findViewById<Button?>(R.id.navigationBack)
        navigationBack.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }
        return view
    }
}