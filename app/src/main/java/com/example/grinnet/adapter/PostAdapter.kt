package com.example.grinnet.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.grinnet.R
import com.example.grinnet.data.PostResponse

class PostAdapter(private var postList: MutableList<PostResponse>, val context: Context):
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    class ViewHolder(element: View): RecyclerView.ViewHolder(element) {
        val username = element.findViewById<TextView>(R.id.usernamePost)
        val postContent = element.findViewById<TextView>(R.id.postContentText)
        val likeButton = element.findViewById<ImageButton>(R.id.likeButton)
        val commentButton = element.findViewById<ImageButton>(R.id.commentButton)
        val replyButton = element.findViewById<ImageButton>(R.id.replyButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("PostAdapter", "onCreateViewHolder")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_element, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        Log.d("PostAdapter", "getItemCount() = ${postList.size}")
        return postList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("PostAdapter", "onBindViewHolder position=$position")
        val post = postList[position]
        holder.username.text = post.user.username
        holder.postContent.text = post.text

        holder.likeButton.setOnClickListener {
            giveLike(post)
        }

        holder.likeButton.setOnClickListener {
            goCommentActivity(post)
        }

        holder.likeButton.setOnClickListener {
            goCreatePostActivity(post)
        }
    }

    private fun giveLike(post: PostResponse) {

    }

    private fun goCommentActivity(post: PostResponse) {

    }

    private fun goCreatePostActivity(post: PostResponse) {

    }

    fun updateData(newItems: List<PostResponse>) {
        postList.clear()
        postList.addAll(newItems)
        notifyDataSetChanged()
    }
}