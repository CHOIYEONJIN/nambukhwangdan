package com.example.nambukhwangdan.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nambukhwangdan.ui.theme.Background
import com.example.nambukhwangdan.ui.theme.OnSurface
import com.example.nambukhwangdan.ui.theme.Primary
import com.example.nambukhwangdan.ui.theme.Surface
import com.example.nambukhwangdan.viewmodel.AuthViewModel

@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel = viewModel(),
    onNavigateToLogin: () -> Unit // 로그아웃 후 로그인 화면으로 이동할 콜백
) {
    // AuthViewModel의 상태 관찰
    val authState by authViewModel.authState.collectAsState()

    // ⚠️ 3번 문제 해결: 불필요한 자동 로그아웃 로직 제거
    // if (!authState.isLoading && !authState.isLoggedIn) {
    //     onNavigateToLogin()
    //     return
    // }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "임시 설정 및 상태 확인",
            style = MaterialTheme.typography.headlineMedium,
            color = OnSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 닉네임 상태 표시 (로컬 DataStore)
                StatusItem(
                    label = "현재 닉네임 (로컬)",
                    value = authState.currentNickname ?: "미설정"
                )
                Spacer(Modifier.height(16.dp))
                // 로그인 상태 표시 (Firebase Auth)
                StatusItem(
                    label = "로그인 상태 (Firebase)",
                    value = if (authState.isLoggedIn) "✅ 로그인됨" else "❌ 로그아웃됨"
                )
                Spacer(Modifier.height(8.dp))
                if (authState.isLoggedIn) {
                    Text(
                        text = "UID: ${authViewModel.auth.currentUser?.uid ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // 로그아웃 버튼
        Button(
            onClick = {
                // 🚀 오류 해결: onSignOutComplete 인수에 onNavigateToLogin 콜백 전달
                authViewModel.signOut(onNavigateToLogin)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = authState.isLoggedIn, // 로그인 상태일 때만 활성화
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary
            )
        ) {
            Text(
                text = "로그아웃",
                color = Surface,
                fontWeight = FontWeight.Bold
            )
        }

        // 개발자를 위한 힌트
        Spacer(Modifier.height(16.dp))
        Text(
            text = "로그아웃 시 시작 경로 재결정 로직 테스트 가능",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun StatusItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = if (value.contains("✅")) Primary else OnSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen(
            onNavigateToLogin = {},
            // Preview에서는 실제 ViewModel 대신 더미 데이터를 사용하여 UI를 봅니다.
            // 실제 앱에서는 viewModel()이 사용됩니다.
        )
    }
}
