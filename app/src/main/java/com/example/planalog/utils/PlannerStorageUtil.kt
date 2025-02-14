package com.example.planalog.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.planalog.ui.home.ctgy.Category
import com.example.planalog.ui.home.memo.ChecklistItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// SharedPreferences 파일 이름 및 키 정의
private const val PREFS_NAME = "planner_prefs"
private const val KEY_CATEGORIES = "categories"
private const val KEY_TASKS = "tasks"
private const val KEY_LAST_SAVED_DATE = "last_saved_date"

// 🔹 카테고리 저장 함수
fun saveCategoriesToPreferences(context: Context, categories: List<Category>) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    val json = Gson().toJson(categories) // JSON으로 변환
    editor.putString(KEY_CATEGORIES, json)
    editor.apply()

    Log.d("PlannerStorageUtil", "✅ 카테고리 저장 완료 (${categories.size}개)")
}

// 🔹 체크리스트 저장 함수
fun saveChecklistToPreferences(context: Context, checklist: List<ChecklistItem>) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    val json = Gson().toJson(checklist) // JSON으로 변환
    editor.putString(KEY_TASKS, json)
    editor.apply()

    Log.d("PlannerStorageUtil", "✅ 체크리스트 저장 완료 (${checklist.size}개)")
}

// 🔹 카테고리 불러오기 함수
fun loadCategoriesFromPreferences(context: Context): MutableList<Category> {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val json = sharedPreferences.getString(KEY_CATEGORIES, null) ?: return mutableListOf()

    val type = object : TypeToken<MutableList<Category>>() {}.type
    return Gson().fromJson(json, type)
}

// 🔹 체크리스트 불러오기 함수
fun loadChecklistFromPreferences(context: Context): MutableList<ChecklistItem> {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val json = sharedPreferences.getString(KEY_TASKS, null) ?: return mutableListOf()

    val type = object : TypeToken<MutableList<ChecklistItem>>() {}.type
    return Gson().fromJson(json, type)
}

// 🔹 마지막 저장된 날짜 가져오기
fun getLastSavedDate(context: Context): String {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return sharedPreferences.getString(KEY_LAST_SAVED_DATE, "") ?: ""
}

// 🔹 새로운 날짜 저장
fun saveLastSavedDate(context: Context, date: String) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    sharedPreferences.edit().putString(KEY_LAST_SAVED_DATE, date).apply()
}

fun resetDataIfDateChanged(context: Context, categories: MutableList<Category>, checklist: MutableList<ChecklistItem>) {
    val currentDate = getCurrentDate()
    val lastSavedDate = getLastSavedDate(context)

    if (currentDate != lastSavedDate) {
        Log.d("PlannerStorageUtil", "📅 날짜 변경 감지됨! 기존 데이터를 초기 상태로 되돌립니다.")

        // ✅ 기존 데이터를 초기 상태로 변경 (삭제가 아니라 기본 값 설정)
        categories.clear()
        checklist.clear()

        // ✅ 초기 카테고리 추가
        addInitialCategory(categories)

        // ✅ 초기 상태 유지
        initializeInitialCtgyState(categories)

        // 변경 사항 저장
        saveCategoriesToPreferences(context, categories)
        saveChecklistToPreferences(context, checklist)
        saveLastSavedDate(context, currentDate)
    } else {
        Log.d("PlannerStorageUtil", "📅 같은 날짜 유지됨, 기존 데이터 유지.")
    }
}

// 🔹 초기 카테고리 추가 함수
private fun addInitialCategory(categories: MutableList<Category>) {
    if (categories.isEmpty()) {
        categories.add(Category("", mutableListOf(), generateRandomColor()))
        Log.d("PlannerStorageUtil", "✅ 기본 카테고리 추가 완료")
    }
}

// 🔹 초기 상태 설정 함수
private fun initializeInitialCtgyState(categories: MutableList<Category>) {
    categories.forEach { category ->
        category.isEditable = true  // 카테고리 제목 수정 가능
        category.checklists.forEach { checklist ->
            checklist.isEditable = false  // 체크리스트 수정 불가능
        }
    }
}
