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
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject
import java.util.Date
import kotlinx.coroutines.flow.map

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

    // FireStore, RoomDB에 저장 및 알람 예약
    fun persistLetter(): String {
        // letter 고유 id 생성
        val letterId = UUID.randomUUID().toString()
        // 현재 시각 (편지 생성 시각) 저장
        val now = System.currentTimeMillis()

        // ⭐️ Letter.date는 이미 setSelectedDate에서 정확히 계산된 selectedDateMillis.value를 사용
        val letter = Letter(
            id = letterId,
            content = letterContent.value,
            nickname = nicknameToUse.value, // ⭐️ 작성자(Sender) 닉네임 사용
            replyToId = replyToId.value,
            createdAt = now,
            date = selectedDateMillis.value, // ⭐️ 정확히 계산된 타임스탬프 사용
            userId = userId ?: ""
        )

        viewModelScope.launch {
            val uid = userId
            if (uid != null) {
                // repo.saveAndScheduleLetter 함수로 통일하여 DB, Firestore 저장 및 알람 예약을 한번에 처리
                // 🚨 이 함수가 존재하지 않다면, repo.insertLetter(letter)와 repo.saveLetterToFirestore(letter, uid)를 호출하고, 알람 예약 로직을 직접 호출해야 합니다.
                repo.saveAndScheduleLetter(letter, uid)
            } else {
                Log.e(TAG, "User ID가 null이라 Firestore 저장 및 알람 예약을 건너뜁니다. 로컬 DB에만 저장됩니다.")
                repo.insertLetter(letter) // 로컬 DB에만 저장 (알람은 예약되지 않음)
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
        replyToId.value = null
        _receiverName.value = "미래의 나" // ⭐️ 수신인 상태 초기화 추가
    }

    // 사용자가 달력에서 선택한 날짜의 시간
    val selectedDateMillis = MutableStateFlow(System.currentTimeMillis())

    // ⭐️ [최종 수정 로직] 날짜 선택 시 KST 23:00를 기준으로 타임스탬프 계산
    fun setSelectedDate(dateMillis: Long) {
        val zoneIdKST = ZoneId.of("Asia/Seoul")

        // 1. DatePicker의 밀리초(UTC 00:00)를 KST 기준으로 LocalDateTime 추출
        // 이 시점에서 localDateTimeKST는 '선택된 날짜'와 '09:00:00'을 가집니다.
        val localDateTimeKST = Instant.ofEpochMilli(dateMillis)
            .atZone(zoneIdKST)
            .toLocalDateTime()

        // 2. 추출된 날짜 구성요소 (연,월,일)를 유지하고, 시간을 23:00로 재구성
        // 이 시점에서 시간은 09:00에서 23:00로 바뀝니다.
        val deliveryDateTimeKST = localDateTimeKST
            .withHour(23)
            .withMinute(0)
            .withSecond(0)

        // 3. 재구성된 KST LocalDateTime에 KST 시간대를 붙여 ZonedDateTime 생성
        val zonedDateTimeKST = deliveryDateTimeKST.atZone(zoneIdKST)

        // 4. 최종 KST 23:00 타임스탬프 계산
        val finalTimestamp = zonedDateTimeKST.toInstant().toEpochMilli()

        // Flow에 최종 Long 값 저장
        selectedDateMillis.value = finalTimestamp

        Log.d(TAG, "DatePicker Input (Raw): ${Date(dateMillis)}")
        Log.d(TAG, "Final KST 23:00 Timestamp: ${Date(finalTimestamp)}")
    }

    // 모든 편지 목록 (Room/Firestore 동기화)
    val allLetters = repo.getAllLetters()
        .map { letters ->
            val now = System.currentTimeMillis()
            // ⭐️ 핵심 필터링 로직: 도착 시간(letter.date)이 현재 시간(now)보다 작거나 같은 편지만 보여줍니다.
            letters.filter { letter ->
                letter.date <= now
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList<Letter>()
        )

    // 편지 삭제
    fun deleteLetter(id: String) = viewModelScope.launch {
        val uid = userId
        try {
            // Firestore 연동이 가능한 경우: 로컬 삭제 + 알람 취소 + Firestore 삭제를 한 번에 처리
            // (중복으로 Room을 지우면 Home 화면으로 돌아갈 때 이미 삭제된 레코드를 다시 찾으면서 예외가 발생할 수 있음)
            if (uid != null) {
                val success = repo.deleteLetterFully(id, uid)
                if (!success) {
                    Log.e(TAG, "Firebase 삭제 실패 – Home 화면 재진입 시 동기화 오류가 발생할 수 있습니다")
                }
            } else {
                // 로그인 정보가 없을 때는 로컬만 삭제
                repo.deleteLetter(id)
                repo.cancelScheduledLetter(id)
                Log.e(TAG, "사용자 ID(uid)가 null이라 Firebase 삭제는 건너뜁니다.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "InboxScreen 삭제 처리 중 예외 발생 – Home 화면 크래시 방지를 위해 무시", e)
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
                .addSnapshotListener { snapshot, error ->
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