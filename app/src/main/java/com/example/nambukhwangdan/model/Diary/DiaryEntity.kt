package com.example.nambukhwangdan.model.Diary

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_table")
data class DiaryEntity(
    @PrimaryKey val id: String,               // Firestore ID 그대로 사용
    val content: String,
    val analyzedAt: Long?,
    val emotion: String,
    val sticker: String?,
    val date: Long,
    val replyToId: String?,
    val createdAt: Long,
    val liked: Boolean,
    val nickname: String,
    val userId:String
)