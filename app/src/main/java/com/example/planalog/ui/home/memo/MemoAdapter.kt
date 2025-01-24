package com.example.planalog.ui.home.ctgy

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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

        // 체크박스 초기 설정
        holder.binding.homePlannerMemoCb.isChecked = memo.isChecked

        // 체크박스 변경 리스너
        holder.binding.homePlannerMemoCb.setOnCheckedChangeListener { _, isChecked ->
            memo.isChecked = isChecked
            onMemoChanged() // 변경 사항 콜백 호출
        }

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
    }
}