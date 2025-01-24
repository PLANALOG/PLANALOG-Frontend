package com.example.planalog.ui.home.ctgy

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
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

        // 더보기 버튼 클릭 시 팝업 메뉴 띄우기
        holder.binding.homePlannerOptionBtn.setOnClickListener { view ->
            showPopupMenu(view, holder, position, isCategory = true)
        }

        // 체크리스트 추가 버튼 클릭 리스너
        holder.binding.homePlannerCtgyPlusIc.setOnClickListener {
            category.checklists.add(ChecklistItem(""))
            notifyItemChanged(holder.adapterPosition)
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

            checklistBinding.homePlannerMemoEt.addTextChangedListener {
                checklistItem.task = it.toString()
                onPlannerChanged()
            }

            holder.binding.homePlannerCtgyCl.addView(checklistBinding.root)
        }
    }

    private fun showPopupMenu(anchor: View, holder: CategoryViewHolder, position: Int, isCategory: Boolean, checklistIndex: Int? = null) {
        val inflater = LayoutInflater.from(anchor.context)
        // 팝업 레이아웃 바인딩
        val popupBinding = HomePlannerPopupMenuBinding.inflate(inflater, null, false)

        val popupWindow = PopupWindow(
            popupBinding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // 저장 버튼 클릭 시
//        popupBinding.saveButton.setOnClickListener {
//            if (isCategory) {
//                // 카테고리 제목 저장
//                val updatedTitle = holder.binding.homeCtgyEt.text.toString()
//                categories[position].title = updatedTitle
//                holder.binding.homeCtgyEt.apply {
//                    isEnabled = false  // 카테고리 텍스트를 수정 불가능하게 설정
//                    isFocusable = false  // 포커스 불가능
//                    isFocusableInTouchMode = false  // 터치모드에서 포커스 불가능
//                }
//            } else {
//                // 체크리스트 내용 저장
//                val checklistBinding = HomePlannerMemoItemBinding.bind(holder.binding.homePlannerCtgyCl.getChildAt(checklistIndex!!) as View)
//                val updatedTask = checklistBinding.homePlannerMemoEt.text.toString() // 바인딩을 통해 EditText의 텍스트 가져오기
//
//                // 텍스트를 수정된 값으로 업데이트
//                categories[position].checklists[checklistIndex].apply {
//                    task = updatedTask
//                    isEditable = false  // isEditable 상태를 false로 설정
//                }
//
//
//                // 수정 불가능하게 설정
//                checklistBinding.homePlannerMemoEt.apply {
//                    isEnabled = false  // 수정 불가
//                    isFocusable = false  // 포커스 불가
//                    isFocusableInTouchMode = false  // 터치모드에서 포커스 불가
//                }
//            }
//            popupWindow.dismiss()
//        }

        // 삭제 버튼 클릭 시
        popupBinding.deleteButton.setOnClickListener {
            if (isCategory) {
                // 카테고리 삭제
                categories.removeAt(position)
                notifyItemRemoved(position)  // 어댑터에 삭제된 항목 알림
                notifyItemRangeChanged(position, categories.size) // 남은 아이템의 위치 갱신
            } else {
                // 체크리스트 항목 삭제
                categories[position].checklists.removeAt(checklistIndex!!)
                notifyItemChanged(holder.adapterPosition)
            }

            popupWindow.dismiss()
        }

        // 팝업 위치 설정 (옵션 아이콘 왼쪽)
        val offsetX = -(anchor.width + 80) // 옵션 아이콘의 왼쪽에 위치하도록 X좌표 오프셋
        popupWindow.showAsDropDown(anchor, offsetX, -150) // 옵션 아이콘 바로 왼쪽에 위치하도록 설정
    }
}