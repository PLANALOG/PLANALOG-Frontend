package com.example.planalog.ui.comment.com.example.planalog.ui.home.calender

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.planalog.databinding.FragmentCalendarBinding
import com.example.planalog.ui.comment.com.example.planalog.utils.generateCalendarDays
import com.example.planalog.ui.comment.com.example.planalog.utils.showDropdownMenu

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var calendarAdapter: CalendarAdapter
    private val calendarDays = mutableListOf<CalendarDay>()
    private var currentYear = 2025 // 초기 연도
    private var currentMonth = 1 // 초기 월
    private val minYear = 2024 // 최소 연도
    private val maxYear = 2025 // 최대 연도

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView() // RecyclerView 설정

        // 초기 캘린더 데이터 생성 및 표시
        val initialYearMonth = "$currentYear. $currentMonth"
        binding.calendarDropdown.text = initialYearMonth
        updateCalendar(initialYearMonth)

        binding.calendarDropdownBtn.setOnClickListener {
            showDropdownMenu(binding.calendarDropdown, dateList) { selectedItem ->
                // 선택된 아이템 처리
                binding.calendarDropdown.text = selectedItem
                updateCalendar(selectedItem) // 선택된 달의 캘린더 갱신
            }
        }

        // 이전 버튼 클릭 시
        binding.prevBtn.setOnClickListener {
            // 이전 달로 이동
            if (currentMonth == 1) {
                if (currentYear > minYear) {
                    currentYear -= 1
                    currentMonth = 12
                }
            } else {
                currentMonth -= 1
            }
            updateCalendar("$currentYear. $currentMonth")
        }

        // 다음 버튼 클릭 시
        binding.nextBtn.setOnClickListener {
            // 다음 달로 이동
            if (currentMonth == 12) {
                if (currentYear < maxYear) {
                    currentYear += 1
                    currentMonth = 1
                }
            } else {
                currentMonth += 1
            }
            updateCalendar("$currentYear. $currentMonth")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        binding.calendarRv.layoutManager = GridLayoutManager(requireContext(), 7)

        calendarAdapter = CalendarAdapter(calendarDays) { selectedDay ->
            selectedDay.isTaskCompleted = !selectedDay.isTaskCompleted
            val position = calendarDays.indexOf(selectedDay)
            calendarAdapter.notifyItemChanged(position) // 데이터 변경 후 효율적인 호출
        }

        binding.calendarRv.adapter = calendarAdapter
    }

    private fun updateCalendar(yearMonth: String) {
        // "2024. 01" 형태의 데이터를 처리
        val (year, month) = yearMonth.split(".").map { it.trim().toInt() }

        // 두 자릿수로 월을 포맷팅
        val formattedMonth = String.format("%02d", month)

        // 캘린더 날짜 생성
        calendarDays.clear()
        calendarDays.addAll(generateCalendarDays(year, formattedMonth.toInt())) // 새로운 날짜 리스트로 갱신

        // RecyclerView에 데이터 변경 알림
        calendarAdapter.notifyDataSetChanged()

        // 드롭다운에 날짜 표시
        binding.calendarDropdown.text = "$year. $formattedMonth"

        // 버튼 활성화/비활성화 처리
        handleButtonState()
    }

    private fun handleButtonState() {
        // 이전 버튼 비활성화 처리
        if (currentYear == minYear && currentMonth == 1) {
            binding.prevBtn.isEnabled = false
            binding.prevBtn.alpha = 0.5f
        } else {
            binding.prevBtn.isEnabled = true
            binding.prevBtn.alpha = 1f
        }

        // 다음 버튼 비활성화 처리
        if (currentYear == maxYear && currentMonth == 12) {
            binding.nextBtn.isEnabled = false
            binding.nextBtn.alpha = 0.5f
        } else {
            binding.nextBtn.isEnabled = true
            binding.nextBtn.alpha = 1f
        }
    }
}