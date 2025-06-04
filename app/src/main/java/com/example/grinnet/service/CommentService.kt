package com.example.grinnet.service

import com.example.grinnet.data.CommentRequest
import com.example.grinnet.data.CommentResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CommentService {

    @POST("/comment")
    fun createComment(@Body request: CommentRequest): Call<CommentResponse>

    @GET("/comment/{id}")
    fun getCommentsByPost(@Path("id") postId: Long): Call<MutableList<CommentResponse>>
}