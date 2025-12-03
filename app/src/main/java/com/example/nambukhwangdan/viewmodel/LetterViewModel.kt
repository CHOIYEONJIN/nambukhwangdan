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
    //닉네임 저장 변수 선언
    private var userNickname:String="닉네임"
    //FireStore에 userNickname을 저장하기 위한 함수
    fun setUserNickname(nickname: String) {
        //userNickname= 실제 사용자 닉네임 , _nicknameToUse = 익명 / 실제 사용자 닉네임
        userNickname=nickname
        _nicknameToUse.value = nickname
    }

    //편지 내용 저장 변수
    val letterContent = MutableStateFlow("")


    //익명 설정 여부
    private val _isAnonymous = MutableStateFlow(false)
    val isAnonymous = _isAnonymous.asStateFlow()


    //_가 있는건 읽기, 쓰기 가능 / 없는건 읽기만 가능
    private val _nicknameToUse = MutableStateFlow(userNickname)
    val nicknameToUse = _nicknameToUse.asStateFlow()
    //익명 체크를 통해 익명이 됨 -> newValue가 TRUE됨
    // 익명이라면 nicknameToUse에 "익명"을 저장, 익명 체크 아니라면 실제 닉네임을 저장
    fun onAnonymousCheckedChange(newValue: Boolean) {
        _isAnonymous.value = newValue
        _nicknameToUse.value = if (newValue) "익명" else userNickname
    }
    //편지 답장 id
    val replyToId = MutableStateFlow<String?>(null)
    // 지금 쓰려는 편지가 어떤 편지에 대한 답장인지를 나타냄
    //답장하기 눌렀을 때 읽은 편지의 값을 매개변수로 줘서 내가 작성하는 편지의 replyId로 넣는다
    fun setReplyToId(id: String?) {
        replyToId.value = id
    }

    //편지 내용 업데이트
    fun updateContent(text: String) {
        letterContent.value = text
    }

    //편지를 받는 사람의 이름(미래의 나 / 익명의 누군가)
    val receiverName = MutableStateFlow("미래의 나") // 기본값은 미래의 나
    //편지 받을 사람 업데이트 (미래의 나 / 익명의 누군가)
    fun setReceiver(name: String) {
        receiverName.value = name
    }

    // FireStore, RoomDB에 저장
    fun persistLetter(): String {
        //letter 고유 id 생성
        val letterId = UUID.randomUUID().toString()
        //현재 시각 (편지 생성 시각) 저장
        val now = System.currentTimeMillis()
        //letter DATA 객체 생성
        val letter = Letter(
            id = letterId,
            content = letterContent.value,
            nickname = receiverName.value,
            replyToId = replyToId.value,
            createdAt = now,
            date = selectedDateMillis.value,
            userId = userId ?: ""
        )

        viewModelScope.launch {
            repo.insertLetter(letter)// ← 🔥 DB(Room)에 저장
            val uid = userId
            if (uid != null) {
                repo.saveLetterToFirestore(letter,uid)    }      // ← 🔥 Firestore에도 저장
        }
        //이전에 작성한 편지 내용이 남아있지 않도록 ViewModel의 상태를 초기화한다
        clearStates()
        //새로 생성한 편지의 id 반환
        return letterId
    }

    //viewModel 초기화 함수
    private fun clearStates() {
        letterContent.value = ""
        receiverName.value = "미래의 나"
        replyToId.value = null
    }

    //사용자가 달력에서 선택한 날짜의 시간
    val selectedDateMillis = MutableStateFlow(System.currentTimeMillis())
   //닐짜 선택 ui에서 받은 선택된 날짜의 millis 값을 달력에서 선택한 날짜를 저장하는 변수의 값으로 업데이트
   fun setSelectedDate(millis: Long) { selectedDateMillis.value = millis }
    val allLetters = repo.getAllLetters()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList<Letter>()
        )

    //편지 삭제
    fun deleteLetter(id: String) = viewModelScope.launch {
        repo.deleteLetter(id)//room에서 삭제
        val uid = userId
        if (uid != null) {
        repo.deleteLetterFromFirestore(id,uid)//fireStore에서 삭제
        }

    }

    //편지에 좋아요 누르는 기능
    fun toggleLike(id: String) = viewModelScope.launch {
        repo.toggleLike(id)
    }

    //auth에서 로그인된 아이디를 받아오는 함수
    private val auth = FirebaseAuth.getInstance()
    private val userId: String?
        get() = auth.currentUser?.uid

    //room과 fireStore에 저장된 data를 동기화하는 함수
    fun syncLettersFromFirestore() {
        val uid = userId
        if (uid != null) {
        firestore.collection("users")
            .document(uid)
            .collection("letters")
            .addSnapshotListener { snapshot, _ ->
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

    //랜덤 유저에게 편지를 전송하는 시스템
    fun sendRandomLetter(content: String) {
        val uid = userId
        if (uid != null) {
            Log.d("RandomTest", "🔥 sendRandomLetter 호출됨 / senderId=${userId}")

            viewModelScope.launch {
            try {
                repo.sendRandomLetter(
                    senderId = uid,
                    senderNickname = nicknameToUse.value,
                    content = content
                )
            } catch (e: Exception) {
                Log.e("LetterViewModel", "Random letter error", e)
            }
        }}
    }



}