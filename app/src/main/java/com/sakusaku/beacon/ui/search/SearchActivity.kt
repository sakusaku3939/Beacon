package com.sakusaku.beacon.ui.search

import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.sakusaku.beacon.FirestoreUtils
import com.sakusaku.beacon.R

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_activity)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        val region = intent.getStringExtra("region") ?: ""
        val subject = intent.getStringExtra("subject") ?: ""

        FirestoreUtils.searchUser("", region, subject) { onlineUserMap, offlineUserMap ->
        }

        // 戻る
        val searchBackButton = findViewById<ImageButton>(R.id.searchBackButton)
        searchBackButton.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, 0)
        finish()
    }
}