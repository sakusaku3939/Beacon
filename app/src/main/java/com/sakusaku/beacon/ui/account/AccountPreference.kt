package com.sakusaku.beacon.ui.account

import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.AttributeSet
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.sakusaku.beacon.MainActivity
import com.sakusaku.beacon.R


class AccountPreference @JvmOverloads constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int = 0) : Preference(context, attrs, defStyleAttr) {
    init {
        widgetLayoutResource = R.layout.preference_account
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val button = holder.findViewById(R.id.logout)
        button.isClickable = true
        button.setOnClickListener {
            AlertDialog.Builder(context)
                    .setTitle("ログアウト")
                    .setMessage("この端末からログアウトします。よろしいですか？")
                    .setPositiveButton("はい") { _, _ ->
                        val restartIntent = Intent(Intent.ACTION_MAIN)
                        restartIntent.setClassName(context.packageName, MainActivity::class.java.name)
                        restartIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(restartIntent)
                        // TODO: Firebaseからサインアウトする
                        // TODO: MainActivityでログインの有無を確認する処理の追加

                        Process.killProcess(Process.myPid())
                    }
                    .setNegativeButton("キャンセル") { _, _ -> }
                    .show()
        }
    }
}