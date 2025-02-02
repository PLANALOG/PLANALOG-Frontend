package com.example.planalog.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun getCurrentDate(): String {
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return currentDate.format(formatter)  // "2025-01-27" 형식으로 반환
}

fun getCurrentMonth(): String {
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
    return currentDate.format(formatter)
}