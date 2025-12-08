package com.example.nambukhwangdan.viewmodel

import android.net.Uri
import android.util.Log // ⭐️ Log import 추가
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.nambukhwangdan.data.repository.DiaryRepository
import com.example.nambukhwangdan.data.repository.TomorrowLetterRepository
import com.example.nambukhwangdan.model.Diary.Diary
import com.example.nambukhwangdan.model.TomorrowLetter.TomorrowLetter
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
    private val diaryRepo: DiaryRepository,
    private val tomorrowLetterRepo: TomorrowLetterRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val userId: String?
        get() = auth.currentUser?.uid
    private val TAG = "DiaryViewModel" // ⭐️ 로그 태그 추가

    // ========== 캘린더/월별 일기 목록 상태 (drawing) ==========

    private val _currentYearMonth = MutableStateFlow(YearMonth.now())
    fun setMonth(year: Int, month: Int) {
        _currentYearMonth.value = YearMonth.of(year, month)
    }

    private val _currentMonthDiaries = MutableStateFlow<List<Diary>>(emptyList())
    val currentMonthDiaries = _currentMonthDiaries.asStateFlow()

    // ========== 전체 일기 데이터 흐름 ==========

    val allDiaries = diaryRepo.getAllDiaries()
        .map { diaries -> diaries.map { it } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allRegularDiaries = allDiaries
        .map { diaries ->
            diaries.filter { it.replyToId == null }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )

    val allSentLetters = allDiaries
        .map { diaries ->
            diaries.filter { it.replyToId != null }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )

    // =====================================================================
    // ========== 미래 편지 (TomorrowLetter) 관련 상태 및 흐름 ==========
    // =====================================================================

    val allUnDeliveredLetters = tomorrowLetterRepo.getAllUnDeliveredLetters()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList<TomorrowLetter>())

    val receivedTomorrowLetters = allUnDeliveredLetters
        .map { letters ->
            val now = System.currentTimeMillis()
            letters.filter { it.deliveryTimestamp <= now && !it.isDelivered }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList<TomorrowLetter>()
        )

    val tomorrowLetterContent = MutableStateFlow("")
    val deliveryDateMillis = MutableStateFlow(System.currentTimeMillis())
    val allTomorrowLetters = tomorrowLetterRepo.getAllTomorrowLetters()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList<TomorrowLetter>()) // 🚨 이 함수가 Repository에 정의되어야 합니다.
    fun updateTomorrowLetterContent(text: String) { tomorrowLetterContent.value = text }

    fun setDeliveryDate(millis: Long) { deliveryDateMillis.value = millis }

    // 새로운 미래 편지를 저장하는 함수
    fun saveTomorrowLetter() {
        Log.d(TAG, "Attempting to save TomorrowLetter...") // ⭐️ 저장 함수 호출 로그

        // ⭐️⭐️⭐️ 유효성 검사 로직을 임시로 주석 처리함 (테스트 목적) ⭐️⭐️⭐️
        /*
        if (tomorrowLetterContent.value.isBlank() || deliveryDateMillis.value <= System.currentTimeMillis()) {
            Log.w(TAG, "Save blocked by validation: content is blank or delivery date is past/present.")
            return
        }
        */
        // ⭐️⭐️⭐️ 주석 해제 후 테스트 후에는 반드시 로직을 다시 활성화해야 합니다! ⭐️⭐️⭐️


        val letter = TomorrowLetter(
            id = UUID.randomUUID().toString(),
            content = tomorrowLetterContent.value,
            deliveryTimestamp = deliveryDateMillis.value,
            createdAt = System.currentTimeMillis(),
            isDelivered = false,
            userId = userId ?: ""
        )

        viewModelScope.launch {
            Log.d(TAG, "Calling Repository to insert letter...") // ⭐️ Repository 호출 직전 로그
            tomorrowLetterRepo.insertTomorrowLetter(letter)

            // 저장 후 상태 초기화
            tomorrowLetterContent.value = ""
            deliveryDateMillis.value = System.currentTimeMillis()
            Log.i(TAG, "TomorrowLetter save process completed in ViewModel.")
        }
    }

    // 전달 시간이 지난 편지들을 확인하고 DiaryEntity로 변환하여 처리하는 함수
    fun processDueLetters() {
        viewModelScope.launch {
            val dueLetters = tomorrowLetterRepo.getDueLettersOnce(System.currentTimeMillis())

            dueLetters.forEach { letter ->
                val deliveredDiary = buildDiary(
                    content = "From Yesterday: ${letter.content}",
                    dateMillis = letter.deliveryTimestamp
                ).copy(
                    id = UUID.randomUUID().toString(),
                    emotion = "",
                    sticker = null,
                    replyToId = null,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                saveDiary(deliveredDiary)
                tomorrowLetterRepo.markLetterAsDelivered(letter)
            }
        }
    }


    // ========== 초기화 및 동기화 (기존 코드 유지) ==========

    init {
        viewModelScope.launch {
            userId?.let {
                diaryRepo.syncDiaries()
                diaryRepo.observeRemoteDiaries().collect { diaries ->
                    diaryRepo.insertDiaries(diaries)
                }
            }
        }
        viewModelScope.launch {
            _currentYearMonth
                .flatMapLatest { ym -> diaryRepo.getDiariesByMonth(ym.year, ym.monthValue) }
                .collect { _currentMonthDiaries.value = it }
        }
    }

    // ========== 일기 작성 관련 상태 (기존 코드 유지) ==========

    val todayDiary = MutableStateFlow("")
    val selectedDateMillis = MutableStateFlow(System.currentTimeMillis())
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()
    val selectedEmotion = MutableStateFlow<String?>(null)
    val selectedSticker = MutableStateFlow<String?>(null)
    val replyToId = MutableStateFlow<String?>(null)
    private val _replyingToTomorrowLetterId = MutableStateFlow<String?>(null)
    val replyingToTomorrowLetterId = _replyingToTomorrowLetterId.asStateFlow()
    fun setReplyingToTomorrowLetterId(id: String?) {
        _replyingToTomorrowLetterId.value = id
    }
    private val _isAnonymous = MutableStateFlow(false)
    val isAnonymous = _isAnonymous.asStateFlow()
    private val userNickname = "나"
    private val _nicknameToUse = MutableStateFlow(userNickname)
    val nicknameToUse = _nicknameToUse.asStateFlow()

    // ========== 일기 작성 관련 함수 (기존 코드 유지) ==========

    fun updateDiary(text: String) { todayDiary.value = text }
    fun setSelectedDate(millis: Long) { selectedDateMillis.value = millis }
    fun chooseEmotion(value: String) { selectedEmotion.value = value }
    fun chooseSticker(value: String) { selectedSticker.value = value }
    fun setReplyToId(id: String) {
        replyToId.value = id
    }
    fun onAnonymousCheckedChange(newValue: Boolean) {
        _isAnonymous.value = newValue
        _nicknameToUse.value = if (newValue) "익명" else userNickname
    }

    fun clearForNewEntry() {
        todayDiary.value = ""
        setSelectedUris(emptyList())
        detectedSentiment.value = null
        selectedEmotion.value = null
        selectedSticker.value = null
        replyToId.value = null
        _replyingToTomorrowLetterId.value = null
        selectedDateMillis.value = System.currentTimeMillis()
    }

    private fun buildDiary(
        content: String = todayDiary.value,
        dateMillis: Long = selectedDateMillis.value
    ): Diary {
        val emotionToSave = selectedEmotion.value ?: detectedSentiment.value ?: ""
        val now = System.currentTimeMillis()
        val replyId = _replyingToTomorrowLetterId.value ?: replyToId.value

        return Diary(
            id = UUID.randomUUID().toString(),
            content = content,
            emotion = emotionToSave,
            sticker = selectedSticker.value,
            date = dateMillis,
            replyToId = replyId,
            createdAt = now,
            userId = userId ?: "",
            updatedAt = now
        )
    }

    fun persistDiary(
        content: String = todayDiary.value,
        dateMillis: Long = selectedDateMillis.value
    ): Diary {
        val diary = buildDiary(content, dateMillis)
        val tomorrowLetterId = _replyingToTomorrowLetterId.value
        if (tomorrowLetterId != null) {
            viewModelScope.launch {
                val letter = tomorrowLetterRepo.getTomorrowLetterById(tomorrowLetterId)
                if (letter != null) {
                    tomorrowLetterRepo.markLetterAsDelivered(letter.copy(isDelivered = true))
                }
            }
        }
        saveDiary(diary)
        clearForNewEntry()
        return diary
    }

    fun saveDiary(diary: Diary) {
        viewModelScope.launch {
            diaryRepo.insertDiary(diary)
            diaryRepo.saveDiaryToFirestore(diary, userId)
        }
    }

    fun persistDiaryAndAnalyze(bottomNavController: NavController) {
        val now = System.currentTimeMillis()
        val diary = Diary(
            id = UUID.randomUUID().toString(),
            content = todayDiary.value,
            emotion = selectedEmotion.value ?: "",
            sticker = selectedSticker.value,
            date = selectedDateMillis.value,
            replyToId = _replyingToTomorrowLetterId.value ?: replyToId.value,
            createdAt = now,
            userId = userId ?: "",
            updatedAt = now
        )

        viewModelScope.launch {
            val tomorrowLetterId = _replyingToTomorrowLetterId.value
            if (tomorrowLetterId != null) {
                val letter = tomorrowLetterRepo.getTomorrowLetterById(tomorrowLetterId)
                if (letter != null) {
                    tomorrowLetterRepo.markLetterAsDelivered(letter.copy(isDelivered = true))
                }
            }

            _isAnalyzing.value = true

            diaryRepo.insertDiary(diary)
            diaryRepo.saveDiaryToFirestore(diary, userId)
            diaryRepo.saveDiaryForAnalysis(diary)

            observeSentiment(diary.id)

            repeat(14) {
                delay(500)
                if (_detectedSentiment.value != null) {
                    _isAnalyzing.value = false
                    bottomNavController.navigate(Routes.AnalyzeResult)
                    return@launch
                }
            }

            _isAnalyzing.value = false
            if (_detectedSentiment.value == null) {
                _detectedSentiment.value = "분석 실패"
            }
            bottomNavController.navigate(Routes.AnalyzeResult)
        }
    }

    // ========== 데이터 관리 및 수정 함수 (기존 코드 유지) ==========

    fun toggleLike(id: String) {
        viewModelScope.launch {
            diaryRepo.toggleLike(id)
        }
    }

    fun deleteDiary(id: String) {
        viewModelScope.launch {
            diaryRepo.deleteDiary(id)
        }
    }

    fun syncDiaries() {
        viewModelScope.launch { diaryRepo.syncDiaries() }
    }


    fun updateDiary(diary: Diary) {
        viewModelScope.launch { diaryRepo.updateDiary(diary) }
    }

    // ========== 사진 및 감정 분석 결과 상태/함수 (기존 코드 유지) ==========

    private val _selectedUris = MutableStateFlow<List<Uri>>(emptyList())
    val selectedUris = _selectedUris.asStateFlow()

    fun setSelectedUris(uris: List<Uri>) {
        _selectedUris.value = uris
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
                            diaryRepo.getDiaryByIdOnce(diaryId)?.let {
                                diaryRepo.updateDiary(
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