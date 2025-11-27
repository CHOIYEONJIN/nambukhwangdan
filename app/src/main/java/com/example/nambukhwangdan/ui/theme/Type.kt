package com.example.nambukhwangdan.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.nambukhwangdan.R

val Pretendard = FontFamily(
    Font(R.font.p_regular, FontWeight.Normal),
    Font(R.font.p_semibold, FontWeight.SemiBold),
    Font(R.font.p_bold, FontWeight.Bold)
)
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    titleLarge = TextStyle(
        fontFamily = Pretendard, // <-- Pretendard로 교체
        fontWeight = FontWeight.SemiBold, // titleLarge는 보통 Medium/SemiBold를 사용합니다.
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Pretendard, // <-- Pretendard로 교체
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)