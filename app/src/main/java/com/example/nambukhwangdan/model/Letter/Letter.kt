package com.example.nambukhwangdan.model.Letter

/**
 * Represents a letter that can be stored locally (Room) and remotely (Firestore).
 * - [scheduledAt]: delivery schedule timestamp
 * - [deliveredAt]: when the letter reached the receiver's inbox
 * - [anonymous]: whether the sender should be hidden from the receiver
 */
data class Letter(
    val id: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt,
    val scheduledAt: Long = createdAt,
    val deliveredAt: Long? = null,
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String? = null,
    val receiverName: String? = null,
    val anonymous: Boolean = false,
    val replyToId: String? = null,
    val liked: Boolean = false,
    val inboxPath: String? = null
)
