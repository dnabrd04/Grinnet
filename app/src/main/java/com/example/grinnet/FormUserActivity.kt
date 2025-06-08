package com.example.grinnet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.grinnet.data.UserRequest
import com.example.grinnet.service.UserService
import com.example.grinnet.utils.SessionManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FormUserActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editUsername: EditText
    private lateinit var editDescription: EditText
    private lateinit var errorText: TextView
    private lateinit var nextButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_form_user)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var firebaseId = ""

        val extras = intent.extras

        if (extras != null) {
            firebaseId = extras.getString("firebaseId")!!
        }

        editName = findViewById(R.id.editName)
        editUsername = findViewById(R.id.editUsername)
        editDescription = findViewById(R.id.editDescription)
        errorText = findViewById(R.id.errorText)
        nextButton = findViewById(R.id.nextButton)
        val auth = Firebase.auth

        nextButton.setOnClickListener {
            val call = ApiClient.userService.existsUsername(editUsername.text.toString())

            call.enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.isSuccessful) {
                        Log.d("Respuesta", response.body().toString())
                        if (!response.body()!!) {
                            getTokenPush(firebaseId)
                            goToHome()
                        } else {
                            errorText.text = getString(R.string.usernameExistsError)
                            auth.currentUser?.delete()
                        }
                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    auth.currentUser?.delete()
                }

            })

        }
    }

    /**
     * Get the token for the push notifications and create the user with the token received
     */
    private fun getTokenPush(firebaseId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                val token = it.result
                createUser(firebaseId, token)
            }
        }
    }

    /**
     * Makes a post call to create the user.
     */
    private fun createUser(firebaseId: String, token: String) {
        val user = UserRequest(null, "", editUsername.text.toString().lowercase(), "public", firebaseId, token, editName.text.toString(), editDescription.text.toString())
        Log.d("", "Creando usuario $user")
        val call = ApiClient.userService.createUser(user)
        call.enqueue(object : Callback<UserRequest> {
            override fun onResponse(
                call: Call<UserRequest>,
                response: Response<UserRequest>
            ) {
                if (response.isSuccessful) {
                    SessionManager.saveUserId(this@FormUserActivity, response.body()?.idUser ?: -1L)
                    Log.d("CreateUser", "Usuario creado correctamente: ${response.body()}")
                } else {
                    Log.e("CreateUser", "Error al crear usuario: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UserRequest>, t: Throwable) {
                Log.e("CreateUser", "Fallo en la creación del usuario: ${t.message}")
            }
        })
    }

    /**
     * Launch the main activity.
     */
    private fun goToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}