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
    // ⚠️ private 키워드를 제거하여 SettingsScreen에서 접근할 수 있도록 변경했습니다.
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val dataStore = application.dataStore

    // 닉네임 저장 상태 및 앱의 초기 상태를 외부에 노출
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    init {
        // ViewModel이 생성될 때 초기 시작 화면을 결정하는 로직을 실행
        determineStartDestination()
    }

    /**
     * 앱을 시작할 때 최초로 보여줄 화면(Route)를 결정합니다.
     * 1. 닉네임이 설정되었는지 확인 (로컬 DataStore)
     * 2. Firebase에 로그인되어 있는지 확인 (Google Auth)
     */
    fun determineStartDestination() { // public으로 변경하여 외부에서 강제 업데이트 가능하게 함
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)

            // 1. DataStore에서 닉네임 로드
            val savedNickname = getNickname()

            // 2. Firebase 인증 상태 확인
            val isLoggedIn = auth.currentUser != null

            // 3. 시작 경로 결정 (닉네임 설정 여부 대신 로그인 여부만 사용)
            val destination = if (isLoggedIn) {
                Routes.MainHost // 로그인 O -> 메인 화면으로
            } else {
                Routes.OnboardingIntro // 로그인 X -> 온보딩 시작 화면으로
            }

            _authState.value = _authState.value.copy(
                isLoading = false,
                currentNickname = savedNickname, // 닉네임 상태는 항상 로드 후 업데이트
                isLoggedIn = isLoggedIn,
                startDestination = destination
            )
        }
    }


    /**
     * 사용자가 입력한 닉네임을 로컬 DataStore에 저장합니다.
     * 닉네임은 서버에 저장되지 않고, 편지 전송 시 데이터에 포함됩니다.
     * @param nickname 저장할 닉네임 문자열
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
                    currentNickname = nickname, // 👈 닉네임 즉시 업데이트 (2번 문제 해결)
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

    /**
     * 현재 사용자를 Firebase에서 로그아웃하고, 앱 상태를 업데이트합니다.
     */
    fun signOut(onSignOutComplete: () -> Unit) { // 콜백 함수를 추가하여 화면 전환을 외부에서 제어
        auth.signOut()

        // 로그아웃 후 상태를 즉시 업데이트하고 초기 화면을 재결정합니다.
        // onSignOutComplete 콜백을 통해 AppNavHost에게 화면 전환을 알립니다.
        determineStartDestination()
        onSignOutComplete() // 콜백 실행
    }
}