package com.example.planalog.ui.comment.com.example.planalog.ui.home.calender

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.planalog.databinding.FragmentCalendarBinding
import com.example.planalog.network.api.PlannerApiHelper
import com.example.planalog.ui.comment.com.example.planalog.utils.generateCalendarDays
import com.example.planalog.ui.comment.com.example.planalog.utils.showDropdownMenu
import com.example.planalog.ui.home.calender.SharedViewModel
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

        var currentYear = Calendar.getInstance().get(Calendar.YEAR)
        var  currentMonth = String.format("%02d", Calendar.getInstance().get(Calendar.MONTH) + 1)

        Log.d("CalendarFragment", "현재 년월: $currentYear-$currentMonth") // 2025-02 형태로 출력됨

        sharedViewModel.calendarDays.value = generateCalendarDays(currentYear, currentMonth.toInt())

        sharedViewModel.calendarDays.observe(viewLifecycleOwner) { updatedDays ->
            calendarDays.clear()
            calendarDays.addAll(updatedDays)
            calendarAdapter.notifyDataSetChanged()
        }

        setupRecyclerView() // RecyclerView 설정

        // 현재 연월을 초기값으로 설정
        val initialYearMonth = "$currentYear. $currentMonth"
        Log.d("CalendarFragment", "initialYearMonth : ${currentYear} ${currentMonth}")
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
            var monthInt = currentMonth.toInt()
            // 이전 달로 이동
            if (monthInt == 1) {
                if (currentYear > minYear) {
                    currentYear -= 1
                    monthInt = 12
                }
            } else {
                monthInt -= 1
            }
            //  다시 두 자리 String으로 변환
            currentMonth = String.format("%02d", monthInt)
            updateCalendar("$currentYear. $currentMonth")
        }

        // 다음 버튼 클릭 시
        binding.nextBtn.setOnClickListener {
            var monthInt = currentMonth.toInt()

            // 다음 달로 이동
            if (monthInt == 12) {
                if (currentYear < maxYear) {
                    currentYear += 1
                    monthInt = 1
                }
            } else {
                monthInt += 1
            }
            //  다시 두 자리 String으로 변환
            currentMonth = String.format("%02d", monthInt)
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

        calendarAdapter = CalendarAdapter(calendarDays, requireContext()) { selectedDay ->
            selectedDay.isTaskCompleted = !selectedDay.isTaskCompleted
            val position = calendarDays.indexOf(selectedDay)
            calendarAdapter.notifyItemChanged(position) // 데이터 변경 후 효율적인 호출
        }

        binding.calendarRv.adapter = calendarAdapter
    }

    private fun updateCalendar(yearMonth: String) {
        //  "YYYY. MM" 형식에서 year와 month 추출
        val parts = yearMonth.split(".").map { it.trim() }
        if (parts.size < 2) {
            Log.e("CalendarFragment", "올바르지 않은 날짜 형식: $yearMonth")
            return
        }

        val year = parts[0].toIntOrNull()
        val month = parts[1].toIntOrNull()

        if (year == null || month == null) {
            Log.e("CalendarFragment", "날짜 변환 오류: year=$year, month=$month")
            return
        }

        //  올바른 YYYY-MM 형식 적용
        val formattedMonth = String.format("%04d-%02d", year, month)

        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", "")

        Log.d("CalendarFragment", "API 요청: userId=$userId, month=$formattedMonth")

        //  플래너 API 호출하여 할 일이 있는 날짜 가져오기
        val plannerApiHelper = PlannerApiHelper(requireContext())
        plannerApiHelper.getCalendarPlanners(userId!!.toInt(), formattedMonth) { plannerSuccess ->
            if (plannerSuccess != null) {
                Log.d("CalendarFragment", "API 응답 수신 성공! 플래너 개수: ${plannerSuccess.planners.size}")

                val savedTaskDates = plannerSuccess.planners.map { it.plannerDate.trim() }.toSet()

                calendarDays.clear()
                val daysInMonth = generateCalendarDays(year, month)
                val firstDayOfWeek = getFirstDayOfWeek(year, month)

                for (i in 0 until firstDayOfWeek) {
                    calendarDays.add(CalendarDay("", isEmpty = true))
                }

                for (day in daysInMonth) {
                    if (!day.isEmpty) {
                        val dayFormatted = day.date.trim()
                        day.hasTask = savedTaskDates.contains(dayFormatted)
                        day.isTaskCompleted = plannerSuccess.planners.any { it.plannerDate == dayFormatted && it.isCompleted }

                        Log.d("CalendarFragment", "날짜: ${day.date}, hasTask: ${day.hasTask}, isTaskCompleted: ${day.isTaskCompleted}")

                        calendarDays.add(day)
                    }
                }

                calendarAdapter.notifyDataSetChanged()
                //  드롭다운 값 업데이트 (YYYY. MM 형식 유지)
                binding.calendarDropdown.text = String.format("%04d. %02d", year, month)
            } else {
                Log.e("CalendarFragment", "플래너 API 응답이 null입니다.")
            }
        }
        handleButtonState()
    }


    private fun getFirstDayOfWeek(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1) // 월은 0부터 시작 (1월은 0)
        return calendar.get(Calendar.DAY_OF_WEEK) - 1 // 일요일=1 -> 0으로 변환
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