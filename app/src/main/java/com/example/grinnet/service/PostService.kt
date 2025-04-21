package com.example.grinnet.service

import com.example.grinnet.data.PostRequest
import com.example.grinnet.data.PostResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PostService {

    @POST("/post")
    fun createPost(@Body post: PostRequest): Call<PostResponse>

    @GET("/post/{id}")
    fun getPost(@Path("id") idPost: Long): Call<PostResponse>

    @GET("/post")
    fun getPosts(): Call<MutableList<PostResponse>>
}