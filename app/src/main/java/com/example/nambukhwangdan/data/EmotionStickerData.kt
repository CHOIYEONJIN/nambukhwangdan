package com.example.nambukhwangdan.data

import androidx.annotation.DrawableRes
import com.example.nambukhwangdan.R

data class Sticker(
    val name: String,
    @DrawableRes val resId: Int
)

// 상세 감정 목록 (Compose 파일 내부에 있어도 되지만 분리하면 좋음)
val detailStickerMap = mapOf(
    "p1" to R.drawable.p1,
    "p2" to R.drawable.p2,
    "p3" to R.drawable.p3,
    "p4" to R.drawable.p4,
    "p5" to R.drawable.p5,

    "a1" to R.drawable.a1,
    "a2" to R.drawable.a2,
    "a3" to R.drawable.a3,
    "a4" to R.drawable.a4,
    "a5" to R.drawable.a5,

    "n1" to R.drawable.n1,
    "n2" to R.drawable.n2,
    "n3" to R.drawable.n3,
    "n4" to R.drawable.n4,
    "n5" to R.drawable.n5
)

