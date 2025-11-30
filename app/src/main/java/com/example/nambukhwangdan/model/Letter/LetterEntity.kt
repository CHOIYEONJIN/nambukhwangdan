package com.example.nambukhwangdan.model.Letter

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "letters")
data class LetterEntity(
    @PrimaryKey val id: String,
    val content: String,
    val createdAt: Long,
    val nickname: String,
    val replyToId: String?,
    val liked: Boolean = false,
    val date: Long,
    val receiverName: String,         // 미래의 나 or 랜덤 인물
    val userId:String
    )