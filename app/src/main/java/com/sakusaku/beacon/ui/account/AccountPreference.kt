package com.sakusaku.beacon.ui.account

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.sakusaku.beacon.R

class AccountPreference @JvmOverloads constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int = 0) : Preference(context, attrs, defStyleAttr) {
    init {
        widgetLayoutResource = R.layout.preference_account
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        // holder.itemView.setClickable(false);
        val button = holder.findViewById(R.id.logout)
        button.isClickable = true
        button.setOnClickListener { Log.d("test", "ok") }
    }
}