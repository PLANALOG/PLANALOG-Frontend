package com.example.planalog.ui.comment.com.example.planalog.utils

fun generateYearMonthOptions(startYear: Int, endYear: Int): List<String> {
    val options = mutableListOf<String>()
    for (year in startYear..endYear) {
        for (month in 1..12) {
            options.add(String.format("%d. %02d", year, month)) // 공백 포함 포맷
        }
    }
    return options
}
