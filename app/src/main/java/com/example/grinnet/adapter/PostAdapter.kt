package com.example.grinnet.adapter

import android.content.Intent
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.grinnet.ApiClient
import com.example.grinnet.CreatePostActivity
import com.example.grinnet.R
import com.example.grinnet.data.Like
import com.example.grinnet.data.PostResponse
import com.example.grinnet.data.UserEmpty
import com.example.grinnet.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostAdapter(private var postList: MutableList<PostResponse>, val context: Context):
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    class ViewHolder(element: View): RecyclerView.ViewHolder(element) {
        val username = element.findViewById<TextView>(R.id.usernamePost)
        val numLikes = element.findViewById<TextView>(R.id.numLikes)
        val numComments = element.findViewById<TextView>(R.id.numComments)
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
        holder.numLikes.text = post.likeCount.toString()
        holder.numComments.text = post.commentCount.toString()

        holder.likeButton.setOnClickListener {
            giveLike(post)
        }

        holder.commentButton.setOnClickListener {
            goCommentActivity(post)
        }

        holder.replyButton.setOnClickListener {
            goCreatePostActivity(post)
        }
    }

    private fun giveLike(post: PostResponse) {
        val user = UserEmpty(SessionManager.init(context) ?: -1L)
        Log.d("Prueba de valores de post", post.toString())
        val like =  Like(user, post)

        ApiClient.likeService.createLike(like).enqueue(object: Callback<Like> {

            override fun onResponse(call: Call<Like>, response: Response<Like>) {
                if(response.isSuccessful) {

                    val list = response.body() as List<PostResponse>
                    for(i in 0..list.size) {
                        Log.d("Respuesta del like", list[i].toString())
                    }
                }
            }

            override fun onFailure(call: Call<Like>, t: Throwable) {
                Log.d("Error del like", t.message.toString())
            }

        })
    }

    private fun goCommentActivity(post: PostResponse) {
        val intent = Intent(context, CreatePostActivity::class.java)
        intent.putExtra("postRelated", post.idPost)
        context.startActivity(intent)
    }

    private fun goCreatePostActivity(post: PostResponse) {
        val intent = Intent(context, CreatePostActivity::class.java)
        intent.putExtra("postRelated", post.idPost)
        context.startActivity(intent)

    }

    fun updateData(newItems: List<PostResponse>) {
        postList.clear()
        postList.addAll(newItems)
        notifyDataSetChanged()
    }
}