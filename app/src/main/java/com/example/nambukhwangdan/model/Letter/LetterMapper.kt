package com.example.nambukhwangdan.model.Letter

// DiaryMapper.kt 방식 참고: :contentReference[oaicite:0]{index=0}

fun Letter.toEntity(): LetterEntity {
    return LetterEntity(
        id = this.id,
        content = this.content,
        createdAt = this.createdAt,
        nickname = this.nickname,
        replyToId = this.replyToId,
        liked = this.liked,
        date=this.date,
        userId=this.userId,
        isReplied = this.isReplied
    )
}

fun LetterEntity.toLetter(): Letter {
    return Letter(
        id = this.id,
        content = this.content,
        createdAt = this.createdAt,
        nickname = this.nickname,
        replyToId = this.replyToId,
        liked = this.liked,
        date=this.date,
        userId = this.userId,
        isReplied = this.isReplied

    )
}
