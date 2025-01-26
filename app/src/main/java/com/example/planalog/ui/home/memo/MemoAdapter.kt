package com.example.planalog.ui.home.ctgy

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.planalog.R
import com.example.planalog.databinding.HomePlannerMemoItemBinding
import com.example.planalog.ui.home.memo.ChecklistItem

class MemoAdapter(
    private val checklists: MutableList<ChecklistItem>,
    private val onMemoChanged: () -> Unit,
    private val onDeleteStateChanged: (Boolean) -> Unit,
): RecyclerView.Adapter<MemoAdapter.MemoViewHolder>(){

    private var isDeleteMode = false // DELETE 모드 상태

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

        // 기존 TextWatcher 제거
        holder.textWatcher?.let {
            holder.binding.homePlannerMemoEt.removeTextChangedListener(it)
        }

        // 메모 초기 설정
        holder.binding.homePlannerMemoEt.setText(memo.task)
        holder.binding.homePlannerMemoEt.isEnabled = memo.isEditable
        holder.binding.homePlannerMemoEt.isFocusable = memo.isEditable
        holder.binding.homePlannerMemoEt.isFocusableInTouchMode = memo.isEditable
        holder.binding.homePlannerMemoSelectBtn.isSelected = memo.isSelected

        // 부분 삭제 아이콘 설정
        val iconRes = if (memo.isSelected) R.drawable.ic_ctgy_delete_selected else R.drawable.ic_ctgy_delete_unselected
        holder.binding.homePlannerMemoSelectBtn.setImageResource(iconRes)
        holder.binding.homePlannerMemoSelectBtn.visibility = if (isDeleteMode) View.VISIBLE else View.GONE

        // 부분 삭제 아이콘 클릭 리스너
        holder.binding.homePlannerMemoSelectBtn.setOnClickListener {
            memo.isSelected = !memo.isSelected // 선택 상태 토글
            notifyItemChanged(position) // UI 업데이트
            onMemoChanged() // SAVE 버튼 활성화 상태 갱신
            onDeleteStateChanged(checklists.any { it.isSelected })
        }

        // 새 TextWatcher 등록
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                memo.task = s.toString()
                onMemoChanged()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        holder.binding.homePlannerMemoEt.addTextChangedListener(textWatcher)

        // 현재 TextWatcher를 저장
        holder.textWatcher = textWatcher
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
}