package com.example.grinnet.data

import java.io.Serializable

/**
 * Model used to create users through the api.
 */
data class UserRequest(
    val idUser: Long? = null,
    val image: String,
    val username: String,
    val privacity: String,
    val firebaseId: String,
    val tokenPush: String,
    val name: String,
    val description: String,
): Serializable

/**
 * Model used to get users through the api.
 */
data class UserResponse(
    val idUser: Long? = null,
    val image: String,
    val username: String,
    val privacity: String,
    val firebaseId: String,
    val tokenPush: String,
    val name: String,
    val description: String,
//    val comments: String,
//    val posts: String,
//    val likes: String,
)

data class UserEmpty(
    val idUser: Long
)
