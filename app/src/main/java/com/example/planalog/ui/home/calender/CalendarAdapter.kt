package com.example.planalog.ui.comment.com.example.planalog.ui.home.calender

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.planalog.R
import com.example.planalog.databinding.ItemCalendarDayBinding
import com.example.planalog.databinding.ItemCalendarHeaderBinding

class CalendarAdapter(
    private val days: MutableList<CalendarDay>,
    private val onDayClicked: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0 // 요일 헤더
        private const val VIEW_TYPE_DAY = 1    // 날짜
    }

    override fun getItemViewType(position: Int): Int {
        // 첫 번째 7개의 아이템은 요일 헤더, 나머지는 날짜
        return if (position < 7) VIEW_TYPE_HEADER else VIEW_TYPE_DAY
    }

    // ViewHolder의 기본 클래스를 정의합니다.
    sealed class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        class HeaderViewHolder(private val binding: ItemCalendarHeaderBinding) : CalendarViewHolder(binding.root) {
            fun bind(dayOfWeek: String) {
                binding.calendarMon.text = dayOfWeek // 요일을 설정
            }
        }

        class DayViewHolder(val binding: ItemCalendarDayBinding) : CalendarViewHolder(binding.root) {
            fun bind(day: CalendarDay) {
                if (day.isEmpty) {
                    binding.calenderDayTv.text = ""
                    binding.calenderCircleView.visibility = View.INVISIBLE // 빈 날짜에 대해 숨김
                } else {
                    binding.calenderDayTv.text = day.date // 날짜를 설정
                    binding.calenderCircleView.visibility = if (day.isTaskCompleted) View.VISIBLE else View.INVISIBLE
                    binding.calenderCircleView.setBackgroundResource(
                        if (day.isTaskCompleted) R.drawable.circle_checked else R.drawable.circle_unchecked
                    ) // 완료된 작업 여부에 따른 원 모양의 표시
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemCalendarHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                CalendarViewHolder.HeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemCalendarDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                CalendarViewHolder.DayViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        when (holder) {
            is CalendarViewHolder.HeaderViewHolder -> {
                val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
                holder.bind(daysOfWeek[position]) // 요일을 bind
            }
            is CalendarViewHolder.DayViewHolder -> {
                val calendarDay = days[position - 7] // 7을 빼는 이유는 첫 7개 항목은 헤더이기 때문
                holder.bind(calendarDay) // 날짜를 bind
                holder.itemView.setOnClickListener { onDayClicked(calendarDay) } // 클릭 시 처리

                // 메모가 있으면 CircleView 보이기
//                holder.binding.calenderCircleView.visibility = if (calendarDay.hasTask) View.VISIBLE else View.INVISIBLE
            }
        }
    }

    override fun getItemCount(): Int {
        // 요일 7개 + 날짜 수
        return 7 + days.size
    }
}