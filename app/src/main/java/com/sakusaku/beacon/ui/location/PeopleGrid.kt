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

    fun add(uid: String, name: String, location: String, timestamp: String) {
        map[uid] = People(name, location, timestamp)
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

    fun update(uid: String, location: String, timestamp: String) {
        adapterPositionList[uid]?.let {
            map[uid]?.location = location
            map[uid]?.timestamp = timestamp
            customAdapter.notifyItemChanged(it)
        }
    }

    fun remove(uid: String) {
        adapterPositionList[uid]?.let {
            map.remove(uid)
            customAdapter.notifyItemRemoved(it)
        }
    }

    fun removeAll() {
        if (count() > 0) {
            map.clear()
            customAdapter.notifyDataSetChanged()
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
            val name: String,
            var location: String,
            var timestamp: String
    )
}