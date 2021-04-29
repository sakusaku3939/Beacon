package com.sakusaku.beacon.ui.search

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
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
        val searchUserUpdate = { name: String ->
            // 検索結果リストをリセット
            thread {
                handler.post {
                    searchList.clear()
                    recyclerView.adapter = SearchListAdapter(searchList)
                }
            }
            // 取得したデータを検索結果に反映
            FirestoreUtils.searchUser(name, region, subject) { onlineUserMap, offlineUserMap ->
                swipeRefresh.isRefreshing = false
                val getStrVal = { map: Map<String, Any?>, key: String ->
                    map[key]?.toString() ?: ""
                }
                val addList = { map: List<Map<String, Any?>>, state: Boolean ->
                    map.forEach {
                        searchList.add(SearchList(getStrVal(it, "name"), getStrVal(it, "position"), getStrVal(it, "location"), state))
                    }
                }
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
            searchUserUpdate("")
        }

        // 入力完了を検知したら検索結果読み込み
        var latestSearchText = ""
        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        searchEditText.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                val inputMethodManager = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
                swipeRefresh.post {
                    swipeRefresh.isRefreshing = true
                    latestSearchText = searchEditText.text.toString()
                    searchUserUpdate(latestSearchText)
                }
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        // リロードが実行された時
        swipeRefresh.setOnRefreshListener {
            searchUserUpdate(latestSearchText)
        }

        // 戻るボタン
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

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val searchToolbar = findViewById<LinearLayout>(R.id.searchToolBar)
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(searchToolbar.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        searchToolbar.requestFocus()
        return super.dispatchTouchEvent(event)
    }
}