package com.example.nambukhwangdan.model.Letter

data class Letter(
    val id: String = "",
    val content: String = "",
    val createdAt: Long = 0L,
    val nickname: String = "",
    val replyToId: String? = null,
    val liked: Boolean = false,
    val date: Long = 0L,
    val receiverName: String = "",
    val userId:String=""
)