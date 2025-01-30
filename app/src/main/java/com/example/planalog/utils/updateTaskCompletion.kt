package com.example.planalog.utils

import android.content.Context

fun updateTaskCompletion(context: Context, date: String, isCompleted: Boolean) {
    val sharedPrefs = context.getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
    val completedDates = sharedPrefs.getStringSet("completed_dates", mutableSetOf()) ?: mutableSetOf()

    if (isCompleted) {
        completedDates.add(date)
    } else {
        completedDates.remove(date)
    }

    sharedPrefs.edit()
        .putStringSet("completed_dates", completedDates)
        .apply()
}
