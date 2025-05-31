package com.example.grinnet.notifications

import android.util.Log
import com.example.grinnet.ApiClient
import com.example.grinnet.data.UserResponse
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Class that controls the push notifications when the app is running.
 *
 * @author github: dnabr04
 * @date 30/05/2025
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            Log.d("FCM", "Notificación recibida: ${it.title} - ${it.body}")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val firebaseUser = Firebase.auth.currentUser

        if (firebaseUser != null) {
            val call = ApiClient.userService.updateToken(firebaseUser.uid, token)
            call.enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    if (response.isSuccessful) {
                        Log.d("Token", "Token actualizado correctamente")
                    } else {
                        Log.e("Token", "Error al actualizar token: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Log.e("Token", "Fallo al enviar token: ${t.message}")
                }
            })
        } else {
            Log.w("Token", "No se puede actualizar el token: usuario no autenticado aún")
        }
    }
}