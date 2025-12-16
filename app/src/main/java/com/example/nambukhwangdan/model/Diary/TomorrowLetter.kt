package com.example.nambukhwangdan.model.TomorrowLetter

import com.google.firebase.firestore.PropertyName
import java.time.ZoneId
import java.util.UUID
import java.time.Instant
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

    @PropertyName("is_arrived")
    var isArrived: Boolean = false,

    @PropertyName("is_replied")
    var isReplied: Boolean = false

) {
    constructor() : this(
        id = UUID.randomUUID().toString(),
        content = "",
        deliveryTimestamp = 0L,
        createdAt = System.currentTimeMillis(),
        userId = "",
        isArrived = false,
        isReplied = false
    )
    companion object {
        const val FIRST_DIARY_DUMMY_ID = "DUMMY_FIRST_DIARY"

        fun createDummy(userId: String): TomorrowLetter {
            val nowKST = Instant.now()
                .atZone(ZoneId.of("Asia/Seoul"))
                .toInstant()
                .toEpochMilli()

            return TomorrowLetter(
                id = FIRST_DIARY_DUMMY_ID,
                content = "아직 미래의 나에게서 온 편지가 없어요!\n첫 일기를 작성해 미래의 나에게 메시지를 남겨보세요.",
                deliveryTimestamp = nowKST, // 현재 시간으로 설정하여 즉시 도착한 것으로 간주
                createdAt = nowKST,
                userId = userId,
                isArrived = true,
                isReplied = false
            )
        }
    }
}
