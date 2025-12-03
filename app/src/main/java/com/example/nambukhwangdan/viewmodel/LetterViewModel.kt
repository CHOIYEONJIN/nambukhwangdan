package com.example.nambukhwangdan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nambukhwangdan.data.repository.LetterRepository
import com.example.nambukhwangdan.model.Letter.Letter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LetterViewModel @Inject constructor(
    private val repo: LetterRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    // 실제 사용자 닉네임 저장 변수 (익명/실명 선택의 기준)
    private var userNickname:String="닉네임"

    // FireStore에 userNickname을 저장하기 위한 함수
    fun setUserNickname(nickname: String) {
        userNickname=nickname
        _nicknameToUse.value = nickname
    }

    // 편지 내용 저장 변수
    val letterContent = MutableStateFlow("")

    // 익명 설정 여부
    private val _isAnonymous = MutableStateFlow(false)
    val isAnonymous = _isAnonymous.asStateFlow()

    // 편지 작성 시 사용할 닉네임 (실제 사용자 닉네임 또는 "익명")
    private val _nicknameToUse = MutableStateFlow(userNickname)
    val nicknameToUse = _nicknameToUse.asStateFlow()

    // ⭐️ 수신인 이름 상태 추가
    private val _receiverName = MutableStateFlow("미래의 나") // 기본값 설정
    val receiverName = _receiverName.asStateFlow()

    // ⭐️ 수신인 이름 설정 함수 추가
    fun setReceiver(name: String) {
        _receiverName.value = name
    }

    // 익명 체크를 통해 닉네임 설정 (익명이라면 "익명", 아니라면 실제 닉네임을 사용)
    fun onAnonymousCheckedChange(newValue: Boolean) {
        _isAnonymous.value = newValue
        _nicknameToUse.value = if (newValue) "익명" else userNickname
    }

    // 답장 대상 편지의 ID (이 필드의 존재 여부로 답장인지 새 편지인지 구분)
    val replyToId = MutableStateFlow<String?>(null)

    // 답장하기 눌렀을 때 읽은 편지의 값을 매개변수로 줘서 내가 작성하는 편지의 replyId로 넣는다
    fun setReplyToId(id: String?) {
        replyToId.value = id
    }

    // 편지 내용 업데이트
    fun updateContent(text: String) {
        letterContent.value = text
    }

    // FireStore, RoomDB에 저장
    fun persistLetter(): String {
        // letter 고유 id 생성
        val letterId = UUID.randomUUID().toString()
        // 현재 시각 (편지 생성 시각) 저장
        val now = System.currentTimeMillis()

        // ⭐️ 수정: Letter.nickname은 작성자(Sender)의 닉네임(_nicknameToUse.value)을 사용합니다.
        val letter = Letter(
            id = letterId,
            content = letterContent.value,
            nickname = nicknameToUse.value, // ⭐️ 작성자(Sender) 닉네임 사용
            replyToId = replyToId.value,
            createdAt = now,
            date = selectedDateMillis.value,
            userId = userId ?: ""
        )

        viewModelScope.launch {
            repo.insertLetter(letter) // ← 🔥 DB(Room)에 저장
            val uid = userId
            if (uid != null) {
                repo.saveLetterToFirestore(letter,uid) // ← 🔥 Firestore에도 저장
            }
        }
        // 이전에 작성한 편지 내용이 남아있지 않도록 ViewModel의 상태를 초기화한다
        clearStates()
        // 새로 생성한 편지의 id 반환
        return letterId
    }

    // viewModel 초기화 함수
    private fun clearStates() {
        letterContent.value = ""
        // 이전에 사용된 receiverName 관련 초기화는 제거됨
        replyToId.value = null
        _receiverName.value = "미래의 나" // ⭐️ 수신인 상태 초기화 추가
    }

    // 사용자가 달력에서 선택한 날짜의 시간
    val selectedDateMillis = MutableStateFlow(System.currentTimeMillis())
    // 닐짜 선택 ui에서 받은 선택된 날짜의 millis 값을 달력에서 선택한 날짜를 저장하는 변수의 값으로 업데이트
    fun setSelectedDate(millis: Long) { selectedDateMillis.value = millis }

    // 모든 편지 목록 (Room/Firestore 동기화)
    val allLetters = repo.getAllLetters()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList<Letter>()
        )

    // 편지 삭제
    fun deleteLetter(id: String) = viewModelScope.launch {
        repo.deleteLetter(id)// room에서 삭제
        val uid = userId
        if (uid != null) {
            repo.deleteLetterFromFirestore(id,uid)// fireStore에서 삭제
        }
    }

    // 편지에 좋아요 누르는 기능
    fun toggleLike(id: String) = viewModelScope.launch {
        repo.toggleLike(id)
    }

    // auth에서 로그인된 아이디를 받아오는 함수
    private val auth = FirebaseAuth.getInstance()
    private val userId: String?
        get() = auth.currentUser?.uid

    companion object {
        private const val TAG = "LetterViewModel"
    }

    // room과 fireStore에 저장된 data를 동기화하는 함수
    fun syncLettersFromFirestore() {
        val uid = userId
        if (uid != null) {
            firestore.collection("users")
                .document(uid)
                .collection("letters")
                .addSnapshotListener { snapshot, error -> // ⭐️ 에러 처리 추가
                    if (error != null) {
                        Log.e(TAG, "Firestore sync error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        viewModelScope.launch {
                            for (doc in snapshot.documents) {
                                val letter = doc.toObject(Letter::class.java)
                                if (letter != null) repo.insertLetter(letter)
                            }
                        }
                    }
                }
        }
    }

    // 랜덤 유저에게 편지를 전송하는 시스템
    fun sendRandomLetter(content: String) {
        val uid = userId
        if (uid != null) {
            Log.d(TAG, "🔥 sendRandomLetter 호출됨 / senderId=${userId}")

            viewModelScope.launch {
                try {
                    repo.sendRandomLetter(
                        senderId = uid,
                        senderNickname = nicknameToUse.value,
                        content = content
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Random letter error", e)
                }
            }}
    }
}