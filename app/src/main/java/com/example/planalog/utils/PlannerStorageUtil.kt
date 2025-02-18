import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.example.planalog.ui.home.ctgy.Category
import com.example.planalog.ui.home.memo.ChecklistItem
import com.example.planalog.utils.generateRandomColor
import com.example.planalog.utils.getCurrentDate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private const val PREF_NAME = "planner_prefs"
private const val CATEGORY_PLANNER_KEY = "planner_category_data"
private const val MEMO_PLANNER_KEY = "planner_memo_data"
private const val DATE_KEY = "last_saved_date"

//  현재 플래너 상태 저장
fun savePlannerState(context: Context, type: String, categories: List<Category>, checklist: List<ChecklistItem>) {
    val sharedPreferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val gson = Gson()

    if (type == "memo_user") {
        val checklistJson = gson.toJson(checklist)
        editor.putString(MEMO_PLANNER_KEY, checklistJson) // 메모형 데이터 저장
        Log.d("PlannerState", "[메모형] 플래너 상태 저장 완료: ${checklist.size}개")
    } else {
        val categoryJson = gson.toJson(categories)
        editor.putString(CATEGORY_PLANNER_KEY, categoryJson) // 카테고리형 데이터 저장
        Log.d("PlannerState", "[카테고리형] 플래너 상태 저장 완료: ${categories.size}개")
    }

    editor.apply()
}

//  저장된 플래너 상태 불러오기
fun loadPlannerState(context: Context, type: String): Pair<List<Category>, List<ChecklistItem>> {
    val sharedPreferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
    val gson = Gson()

    return if (type == "memo_user") {
        val checklistJson = sharedPreferences.getString(MEMO_PLANNER_KEY, null)
        val checklistList: List<ChecklistItem> = if (checklistJson != null) {
            val typeToken = object : TypeToken<List<ChecklistItem>>() {}.type
            gson.fromJson(checklistJson, typeToken) ?: emptyList()
        } else emptyList()

        Log.d("PlannerState", "[메모형] 플래너 데이터 불러오기 완료: ${checklistList.size}개")
        Pair(emptyList(), checklistList) // 카테고리는 빈 리스트 반환
    } else {
        val categoryJson = sharedPreferences.getString(CATEGORY_PLANNER_KEY, null)
        val categoryList: List<Category> = if (categoryJson != null) {
            val typeToken = object : TypeToken<List<Category>>() {}.type
            gson.fromJson(categoryJson, typeToken) ?: emptyList()
        } else emptyList()

        Log.d("PlannerState", "[카테고리형] 플래너 데이터 불러오기 완료: ${categoryList.size}개")
        Pair(categoryList, emptyList()) // 체크리스트는 빈 리스트 반환
    }
}

fun saveLastSavedDate(context: Context, type: String, date: String) {
    val sharedPreferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    if (type == "memo_user") {
        editor.putString("memo_last_saved_date", date) // ✅ 메모형 마지막 저장 날짜 저장
    } else {
        editor.putString("category_last_saved_date", date) // ✅ 카테고리형 마지막 저장 날짜 저장
    }

    editor.apply()
    Log.d("PlannerState", "📌 [${type}] 마지막 저장 날짜 업데이트: $date")
}

//  마지막 저장된 날짜 불러오기
fun loadLastSavedDate(context: Context, type: String): String {
    val sharedPreferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)

    val lastSavedDate = if (type == "memo_user") {
        sharedPreferences.getString("memo_last_saved_date", null)
    } else {
        sharedPreferences.getString("category_last_saved_date", null)
    }

    if (lastSavedDate.isNullOrEmpty()) {
        Log.e("PlannerState", " [${type}] 저장된 날짜 없음! 기본값 반환")
        return "2025-02-01" // 기본값
    }

    Log.d("PlannerState", " [${type}] 마지막 저장 날짜: $lastSavedDate")
    return lastSavedDate
}