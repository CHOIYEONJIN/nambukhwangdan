package com.example.nambukhwangdan.model.Diary

data class Diary(
    val id: String="",
    val content: String="",
    val emotion: String = "",          // 감정 분석 결과
    val sticker: String? = null,       // 선택 스티커
    val analyzedAt: Long? = null,      // 분석 완료 시점
    val replyToId: String? = null,     // 과거 나에게 답장 기능
    val date: Long = System.currentTimeMillis(), //사용자가 일기 작성 일자로 선택한 날짜
    val createdAt: Long = System.currentTimeMillis(),
    val liked: Boolean = false,        // 북마크 기능
    val photoUrls: List<String> = emptyList(),
    val nickname: String = "나",
    val userId:String=""
)