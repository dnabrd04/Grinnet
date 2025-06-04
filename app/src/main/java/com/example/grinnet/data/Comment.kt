package com.example.grinnet.data

import java.util.Date

data class CommentRequest(
    val user: UserEmpty,
    val post: PostResponse,
    val text: String,
    val creation_date: String
)

data class CommentResponse(
    val user: UserResponse,
    val post: PostResponse,
    val text: String,
    val creation_date: String
)