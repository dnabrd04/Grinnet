package com.example.grinnet

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.grinnet.data.PostRelated
import com.example.grinnet.data.PostRequest
import com.example.grinnet.data.PostResponse
import com.example.grinnet.data.ResourceRequest
import com.example.grinnet.data.UserRequest
import com.example.grinnet.utils.SessionManager
import com.example.grinnet.utils.Utils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CreatePostActivity : AppCompatActivity() {

    private var postRelatedValue: Long? = null
    private lateinit var storage: FirebaseStorage
    private val auth = Firebase.auth
    private val firebaseId = auth.currentUser!!.uid
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var resourceList: MutableList<ResourceRequest> = mutableListOf()
    private lateinit var imageContainer: GridLayout

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
            postRelatedValue = extras.getLong("postRelated")
        }

        storage = Firebase.storage(getString(R.string.BucketURL))

        setGalleryLauncher()

        val publishButton = findViewById<Button>(R.id.publishButton)
        val closeButton = findViewById<Button>(R.id.closeButton)
        val addImageButton = findViewById<ImageButton>(R.id.addImage)
        imageContainer = findViewById(R.id.imageContainer)

        publishButton.setOnClickListener {
            createPost()
        }

        closeButton.setOnClickListener {
            this.finish()
        }

        addImageButton.setOnClickListener {
            if (resourceList.size >= 4) {
                Toast.makeText(this, "Solo puedes subir hasta 4 imágenes", Toast.LENGTH_SHORT).show()
            } else {
                openGallery()
            }
        }
    }

    private fun setGalleryLauncher() {
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (resourceList.size >= 4) {
                    Toast.makeText(this, "Has alcanzado el límite de 4 imágenes", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                
                val data: Intent? = result.data
                val imageUri: Uri? = data?.data
                if (imageUri != null) {
                    val ref = storage.reference
                    val imageRef = ref.child("images/$firebaseId/${UUID.randomUUID()}.jpg")
                    val uploadTask = imageRef.putFile(imageUri)

                    uploadTask.addOnSuccessListener {
                        imageRef.downloadUrl.addOnCompleteListener {
                            uri ->
                            addToResourceList(uri.result.toString())
                            //addViewImage()
                            Utils.addViewImage(imageContainer, resourceList, this)
                            Toast.makeText(this, "Imagen subida", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /*private fun addViewImage() {
        imageContainer.removeAllViews()
        val totalImages = resourceList.size

        for ((index, image) in resourceList.withIndex()) {
            val imageView = ImageView(this)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(this).load(image.url).centerCrop().into(imageView)
            val params = GridLayout.LayoutParams()

            when (totalImages) {
                1 -> {
                    params.rowSpec = GridLayout.spec(0)
                    params.columnSpec = GridLayout.spec(0, 2)
                    params.width = GridLayout.LayoutParams.MATCH_PARENT
                    params.height = GridLayout.LayoutParams.MATCH_PARENT
                }
                2 -> {
                    params.rowSpec = GridLayout.spec(0, 1f)
                    params.columnSpec = GridLayout.spec(index, 1f)
                    params.width = 0
                    params.height = 0
                }
                3 -> {
                    if (index == 0) {
                        params.rowSpec = GridLayout.spec(0, 2, 1f)
                        params.columnSpec = GridLayout.spec(0, 1f)
                        params.width = 0
                        params.height = 0
                    } else {
                        params.rowSpec = GridLayout.spec(index - 1, 1f)
                        params.columnSpec = GridLayout.spec(1, 1f)
                        params.width = 0
                        params.height = 0
                    }
                }
                4 -> {
                    params.rowSpec = GridLayout.spec(index / 2, 1f)
                    params.columnSpec = GridLayout.spec(index % 2, 1f)
                    params.width = 0
                    params.height = 0
                }
            }
            imageView.layoutParams = params
            imageContainer.addView(imageView)
        }
    }*/

    /**
     * Adds the resource to the list if the list has minus than 4 elements.
     */
    private fun addToResourceList(uri: String) {
        if (resourceList.size == 4) return

        val resourceRequest = ResourceRequest(null, uri, null, resourceList.size)
        resourceList.add(resourceRequest)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/* video/*"
        }
        pickImageLauncher.launch(intent)
    }

    private fun createPost() {
        SessionManager.init(this)

        //I need this because the user field in the backend is an object and not a long
        val userRequest = UserRequest(SessionManager.userId!!, "", "", "", "", "", "", "")

        //I need this because the format of the date doesn't match the backend format.
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date())

        val creationPostText = findViewById<EditText>(R.id.creationPostText)

        //The creation of the post that will be send to the backend
        val postRelated = if(postRelatedValue == null) null else PostRelated(postRelatedValue)
        val postRequest = PostRequest(null, userRequest, postRelated, "public", creationPostText.text.toString(), formattedDate, resourceList)
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