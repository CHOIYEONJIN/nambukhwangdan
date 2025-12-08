package com.example.nambukhwangdan.model.TomorrowLetter

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tomorrow_letters")
data class TomorrowLetterEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val content: String,

    val deliveryTimestamp: Long,

    val createdAt: Long,

    // 🚨 KSP 오류 해결: 'userId'가 DB 컬럼 목록에 없다는 오류를 해결합니다.
    // DB의 SnakeCase 이름인 'user_id'로 매핑합니다.
    @ColumnInfo(name = "user_id")
    val userId: String,

    val isDelivered: Boolean = false
)