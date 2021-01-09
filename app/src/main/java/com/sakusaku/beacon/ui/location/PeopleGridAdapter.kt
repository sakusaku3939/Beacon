package com.sakusaku.beacon.ui.location

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sakusaku.beacon.R

class PeopleGridAdapter(
        private val peopleList: List<PeopleGrid>,
        private val listener: ListListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class PeopleGridHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val location: TextView = itemView.findViewById(R.id.location)
    }

    interface ListListener {
        fun onClickItem(tappedView: View, name: String, location: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.people_grid, parent, false)
        return PeopleGridHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val people = peopleList[position]
        holder.itemView.findViewById<TextView>(R.id.name).text = people.name
        holder.itemView.findViewById<TextView>(R.id.location).text = people.location
        holder.itemView.setOnClickListener {
            listener.onClickItem(it, people.name, people.location)
        }
    }

    override fun getItemCount(): Int = peopleList.size
}