package com.example.grinnet.adapter

import android.content.Intent
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grinnet.ApiClient
import com.example.grinnet.CreatePostActivity
import com.example.grinnet.R
import com.example.grinnet.data.CommentRequest
import com.example.grinnet.data.CommentResponse
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
import java.util.Date
import androidx.core.view.isVisible
import java.text.SimpleDateFormat
import java.util.Locale

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
        val commentContainer = element.findViewById<LinearLayout>(R.id.commentsContainer)
        val commentList = element.findViewById<RecyclerView>(R.id.commentList)
        val commentInput = element.findViewById<EditText>(R.id.commentInput)
        val senderCommentButton = element.findViewById<ImageButton>(R.id.sendCommentButton)
        val relatedPostContainer = element.findViewById<LinearLayout>(R.id.relatedPostContainer)
        val relatedPostUsername = element.findViewById<TextView>(R.id.relatedPostUsername)
        val relatedPostText = element.findViewById<TextView>(R.id.relatedPostText)
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
        holder.imageContainer.removeAllViews()
        val post = postList[position]
        holder.username.text = post.user.username
        holder.postContent.text = post.text
        holder.numLikes.text = post.likeCount.toString()
        holder.numComments.text = post.commentCount.toString()
        holder.likeButton.setImageResource(R.drawable.empty_favorite_icon)
        holder.commentContainer.visibility = View.GONE

        val locale = holder.itemView.context.resources.configuration.locales[0]
        val date = OffsetDateTime.parse(post.creationDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val format = DateTimeFormatter.ofPattern("d MMM yyyy", locale).withLocale(locale)
        val formattedDate = date.format(format)
        holder.publicationDate.text = formattedDate

        Log.d("Post related", "Este es el post: ${post.toString()}")
        if (post.postRelated?.idPost != null) {
            holder.relatedPostContainer.visibility = View.VISIBLE
            val dto = PostDTORequest(post.postRelated.idPost, Firebase.auth.uid ?: "")
            ApiClient.postService.getPost(dto).enqueue(object : Callback<PostResponse> {
                override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                    if (response.isSuccessful) {
                        val originalPost = response.body()!!
                        holder.relatedPostUsername.text = originalPost.user.username
                        holder.relatedPostText.text = originalPost.text

                        /*holder.relatedPostContainer.setOnClickListener {
                            goCreatePostActivity(originalPost)
                        }*/
                    }
                }

                override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                    holder.relatedPostContainer.visibility = View.GONE
                }
            })
        } else {
            holder.relatedPostContainer.visibility = View.GONE
        }

        if (post.resources != null && post.resources.isNotEmpty()) {
            holder.imageContainer.visibility = View.VISIBLE
            Utils.addViewImage(holder.imageContainer, post.resources, context)
        } else {
            holder.imageContainer.visibility = View.GONE
        }

        if (post.liked) {
            holder.likeButton.setImageResource(R.drawable.favorite_icon)
        }

        //if (post.user.image == "") {
            holder.userImage.setImageResource(R.drawable.account_icon)
        /*} else {
//            holder.userImage.setImageResource()
        }*/

        holder.likeButton.setOnClickListener {
            val currentPosition = holder.adapterPosition

            if (currentPosition != RecyclerView.NO_POSITION) {
                val currentPost = postList[currentPosition]
                if (currentPost.liked) {
                    removeLike(currentPost, currentPosition)
                } else {
                    giveLike(currentPost, currentPosition)
                }
            }
        }

        holder.commentButton.setOnClickListener {
            if (holder.commentContainer.isVisible) {
                holder.commentContainer.visibility = View.GONE
            } else {
                holder.commentContainer.visibility = View.VISIBLE
                val callCommentList = ApiClient.commentService.getCommentsByPost(post.idPost)
                callCommentList.enqueue(object : Callback<MutableList<CommentResponse>> {
                    override fun onResponse(
                        call: Call<MutableList<CommentResponse>>,
                        response: Response<MutableList<CommentResponse>>
                    ) {
                        if (response.isSuccessful) {
                            holder.commentList.adapter = CommentAdapter(response.body()!!, context)
                            holder.commentList.layoutManager = LinearLayoutManager(context)
                        }
                    }

                    override fun onFailure(call: Call<MutableList<CommentResponse>>, t: Throwable) {
                    }

                })
            }
        }

        holder.replyButton.setOnClickListener {
            goCreatePostActivity(post)
        }

        holder.userImage.setOnClickListener {
            goUserProfile(post.user)
        }

        holder.senderCommentButton.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                val currentPost = postList[currentPosition]
                sendComment(currentPost, (holder.commentInput.text ?: "").toString(), currentPosition)
            }
        }
    }

    private fun goUserProfile(user: UserRequest) {
//        val intent = Intent(context, UserProfileActivity::class.java)
//        intent.putExtra("user", user)
//        context.startActivity(intent)
        onUserClickListener.onUserClick(user)
    }

    private fun sendComment(post: PostResponse, text: String, position: Int) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date())

        val commentUser = UserEmpty(SessionManager.init(context) ?: -1L)
        val comment = CommentRequest(commentUser, post, text, formattedDate)
        val call = ApiClient.commentService.createComment(comment)

        call.enqueue(object : Callback<CommentResponse> {
            override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                if (response.isSuccessful) {
                    updatePost(post, position)
                }
            }

            override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
            }
        })
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
        postList = newItems.toMutableList()
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