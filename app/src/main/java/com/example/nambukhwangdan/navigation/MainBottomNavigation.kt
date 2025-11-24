package com.example.nambukhwangdan.navigation

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.nambukhwangdan.ui.theme.Primary
import com.example.nambukhwangdan.R

@Composable
fun MainBottomNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // TODO: Pretendard 폰트 연결
    val pretendard = FontFamily.Default
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        Triple(R.drawable.desk, "홈", Routes.Home),
        Triple(R.drawable.diary, "일기", Routes.Journal),
        Triple(R.drawable.letter, "편지함", Routes.Inbox),
        Triple(R.drawable.calander, "캘린더", Routes.Calendar),
        Triple(R.drawable.setting, "내정보", Routes.Settings)
    )

    NavigationBar(
        containerColor = Color.Transparent,
        tonalElevation = 0.dp, // 그림자 제거

        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        items.forEach { (iconId, label, route) ->
            val isSelected = currentRoute == route
            val color = if (isSelected) Primary else Gray

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(route) {
                        navController.graph.startDestinationRoute?.let { r ->
                            popUpTo(r) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent, // 진회색 배경 제거
                    selectedIconColor = Primary,
                    unselectedIconColor = Gray
                ),
                interactionSource = remember { MutableInteractionSource() },

                icon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 12.dp) // 상단 여백 18dp
                    ) {
                        Icon(
                            painter = painterResource(id = iconId),
                            contentDescription = label,
                            tint = color
                        )
                        Spacer(modifier = Modifier.height(1.dp)) // 아이콘-텍스트 간격 5dp

                        Text(
                            text = label,
                            fontFamily = pretendard,
                            fontSize = 10.sp,
                            color = color
                        )
                    }
                },
                label = null
            )
        }
    }
}