package com.example.grinnet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.grinnet.R
import com.example.grinnet.data.UserRequest

class UserAdapter(private val users: List<UserRequest>, private val onUserClickListener: OnUserClickListener) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val username = view.findViewById<TextView>(R.id.userItemName)
        val userItemContainer = view.findViewById<LinearLayout>(R.id.userItemContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_element, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.username.text = user.username

        holder.userItemContainer.setOnClickListener {
            onUserClickListener.onUserClick(user)
        }
    }
}