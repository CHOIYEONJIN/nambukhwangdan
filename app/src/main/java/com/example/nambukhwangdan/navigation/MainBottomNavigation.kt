package com.example.nambukhwangdan.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState


@Composable
fun MainBottomNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // ⚠️ TODO: 실제 프로젝트의 Primary 색상을 사용하세요. 임시로 파란색으로 지정합니다.
    val Primary = Color(0xFF4285F4)

    val pretendard = FontFamily.Default // TODO: Pretendard 폰트 연결
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 탭 아이템 정의: (아이콘, 라벨, 경로)
    val items = listOf(
        Triple(Icons.Default.Home, "홈", Routes.Home),
        Triple(Icons.Default.Edit, "일기", Routes.Journal),
        Triple(Icons.Default.Send, "편지함", Routes.Inbox),
        Triple(Icons.Default.DateRange, "캘린더", Routes.Calendar),
        Triple(Icons.Default.Person, "내정보", Routes.Settings)
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            .fillMaxWidth()
            .height(85.dp) // 높이 고정
    ) {
        items.forEach { (icon, label, route) ->
            val isSelected = currentRoute == route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(route) {
                        // 탭 전환 시:
                        // 1. 현재 탭 경로를 백 스택에서 제거하여 최상위 상태 유지
                        navController.graph.startDestinationRoute?.let { r ->
                            popUpTo(r) { saveState = true }
                        }
                        // 2. 같은 탭을 다시 눌러도 화면이 재생성되지 않도록 함
                        launchSingleTop = true
                        // 3. 이전 탭의 상태(스크롤 위치 등)를 복원
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) Primary else Color.Gray
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontFamily = pretendard,
                        fontSize = 10.sp
                    )
                }
            )
        }
    }
}