package com.example.grinnet.data

/**
 * Model used to create users through the api.
 */
data class UserRequest(
    val idUser: Long? = null,
    val image: String,
    val username: String,
    val privacity: String,
    val firebaseId: String,
    val name: String,
    val description: String,
)

/**
 * Model used to get users through the api.
 */
data class UserResponse(
    val idUser: Long? = null,
    val image: String,
    val username: String,
    val privacity: String,
    val firebaseId: String,
    val name: String,
    val description: String,
//    val comments: String,
//    val posts: String,
//    val likes: String,
)

data class UserEmpty(
    val idUser: Long
)
