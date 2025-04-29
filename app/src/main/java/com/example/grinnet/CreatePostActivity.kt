package com.example.grinnet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.grinnet.data.PostRequest
import com.example.grinnet.data.PostResponse
import com.example.grinnet.data.UserRequest
import com.example.grinnet.utils.SessionManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreatePostActivity : AppCompatActivity() {

    private var postRelated: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_post)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.creationPost)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val extras = intent.extras

        if(extras != null) {
            postRelated = extras.getLong("postRelated")
        }

        val publishButton = findViewById<Button>(R.id.publishButton)
        val closeButton = findViewById<Button>(R.id.closeButton)

        publishButton.setOnClickListener {
            createPost()
        }

        closeButton.setOnClickListener {
            this.finish()
        }
    }

    private fun createPost() {
        SessionManager.init(this)

        //I need this because the user field in the backend is an object and not a long
        val userRequest = UserRequest(SessionManager.userId!!, "", "", "", "", "", "")

        //I need this because the format of the date doesn't match the backend format.
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date())

        val creationPostText = findViewById<EditText>(R.id.creationPostText)

        //The creation of the post that will be send to the backend
        val postRequest = PostRequest(null, userRequest, postRelated, "public", creationPostText.text.toString(), formattedDate)
        val call = ApiClient.postService.createPost(postRequest)

        call.enqueue(object: Callback<PostResponse>{
            override fun onResponse(
                call: Call<PostResponse>,
                response: Response<PostResponse>
            ) {
                Log.d("Respuesta", response.body().toString())
            }

            override fun onFailure(call: Call<PostResponse>, t: Throwable) {
            }
        })
        this.finish()
    }
}