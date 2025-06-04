package com.example.grinnet.adapter

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grinnet.data.CommentResponse
import com.example.grinnet.R

class CommentAdapter(
    private val commentList: MutableList<CommentResponse>, private val context: Context
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: ImageView = itemView.findViewById(R.id.commentAvatar)
        val username: TextView = itemView.findViewById(R.id.commentUsername)
        val text: TextView = itemView.findViewById(R.id.commentText)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.comment_element, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        holder.username.text = comment.user.username
        holder.text.text = comment.text

        if (!comment.user.image.isNullOrEmpty()) {
            Glide.with(context)
                .load(comment.user.image)
                .centerCrop()
                .into(holder.userImage)
        } else {
            holder.userImage.setImageResource(R.drawable.account_icon)
        }
    }

    override fun getItemCount(): Int {
        Log.d("commentAdapter", "getItemCount() = ${commentList.size}")
        return commentList.size
    }

    /**
     * Actualiza la lista entera de comentarios
     */
    fun updateData(newComments: List<CommentResponse>) {
        commentList.clear()
        commentList.addAll(newComments)
        notifyDataSetChanged()
    }

    /**
     * Añade un nuevo comentario al final
     */
    fun addComment(comment: CommentResponse) {
        commentList.add(comment)
        notifyItemInserted(commentList.size - 1)
    }
}