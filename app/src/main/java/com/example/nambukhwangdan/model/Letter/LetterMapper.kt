package com.example.nambukhwangdan.model.Letter

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue

fun Letter.toEntity(): LetterEntity =
    LetterEntity(
        id = id,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt,
        scheduledAt = scheduledAt,
        deliveredAt = deliveredAt,
        senderId = senderId,
        senderName = senderName,
        receiverId = receiverId,
        receiverName = receiverName,
        anonymous = anonymous,
        replyToId = replyToId,
        liked = liked,
        inboxPath = inboxPath
    )

fun LetterEntity.toLetter(): Letter =
    Letter(
        id = id,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt,
        scheduledAt = scheduledAt,
        deliveredAt = deliveredAt,
        senderId = senderId,
        senderName = senderName,
        receiverId = receiverId,
        receiverName = receiverName,
        anonymous = anonymous,
        replyToId = replyToId,
        liked = liked,
        inboxPath = inboxPath
    )

fun Letter.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "content" to content,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt,
    "scheduledAt" to scheduledAt,
    "deliveredAt" to deliveredAt,
    "senderId" to senderId,
    "senderName" to senderName,
    "receiverId" to receiverId,
    "receiverName" to receiverName,
    "anonymous" to anonymous,
    "replyToId" to replyToId,
    "liked" to liked,
    "inboxPath" to inboxPath,
    "serverTimestamp" to FieldValue.serverTimestamp()
)

fun DocumentSnapshot.toLetterSafe(): Letter? = try {
    Letter(
        id = getString("id") ?: id,
        content = getString("content") ?: "",
        createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
        updatedAt = getLong("updatedAt") ?: (getLong("createdAt") ?: System.currentTimeMillis()),
        scheduledAt = getLong("scheduledAt") ?: getLong("createdAt") ?: System.currentTimeMillis(),
        deliveredAt = getLong("deliveredAt"),
        senderId = getString("senderId") ?: "",
        senderName = getString("senderName") ?: "",
        receiverId = getString("receiverId"),
        receiverName = getString("receiverName"),
        anonymous = getBoolean("anonymous") ?: false,
        replyToId = getString("replyToId"),
        liked = getBoolean("liked") ?: false,
        inboxPath = getString("inboxPath")
    )
} catch (_: Exception) {
    null
}
