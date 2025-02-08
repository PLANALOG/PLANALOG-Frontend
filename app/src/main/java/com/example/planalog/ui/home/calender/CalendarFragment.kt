package com.example.planalog.ui.comment.com.example.planalog.ui.home.calender

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.util.LocalePreferences.getFirstDayOfWeek
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.planalog.databinding.FragmentCalendarBinding
import com.example.planalog.ui.comment.com.example.planalog.utils.generateCalendarDays
import com.example.planalog.ui.comment.com.example.planalog.utils.showDropdownMenu
import com.example.planalog.ui.home.calender.SharedViewModel
import com.example.planalog.utils.getCurrentDate
import java.util.Calendar

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var calendarAdapter: CalendarAdapter
    private val calendarDays = mutableListOf<CalendarDay>()
    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private val minYear = 2024 // 최소 연도
    private val maxYear = 2025 // 최대 연도

    private val sharedViewModel: SharedViewModel by activityViewModels()

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

//        sharedViewModel.calendarDays.value = generateCalendarDays(currentYear, currentMonth)

        var currentYear = Calendar.getInstance().get(Calendar.YEAR)
        var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        sharedViewModel.calendarDays.value = generateCalendarDays(currentYear, currentMonth)

        sharedViewModel.calendarDays.observe(viewLifecycleOwner) { updatedDays ->
            calendarDays.clear()
            calendarDays.addAll(updatedDays)
            calendarAdapter.notifyDataSetChanged()
        }

        setupRecyclerView() // RecyclerView 설정

        // 현재 연월을 초기값으로 설정
        val initialYearMonth = "$currentYear. $currentMonth"
        binding.calendarDropdown.text = initialYearMonth
        updateCalendar(initialYearMonth)

//        initializeCalendar()

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

    override fun onResume() {
        super.onResume()
        // 화면에 복귀할 때 최신 데이터를 보장
        updateCalendar("$currentYear. $currentMonth")
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

        // SharedPreferences에서 저장된 날짜 불러오기
        val savedDates = requireContext()
            .getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
            .getStringSet("saved_dates", emptySet()) ?: emptySet()

        // 캘린더 날짜 생성
        calendarDays.clear()

        val daysInMonth = generateCalendarDays(year, formattedMonth.toInt())

        // 달의 시작 요일 확인 (1일의 요일 가져오기)
        val firstDayOfWeek = getFirstDayOfWeek(year, formattedMonth.toInt())

        // 빈 날짜 추가 (달의 시작 위치 조정)
        for (i in 0 until firstDayOfWeek) {
            calendarDays.add(CalendarDay("", isEmpty = true))
        }

        // 저장된 날짜 확인 후 해당 날짜에 표시 추가
        for (day in daysInMonth) {

            if (!day.isEmpty) { // 빈 날짜가 아닌 경우에만 추가
                day.isTaskCompleted = savedDates.contains(day.date.trim()) // 정확한 포맷 비교
                day.hasTask = savedDates.contains(day.date)
                calendarDays.add(day)
            }
        }

        // RecyclerView에 데이터 변경 알림
        calendarAdapter.notifyDataSetChanged()

        // 드롭다운에 날짜 표시
        binding.calendarDropdown.text = "$year. $formattedMonth"

        // 버튼 활성화/비활성화 처리
        handleButtonState()
    }

    private fun getFirstDayOfWeek(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1) // 월은 0부터 시작 (1월은 0)
        return calendar.get(Calendar.DAY_OF_WEEK) - 1 // 일요일=1 -> 0으로 변환
    }

//    private fun initializeCalendar() {
//        val updatedDays = generateCalendarDays(
//            Calendar.getInstance().get(Calendar.YEAR),
//            Calendar.getInstance().get(Calendar.MONTH) + 1
//        )
//
//        // 기존 ViewModel 값 병합
//        val existingDays = sharedViewModel.calendarDays.value ?: mutableListOf()
//
//        updatedDays.forEach { newDay ->
//            val existingDay = existingDays.find { it.date == newDay.date }
//            if (existingDay != null) {
//                // 기존 데이터가 있으면 상태 병합
//                newDay.hasTask = existingDay.hasTask
//                newDay.isTaskCompleted = existingDay.isTaskCompleted
//            }
//        }
//
//        sharedViewModel.calendarDays.value = updatedDays.toMutableList()
//
//        Log.d("InitializeCalendar", "초기화된 CalendarDays: ${updatedDays.map { "${it.date}: ${it.hasTask}" }}")
//    }




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