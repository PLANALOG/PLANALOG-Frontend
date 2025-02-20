package com.example.planalog.ui.comment.com.example.planalog.ui.home.calender

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.planalog.R
import com.example.planalog.databinding.ItemCalendarDayBinding
import com.example.planalog.databinding.ItemCalendarHeaderBinding

class CalendarAdapter(
    private val days: MutableList<CalendarDay>,
    private val context : Context,
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
                    Log.d("Calendar", "day.isEmpty: ${day.isEmpty}")
                } else {
                    binding.calenderDayTv.text = day.getDayOfMonth() // 날짜를 설정

                    //  모든 날짜에서 아이콘을 표시하도록 설정
                    binding.calenderCircleView.visibility = View.VISIBLE

                    binding.calenderCircleView.setBackgroundResource(
                        if (day.isTaskCompleted) R.drawable.circle_checked else R.drawable.circle_unchecked
                    ) // 완료된 작업 여부에 따른 원 모양의 표시
                    Log.d("CalendarAdapter", "날짜: ${day.date}, hasTask: ${day.hasTask}, day.isEmpty: ${day.isEmpty}, day.isTaskComplete: ${day.isTaskCompleted}")
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

                // 저장된 완료 상태 불러오기
                calendarDay.isTaskCompleted = getTaskCompletionState(calendarDay.date)

                holder.binding.calenderCircleView.visibility = View.VISIBLE

                holder.bind(calendarDay) // 날짜를 bind

                // ✅ 플래너가 있는 날짜만 동그라미 표시
                if (calendarDay.hasTask) {
                    holder.binding.calenderCircleView.visibility = View.VISIBLE
                    holder.binding.calenderCircleView.setBackgroundResource(
                        if (calendarDay.isTaskCompleted) R.drawable.circle_checked
                        else R.drawable.circle_unchecked
                    )
                } else {
                    holder.binding.calenderCircleView.visibility = View.INVISIBLE
                }
                holder.itemView.setOnClickListener { onDayClicked(calendarDay) } // 클릭 시 처리

                Log.d(
                    "CalendarAdapter",
                    "날짜: ${calendarDay.date}, hasTask: ${calendarDay.hasTask}, isTaskCompleted: ${calendarDay.isTaskCompleted}"
                )
            }
        }
    }

    override fun getItemCount(): Int {
        // 요일 7개 + 날짜 수
        return 7 + days.size
    }

    private fun getTaskCompletionState(date: String): Boolean {
        val sharedPreferences = context.getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
        val savedCompletedDates = sharedPreferences.getStringSet("completed_dates", emptySet()) ?: emptySet()

        return savedCompletedDates.contains(date.trim()) // 해당 날짜가 저장된 완료 목록에 있는지 확인
    }

}