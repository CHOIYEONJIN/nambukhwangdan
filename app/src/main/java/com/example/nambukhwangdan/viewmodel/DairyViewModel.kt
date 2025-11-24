package com.example.nambukhwangdan.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.nambukhwangdan.data.repository.DiaryRepository
import com.example.nambukhwangdan.model.Diary
import com.example.nambukhwangdan.model.toDiary
import com.example.nambukhwangdan.model.toEntity
import com.example.nambukhwangdan.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val repo: DiaryRepository
) : ViewModel() {
    var isAnalyzing by mutableStateOf(false)
        private set
    // 설계서: "과거의 내 편지 표시":contentReference[oaicite:2]{index=2}
    private val _pastLetters = MutableStateFlow(
        listOf(
            Diary(
                id = UUID.randomUUID().toString(),
                content = "과거에 작성한 일기 내용이 여기에 뜨게 됩니다",
                sendToFuture = true
            )
        )
    )
    val pastLetters = _pastLetters.asStateFlow()

    // 오늘 작성 중인 일기
    val todayDiary = MutableStateFlow("")
    // 선택한 날짜(초기값: 오늘)
    val selectedDateMillis = MutableStateFlow(System.currentTimeMillis())

    // 감정 분석 결과(설계서: 자동 추천 표시):contentReference[oaicite:3]{index=3}
    val detectedEmotion = MutableStateFlow<String?>(null)
    val selectedEmotion = MutableStateFlow<String?>(null)
    val selectedSticker = MutableStateFlow<String?>(null)

    private val defaultReceiver = "누군가"
    val receiverName = MutableStateFlow(defaultReceiver)


    fun updateDiary(text: String) { todayDiary.value = text }

    fun setSelectedDate(millis: Long) { selectedDateMillis.value = millis }

    val selectedPhotos = MutableStateFlow<List<Uri>>(emptyList())

    fun setSelectedUris(uris: List<Uri>) {
        selectedPhotos.value = uris
    }

    fun runAnalyze(bottomNavController: NavController) {
        // 실제에선 API 호출; 지금은 로딩 시뮬레이션
        viewModelScope.launch {
            delay(1200)
            detectedEmotion.value = "긍정" // 예시 자동 추천
            isAnalyzing = false
            bottomNavController.navigate(Routes.AnalyzeResult)
        }
    }
    fun loadReplyLetterById(_id: String) { // 아직 미구현!!!!!!
        // 나중에 repo에서 가져와서 세팅
        //_replyLetter.value = repository.getById(id)
    }

    fun chooseEmotion(value: String) { selectedEmotion.value = value }

    fun chooseSticker(value: String) { selectedSticker.value = value }

    fun clearForNewEntry() {
        todayDiary.value = ""
        selectedPhotos.value = emptyList()
        detectedEmotion.value = null
        selectedEmotion.value = null
        selectedSticker.value = null
        replyToId.value = null
        receiverName.value = defaultReceiver
        selectedDateMillis.value = System.currentTimeMillis()
    }

    private fun buildDiary(
        content: String = todayDiary.value,
        sendToFuture: Boolean = false,
        dateMillis: Long = selectedDateMillis.value
    ): Diary {
        val emotionToSave = selectedEmotion.value ?: detectedEmotion.value ?: ""
        return Diary(
            id = UUID.randomUUID().toString(),
            content = content,
            emotion = emotionToSave,
            sticker = selectedSticker.value,
            date = dateMillis,
            sendToFuture = sendToFuture,
            replyToId = replyToId.value,
            createdAt = System.currentTimeMillis(),
            nickname = nicknameToUse.value
        )
    }

    fun persistDiary(
        content: String = todayDiary.value,
        sendToFuture: Boolean = false,
        dateMillis: Long = selectedDateMillis.value
    ): Diary {
        val diary = buildDiary(content, sendToFuture, dateMillis)
        addDiary(diary)
        saveDiary(diary)
        clearForNewEntry()
        return diary
    }
    fun startAnalyze(bottomNavController: NavController) {
        isAnalyzing = true
        runAnalyze(bottomNavController)  // 기존 분석 함수
    }
    fun addDiary(diary: Diary) {
        _pastLetters.value = _pastLetters.value + diary
    }
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

    fun setReceiver(name: String) {
        receiverName.value = name
    }
    // --- Repository를 사용하는 로직 (기존 두 번째 ViewModel의 내용) ---
    val allDiaries = repo.getAllDiaries()
        .map { diaries -> diaries.map { it.toDiary() } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun saveDiary(diary: Diary) {
        viewModelScope.launch {
            repo.insertDiary(diary.toEntity())
        }
    }

    }


