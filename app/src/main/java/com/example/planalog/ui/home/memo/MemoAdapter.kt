package com.example.planalog.ui.home.ctgy

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.example.planalog.databinding.HomePlannerPopupMenuBinding
import com.example.planalog.databinding.HomePlannerMemoItemBinding
import com.example.planalog.ui.home.memo.ChecklistItem

class MemoAdapter(
    private val checklists: MutableList<ChecklistItem>,
    private val onMemoChanged: () -> Unit
): RecyclerView.Adapter<MemoAdapter.MemoViewHolder>(){

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

        // EditText 상태 설정
        holder.binding.homePlannerMemoEt.isEnabled = memo.isEditable
        holder.binding.homePlannerMemoEt.isFocusable = memo.isEditable
        holder.binding.homePlannerMemoEt.isFocusableInTouchMode = memo.isEditable

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

    // 메모를 수정 불가능하게 고정하는 메서드
    fun lockAllMemos() {
        checklists.forEach { it.isEditable = false }
        notifyDataSetChanged()
        onMemoChanged() // 저장 버튼 비활성화를 위한 콜백 호출
    }
}