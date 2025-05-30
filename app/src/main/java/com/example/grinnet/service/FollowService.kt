package com.example.grinnet.service

import com.example.grinnet.data.FollowRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FollowService {

    @POST("/follow")
    fun createFollow(@Body follow: FollowRequest): Call<FollowRequest>

    @GET("/follow/{id}/followers")
    fun getFollowersByUser(@Path("id") idPost: Long): Call<Long>

    @GET("/follow/{id}/following")
    fun getFollowingsByUser(@Path("id") idPost: Long): Call<Long>
}