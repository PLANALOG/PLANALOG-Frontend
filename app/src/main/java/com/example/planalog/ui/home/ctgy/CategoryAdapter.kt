package com.example.planalog.ui.home.ctgy

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.example.planalog.R
import com.example.planalog.databinding.HomePlannerCtgyItemBinding
import com.example.planalog.databinding.HomePlannerMemoItemBinding
import com.example.planalog.ui.home.memo.ChecklistItem

class CategoryAdapter(
    private val categories: MutableList<Category>,
    private val onPlannerChanged: () -> Unit, // 변경사항 발생 시 호출될 콜백 함수
    private val onDeleteStateChanged: (Boolean) -> Unit,
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var isDeleteMode = false // DELETE 모드 상태

    inner class CategoryViewHolder(val binding: HomePlannerCtgyItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = HomePlannerCtgyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun getItemCount(): Int = categories.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]

        // 기존 TextWatcher 제거 및 설정 유지
        holder.binding.homeCtgyEt.removeTextChangedListener(holder.binding.homeCtgyEt.tag as? TextWatcher)
        holder.binding.homeCtgyEt.setText(category.title)
        holder.binding.homeCtgyEt.setTextColor(category.color)
        holder.binding.homeCtgyEt.isEnabled = category.isEditable
        holder.binding.homePlannerCtgySelectBtn.visibility = if (isDeleteMode) View.VISIBLE else View.GONE


        // 새로운 TextWatcher 등록
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (category.isEditable) {
                    category.title = s.toString()
                    onPlannerChanged()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        holder.binding.homeCtgyEt.addTextChangedListener(textWatcher)

        // TextWatcher를 태그로 저장하여 다음에 제거할 수 있도록 함
        holder.binding.homeCtgyEt.tag = textWatcher

        // 데이터와 동기화
        holder.binding.homeCtgyEt.setText(category.title)
        holder.binding.homeCtgyEt.isEnabled = category.isEditable

        // 체크리스트 추가 버튼 클릭 리스너
        holder.binding.homePlannerCtgyPlusIc.setOnClickListener {
            category.checklists.add(ChecklistItem(""))
            notifyItemChanged(holder.adapterPosition) // 변경된 항목만 갱신
            onPlannerChanged()
        }

        // 부분 삭제 아이콘 설정
        val iconRes = if (category.isSelected) R.drawable.ic_ctgy_delete_selected else R.drawable.ic_ctgy_delete_unselected
        holder.binding.homePlannerCtgySelectBtn.setImageResource(iconRes)
        holder.binding.homePlannerCtgySelectBtn.visibility = if (isDeleteMode) View.VISIBLE else View.GONE

        // 부분 삭제 아이콘 클릭 리스너
        holder.binding.homePlannerCtgySelectBtn.setOnClickListener {
            category.isSelected = !category.isSelected // 선택 상태 토글
            notifyItemChanged(position) // UI 업데이트
            onDeleteStateChanged(categories.any { it.isSelected || it.checklists.any { checklist -> checklist.isSelected } })
            onPlannerChanged() // SAVE 버튼 활성화 상태 갱신
        }

        // 체크리스트 렌더링
        holder.binding.homePlannerCtgyCl.removeAllViews()
        category.checklists.forEachIndexed { _, checklistItem ->
            val checklistBinding = HomePlannerMemoItemBinding.inflate(
                LayoutInflater.from(holder.binding.root.context),
                holder.binding.homePlannerCtgyCl,
                false
            )

            checklistBinding.homePlannerMemoEt.setText(checklistItem.task)
//            checklistBinding.homePlannerMemoCb.isChecked = checklistItem.isChecked
            checklistBinding.homePlannerMemoEt.isEnabled = checklistItem.isEditable

            // 선택 아이콘 상태 관리
            checklistBinding.homePlannerMemoSelectBtn.visibility = if (isDeleteMode) View.VISIBLE else View.GONE
            val checklistIconRes = if (checklistItem.isSelected) R.drawable.ic_ctgy_delete_selected else R.drawable.ic_ctgy_delete_unselected
            checklistBinding.homePlannerMemoSelectBtn.setImageResource(checklistIconRes)

            checklistBinding.homePlannerMemoSelectBtn.setOnClickListener {
                checklistItem.isSelected = !checklistItem.isSelected
                notifyDataSetChanged()
                onDeleteStateChanged(categories.any { it.isSelected || it.checklists.any { it.isSelected } })
                onPlannerChanged()
            }

            // 메모 텍스트 변경 리스너
            checklistBinding.homePlannerMemoEt.addTextChangedListener {
                checklistItem.task = it.toString()
                onPlannerChanged()
            }

            holder.binding.homePlannerCtgyCl.addView(checklistBinding.root)
        }
    }

    // DELETE 모드 활성화/비활성화
    fun toggleDeleteMode(enabled: Boolean) {
        isDeleteMode = enabled
        notifyDataSetChanged()
    }

    // 선택된 항목 삭제
    fun deleteSelectedItems() {
        val iterator = categories.iterator()

        while (iterator.hasNext()) {
            val category = iterator.next()

            // 선택된 체크리스트 삭제
            val checklistIterator = category.checklists.iterator()
            while (checklistIterator.hasNext()) {
                val checklistItem = checklistIterator.next()
                if (checklistItem.isSelected) {
                    checklistIterator.remove() // 체크리스트 항목 삭제
                }
            }

            // 선택된 카테고리 삭제
            if (category.isSelected) {
                iterator.remove()
            }
        }
        notifyDataSetChanged()

        // 선택 상태 초기화
        resetSelectionStates()
    }

    private fun resetSelectionStates() {
        categories.forEach { category ->
            category.isSelected = false
            category.checklists.forEach { it.isSelected = false }
        }
        notifyDataSetChanged()
    }

    fun hasSelectedItems(): Boolean {
        for (category in categories) {
            if (category.isSelected) return true
            for (checklistItem in category.checklists) {
                if (checklistItem.isSelected) return true
            }
        }
        return false
    }
}