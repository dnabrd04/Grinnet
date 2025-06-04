package com.example.grinnet.data

data class PostDTORequest(val idPost: Long, val firebaseUserId: String)

data class PostListRequest(val idUser: Long, val firebaseUserId: String)