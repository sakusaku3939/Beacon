package com.sakusaku.beacon

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.EditText

object NameRestriction {
    fun add(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                // 改行を削除
                for (i in s.length - 1 downTo 0) {
                    if (s[i] == '\n') {
                        s.delete(i, i + 1)
                        return
                    }
                }
                // 半角文字のみ入力されているかで制限文字数を変更
                if (Regex("^[ -~｡-ﾟ]+\$").containsMatchIn(editText.text)) {
                    editText.filters = arrayOf(InputFilter.LengthFilter(12))
                } else {
                    if (editText.text.length > 8) editText.text.delete(8, editText.text.length)
                    editText.filters = arrayOf(InputFilter.LengthFilter(8))
                }
            }
        })
    }
}