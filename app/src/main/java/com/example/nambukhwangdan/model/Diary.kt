package com.example.nambukhwangdan.model

import android.provider.ContactsContract

data class Diary(
    val id: Int,       // Firestore 문서 ID
    val content: String = "",
    val emotion: String = "",  // 감정 결과
    val date: Long = System.currentTimeMillis(),
    val sendToFuture: Boolean = false, // 미래에게 보내는 메시지 여부
    val sendTime: Long? = null, // 예약 알림 시간
    val liked : Boolean = false, // 편지 즐겨찾기 여부
    val nickname: String =""
)