package com.sakusaku.beacon.ui.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import com.sakusaku.beacon.R


class SearchFragment : Fragment() {
    private lateinit var listener: RadioGroup.OnCheckedChangeListener

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_search, container, false)

        // 検索フィルターメニュー表示・非表示の切り替え
        val accordion = root.findViewById<LinearLayout>(R.id.accordion)
        val accordionArrow = root.findViewById<ImageView>(R.id.accordionArrow)
        val searchFilter = root.findViewById<View>(R.id.searchFilter)
        accordion.setOnClickListener {
            if (searchFilter.visibility != View.VISIBLE) {
                // 表示
                accordionArrow.animate().rotation(90F).duration = 300
                searchFilter.animate().alpha(1f).translationY(15F).setDuration(300).withStartAction {
                    searchFilter.visibility = View.VISIBLE
                }
            } else {
                // 非表示
                accordionArrow.animate().rotation(0F).duration = 300
                searchFilter.animate().alpha(0f).translationY(-15F).setDuration(300).withEndAction {
                    searchFilter.visibility = View.GONE
                }
            }
        }

        val radioRegion: RadioGroup = root.findViewById(R.id.radioRegion)
        val radioSubject: RadioGroup = root.findViewById(R.id.radioSubject)

        // 検索フィルターの選択を再タップで解除
        val regionIdList = listOf(R.id.radioRegionIT, R.id.radioRegionET, R.id.radioRegionBT, R.id.radioRegionNT)
        DeselectRadio(root, radioRegion).registerRadioButtonClickListener(regionIdList)

        val subjectIdList = listOf(
                R.id.radioSubjectPhysics, R.id.radioSubjectScience, R.id.radioSubjectBiology, R.id.radioSubjectMath,
                R.id.radioSubjectEnglish, R.id.radioSubjectModern, R.id.radioSubjectGeography, R.id.radioSubjectPE,
                R.id.radioSubjectHE, R.id.radioSubjectArt, R.id.radioSubjectOther)
        DeselectRadio(root, radioSubject).registerRadioButtonClickListener(subjectIdList)

        // 検索画面への遷移
        val searchBar = root.findViewById<LinearLayout>(R.id.searchBar)
        searchBar.setOnClickListener {
            val region = if (radioRegion.checkedRadioButtonId != -1)
                root.findViewById<RadioButton>(radioRegion.checkedRadioButtonId).text else null
            val subject = if (radioSubject.checkedRadioButtonId != -1)
                root.findViewById<RadioButton>(radioSubject.checkedRadioButtonId).text else null

            val intent = Intent(requireActivity(), SearchActivity::class.java)
            if (searchFilter.visibility == View.VISIBLE) {
                intent.putExtra("region", region)
                intent.putExtra("subject", subject)
            }
            startActivity(intent)
            requireActivity().overridePendingTransition(0, 0)
        }

        return root
    }

    private fun checkAnswer(radioGroup: RadioGroup) {
        radioGroup.setOnCheckedChangeListener(null)
        radioGroup.clearCheck()
        radioGroup.setOnCheckedChangeListener(listener)
    }
}