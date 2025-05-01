package com.example.grinnet.data

import java.util.Date

data class PostRequest(
    val id_post: Long? = null,
    val user: UserRequest,
    val post: Long?,
    val privacity: String,
    val text: String,
    val creation_date: String
)

data class PostResponse(
    val id_post: Long,
    val postRelated: PostResponse?,
    val user: UserRequest,
    val post: Long?,
    val privacity: String,
    val text: String,
    val creationDate: String,
    val likeCount: Long,
    val commentCount: Long
)
