package com.sakusaku.beacon.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sakusaku.beacon.CircleView
import com.sakusaku.beacon.R

class SearchListAdapter(private val searchList: ArrayList<SearchList>) : RecyclerView.Adapter<SearchListAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.searchResultName)
        val position: TextView = view.findViewById(R.id.searchResultPosition)
        val location: TextView = view.findViewById(R.id.searchResultLocation)
        val state: CircleView = view.findViewById(R.id.stateCircle)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.search_result_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val list = searchList[position]

        viewHolder.name.text = list.name
        viewHolder.position.text = list.position
        viewHolder.location.text = list.location
        viewHolder.state.setColor(if (list.state) R.color.green else R.color.gray)
    }

    override fun getItemCount() = searchList.size
}