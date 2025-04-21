package com.example.grinnet.utils

import android.content.Context

object SessionManager {
    private const val FILE_NAME = "session_prefs"
    private const val ID_KEY = "user_id"
    var userId: Long? = null

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        userId = prefs.getLong(ID_KEY, -1L).takeIf { it != -1L }
    }

    fun saveUserId(context: Context, id: Long) {
        val prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(ID_KEY, id).apply()
        userId = id
    }
}