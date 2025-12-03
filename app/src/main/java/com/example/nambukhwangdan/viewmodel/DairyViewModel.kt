package com.example.nambukhwangdan.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.nambukhwangdan.data.repository.DiaryRepository
import com.example.nambukhwangdan.model.Diary.Diary
import com.example.nambukhwangdan.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val repo: DiaryRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val userId: String?
        get() = auth.currentUser?.uid

    // 설계서: "과거의 내 편지 표시":contentReference[oaicite:2]{index=2}
    private val _pastLetters = MutableStateFlow(
        listOf(
            Diary(
                id = UUID.randomUUID().toString(),
                content = "과거에 작성한 일기 내용이 여기에 뜨게 됩니다",
            )
        )
    )
    val pastLetters = _pastLetters.asStateFlow()

    private val _remoteDiaries = MutableStateFlow<List<Diary>>(emptyList())
    val remoteDiaries = _remoteDiaries.asStateFlow()

    private val _currentYearMonth = MutableStateFlow(YearMonth.now())
    private val _currentMonthDiaries = MutableStateFlow<List<Diary>>(emptyList())
    val currentMonthDiaries = _currentMonthDiaries.asStateFlow()

    init {
        viewModelScope.launch {
            userId?.let {
                repo.syncDiaries()
                repo.observeRemoteDiaries().collect { diaries ->
                    _remoteDiaries.value = diaries
                    repo.insertDiaries(diaries)
                }
            }
        }
        viewModelScope.launch {
            _currentYearMonth
                .flatMapLatest { ym -> repo.getDiariesByMonth(ym.year, ym.monthValue) }
                .collect { _currentMonthDiaries.value = it }
        }
    }

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
    private val _isAnalyzing = MutableStateFlow(false)   // 내부에서 변경
    val isAnalyzing = _isAnalyzing.asStateFlow()         // 외부에서는 읽기 전용

    fun chooseEmotion(value: String) { selectedEmotion.value = value }

    fun chooseSticker(value: String) { selectedSticker.value = value }

    fun persistDiaryAndAnalyze(bottomNavController: NavController) {
        // 🔹 여기서 새 id 만들어서 Diary 하나 생성
        val now = System.currentTimeMillis()
        val diary = Diary(
            id = UUID.randomUUID().toString(),
            content = todayDiary.value,
            emotion = selectedEmotion.value ?: "",
            sticker = selectedSticker.value,
            date = selectedDateMillis.value,
            replyToId = replyToId.value,
            createdAt = now,
            nickname = nicknameToUse.value,
            userId = userId ?: "",
            updatedAt = now
        )

        viewModelScope.launch {
            _isAnalyzing.value = true  // 🔥 로딩 시작

            // 1) Room 저장
            repo.insertDiary(diary)

            // 2) 유저별 Firestore 저장
            repo.saveDiaryToFirestore(diary, userId)

            // 3) 감정 분석용 컬렉션에 저장 → Cloud Function 트리거
            repo.saveDiaryForAnalysis(diary)

            // 4) 감정 분석 결과 감시 시작 (⭐ 딱 한 번만 호출!)
            observeSentiment(diary.id)

            // 5) 결과를 최대 7초까지 기다림
            repeat(14) { // 14 × 500ms = 7초
                delay(500)
                if (_detectedSentiment.value != null) {
                    _isAnalyzing.value = false
                    bottomNavController.navigate(Routes.AnalyzeResult)
                    return@launch
                }
            }

            // 🔥 7초 동안 결과가 없으면 '분석 실패' 처리
            _isAnalyzing.value = false
            if (_detectedSentiment.value == null) {
                _detectedSentiment.value = "분석 실패"
            }
            bottomNavController.navigate(Routes.AnalyzeResult)
        }
    }

    private val _isAnonymous = MutableStateFlow(false)

    private val userNickname = "닉네임" // 실제 로그인 정보에서 가져올 예정 (임시)
    private val _nicknameToUse = MutableStateFlow(userNickname)
    val nicknameToUse = _nicknameToUse.asStateFlow()

    val replyToId = MutableStateFlow<String?>(null)

    fun setReplyToId(id: String) {
        replyToId.value = id
    }

    val allDiaries = repo.getAllDiaries()
        .map { diaries -> diaries.map { it } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun toggleLike(id: String) {
        viewModelScope.launch {
            repo.toggleLike(id)   // refresh 필요 없음
        }
    }

    fun deleteDiary(id: String) {
        viewModelScope.launch {
            repo.deleteDiary(id)
        }
    }

    fun syncDiaries() {
        viewModelScope.launch { repo.syncDiaries() }
    }

    fun setMonth(year: Int, month: Int) {
        _currentYearMonth.value = YearMonth.of(year, month)
    }

    fun updateDiary(diary: Diary) {
        viewModelScope.launch { repo.updateDiary(diary) }
    }

    private val _selectedUris = MutableStateFlow<List<Uri>>(emptyList())
    val selectedUris = _selectedUris.asStateFlow()

    fun setSelectedUris(uris: List<Uri>) {
        _selectedUris.value = uris   // 🔥 이게 반드시 있어야 해!
    }

    fun removeUri(uri: Uri) {
        _selectedUris.value = _selectedUris.value.filterNot { it == uri }
    }

    private val _detectedSentiment = MutableStateFlow<String?>(null)
    val detectedSentiment = _detectedSentiment

    private val _detectedScore = MutableStateFlow<Float?>(null)
    val detectedScore = _detectedScore.asStateFlow()

    fun observeSentiment(diaryId: String) {
        userId?.let { uid ->
            firestore.collection("users")
                .document(uid)
                .collection("diaries")
                .document(diaryId)
                .addSnapshotListener { snapshot, _ ->
                    val sentiment = snapshot?.getString("sentiment")
                    val score = snapshot?.getDouble("score")?.toFloat()

                    if (sentiment != null) {
                        _detectedSentiment.value = sentiment
                        _detectedScore.value = score
                        _isAnalyzing.value = false
                        viewModelScope.launch {
                            repo.getDiaryById(diaryId)?.let {
                                repo.updateDiary(
                                    it.copy(
                                        sentimentLabel = sentiment,
                                        sentimentScore = score
                                    )
                                )
                            }
                        }
                    }
                }
        }
    }
    fun resetDetectedSentiment() {
        _detectedSentiment.value = null
    }
}
