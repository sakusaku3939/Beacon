package com.sakusaku.beacon.ui.location

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sakusaku.beacon.R
import com.sakusaku.beacon.firebase.CloudStorageUtils

class PeopleGridAdapter(
        private val peopleMap: MutableMap<String, PeopleGrid.People>,
        private val listener: ListListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class PeopleGridHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val location: TextView = itemView.findViewById(R.id.location)
    }

    interface ListListener {
        fun onBindView(holder: RecyclerView.ViewHolder, uid: String, position: Int)
        fun onClickItem(tappedView: View, name: String, location: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.people_grid, parent, false)
        return PeopleGridHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val uid = peopleMap.keys.toMutableList()[position]
        val people = peopleMap.values.toMutableList()[position]
        val profilePhoto = holder.itemView.findViewById<ImageView>(R.id.profilePhoto)
        CloudStorageUtils.setProfileImage(profilePhoto, uid = uid)

        holder.itemView.findViewById<TextView>(R.id.name).text = people.name
        holder.itemView.findViewById<TextView>(R.id.location).text = people.location

        listener.onBindView(holder, uid, position)
        holder.itemView.setOnClickListener {
            listener.onClickItem(it, people.name, people.location)
        }
    }

    override fun getItemCount(): Int = peopleMap.size
}