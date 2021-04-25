package com.sakusaku.beacon.ui.search

import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup

class DeselectRadio(private val root: View, private val radioGroup: RadioGroup) {
    private var isRadioButtonCheck = true

    init {
        radioGroup.setOnCheckedChangeListener { _, _ -> isRadioButtonCheck = false }
    }

    fun registerRadioButtonClickListener(idList: List<Int>) {
        for (id in idList) root.findViewById<RadioButton>(id).setOnClickListener { v ->
            if (v.id == radioGroup.checkedRadioButtonId && isRadioButtonCheck) {
                radioGroup.clearCheck()
            } else {
                isRadioButtonCheck = true
            }
        }
    }
}