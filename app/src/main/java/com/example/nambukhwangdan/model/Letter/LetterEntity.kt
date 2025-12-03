package com.example.nambukhwangdan.model.Letter

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "letters")
data class LetterEntity(
    @PrimaryKey val id: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val scheduledAt: Long,
    val deliveredAt: Long?,
    val senderId: String,
    val senderName: String,
    val receiverId: String?,
    val receiverName: String?,
    val anonymous: Boolean,
    val replyToId: String?,
    val liked: Boolean,
    val inboxPath: String?
)
