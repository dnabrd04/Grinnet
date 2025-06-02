package com.example.grinnet.utils

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.grinnet.MainActivity
import com.example.grinnet.data.ResourceRequest
import com.example.grinnet.data.ResourceResponse

object Utils{

    fun <T> addViewImage(imageContainer: GridLayout, resourceList: MutableList<T>, context: Context) {
        imageContainer.removeAllViews()
        val totalImages = resourceList.size

        for ((index, image) in resourceList.withIndex()) {
            val url = when (image) {
                is ResourceRequest -> image.url
                is ResourceResponse -> image.url
                else -> null
            }
            Log.d("Resource", url.toString())

            val imageView = ImageView(context)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(context).load(url).centerCrop().into(imageView)
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
    }

    /**
     * Launch the main activity.
     */
    fun goToHome(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)
        (context as Activity).finish()
    }

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "seguidores",
                "Notificaciones de Seguidores",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para notificaciones de nuevos seguidores"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
