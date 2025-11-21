package com.example.nambukhwangdan.model

fun Diary.toEntity(): DiaryEntity {
    return DiaryEntity(
        id = this.id,
        photoUrls = this.photoUrls,
        content = this.content,
        analyzedAt = this.analyzedAt,
        emotion = this.emotion,
        sticker = this.sticker,
        date = this.date,
        sendToFuture = this.sendToFuture,
        replyToId = this.replyToId,
        createdAt = this.createdAt,
        sendTime = this.sendTime,
        liked = this.liked,
        nickname = this.nickname
    )
}