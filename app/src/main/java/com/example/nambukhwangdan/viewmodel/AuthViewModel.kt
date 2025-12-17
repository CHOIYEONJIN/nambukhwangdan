package com.example.nambukhwangdan.viewmodel

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nambukhwangdan.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// DataStore 파일 이름 정의
private const val USER_PREFERENCES_NAME = "user_preferences"

// DataStore 인스턴스 생성 (Application Context에 연결)
private val Application.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

// DataStore Key 정의
private val PREF_NICKNAME = stringPreferencesKey("nickname")


data class AuthState(
    val isLoading: Boolean = true, // 초기 상태 로딩 중 여부
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val isNicknameSaved: Boolean = false, // 닉네임 저장 성공 여부
    val currentNickname: String? = null, // 현재 로컬에 저장된 닉네임
    val isLoggedIn: Boolean = false,
    // 추가: 초기 내비게이션 경로
    val startDestination: String = Routes.OnboardingIntro // 기본값은 인트로 화면
)

/**
 * 사용자 인증 및 온보딩(닉네임 설정 등) 관련 로직을 처리하는 ViewModel.
 * DataStore를 사용하여 닉네임을 로컬에 저장합니다.
 * @param application AndroidViewModel은 Application Context를 필요로 합니다.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    // Firebase Authentication 인스턴스
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val dataStore = application.dataStore

    // 닉네임 저장 상태 및 앱의 초기 상태를 외부에 노출
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    init {
        // 1. 초기 시작 화면 결정
        determineStartDestination()

        // 2. 🚀 Firebase 인증 상태 변화 리스너 등록 (1, 3번 문제 해결의 핵심)
        // 로그인/로그아웃 이벤트가 발생할 때마다 이 리스너가 호출되어 상태를 업데이트합니다.
        auth.addAuthStateListener { firebaseAuth ->
            viewModelScope.launch {
                updateAuthStateFromListener(firebaseAuth.currentUser)
            }
        }
    }
    /**
     * 리스너를 통해 전달받은 Firebase 인증 정보를 바탕으로 AuthState를 업데이트합니다.
     */
    private suspend fun updateAuthStateFromListener(user: FirebaseUser?) {
        val isLoggedIn = user != null
        val savedNickname = getNickname()

        if (user != null) {
            ensureUserDocument(user.uid)
        }
        // 리스너가 호출될 때는 startDestination을 바꾸지 않고,
        // 닉네임과 로그인 상태만 실시간으로 업데이트합니다.
        _authState.value = _authState.value.copy(
            isLoggedIn = isLoggedIn,
            currentNickname = savedNickname, // 닉네임 상태는 항상 로드 후 업데이트
            isLoading = false // 리스너가 호출되었다는 것은 초기 로딩이 끝났음을 의미
        )
    }



    /**
     * 앱을 시작할 때 최초로 보여줄 화면(Route)를 결정합니다.
     */
    fun determineStartDestination() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)

            // 1. DataStore에서 닉네임 로드
            val savedNickname = getNickname()

            // 2. Firebase 인증 상태 확인
            val isLoggedIn = auth.currentUser != null

            // 3. 시작 경로 결정 (1번 문제 해결: 로그인 상태만으로 판단)
            val destination = if (isLoggedIn) {
                Routes.MainHost // 로그인 O -> 메인 화면으로
            } else {
                Routes.OnboardingIntro // 로그인 X -> 온보딩 시작 화면으로
            }

            // 4. 상태 업데이트
            _authState.value = _authState.value.copy(
                isLoading = false,
                currentNickname = savedNickname,
                isLoggedIn = isLoggedIn,
                startDestination = destination
            )
        }
    }


    /**
     * 사용자가 입력한 닉네임을 로컬 DataStore에 저장합니다. (2번 문제 해결)
     */
    fun saveNickname(nickname: String) {
        // 1. 상태를 '저장 중'으로 변경
        _authState.value = _authState.value.copy(isSaving = true, saveError = null)

        viewModelScope.launch {
            try {
                // DataStore에 닉네임 저장
                dataStore.edit { preferences ->
                    preferences[PREF_NICKNAME] = nickname
                }

                // 2. 저장 성공 상태 업데이트: currentNickname을 저장된 nickname으로 즉시 업데이트
                _authState.value = _authState.value.copy(
                    isSaving = false,
                    isNicknameSaved = true,
                    currentNickname = nickname,
                    saveError = null
                )

            } catch (e: Exception) {
                // 3. 저장 실패 상태 업데이트
                _authState.value = _authState.value.copy(
                    isSaving = false,
                    saveError = "로컬 저장 실패: ${e.message}"
                )
            }
        }
    }

    /**
     * DataStore에서 닉네임을 비동기적으로 읽어옵니다.
     */
    suspend fun getNickname(): String? {
        val preferences = dataStore.data.first()
        return preferences[PREF_NICKNAME]
    }


    fun updateNickname(newNickname: String) {
        if (newNickname.isBlank()) return

        // 저장 중 상태로 표시 (UI 피드백 제공)
        _authState.value = _authState.value.copy(isSaving = true)

        viewModelScope.launch {
            try {
                // 1. 로컬 DataStore 업데이트
                dataStore.edit { preferences ->
                    preferences[PREF_NICKNAME] = newNickname
                }

                // 2. Firebase Firestore 업데이트 (서버 동기화)
                // ensureUserDocument 로직이 있는 것으로 보아 서버에도 닉네임을 관리하는 것이 좋습니다.
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .update("nickname", newNickname) // 서버의 nickname 필드 업데이트
                }

                // 3. UI 상태 업데이트
                _authState.value = _authState.value.copy(
                    isSaving = false,
                    currentNickname = newNickname,
                    saveError = null
                )

                // 성공 로그 (디버깅용)
                android.util.Log.d("AuthViewModel", "닉네임 업데이트 성공: $newNickname")

            } catch (e: Exception) {
                // 에러 발생 시 상태 복구
                _authState.value = _authState.value.copy(
                    isSaving = false,
                    saveError = "닉네임 업데이트 실패: ${e.message}"
                )
            }
        }
    }
    /**
     * 현재 사용자를 Firebase에서 로그아웃하고, 앱 상태를 업데이트합니다.
     */
    fun signOut(onSignOutComplete: () -> Unit) {
        auth.signOut()

        // 로그아웃이 완료되면 리스너가 자동으로 상태를 "isLoggedIn=false"로 업데이트합니다.
        // 우리는 화면 전환만 요청합니다.
        onSignOutComplete()
    }
    private fun ensureUserDocument(uid: String) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("users")
            .document(uid)
            .set(
                mapOf(
                    "createdAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
    }

}