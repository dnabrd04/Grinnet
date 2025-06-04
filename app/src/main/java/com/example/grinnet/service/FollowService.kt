package com.example.grinnet.service

import com.example.grinnet.data.FollowRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FollowService {

    @POST("/follow")
    fun createFollow(@Body follow: FollowRequest): Call<FollowRequest>

    @GET("/follow/{id}/followers")
    fun getFollowersByUser(@Path("id") id: Long): Call<String>

    @GET("/follow/{id}/following")
    fun getFollowingsByUser(@Path("id") id: Long): Call<String>

    @GET("/follow/check")
    fun checkIfUserFollows(
        @Query("idFollowed") idFollowed: Long,
        @Query("idFollower") idFollower: Long
    ): Call<Boolean>

    @DELETE("/follow/unfollow")
    fun unfollowUser(
        @Query("followerId") followerId: Long,
        @Query("followedId") followedId: Long
    ): Call<Void>
}