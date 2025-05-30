package com.example.grinnet.data

import java.util.Date

data class FollowRequest(
    val idFollow: Long?,
    val follower: UserEmpty,
    val followed: UserRequest,
    val follow_date: Date
)