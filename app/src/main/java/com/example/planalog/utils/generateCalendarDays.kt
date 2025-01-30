package com.example.planalog.ui.comment.com.example.planalog.utils

import com.example.planalog.ui.comment.com.example.planalog.ui.home.calender.CalendarDay
import java.util.Calendar

fun generateCalendarDays(year: Int, month: Int): List<CalendarDay> {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month - 1) // Calendar.MONTH는 0부터 시작하므로 1을 빼서 전달
    calendar.set(Calendar.DAY_OF_MONTH, 1) // 해당 월의 첫 번째 날짜 설정

    val days = mutableListOf<CalendarDay>()

    // 첫 번째 날의 요일 (1: 일요일, 2: 월요일, ... 7: 토요일)
    val startDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

    // 해당 월의 날짜 추가
    val maxDayInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    for (day in 1..maxDayInMonth) {
        days.add(CalendarDay(day.toString(), isEmpty = false))
    }

    // 공백 추가 (1일이 시작되는 요일 앞의 공백)
//    for (i in 1 until startDayOfWeek) {
//        days.add(CalendarDay("", false)) // 빈 날짜 (공백)
//    }
//
//    // 해당 월의 날짜 추가
//    for (day in 1..maxDayInMonth) {
//        days.add(CalendarDay(day.toString(), false))
//    }

    return days
}
