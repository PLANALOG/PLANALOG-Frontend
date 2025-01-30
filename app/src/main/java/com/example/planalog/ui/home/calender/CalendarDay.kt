package com.example.planalog.ui.comment.com.example.planalog.ui.home.calender

data class CalendarDay(
    val date: String,
    var isTaskCompleted: Boolean = false,
    var isEmpty: Boolean = false, // 빈 날짜 여부 (월의 시작을 조정하기 위함)
    var hasTask: Boolean = false,
)
