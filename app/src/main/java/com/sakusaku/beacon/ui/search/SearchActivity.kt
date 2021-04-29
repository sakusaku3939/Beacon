package com.sakusaku.beacon.ui.search

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sakusaku.beacon.FirestoreUtils
import com.sakusaku.beacon.R
import kotlin.concurrent.thread

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_activity)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        val region = intent.getStringExtra("region") ?: ""
        val subject = intent.getStringExtra("subject") ?: ""

        val recyclerView = findViewById<RecyclerView>(R.id.searchRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val handler = Handler()
        val searchList = arrayListOf<SearchList>()
        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        val searchUserUpdate = {
            // 検索結果の中身をリセット
            thread {
                handler.post {
                    searchList.clear()
                    recyclerView.adapter = SearchListAdapter(searchList)
                }
            }
            // 取得したデータを検索結果に反映
            FirestoreUtils.searchUser("", region, subject) { onlineUserMap, offlineUserMap ->
                swipeRefresh.isRefreshing = false
                val getStrVal = { map: Map<String, Any?>, key: String ->
                    map[key]?.toString() ?: ""
                }
                val addList = {map: List<Map<String, Any?>>, state: Boolean -> map.forEach {
                    searchList.add(SearchList(getStrVal(it, "name"), getStrVal(it, "position"), getStrVal(it, "location"), state))
                }}
                addList(onlineUserMap, true)
                addList(offlineUserMap, false)
                thread {
                    handler.post {
                        recyclerView.adapter = SearchListAdapter(searchList)
                    }
                }
            }
        }

        // 初回読み込み
        swipeRefresh.post {
            swipeRefresh.isRefreshing = true
            searchUserUpdate()
        }
        // リロードが実行された時
        swipeRefresh.setOnRefreshListener {
            searchUserUpdate()
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