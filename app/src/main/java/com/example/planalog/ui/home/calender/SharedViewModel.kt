package com.example.planalog.ui.home.calender

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.planalog.ui.comment.com.example.planalog.ui.home.calender.CalendarDay

class SharedViewModel : ViewModel() {
    val calendarDays: MutableLiveData<List<CalendarDay>> = MutableLiveData()
}
