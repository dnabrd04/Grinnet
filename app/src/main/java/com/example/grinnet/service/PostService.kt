package com.example.grinnet.service

import com.example.grinnet.data.PostDTORequest
import com.example.grinnet.data.PostListRequest
import com.example.grinnet.data.PostRequest
import com.example.grinnet.data.PostResponse
import com.example.grinnet.data.UserIdRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface PostService {

    @POST("/post")
    fun createPost(@Body post: PostRequest): Call<PostResponse>

    @POST("/post/one-with-likes")
    fun getPost(@Body post: PostDTORequest): Call<PostResponse>

    @POST("/post/with-likes")
    fun getPosts(@Body request: UserIdRequest): Call<MutableList<PostResponse>>

    @POST("/posts-followed")
    fun getPostsFollowed(@Body request: PostDTORequest): Call<MutableList<PostResponse>>

    @POST("/post/user-list-with-likes")
    fun getPostList(@Body request: PostListRequest): Call<MutableList<PostResponse>>
}