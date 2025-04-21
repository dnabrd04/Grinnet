package com.example.grinnet

import com.example.grinnet.service.PostService
import com.example.grinnet.service.UserService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:8081"

    private val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val userService: UserService by lazy {
        instance.create(UserService::class.java)
    }

    val postService: PostService by lazy {
        instance.create(PostService::class.java)
    }
}