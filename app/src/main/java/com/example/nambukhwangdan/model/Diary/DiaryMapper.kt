package com.example.nambukhwangdan.model.Diary

fun Diary.toEntity(): DiaryEntity {
    return DiaryEntity(
        id = this.id,
        content = this.content,
        analyzedAt = this.analyzedAt,
        emotion = this.emotion,
        sticker = this.sticker,
        date = this.date,
        replyToId = this.replyToId,
        createdAt = this.createdAt,
        liked = this.liked,
        userId = this.userId,
        updatedAt = this.updatedAt,
        sentimentLabel = this.sentimentLabel,
        sentimentScore = this.sentimentScore
    )
}

fun DiaryEntity.toDiary(): Diary {
    return Diary(
        id = this.id,
        content = this.content,
        analyzedAt = this.analyzedAt,
        emotion = this.emotion,
        sticker = this.sticker,
        date = this.date,
        replyToId = this.replyToId,
        createdAt = this.createdAt,
        liked = this.liked,
        userId = this.userId,
        updatedAt = this.updatedAt,
        sentimentLabel = this.sentimentLabel,
        sentimentScore = this.sentimentScore
    )
}
