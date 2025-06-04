package com.example.grinnet.adapter

import android.content.Intent
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.grinnet.ApiClient
import com.example.grinnet.CreatePostActivity
import com.example.grinnet.R
import com.example.grinnet.data.Like
import com.example.grinnet.data.PostDTORequest
import com.example.grinnet.data.PostResponse
import com.example.grinnet.data.UserEmpty
import com.example.grinnet.data.UserRequest
import com.example.grinnet.utils.SessionManager
import com.example.grinnet.utils.Utils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class PostAdapter(private var postList: MutableList<PostResponse>, val context: Context, private val onUserClickListener: OnUserClickListener):
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    class ViewHolder(element: View): RecyclerView.ViewHolder(element) {
        val userImage = element.findViewById<ImageView>(R.id.imageUserPost)
        val username = element.findViewById<TextView>(R.id.usernamePost)
        val publicationDate = element.findViewById<TextView>(R.id.publicationDate)
        val numLikes = element.findViewById<TextView>(R.id.numLikes)
        val numComments = element.findViewById<TextView>(R.id.numComments)
        val postContent = element.findViewById<TextView>(R.id.postContentText)
        val likeButton = element.findViewById<ImageButton>(R.id.likeButton)
        val commentButton = element.findViewById<ImageButton>(R.id.commentButton)
        val replyButton = element.findViewById<ImageButton>(R.id.replyButton)
        val imageContainer = element.findViewById<GridLayout>(R.id.imageContainer)
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
        holder.likeButton.setImageResource(R.drawable.empty_favorite_icon)

        val locale = holder.itemView.context.resources.configuration.locales[0]
        val date = OffsetDateTime.parse(post.creationDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val format = DateTimeFormatter.ofPattern("d MMM yyyy", locale).withLocale(locale)
        val formattedDate = date.format(format)
        holder.publicationDate.text = formattedDate

        if (post.resources != null && post.resources.isNotEmpty()) {
            holder.imageContainer.visibility = View.VISIBLE
            Utils.addViewImage(holder.imageContainer, post.resources, context)
        } else {
            holder.imageContainer.visibility = View.GONE
        }

        if (post.liked) {
            holder.likeButton.setImageResource(R.drawable.favorite_icon)
        }

        if (post.user.image == "") {
            holder.userImage.setImageResource(R.drawable.account_icon)
        } else {
//            holder.userImage.setImageResource()
        }

        holder.likeButton.setOnClickListener {

            if (post.liked) {
                removeLike(post, position)
            } else {
                giveLike(post, position)
            }
        }

        holder.commentButton.setOnClickListener {
            goCommentActivity(post)
        }

        holder.replyButton.setOnClickListener {
            goCreatePostActivity(post)
        }

        holder.userImage.setOnClickListener {
            goUserProfile(post.user)
        }
    }

    private fun goUserProfile(user: UserRequest) {
//        val intent = Intent(context, UserProfileActivity::class.java)
//        intent.putExtra("user", user)
//        context.startActivity(intent)
        onUserClickListener.onUserClick(user)
    }

    private fun giveLike(post: PostResponse, position: Int) {
        val user = UserEmpty(SessionManager.init(context) ?: -1L)
        val like =  Like(user, post)

        ApiClient.likeService.createLike(like).enqueue(object: Callback<Void> {

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.isSuccessful) {
                    updatePost(post, position)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("Error del like", t.message.toString())
            }

        })
    }

    private fun removeLike(post: PostResponse, position: Int) {
        val user = UserEmpty(SessionManager.init(context) ?: -1L)

        ApiClient.likeService.deleteLike(user.idUser, post.idPost).enqueue(object: Callback<Void> {

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    updatePost(post, position)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
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

    fun updatePost(postResponse: PostResponse, position: Int) {
        val postDTO = PostDTORequest(postResponse.idPost, Firebase.auth.uid ?: "")
        val call = ApiClient.postService.getPost(postDTO)
        call.enqueue(object: Callback<PostResponse> {
            override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                if (response.isSuccessful) {
                    val post = response.body()!!
                    Log.d("Post update", "Estos son los nuevos valores del post: ${post.likeCount}, ${post.liked}")
                    postList[position] = post
                    Log.d("Post update", "Estos son los valores cambiados: ${postList[position].likeCount}, ${postList[position].liked}")
                    notifyItemChanged(position)
                }
            }

            override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                Log.d("Error del like", t.message.toString())
            }

        })
    }
}

interface OnUserClickListener {
    fun onUserClick(user: UserRequest)
}