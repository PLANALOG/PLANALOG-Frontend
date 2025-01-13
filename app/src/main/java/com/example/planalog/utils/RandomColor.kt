package com.example.planalog.utils

import android.graphics.Color
import kotlin.random.Random

private var categoryCount = 0

fun generateColor() : Int {
    val hue = (categoryCount * 137) % 360
    categoryCount++
    return Color.HSVToColor(floatArrayOf(hue.toFloat(), 0.6f, 0.8f)) // 0.7f: 채도, 0.9f : 밝기
}

fun generateRandomColor(): Int {
    // 100~255 사이로 제한하여 너무 어두운 색 방지
    val red = Random.nextInt(100, 256)
    val green = Random.nextInt(100, 256)
    val blue = Random.nextInt(100, 256)
    return Color.rgb(red, green, blue)
}