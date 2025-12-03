package com.example.nambukhwangdan.ui.onboarding

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.nambukhwangdan.R
import com.example.nambukhwangdan.ui.theme.Background
import com.example.nambukhwangdan.ui.theme.Grey
import com.example.nambukhwangdan.ui.theme.OnSurface
import com.example.nambukhwangdan.ui.theme.Primary
import com.example.nambukhwangdan.ui.theme.Surface
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

// swipeBack 함수는 기존대로 유지
// 이 함수는 외부에서 확장 함수로 정의되었거나, LoginScreen 내부에 정의되어 있다고 가정하고 그대로 사용합니다.
// 예제에서는 컴파일을 위해 'composed' Modifier 함수를 사용하지 않고 더미로 대체합니다.
private fun Modifier.swipeBack(onBack: () -> Unit) = this // 실제 구현 대신 임시 함수로 대체

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("VisibleForTests") // Credential Manager 테스트 용도로 필요
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {}, // 메인 화면으로 이동하는 AppNavHost의 람다
    onBack: () -> Unit = {}         // 뒤로 가기 (현재는 "시작하기" 버튼에 연결 요청을 하셨으므로 로직 변경)
) {
    val pretendard = FontFamily.Default
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val scope = rememberCoroutineScope()

    // 1. 로그인 성공 상태를 추적하는 State
    var isLoggedIn by remember { mutableStateOf(false) }
    // 2. 로그인 중 발생하는 에러 메시지 State
    var errorMessage by remember { mutableStateOf<String?>(null) }
    // 3. Credential Manager 요청 옵션 구성 (Google Login)
    val googleIdOption = remember {
        GetSignInWithGoogleOption.Builder(
            /* serverClientId = */ context.getString(R.string.default_web_client_id)
        ).build()
    }
    val credentialManager = remember { CredentialManager.create(context) }
    val request = remember {
        GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    // 로그인 성공 시 호출될 최종 콜백
    val handleLoginSuccess = {
        // FirebaseAuth 로그인이 성공하면 isLogged 상태를 true로 변경하여 '시작하기' 버튼 활성화
        isLoggedIn = true
        errorMessage = null
    }

    // 구글 로그인 처리 함수
    val handleGoogleSignIn: () -> Unit = {
        scope.launch {
            try {
                // 사용자에게 Google 선택 UI 표시
                val result = credentialManager.getCredential(context, request)
                val cred = result.credential

                // Google ID 토큰 꺼내기
                val googleIdTokenCred = GoogleIdTokenCredential.createFrom(cred.data)
                val idToken = googleIdTokenCred.idToken

                if (idToken == null) {
                    errorMessage = "Google 로그인이 취소되었거나 토큰을 받지 못했습니다."
                    return@launch        // ❗ Firebase로 넘기지 말고 종료
                }

                // Firebase Auth로 교환
                val firebaseCred = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCred)
                    .addOnSuccessListener {
                        // Firebase Authentication 성공 후 상태 업데이트
                        handleLoginSuccess()
                    }
                    .addOnFailureListener { e ->
                        errorMessage = "Firebase 로그인 실패: ${e.message}"
                    }
            } catch (e: GetCredentialException) {
                // 사용자가 취소하거나 Credential Manager 오류 발생
                errorMessage = "Google 로그인 취소/오류: ${e::class.simpleName}"
            } catch (e: Exception) {
                // 예상치 못한 오류
                errorMessage = "예상치 못한 오류: ${e.message}"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .swipeBack(onBack = onBack), // 기존 Back 로직 유지
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(120.dp))

        Text(
            text = "로그인을 해주세요",
            fontSize = 20.sp,
            color = OnSurface,
            fontFamily = pretendard
        )

        Spacer(Modifier.height(24.dp))

        // Google 로그인 버튼 영역
        Box(
            modifier = Modifier
                .size(width = 300.dp, height = 50.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                // 4. 구글 로그인 기능 연동
                .clickable(onClick = handleGoogleSignIn),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .size(width = 300.dp, height = 50.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    // Google 'G' 아이콘 대신 텍스트로 대체 (아이콘 미포함 가정)
                    Text("G", fontWeight = FontWeight.Bold, color = Color(0xFF4285F4), fontSize = 14.sp)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Google 계정으로 계속하기",
                    color = Color(0xFF3C4043),
                    fontSize = 16.sp,
                    fontFamily = pretendard
                )
            }
        }

        // 로그인 에러 메시지 표시
        errorMessage?.let { msg ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(300.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "계속을 클릭하면 당사의 서비스 이용 약관 및 개인정보 처리 방침에 동의하는 것으로 간주됩니다.",
            color = Color(0xFF6B7BA0),
            modifier = Modifier.width(300.dp),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(Grey))
            Spacer(Modifier.width(10.dp))
            Box(Modifier.size(10.dp).clip(CircleShape).background(Grey))
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier
                    .size(width = 30.dp, height = 10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Primary)
            )

        }

        Spacer(Modifier.height(56.dp))

        // 5. 하단 시작하기 버튼 (로그인 상태에 따라 활성화/비활성화)
        val buttonColor = if (isLoggedIn) Primary else Grey
        val buttonClickable = isLoggedIn

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(85.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 10.dp, topEnd = 10.dp,
                        bottomStart = 0.dp, bottomEnd = 0.dp
                    )
                )
                .background(buttonColor)
                .clickable(enabled = isLoggedIn) {
                    if(buttonClickable){
                    // 로그인 성공 시에만 MainHost로 이동하는 onLoginSuccess 콜백 실행

                        onLoginSuccess()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "시작하기",
                color = Surface,
                fontSize = 18.sp,
                fontFamily = pretendard
            )
        }
    }
}

// Android O 버전 이상이 필요한 @RequiresApi 때문에 Preview를 별도로 분리했습니다.
@Preview(showBackground = true, device = "spec:width=393dp,height=852dp,dpi=440")
@Composable
private fun LoginScreeny_Preview() {
    MaterialTheme {
        // Preview에서는 기능 없이 UI만 보여줍니다.
        Column {
            Text("로그인 화면 미리보기", modifier = Modifier.padding(16.dp))
        }
    }
}