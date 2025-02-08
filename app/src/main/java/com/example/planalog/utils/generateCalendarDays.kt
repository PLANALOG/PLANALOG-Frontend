package com.example.planalog.ui.comment.com.example.planalog.utils

import com.example.planalog.ui.comment.com.example.planalog.ui.home.calender.CalendarDay
import java.util.Calendar

fun generateCalendarDays(year: Int, month: Int): List<CalendarDay> {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month - 1) // Calendar.MONTH는 0부터 시작
    calendar.set(Calendar.DAY_OF_MONTH, 1)

    val days = mutableListOf<CalendarDay>()

    // 달의 첫 번째 날의 요일 (0: 일요일)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1

    // 빈 날짜 추가 (달력의 첫 번째 줄을 맞추기 위해)
    for (i in 0 until firstDayOfWeek) {
        days.add(CalendarDay("", isEmpty = true)) // 빈 날짜 추가
    }

    // 해당 월의 모든 날짜 추가
    val maxDayInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    for (day in 1..maxDayInMonth) {
        val formattedDate = String.format("%04d-%02d-%02d", year, month, day)
        days.add(CalendarDay(formattedDate, isEmpty = false)) // 실제 날짜 추가
    }

    return days
}
