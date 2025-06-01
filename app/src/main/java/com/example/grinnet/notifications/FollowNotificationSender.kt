package com.example.grinnet.notifications

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.grinnet.MainActivity
import org.json.JSONObject

object FollowNotificationSender {
    fun sendFollowNotification(
        context: Context,
        fcmToken: String,
        followerUserId: Long
    ) {
        val url = "http://10.0.2.2:8081/sendFollowNotification"

        val payload = JSONObject().apply {
            put("token", fcmToken)
            put("followerUserId", followerUserId)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, url, payload,
            { response -> Log.d("FollowNotify", "Notificación enviada: $response") },
            { error -> Log.e("FollowNotify", "Error: ${error.message}") }
        )

        MainActivity.requestQueue.add(request)
    }
}