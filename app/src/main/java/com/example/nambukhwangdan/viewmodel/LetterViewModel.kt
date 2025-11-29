package com.example.nambukhwangdan.viewmodel

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

    // --- UI 입력 상태 ---
    val letterContent = MutableStateFlow("")
    val receiverName = MutableStateFlow("미래의 나")
    val replyToDiaryId = MutableStateFlow<String?>(null)

    private val _isAnonymous = MutableStateFlow(false)
    val isAnonymous = _isAnonymous.asStateFlow()

    private val userNickname = "닉네임" // 실제 로그인 정보에서 가져올 예정 (임시)
    private val _nicknameToUse = MutableStateFlow(userNickname)
    val nicknameToUse = _nicknameToUse.asStateFlow()

    fun onAnonymousCheckedChange(newValue: Boolean) {
        _isAnonymous.value = newValue
        _nicknameToUse.value = if (newValue) "익명" else userNickname
    }
    val replyToId = MutableStateFlow<String?>(null)

    fun setReplyToId(id: String) {
        replyToId.value = id
    }
    // 받는 사람 이름 상태

    fun updateContent(text: String) {
        letterContent.value = text
    }

    fun setReceiver(name: String) {
        receiverName.value = name
    }

    fun setReplyToDiaryId(id: String?) {
        replyToDiaryId.value = id
    }

    // --- Firestore 저장 ---
    fun persistLetter(): String {
        val letterId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val letter = Letter(
            id = letterId,
            content = letterContent.value,
            receiverName = receiverName.value,
            replyToId = replyToDiaryId.value,
            createdAt = now,
            date = selectedDateMillis.value,
            userId = userId ?: ""
        )

        viewModelScope.launch {
            repo.insertLetter(letter)
            val uid = userId          // 🔥 여기서 다시 한 번 안전 체크
            if (uid != null) {// ← 🔥 DB(Room)에 저장
            repo.saveLetterToFirestore(letter,uid)    }      // ← 🔥 Firestore에도 저장
        }

        clearStates()
        return letterId
    }

    private fun clearStates() {
        letterContent.value = ""
        receiverName.value = "미래의 나"
        replyToDiaryId.value = null
    }
    val selectedDateMillis = MutableStateFlow(System.currentTimeMillis())
    fun setSelectedDate(millis: Long) { selectedDateMillis.value = millis }
    val allLetters = repo.getAllLetters()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList<Letter>()
        )

    fun deleteLetter(id: String) = viewModelScope.launch {
        repo.deleteLetter(id)
        val uid = userId          // 🔥 여기서 다시 한 번 안전 체크
        if (uid != null) {
        repo.deleteLetterFromFirestore(id,uid)}

    }

    fun toggleLike(id: String) = viewModelScope.launch {
        repo.toggleLike(id)
    }
    private val auth = FirebaseAuth.getInstance()
    private val userId: String?
        get() = auth.currentUser?.uid



    fun syncLettersFromFirestore() {
        val uid = userId          // 🔥 여기서 다시 한 번 안전 체크
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


}