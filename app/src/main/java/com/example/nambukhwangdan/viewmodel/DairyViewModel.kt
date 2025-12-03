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

    //구글 로그인된 유저의 userid값을 받아옴
    private val auth = FirebaseAuth.getInstance()
    private val userId: String?
        get() = auth.currentUser?.uid

    // ========== 캘린더/월별 일기 목록 상태 (drawing) ==========

    // 올해의 이번 달을 받아옴
    private val _currentYearMonth = MutableStateFlow(YearMonth.now())
    fun setMonth(year: Int, month: Int) {
        _currentYearMonth.value = YearMonth.of(year, month)
    }

    //선택한 달에 해당하는 일기 리스트들(읽기/쓰기 버전)
    private val _currentMonthDiaries = MutableStateFlow<List<Diary>>(emptyList())
    //읽기버전만 가능하게 변수로 저장
    val currentMonthDiaries = _currentMonthDiaries.asStateFlow()

    // ========== 전체 일기 데이터 흐름 ==========

    // 전체 일기를 받아오기
    val allDiaries = repo.getAllDiaries()
        .map { diaries -> diaries.map { it } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 일반 일기 목록 (답장 아이디가 없는 일기)
    val allRegularDiaries = allDiaries
        .map { diaries ->
            diaries.filter { it.replyToId == null }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )

    // 보낸 편지 목록 (답장 아이디가 있는 일기)
    val allSentLetters = allDiaries
        .map { diaries ->
            diaries.filter { it.replyToId != null }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )

    // ========== 초기화 및 동기화 ==========

    init {
        //온라인과 로컬 일기를 동기화함 (초기 동기화)
        viewModelScope.launch {
            userId?.let {
                repo.syncDiaries() //FireStore에서 일기 한번에 가져와 Room에 넣는다
                repo.observeRemoteDiaries().collect { diaries -> // 실시간으로 fireStore에 변경사항 생길 시
                    // _remoteDiaries.value = diaries // UI에서도 사용 가능하게 함 (선택적)
                    repo.insertDiaries(diaries) // Room에 전체 sync
                }
            }
        }
        viewModelScope.launch {
            //선택된 달이 변할 때마다
            _currentYearMonth
                .flatMapLatest { ym -> repo.getDiariesByMonth(ym.year, ym.monthValue) }
                .collect { _currentMonthDiaries.value = it } // 해당 달의 일기만 가져와서 _currentMonth에 저장
        }
    }

    // ========== 일기 작성 관련 상태 ==========

    // 오늘 작성 중인 일기
    val todayDiary = MutableStateFlow("")

    // 사용자가 일기 작성일로 선택한 날짜(초기값: 오늘)
    val selectedDateMillis = MutableStateFlow(System.currentTimeMillis())

    // 분석중인지 아닌지 변경 가능한 변수
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()

    //실제 사용자의 선택 감정
    val selectedEmotion = MutableStateFlow<String?>(null)

    //사용자가 선택한 스티커
    val selectedSticker = MutableStateFlow<String?>(null)

    //일기 작성 시 답장하는 일기의 아이디를 replyToId에 저장
    val replyToId = MutableStateFlow<String?>(null)

    private val _isAnonymous = MutableStateFlow(false)
    val isAnonymous = _isAnonymous.asStateFlow()

    private val userNickname = "나" // 실제 로그인 정보에서 가져올 예정 (임시)
    private val _nicknameToUse = MutableStateFlow(userNickname)
    val nicknameToUse = _nicknameToUse.asStateFlow()

    // ========== 일기 작성 관련 함수 ==========

    //일기 내용 업데이트
    fun updateDiary(text: String) { todayDiary.value = text }

    //사용자가 선택한 날짜 업데이트
    fun setSelectedDate(millis: Long) { selectedDateMillis.value = millis }

    //사용자가 선택한 감정으로 선택 감정 업데이트
    fun chooseEmotion(value: String) { selectedEmotion.value = value }

    //사용자가 실제 선택한 스티커로 선택 스티커 업데이트
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
        selectedDateMillis.value = System.currentTimeMillis()
    }

    private fun buildDiary(
        content: String = todayDiary.value,
        // sendToFuture는 LetterViewModel에서 처리되거나, replyToId로 대체될 수 있습니다.
        dateMillis: Long = selectedDateMillis.value
    ): Diary {
        val emotionToSave = selectedEmotion.value ?: detectedSentiment.value ?: ""
        val now = System.currentTimeMillis()
        return Diary(
            id = UUID.randomUUID().toString(),
            content = content,
            emotion = emotionToSave,
            sticker = selectedSticker.value,
            date = dateMillis,
            replyToId = replyToId.value,
            createdAt = now,
            userId = userId ?: "",
            updatedAt = now
        )
    }

    // 분석 없이 일기 저장 (LetterToTomorrow 등의 용도)
    fun persistDiary(
        content: String = todayDiary.value,
        dateMillis: Long = selectedDateMillis.value
    ): Diary {
        val diary = buildDiary(content, dateMillis)
        saveDiary(diary)
        clearForNewEntry()
        return diary
    }

    fun saveDiary(diary: Diary) {
        viewModelScope.launch {
            repo.insertDiary(diary)
            repo.saveDiaryToFirestore(diary, userId) // Firestore에도 저장
        }
    }

    //diary 생성해 저장 및 분석하는 함수
    fun persistDiaryAndAnalyze(bottomNavController: NavController) {
        val now = System.currentTimeMillis()
        //일기 생성시간을 위해 현재 시간을 받아옴
        //일기 객체 생성
        val diary = Diary(
            id = UUID.randomUUID().toString(),
            content = todayDiary.value,
            emotion = selectedEmotion.value ?: "",
            sticker = selectedSticker.value,
            date = selectedDateMillis.value,
            replyToId = replyToId.value,
            createdAt = now,
            userId = userId ?: "",
            updatedAt = now
        )
        //객체 저장과 함께 분석 시작
        viewModelScope.launch {
            _isAnalyzing.value = true  //분석중으로 바꿈

            // 1) Room 저장
            repo.insertDiary(diary)

            // 2) 유저별 Firestore 저장
            repo.saveDiaryToFirestore(diary, userId)

            // 3) 감정 분석용 컬렉션에 저장 → Cloud Function 트리거
            repo.saveDiaryForAnalysis(diary)

            // 4) 감정 분석 결과 감시 시작
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
            _isAnalyzing.value = false // 분석 안하는 중으로 다시 바꿈
            if (_detectedSentiment.value == null) {
                _detectedSentiment.value = "분석 실패"
            }
            bottomNavController.navigate(Routes.AnalyzeResult)
        }
    }

    // ========== 데이터 관리 및 수정 함수 ==========

    //좋아요 누른걸 반영하는 함수
    fun toggleLike(id: String) {
        viewModelScope.launch {
            repo.toggleLike(id)   // refresh 필요 없음
        }
    }

    //일기 삭제 함수
    fun deleteDiary(id: String) {
        viewModelScope.launch {
            repo.deleteDiary(id)
        }
    }

    //일기들을 동기화시킴 (room <-->fireStore)
    fun syncDiaries() {
        viewModelScope.launch { repo.syncDiaries() }
    }


    //일기 수정 시 업데이트 하는 함수
    fun updateDiary(diary: Diary) {
        viewModelScope.launch { repo.updateDiary(diary) }
    }

    // ========== 사진 및 감정 분석 결과 상태/함수 ==========

    //선택한 사진의 위치를 저장함
    private val _selectedUris = MutableStateFlow<List<Uri>>(emptyList())
    val selectedUris = _selectedUris.asStateFlow()

    fun setSelectedUris(uris: List<Uri>) {
        _selectedUris.value = uris
    }

    //사진 삭제 함수
    fun removeUri(uri: Uri) {
        _selectedUris.value = _selectedUris.value.filterNot { it == uri }
    }

    //감정 분석 결과
    private val _detectedSentiment = MutableStateFlow<String?>(null)
    val detectedSentiment = _detectedSentiment

    //감정 분석 결과 (감정의 강도를 나타내는 점수)
    private val _detectedScore = MutableStateFlow<Float?>(null)
    val detectedScore = _detectedScore.asStateFlow()

    //fireStore에 저장되어있는 감정분석결과를 가져오는 함수
    fun observeSentiment(diaryId: String) {
        userId?.let { uid ->
            firestore.collection("users")
                .document(uid)
                .collection("diaries")
                .document(diaryId) //users/유저아이디/diaries/다이어리아이디 에 접근
                .addSnapshotListener { snapshot, _ ->
                    val sentiment = snapshot?.getString("sentiment")
                    val score = snapshot?.getDouble("score")?.toFloat()
                    //해당 diaryId에 저장된 감정과 점수를 가져옴

                    //감정이 분석되었다면 감정,점수를 저장하고 분석중 상태를 분석 안함으로 변경
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
                                )// roomDB에도 감정과 점수를 저장
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