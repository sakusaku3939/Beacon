package com.sakusaku.beacon.ui.search

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.sakusaku.beacon.firebase.FirestoreUtils
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
        swipeRefresh.isEnabled = false

        val searchUserUpdate = { name: String ->
            // 検索結果リストをリセット
            thread {
                handler.post {
                    searchList.clear()
                    recyclerView.adapter = SearchListAdapter(searchList)
                }
            }
            // 取得したデータを検索結果に反映
            FirestoreUtils.searchUser(name = name, region = region, subject = subject) { onlineUserMap, offlineUserMap ->
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

        // Enterボタンが押されたらキーボードを隠す
        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        searchEditText.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                val inputMethodManager = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        // 入力欄リスナー
        var latestSearchText = ""
        searchEditText.addTextChangedListener(object : TextWatcher {
            val editHandler = Handler()
            var runnable: Runnable? = null
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                // 一秒経過で検索結果を読み込み
                runnable?.let { editHandler.removeCallbacks(it) }
                val r = Runnable {
                    swipeRefresh.post {
                        swipeRefresh.isRefreshing = true
                        latestSearchText = searchEditText.text.toString()
                        searchUserUpdate(latestSearchText)
                    }
                }
                editHandler.postDelayed(r, 1000)
                runnable = r

                // 入力欄が空になったら再読み込み
                if (s.isEmpty()) swipeRefresh.post {
                    swipeRefresh.isRefreshing = true
                    latestSearchText = searchEditText.text.toString()
                    searchUserUpdate(latestSearchText)
                }
            }
        })

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