package com.example.planalog.ui.home.ctgy

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.example.planalog.R
import com.example.planalog.databinding.HomePlannerCtgyItemBinding
import com.example.planalog.databinding.HomePlannerPopupMenuBinding
import com.example.planalog.databinding.HomePlannerMemoItemBinding
import com.example.planalog.ui.home.HomeFragment
import com.example.planalog.ui.home.memo.ChecklistItem

class CategoryAdapter(
    private val categories: MutableList<Category>,
    private val onPlannerChanged: () -> Unit // 변경사항 발생 시 호출될 콜백 함수
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(val binding: HomePlannerCtgyItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = HomePlannerCtgyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun getItemCount(): Int = categories.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]

        // 카테고리 제목 및 색상 설정
        holder.binding.homeCtgyEt.setText(category.title)
        holder.binding.homeCtgyEt.setTextColor(category.color)

        // 카테고리 제목 수정 가능 여부 설정
        holder.binding.homeCtgyEt.isEnabled = category.isEditable
        holder.binding.homeCtgyEt.isFocusable = category.isEditable
        holder.binding.homeCtgyEt.isFocusableInTouchMode = category.isEditable

        // 카테고리 변경 리스너
        holder.binding.homeCtgyEt.addTextChangedListener {
            category.title = it.toString()
            onPlannerChanged() // 변경 사항 콜백 호출
        }

        // 제목 변경 이벤트 처리
        holder.binding.homeCtgyEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                category.title = s.toString() // 정확한 카테고리 데이터 업데이트
                onPlannerChanged()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 체크리스트 추가 버튼 클릭 리스너
        holder.binding.homePlannerCtgyPlusIc.setOnClickListener {
            category.checklists.add(ChecklistItem(""))
            notifyItemChanged(holder.adapterPosition)
        }

        // 카테고리 선택 상태에 따른 아이콘 설정
        val iconRes = if (category.isSelected) R.drawable.ic_ctgy_delete_selected else R.drawable.ic_ctgy_delete_unselected
        holder.binding.homePlannerSelectBtn.setImageResource(iconRes)

        // 카테고리 선택 아이콘 클릭 리스너
        holder.binding.homePlannerSelectBtn.setOnClickListener {
            category.isSelected = !category.isSelected // 선택 상태 토글
            notifyItemChanged(position) // 선택 상태 변경 시 UI 업데이트
            onPlannerChanged() // 변경 사항 콜백 호출
        }

        // 체크리스트 렌더링
        holder.binding.homePlannerCtgyCl.removeAllViews()
        category.checklists.forEachIndexed { checklistIndex, checklistItem ->
            val checklistBinding = HomePlannerMemoItemBinding.inflate(
                LayoutInflater.from(holder.binding.root.context),
                holder.binding.homePlannerCtgyCl,
                false
            )

            checklistBinding.homePlannerMemoEt.setText(checklistItem.task)
            checklistBinding.homePlannerMemoEt.isEnabled = checklistItem.isEditable

            // 체크박스의 체크 상태 설정
            checklistBinding.homePlannerMemoCb.isChecked = checklistItem.isChecked

            checklistBinding.homePlannerMemoEt.addTextChangedListener {
                checklistItem.task = it.toString()
                onPlannerChanged()
            }

            // 체크박스 클릭 리스너
            checklistBinding.homePlannerMemoCb.setOnCheckedChangeListener { _, isChecked ->
                checklistItem.isChecked = isChecked
                onPlannerChanged() // 변경 사항 콜백 호출
            }

            holder.binding.homePlannerCtgyCl.addView(checklistBinding.root)
        }
    }
}