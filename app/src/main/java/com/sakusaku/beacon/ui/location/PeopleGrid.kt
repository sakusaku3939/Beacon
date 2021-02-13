package com.sakusaku.beacon.ui.location

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PeopleGrid(private val context: Context, private val peopleRecyclerView: RecyclerView) {
    private val map = mutableMapOf<String, People>()
    private val adapterPositionList = mutableMapOf<String, Int>()
    private lateinit var customAdapter: PeopleGridAdapter
    private var listener: OnClickListener? = null

    fun add(image: Int, uid: String, name: String, location: String, timestamp: String = "") {
        map[uid] = People(image, name, location, timestamp)
        customAdapter = PeopleGridAdapter(map, object : PeopleGridAdapter.ListListener {
            override fun onBindView(holder: RecyclerView.ViewHolder, uid: String, position: Int) {
                adapterPositionList[uid] = position
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
            map.remove(uid)
        }
    }

    fun count(): Int = map.size

    private fun applyView(context: Context, peopleRecyclerView: RecyclerView, customAdapter: PeopleGridAdapter) {
        val manager = LinearLayoutManager(context)
        manager.orientation = LinearLayoutManager.HORIZONTAL
        peopleRecyclerView.apply {
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