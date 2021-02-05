package com.sakusaku.beacon.ui.location

import android.content.Context
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PeopleGrid(private val context: Context, private val peopleRecyclerView: RecyclerView?) {
    val list = mutableMapOf<String, People>()
    private val adapterPositionList = mutableMapOf<String, Int>()
    private lateinit var customAdapter: PeopleGridAdapter
    private var listener: OnClickListener? = null

    fun add(image: Int, uid: String, name: String, location: String, timestamp: String = "") {
        list[uid] = People(image, name, location, timestamp)
        customAdapter = PeopleGridAdapter(list, object : PeopleGridAdapter.ListListener {
            override fun onBindView(holder: RecyclerView.ViewHolder, uid: String, position: Int) {
                adapterPositionList[uid] = position
                Log.d("test", "$uid:$position")
            }

            override fun onClickItem(tappedView: View, name: String, location: String) {
                listener?.onClickItem(tappedView, name, location)
            }
        })
        applyView(context, peopleRecyclerView, customAdapter)
    }

    fun remove(uid: String) {
        adapterPositionList[uid]?.let {
            customAdapter.notifyItemRemoved(it)
            list.remove(uid)
        }
    }

    private fun applyView(context: Context, peopleRecyclerView: RecyclerView?, customAdapter: PeopleGridAdapter) {
        peopleRecyclerView?.apply {
            val manager = LinearLayoutManager(context)
            manager.orientation = LinearLayoutManager.HORIZONTAL
            layoutManager = manager
            isNestedScrollingEnabled = false
            adapter = customAdapter
            setHasFixedSize(true)
        }
    }

    interface OnClickListener {
        fun onClickItem(tappedView: View, name: String, location: String) {}
    }

    fun onClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    data class People(
            val image: Int,
            val name: String,
            val location: String,
            val timestamp: String
    )
}