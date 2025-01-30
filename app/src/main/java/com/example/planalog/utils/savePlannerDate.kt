package com.example.planalog.utils

import android.content.Context

fun savePlannerDate(context: Context, date: String) {
    val sharedPrefs = context.getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
    val savedDates = sharedPrefs.getStringSet("saved_dates", mutableSetOf()) ?: mutableSetOf()

    savedDates.add(date)

    sharedPrefs.edit()
        .putStringSet("saved_dates", savedDates)
        .apply()
}