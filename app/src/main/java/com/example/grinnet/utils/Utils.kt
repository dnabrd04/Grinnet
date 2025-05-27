package com.example.grinnet.utils

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.grinnet.data.ResourceRequest
import com.example.grinnet.data.ResourceResponse

object Utils{

    fun <T> addViewImage(imageContainer: GridLayout, resourceList: MutableList<T>, context: Context) {
        imageContainer.visibility = View.VISIBLE
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
}
