package com.example.planalog.ui.comment.com.example.planalog.ui.home.calender

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.planalog.databinding.ItemCalendarDropdownBinding

class DropdownAdapter(
    private val items: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<DropdownAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemCalendarDropdownBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            binding.dropdownTv.text = item
            binding.root.setOnClickListener {
                onItemClick(item) // 클릭 이벤트 전달
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCalendarDropdownBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}

