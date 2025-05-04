package com.example.grinnet.service

import com.example.grinnet.data.Like
import com.example.grinnet.data.UserRequest
import com.example.grinnet.data.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LikeService {
    @POST("/like")
    fun createLike(@Body like: Like): Call<Like>

    @GET("/like/{id}")
    fun getCountLike(@Path("id") id: Long): Call<Like>

    @DELETE("/like/{id}")
    fun deleteLike(@Path("id") id: Long, @Query("postId") postId: Long): Call<Like>
}