package com.sakusaku.beacon.ui.location

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PeopleGrid(private val context: Context, private val peopleRecyclerView: RecyclerView?) {
    private val list = mutableListOf<People>()
    private lateinit var listener: OnClickListener

    fun add(image: Int, name: String, location: String) {
        list.add(People(image, name, location))
        val customAdapter = PeopleGridAdapter(list, object : PeopleGridAdapter.ListListener {
            override fun onClickItem(tappedView: View, name: String, location: String) {
                listener.onClickItem(tappedView, name, location)
            }
        })
        applyView(context, peopleRecyclerView, customAdapter)
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
            val location: String
    )
}