package com.example.nambukhwangdan.model.Letter

data class Letter(
    val id: String = "", //편지의 id
    val content: String = "", // 편지 내용
    val createdAt: Long = 0L, // 편지의 생성 날짜
    val nickname: String = "", // 편지를 보낸 사람의 닉네임
    val replyToId: String? = null, //답장할 편지의 Id
    val liked: Boolean = false, // 편지 좋아요 기능
    val date: Long = 0L, //사용자가 선택한 편지 도착 날짜
    val userId:String="", //편지를 보낸 사람의 id
    val isReplied: Boolean = false,
    val writerId: String = "", // ✍️ 작성자 (추가)
)