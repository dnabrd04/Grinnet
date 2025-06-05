package com.example.grinnet

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.example.grinnet.data.UserRequest
import com.example.grinnet.data.UserResponse
import com.example.grinnet.network.OkHttp3Stack
import com.example.grinnet.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    companion object {
        lateinit var requestQueue: RequestQueue
            private set
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {}

    private lateinit var notificationButton: ImageView

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Cambiar el ícono de notificación cuando llegue nueva
            notificationButton.setImageResource(R.drawable.notification_icon)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Utils.initNotificationChannel(this)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val stack = OkHttp3Stack(okHttpClient)
        requestQueue = Volley.newRequestQueue(this, stack)

        val fragment = HomeFragment()

        supportFragmentManager.beginTransaction().add(R.id.fragmentContainerView, fragment).commit()

        checkNotifiationPermission()

        val homeButton = findViewById<ImageView>(R.id.homeButton)

        homeButton.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, HomeFragment())
                .addToBackStack(null)
                .commit()
        }

        notificationButton = findViewById<ImageView>(R.id.notificationButton)

        notificationButton.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, NotificationsFragment())
                .addToBackStack(null)
                .commit()
        }

        val userAvatar = findViewById<ImageView>(R.id.userAvatar)

        userAvatar.setOnClickListener {
            val callUserByFirebaseid = ApiClient.userService.getUserByFirebaseId(Firebase.auth.uid ?: "")

            callUserByFirebaseid.enqueue(object: Callback<UserRequest> {
                override fun onResponse(
                    call: Call<UserRequest>,
                    response: Response<UserRequest>
                ) {
                    if (response.isSuccessful) {
                        changeFragmentToUserProfile(response.body()!!)
                    }
                }

                override fun onFailure(call: Call<UserRequest>, t: Throwable) {
                }
            })
        }



        /*val button = findViewById<Button>(R.id.button)



        button.setOnClickListener {
            Firebase.auth.signOut()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }*/

    }

    fun changeNotificationIcon() {
        notificationButton.setImageResource(R.drawable.notification_none_icon)
    }

    fun changeFragmentToUserProfile(user: UserRequest) {
        val fragment = UserProfileFragment()
        val args = Bundle()
        args.putSerializable("user", user)
        fragment.arguments = args
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, fragment).commit()
    }

    fun checkNotifiationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission()
            }
        }
    }

    fun requestNotificationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}