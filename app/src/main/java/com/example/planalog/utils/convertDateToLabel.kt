package com.example.planalog.utils

import java.text.SimpleDateFormat
import java.util.*

fun convertDateToLabel(dateString: String): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = Calendar.getInstance()
    val targetDate = Calendar.getInstance()

    targetDate.time = dateFormat.parse(dateString) ?: return dateString

    return when {
        isSameDay(today, targetDate) -> "오늘"
        isYesterday(today, targetDate) -> "어제"
        else -> SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(targetDate.time)
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(today: Calendar, target: Calendar): Boolean {
    val yesterday = today.clone() as Calendar
    yesterday.add(Calendar.DAY_OF_YEAR, -1)
    return isSameDay(yesterday, target)
}
