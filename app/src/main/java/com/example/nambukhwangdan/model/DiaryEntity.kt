
package com.example.nambukhwangdan.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "diary_table")
data class DiaryEntity(
    @PrimaryKey val id: String,               // Firestore ID 그대로 사용
    val photoUrls: List<String>,             // TypeConverter 필요
    val content: String,
    val analyzedAt: Long?,
    val emotion: String,
    val sticker: String?,
    val date: Long,
    val sendToFuture: Boolean,
    val replyToId: String?,
    val createdAt: Long,
    val sendTime: Long?,
    val nickname: String,
    @ColumnInfo(name = "liked") val liked: Boolean = false
)

