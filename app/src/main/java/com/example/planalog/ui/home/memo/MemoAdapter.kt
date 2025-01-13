package com.example.planalog.ui.home.ctgy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.example.planalog.databinding.HomePlannerPopupMenuBinding
import com.example.planalog.databinding.HomePlannerMemoItemBinding
import com.example.planalog.ui.home.memo.ChecklistItem

class MemoAdapter(private val checklists: MutableList<ChecklistItem>):
    RecyclerView.Adapter<MemoAdapter.MemoViewHolder>(){

    inner class MemoViewHolder(val binding: HomePlannerMemoItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val binding = HomePlannerMemoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemoViewHolder(binding)
    }

    override fun getItemCount(): Int = checklists.size

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        val memo = checklists[position]

        // 카테고리 제목 및 색상 설정
        holder.binding.homePlannerMemoEt.setText(memo.task)

        // Enable or disable EditText based on saved state
        if (memo.isEditable) {
            holder.binding.homePlannerMemoEt.apply {
                isEnabled = true
                isFocusable = true
                isFocusableInTouchMode = true
            }
        } else {
            holder.binding.homePlannerMemoEt.apply {
                isEnabled = false
                isFocusable = false
                isFocusableInTouchMode = false
            }
        }

        // 더보기 버튼 클릭 시 팝업 메뉴 띄우기
        holder.binding.homePlannerOptionBtn.setOnClickListener { view ->
            showPopupMenu(view, holder, position)
        }
    }

    private fun showPopupMenu(anchor: View, holder: MemoViewHolder, position: Int) {
        val inflater = LayoutInflater.from(anchor.context)
        // 팝업 레이아웃 바인딩
        val popupBinding = HomePlannerPopupMenuBinding.inflate(inflater, null, false)

        val popupWindow = PopupWindow(
            popupBinding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // 저장 버튼 클릭 리스너
        popupBinding.saveButton.setOnClickListener {

            val updatedTask = holder.binding.homePlannerMemoEt.text.toString()

            checklists[position].apply {
                task = updatedTask
                isEditable = false  // isEditable 상태를 false로 설정
            }

            holder.binding.homePlannerMemoEt.apply {
                isEnabled = false
                isFocusable = false
                isFocusableInTouchMode = false
            }
            popupWindow.dismiss()
        }

        // 삭제 버튼 클릭 리스너
        popupBinding.deleteButton.setOnClickListener {
            checklists.removeAt(position)
            notifyItemRemoved(position)  // 어댑터에 삭제된 항목 알림
            notifyItemRangeChanged(position, checklists.size) // 남은 아이템의 위치 갱신
            popupWindow.dismiss()
        }

        // 팝업 창 위치 설정
        val offsetX = -(anchor.width + 80) // 옵션 아이콘의 왼쪽에 위치하도록 X좌표 오프셋
        popupWindow.showAsDropDown(anchor, offsetX, -150) // 옵션 아이콘 바로 왼쪽에 위치하도록 설정
    }
}