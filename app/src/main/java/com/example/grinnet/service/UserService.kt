package com.example.grinnet.service

import com.example.grinnet.data.UserRequest
import com.example.grinnet.data.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserService {
    @POST("/user")
    fun createUser(@Body user: UserRequest): Call<UserRequest>

    @GET("/user/firebase/{firebaseId}")
    fun getUserByFirebaseId(@Path("firebaseId") id: String): Call<UserResponse>

    @GET("/user/{id}")
    fun getUser(@Path("id") id: Long): Call<UserResponse>

    @GET("/user/username/{username}")
    fun existsUsername(@Path("username") username: String): Call<Boolean>

    @PUT("/user/token/{firebaseId}")
    fun updateToken(@Path("firebaseId") firebaseId: String, @Body token: String): Call<UserResponse>
}