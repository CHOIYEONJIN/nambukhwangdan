package com.example.nambukhwangdan.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.nambukhwangdan.model.Diary
import com.example.nambukhwangdan.navigation.Routes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID


class DiaryViewModel : ViewModel() {

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
    }
    fun startAnalyze(bottomNavController: NavController) {
        isAnalyzing = true
        runAnalyze(bottomNavController)  // 기존 분석 함수
    }
}