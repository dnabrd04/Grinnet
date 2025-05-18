package com.example.grinnet.data

import java.util.Date

data class PostRequest(
    val idPost: Long? = null,
    val user: UserRequest,
    val post: PostRelated?,
    val privacity: String,
    val text: String,
    val creation_date: String
)

data class PostResponse(
    val idPost: Long,
    val user: UserRequest,
    val post: PostRelated?,
    val privacity: String,
    val text: String,
    val creationDate: String,
    val likeCount: Long,
    val commentCount: Long,
    val liked: Boolean
)

data class PostRelated(
    val idPost: Long?
)