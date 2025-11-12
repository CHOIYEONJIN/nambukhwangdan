package com.example.nambukhwangdan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nambukhwangdan.model.Diary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID


class DiaryViewModel : ViewModel() {

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
    // 사진 임시 데이터(로컬 경로/URL)
    val allPhotos = (1..30).map { "https://picsum.photos/seed/$it/300/300" }
    val selectedPhotos = MutableStateFlow<List<String>>(emptyList())

    // 감정 분석 결과(설계서: 자동 추천 표시):contentReference[oaicite:3]{index=3}
    val detectedEmotion = MutableStateFlow<String?>(null)
    val selectedEmotion = MutableStateFlow<String?>(null)
    val selectedSticker = MutableStateFlow<String?>(null)

    fun updateDiary(text: String) { todayDiary.value = text }

    fun setSelectedDate(millis: Long) { selectedDateMillis.value = millis }

    fun togglePhoto(url: String) {
        val cur = selectedPhotos.value.toMutableList()
        if (cur.contains(url)) cur.remove(url) else cur.add(url)
        selectedPhotos.value = cur
    }

    fun runAnalyze() {
        // 실제에선 API 호출; 지금은 로딩 시뮬레이션
        viewModelScope.launch {
            delay(1200)
            detectedEmotion.value = "긍정" // 예시 자동 추천
        }
    }
    fun loadReplyLetterById(id: String) {
        // TODO: repo에서 가져와서 세팅
        // _replyLetter.value = repository.getById(id)
    }

    fun chooseEmotion(value: String) { selectedEmotion.value = value }

    fun chooseSticker(value: String) { selectedSticker.value = value }

    fun clearForNewEntry() {
        todayDiary.value = ""
        selectedPhotos.value = emptyList()
        detectedEmotion.value = null
        selectedEmotion.value = null
        selectedSticker.value = null
    }
}