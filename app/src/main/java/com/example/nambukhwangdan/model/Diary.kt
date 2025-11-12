package com.example.nambukhwangdan.model

data class Diary(
    val id: String,       // Firestore 문서 ID
    val photoUrls: List<String> = emptyList(),
    val content: String = "", // 일기 내용
    val analyzedAt: Long? = null, //감정 분석 완료 시점
    val emotion: String = "",  // 감정 결과 (긍/부/중립)
    val sticker: String? = null, // 사용자가 선택한 세부 스티커명
    val date: Long = System.currentTimeMillis(), // 사용자가 선택한 시간
    val sendToFuture: Boolean = false, // 미래에게 보내는 메시지 여부
    val replyToId: String? = null, // 어떤 과거의 편지에 답장하는지 알기 위한 변수
    val createdAt: Long = System.currentTimeMillis(), // 일기 및 편지 작성시간 (Firebase 정렬 시 필요)
    val sendTime: Long? = null, // 예약 알림 시간
    val liked : Boolean = false, // 편지 즐겨찾기 여부
    val nickname: String ="나" // 작성자 닉네임
)