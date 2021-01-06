package com.sakusaku.beacon.ui.account

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.sakusaku.beacon.R


class AccountPreference @JvmOverloads constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int = 0) : Preference(context, attrs, defStyleAttr) {
    init {
        widgetLayoutResource = R.layout.preference_account
    }

    lateinit var listener: OnLogoutClickListener

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val button = holder.findViewById(R.id.logout)
        button.isClickable = true
        button.setOnClickListener {
            AlertDialog.Builder(context)
                    .setTitle("ログアウト")
                    .setMessage("この端末からログアウトします。よろしいですか？")
                    .setPositiveButton("はい") { _, _ ->
                        listener.onLogoutClickListener(it)
                    }
                    .setNegativeButton("キャンセル") { _, _ -> }
                    .show()
        }
    }

    interface OnLogoutClickListener {
        fun onLogoutClickListener(view: View)
    }

    fun setOnLogoutClickListener(listener: OnLogoutClickListener) {
        this.listener = listener
    }
}