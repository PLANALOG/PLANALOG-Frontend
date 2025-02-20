package com.example.planalog.ui.home.ctgy

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.planalog.R
import com.example.planalog.databinding.HomePlannerMemoItemBinding
import com.example.planalog.network.api.TaskApiHelper
import com.example.planalog.ui.home.HomeFragment
import com.example.planalog.ui.home.memo.ChecklistItem
import com.example.planalog.utils.getCurrentDate
import com.example.planalog.utils.updateTaskCompletion

class MemoAdapter(
    private val context: Context,  // 추가된 context
    private val checklists: MutableList<ChecklistItem>,
    private val onMemoChanged: () -> Unit,
    private val onDeleteStateChanged: (Boolean) -> Unit,
    private val onAllChecked: (Boolean) -> Unit // 모든 체크 확인 콜백 추가
): RecyclerView.Adapter<MemoAdapter.MemoViewHolder>(){

    private var isDeleteMode = false // DELETE 모드 상태
    private val taskApiHelper = TaskApiHelper(context)

    inner class MemoViewHolder(val binding: HomePlannerMemoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var textWatcher: TextWatcher? = null // TextWatcher를 저장
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val binding = HomePlannerMemoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemoViewHolder(binding)
    }

    override fun getItemCount(): Int = checklists.size

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        val memo = checklists[position]

        Log.d("MemoAdapter", "현재 항목 - Task: ${memo.task}, Task ID: ${memo.taskId}, isSelected: ${memo.isSelected}, isChecked: ${memo.isChecked}")

        // 기존 체크박스 리스너를 제거
        holder.binding.homePlannerMemoCb.setOnCheckedChangeListener(null)

        // 기존 TextWatcher 제거
        holder.textWatcher?.let {
            holder.binding.homePlannerMemoEt.removeTextChangedListener(it)
        }

        // 메모 초기 설정
        holder.binding.homePlannerMemoEt.setText(memo.task)
        holder.binding.homePlannerMemoEt.isEnabled = memo.isEditable
        holder.binding.homePlannerMemoSelectBtn.isSelected = memo.isSelected

        // 저장된 체크 상태 불러오기
        memo.isChecked = memo.taskId?.let { getTaskCheckedState(it) } ?: false
        holder.binding.homePlannerMemoCb.isChecked = memo.isChecked

        // 부분 삭제 아이콘 설정
        val iconRes = if (memo.isSelected) R.drawable.ic_ctgy_delete_selected else R.drawable.ic_ctgy_delete_unselected
        holder.binding.homePlannerMemoSelectBtn.setImageResource(iconRes)
        holder.binding.homePlannerMemoSelectBtn.visibility = if (isDeleteMode) View.VISIBLE else View.GONE

        // 체크박스 클릭 시 API 호출
        holder.binding.homePlannerMemoCb.setOnCheckedChangeListener { _, isChecked ->
            memo.isChecked = isChecked
            checkCompletionState()

            // 체크 상태 저장
            memo.taskId?.let { saveTaskCheckedState(it, isChecked) }

            // 모든 항목이 완료되었는지 확인 후 날짜 저장
            val allCompleted = checklists.all { it.isChecked }
            saveTaskCompletionState(getCurrentDate(), allCompleted)

            // API 호출 - 서버에도 완료 상태 동기화
            memo.taskId?.let { taskId ->
                taskApiHelper.toggleTaskComplete(
                    listOf(taskId),
                    onSuccess = {
                        Log.d("MemoAdapter", "할 일 완료 여부 수정 성공: $taskId")
                    },
                    onFailure = { errorMessage ->
                        Log.e("MemoAdapter", "할 일 완료 여부 수정 실패: $errorMessage")
                        memo.isChecked = !isChecked  // API 실패 시 원래 상태로 복구
                        holder.binding.homePlannerMemoCb.isChecked = !isChecked
                    }
                )
            }
        }

        // 부분 삭제 아이콘 클릭 리스너
        holder.binding.homePlannerMemoSelectBtn.setOnClickListener {
            memo.isSelected = !memo.isSelected // 선택 상태 토글
            notifyItemChanged(position) // UI 업데이트
            onMemoChanged() // SAVE 버튼 활성화 상태 갱신
            onDeleteStateChanged(checklists.any { it.isSelected })
            Log.d("MemoAdapter", "선택 상태 변경 - Task ID: ${memo.taskId}, isSelected: ${memo.isSelected}")
        }

        // 새 TextWatcher 등록
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                memo.task = s.toString()
                Log.d("MemoAdapter", "Task 입력 감지됨: ${memo.task}")
                onMemoChanged()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        holder.binding.homePlannerMemoEt.addTextChangedListener(textWatcher)

        // 현재 TextWatcher를 저장
        holder.textWatcher = textWatcher
    }

    fun getSelectedTaskIds(): List<Int> {
        val selectedTasks = checklists.filter { it.isSelected }
        val selectedIds = checklists.filter { it.isSelected && it.taskId != null }.map { it.taskId!! }

        checklists.forEach {
            Log.d("MemoAdapter", "Task: ${it.task}, Task ID: ${it.taskId}, isSelected: ${it.isSelected}")
        }

        selectedTasks.forEach {
            if (it.taskId == null) {
                Log.e("MemoAdapter", " 선택된 항목의 Task ID가 null입니다! Task: ${it.task}")
            }
        }

        Log.d("MemoAdapter", "선택된 Task ID 목록: $selectedIds")

        return selectedIds
    }



    // DELETE 모드 활성화/비활성화
    fun toggleDeleteMode(enabled: Boolean) {
        isDeleteMode = enabled
        notifyDataSetChanged()
    }

    // 선택된 항목 삭제
    fun deleteSelectedItems() {
        val iterator = checklists.iterator()

        while (iterator.hasNext()) {
            val memo = iterator.next()

            // 선택된 메모 삭제
            if (memo.isSelected) {
                iterator.remove()
            }
        }
        notifyDataSetChanged()

        // 선택 상태 초기화
        resetSelectionStates()
    }

    private fun resetSelectionStates() {
        checklists.forEach { memo ->
            memo.isSelected = false
        }
        notifyDataSetChanged()

        onDeleteStateChanged(checklists.any { it.isSelected })
    }

    fun hasSelectedItems(): Boolean {
        return checklists.any { it.isSelected }
    }

    // 체크 상태 업데이트 및 콜백 호출
    // MemoAdapter 클래스 안에 있는 함수
    private fun checkCompletionState() {
        val allChecked = checklists.all { it.isChecked } // 모든 항목이 체크되었는지 확인

        // SharedPreferences에 현재 날짜와 상태 저장
        val sharedPreferences = context.getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
        val savedDates = sharedPreferences.getStringSet("saved_dates", mutableSetOf()) ?: mutableSetOf()
        val currentDate = getCurrentDate()

        if (allChecked) {
            savedDates.add(currentDate) // 모든 항목이 체크되었으면 날짜 저장
        } else {
            savedDates.remove(currentDate) // 하나라도 체크 해제되면 날짜 제거
        }

        sharedPreferences.edit().putStringSet("saved_dates", savedDates).apply()

        // onAllChecked 콜백 호출 (프래그먼트로 알림)
        onAllChecked(allChecked)
    }

    private fun saveTaskCheckedState(taskId: Int, isChecked: Boolean) {
        val sharedPreferences = context.getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("task_checked_$taskId", isChecked)
        editor.apply()
    }

    private fun getTaskCheckedState(taskId: Int): Boolean {
        val sharedPreferences = context.getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("task_checked_$taskId", false) // 기본값 false
    }

    private fun saveTaskCompletionState(date: String, isCompleted: Boolean) {
        val sharedPreferences = context.getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
        val savedDates = sharedPreferences.getStringSet("completed_dates", mutableSetOf()) ?: mutableSetOf()

        if (isCompleted) {
            savedDates.add(date) // 모든 할 일이 완료되면 날짜 저장
        } else {
            savedDates.remove(date) // 하나라도 미완료면 제거
        }

        sharedPreferences.edit().putStringSet("completed_dates", savedDates).apply()
    }

}