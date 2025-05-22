package com.example.grinnet.data

data class ResourceRequest(
    val idResource: Long? = null,
    val url: String,
    val postRelated: PostRelated?,
    val order: Int
)

data class ResourceResponse(
    val idResource: Long,
    val url: String,
    val postRelated: PostRelated,
    val order: Int
)