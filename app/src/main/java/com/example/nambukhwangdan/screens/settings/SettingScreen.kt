package com.example.nambukhwangdan.ui.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nambukhwangdan.ui.theme.Primary
import com.example.nambukhwangdan.ui.theme.Surface
import com.example.nambukhwangdan.ui.theme.Variables
import com.example.nambukhwangdan.viewmodel.AuthViewModel

@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel = viewModel(),
    onNavigateToLogin: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val localContext = LocalContext.current
    // 닉네임 변경 다이얼로그 상태
    var showNicknameDialog by remember { mutableStateOf(false) }
    var newNickname by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Variables.Color4)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Text(text = "설정", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(30.dp))

            // 🔹 [그룹 1] 계정 정보 카드
            Column(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(10.dp))
                    .fillMaxWidth()
                    .background(Surface, RoundedCornerShape(10.dp))
                    .padding(horizontal = 20.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "계정 설정",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                    modifier = Modifier.padding(top = 15.dp, bottom = 10.dp)
                )

                // 닉네임 설정 (변경 버튼 포함)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "닉네임", fontSize = 14.sp, color = Color.Black)
                        Text(text = authState.currentNickname ?: "미설정", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    }
                    Text(
                        text = "변경",
                        fontSize = 13.sp,
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                newNickname = authState.currentNickname ?: ""
                                showNicknameDialog = true
                            }
                            .padding(8.dp)
                    )
                }

                HorizontalDivider(color = Variables.Color4.copy(alpha = 0.5f), thickness = 0.5.dp)

                SettingClickableItem(label = "로그아웃", onClick = { authViewModel.signOut(onNavigateToLogin) })
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 🔹 [그룹 2] 앱 설정 및 지원 카드 (추가 추천 기능)
            Column(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(10.dp))
                    .fillMaxWidth()
                    .background(Surface, RoundedCornerShape(10.dp))
                    .padding(horizontal = 20.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "서비스 이용",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                    modifier = Modifier.padding(top = 15.dp, bottom = 10.dp)
                )

                SettingClickableItem(
                    label = "알림 설정",
                    onClick = {
                        val intent = Intent().apply {
                            // 💡 모든 Settings 참조를 전체 경로로 적어줍니다.
                            action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                            putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, localContext.packageName)
                        }
                        localContext.startActivity(intent)
                    }
                )
                HorizontalDivider(color = Variables.Color4.copy(alpha = 0.5f), thickness = 0.5.dp)
                SettingInfoItem(label = "문의하기 및 피드백", value = "funnyyul0506@gmail.com")
            }

            // 🔹 회원 탈퇴 (버튼 대신 하단 텍스트 링크로 배치 - 시각적 부담 감소)
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = "회원 탈퇴",
                fontSize = 12.sp,
                color = Color.LightGray,
                modifier = Modifier.clickable { /* TODO: 탈퇴 로직 */ }
            )
        }

        // 🔹 닉네임 변경 다이얼로그 (NewLetterScreen 스타일 계승)
        if (showNicknameDialog) {
            Dialog(onDismissRequest = { showNicknameDialog = false }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    modifier = Modifier.padding(20.dp).fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("닉네임 변경", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = newNickname,
                            onValueChange = { newNickname = it },
                            placeholder = { Text("새 닉네임을 입력하세요") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )

                        Spacer(Modifier.height(20.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { showNicknameDialog = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                            ) { Text("취소", color = Color.White) }

                            Button(
                                onClick = {
                                    authViewModel.updateNickname(newNickname)
                                    showNicknameDialog = false
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) { Text("저장", color = Color.White) }
                        }
                    }
                }
            }
        }
    }
}

// 공통 클릭 아이템 컴포넌트
@Composable
private fun SettingClickableItem(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.Black)
        Text(text = ">", fontSize = 14.sp, color = Color.LightGray)
    }
}

@Composable
private fun SettingInfoItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.Black)
        Text(text = value, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}