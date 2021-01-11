package com.sakusaku.beacon.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sakusaku.beacon.GridRadioGroup
import com.sakusaku.beacon.R

class SearchFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_search, container, false)

        val radioSubject: GridRadioGroup = root.findViewById(R.id.radioSubject)
        radioSubject.check(R.id.radioSubjectPhysics)

        // フィルターメニュー表示・非表示の切り替え
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

        return root
    }
}