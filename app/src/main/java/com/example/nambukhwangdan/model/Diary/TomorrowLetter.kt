package com.example.nambukhwangdan.model.TomorrowLetter

import com.google.firebase.firestore.PropertyName
import java.util.UUID

/**
 * 미래의 나에게 보내는 편지 데이터 모델 (Firestore 최적화 버전)
 * ⭐️ 모든 필드를 var로 변경하고 빈 생성자를 추가하여 직렬화 안정성 증대
 */
data class TomorrowLetter(
    var id: String = UUID.randomUUID().toString(),

    var content: String = "",

    @PropertyName("delivery_timestamp")
    var deliveryTimestamp: Long = 0L,

    @PropertyName("created_at")
    var createdAt: Long = System.currentTimeMillis(),

    @PropertyName("user_id")
    var userId: String = "",

    @PropertyName("is_delivered")
    var isDelivered: Boolean = false
) {
    constructor() : this(
        id = UUID.randomUUID().toString(),
        content = "",
        deliveryTimestamp = 0L,
        createdAt = System.currentTimeMillis(),
        userId = "",
        isDelivered = false
    )
}