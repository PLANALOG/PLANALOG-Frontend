package com.example.planalog.ui.home

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planalog.R
import com.example.planalog.databinding.FragmentHomeBinding
import com.example.planalog.network.planner.PlannerService
import com.example.planalog.network.task.TaskService
import com.example.planalog.ui.comment.CommentFragment
import com.example.planalog.ui.comment.com.example.planalog.ui.home.calender.CalendarAdapter
import com.example.planalog.ui.comment.com.example.planalog.ui.home.calender.CalendarDay
import com.example.planalog.ui.comment.com.example.planalog.utils.generateCalendarDays
import com.example.planalog.ui.home.api.PlannerApiHelper
import com.example.planalog.ui.home.api.TaskApiHelper
import com.example.planalog.ui.home.calender.SharedViewModel
import com.example.planalog.ui.home.ctgy.Category
import com.example.planalog.ui.home.ctgy.CategoryAdapter
import com.example.planalog.ui.home.ctgy.MemoAdapter
import com.example.planalog.ui.home.memo.ChecklistItem
import com.example.planalog.utils.generateRandomColor
import com.example.planalog.utils.getCurrentDate
import com.example.planalog.utils.getCurrentMonth
import com.example.planalog.utils.savePlannerDate
import com.example.planalog.utils.updateTaskCompletion
import java.util.Calendar

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val checklist = mutableListOf<ChecklistItem>()
    private val categories = mutableListOf<Category>()
    private lateinit var ctgyAdapter : CategoryAdapter
    private lateinit var memoAdapter: MemoAdapter
    private lateinit var calendarAdapter: CalendarAdapter
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

    private val taskIdsToDelete = mutableListOf<Int>()

    private lateinit var plannerService: PlannerService
    private lateinit var taskService: TaskService
    private lateinit var taskApiHelper: TaskApiHelper
    private lateinit var plannerApiHelper: PlannerApiHelper

    private val calendarDays = mutableListOf<CalendarDay>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 전달된 result 값 처리
//        val type = arguments?.getString("type") ?: ""
        val (userId, type) = loadUserPreferences()
        updateLayoutBasedOnResult(type ?: "")

//        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", MODE_PRIVATE)
//        val userId = sharedPreferences.getString("user_id", null)

        val date = getCurrentDate()
        val month = getCurrentMonth()

        plannerApiHelper = PlannerApiHelper(requireContext())
        plannerApiHelper.getPlanner(userId, date, month)

        // 초기 상태 버튼 설정
        setInitialBtnState()

        val days = mutableListOf<CalendarDay>()
        val onDayClicked: (CalendarDay) -> Unit = { day ->
            if (!day.isEmpty) {
                Toast.makeText(context, "Clicked: ${day.date}", Toast.LENGTH_SHORT).show()
            }
        }

        calendarAdapter = CalendarAdapter(days, onDayClicked)

        // ViewModel 초기화
        sharedViewModel.calendarDays.value = generateCalendarDays(currentYear, currentMonth)

        sharedViewModel.calendarDays.observe(viewLifecycleOwner) { updatedDays ->
            calendarDays.clear()
            calendarDays.addAll(updatedDays)
            calendarAdapter.notifyDataSetChanged()
        }

        // 캘린더 초기화
        if (sharedViewModel.calendarDays.value.isNullOrEmpty()) {
            initializeCalendar() // ViewModel에 데이터가 없는 경우에만 호출
        }

        // 카테고리형 RecyclerView 설정
        ctgyAdapter = CategoryAdapter(categories, {
            updateSaveButtonState() // SAVE 버튼 활성화 로직
        }, {
            updateDeleteButtonState() // DELETE 버튼 상태 갱신
        })

        binding.homePlannerCtgyRv.adapter = ctgyAdapter
        binding.homePlannerCtgyRv.layoutManager = LinearLayoutManager(context)


        // 메모형 RecyclerView 설정
        memoAdapter = MemoAdapter(
            requireContext(),
            checklist,
            onMemoChanged = {
                // Checklist 변경 상태 확인
                updateDeleteButtonState()
                updateSaveButtonState()
                checkAllItemsChecked()  // Ensure all items checked
            },
            onDeleteStateChanged = { hasSelected ->
                // 삭제 모드 상태 업데이트
                binding.homePlannerMemoDeleteBtn.isEnabled = hasSelected
            },
            onAllChecked = { allChecked ->
                if (allChecked) {
                    markCurrentDateCompleted(true)
                } else {
                    markCurrentDateCompleted(false)
                }
            }
        )
        binding.homePlannerMemoRv.adapter = memoAdapter
        binding.homePlannerMemoRv.layoutManager = LinearLayoutManager(context)


        // 초기 카테고리 추가
        if (categories.isEmpty()) {
            addCategory("")
        }

        // 초기 카테고리 상태 설정
        initializeInitialCtgyState()


        // 카테고리 추가 버튼 클릭 리스너
        binding.homePlannerCtgyToolsIc.setOnClickListener {
            addCategory("")
        }

        // 메모 추가 버튼 클릭 리스너
        binding.homePlannerMemoPlusIc.setOnClickListener {
            addCheckListItem("")
            binding.homePlannerMemoSaveBtn.isEnabled = true
        }


        // 플래너 기능 세팅
        setPlanner()


        // home_reply_iv 클릭 리스너 추가
        binding.homeReplyIv.setOnClickListener {
            // CommentFragment 이동
            val transaction = parentFragmentManager.beginTransaction()
            val fragment = CommentFragment()  // CommentFragment 실제로 생성한 프래그먼트 클래스명으로 변경
            transaction.replace(R.id.main_frm, fragment)
            transaction.commit()
        }

    }

    private fun initializeInitialCtgyState() {
        // 초기 카테고리 상태에서 "변경되지 않은 상태"로 간주하고 처리
        binding.homePlannerCtgySaveBtn.isEnabled = false

        categories.forEach { category ->
            category.isEditable = true  // 카테고리 제목 수정 가능
            category.checklists.forEach { checklist ->
                checklist.isEditable = false  // 체크리스트 수정 불가능
            }
        }

        ctgyAdapter.notifyDataSetChanged()
    }

    private fun setInitialBtnState() {
        // 초기 상태 SAVE 버튼 비활성화
        binding.homePlannerCtgySaveBtn.isEnabled = false
        binding.homePlannerMemoSaveBtn.isEnabled = false

        // 초기 DELETE 버튼 활성화
        binding.homePlannerCtgyDeleteBtn.isEnabled = true
        binding.homePlannerMemoDeleteBtn.isEnabled = true
    }

    private fun setPlanner() {


        // 카테고리형 플래너 저장 버튼 클릭 리스너
        binding.homePlannerCtgySaveBtn.setOnClickListener {
            // 모든 카테고리 제목 및 체크리스트 수정 상태 고정
            categories.forEach { category ->
                category.isEditable = false // 제목 수정 불가능
                category.checklists.forEach { checklist ->
                    checklist.isEditable = false // 체크리스트 수정 불가능
                }
            }

            // 선택 상태 초기화 (이전 선택 항목 해제)
            categories.forEach { category ->
                category.isSelected = false
                category.checklists.forEach { checklist ->
                    checklist.isSelected = false
                }
            }

            // 어댑터에 변경 사항 반영
            ctgyAdapter.notifyDataSetChanged()

            // SAVE 버튼 비활성화
            binding.homePlannerCtgySaveBtn.isEnabled = false

            // DELETE 버튼 활성화
            binding.homePlannerCtgyDeleteBtn.isEnabled = true

            ctgyAdapter.toggleDeleteMode(false) // 삭제 모드 비활성화
        }

        // 카테고리형 삭제 버튼 클릭 리스너
        binding.homePlannerCtgyDeleteBtn.setOnClickListener {

            if (binding.homePlannerCtgyDeleteBtn.isEnabled) {
                if (ctgyAdapter.hasSelectedItems()) { // 선택된 항목이 있는 경우
                    ctgyAdapter.deleteSelectedItems() // 선택된 항목 삭제
                    binding.homePlannerCtgyDeleteBtn.isEnabled = false // 삭제 버튼 비활성화
                } else {
                    ctgyAdapter.toggleDeleteMode(true) // 선택 항목이 없으면 삭제 모드만 활성화
                }
            }
            binding.homePlannerCtgySaveBtn.isEnabled = true // SAVE 버튼 활성화
        }

        // 메모형 플래너 저장 버튼 클릭 리스너
        binding.homePlannerMemoSaveBtn.setOnClickListener {
            // 모든 항목 수정 불가능으로 변경
            checklist.forEach { it.isEditable = false }
            memoAdapter.notifyDataSetChanged()

            // 선택 상태 초기화 (이전 선택 항목 해제)
            checklist.forEach { memo -> memo.isSelected = false }

            // SAVE 버튼 비활성화
            binding.homePlannerMemoSaveBtn.isEnabled = false

            // DELETE 버튼 활성화
            binding.homePlannerMemoDeleteBtn.isEnabled = true

            // 삭제 모드 비활성화
            memoAdapter.toggleDeleteMode(false) // 삭제 모드 비활성화
            sendTaskToApi()
        }

        // 메모형 삭제 버튼 클릭 리스너
        binding.homePlannerMemoDeleteBtn.setOnClickListener {

            if (binding.homePlannerMemoDeleteBtn.isEnabled) {
                if (memoAdapter.hasSelectedItems()) {
                    // 선택된 할 일의 ID 가져오기
                    val selectedTaskIds = memoAdapter.getSelectedTaskIds()

                    if (selectedTaskIds.isNotEmpty()) {
                        // API 호출로 선택된 할 일 삭제
//                        taskApiHelper = TaskApiHelper(requireContext())
//                        taskApiHelper.deleteTasks(selectedTaskIds)
                        binding.homePlannerMemoDeleteBtn.isEnabled = false
                    }

                    memoAdapter.deleteSelectedItems()
//                    binding.homePlannerMemoDeleteBtn.isEnabled = false

                } else {
                    memoAdapter.toggleDeleteMode(true)
                }
            }

            binding.homePlannerMemoSaveBtn.isEnabled = true // SAVE 버튼 활성화
        }
    }


    private fun updateSaveButtonState() {
        // 카테고리나 체크리스트에 변경사항이 있는 경우 SAVE 버튼 활성화
        val hasUnsavedChanges = categories.any { category ->
            category.isEditable || category.isSelected || category.checklists.any { it.isEditable || it.isSelected }
        }

        binding.homePlannerCtgySaveBtn.isEnabled = hasUnsavedChanges
        binding.homePlannerMemoSaveBtn.isEnabled = hasUnsavedChanges
    }



    private fun updateDeleteButtonState() {
        // 선택된 카테고리나 체크리스트가 있는지 확인
        val hasSelectedItems = categories.any { it.isSelected || it.checklists.any { checklist -> checklist.isSelected } }

        // 삭제 버튼 활성화 여부 갱신
        binding.homePlannerCtgyDeleteBtn.isEnabled = hasSelectedItems
        binding.homePlannerMemoDeleteBtn.isEnabled = hasSelectedItems
    }


    // 체크리스트 추가 함수
    private fun addCheckListItem(task: String) {
        checklist.add(ChecklistItem(task, true, false))
        memoAdapter.notifyItemInserted(checklist.size - 1)

        // 오늘 날짜에 할 일이 추가되었음을 캘린더에 반영
        val currentDate = getCurrentDate()
        val currentDay = calendarDays.find { it.date == currentDate }

        if (currentDay != null) {
            currentDay.hasTask = true // 할 일이 있는 상태로 설정
            Log.d("HomeFragment Calendar", "날짜: ${currentDay.date}, hasTask: ${currentDay.hasTask}, isTaskCompleted: ${currentDay.isTaskCompleted}")
            calendarAdapter.notifyDataSetChanged()
        }
    }

    fun updateCalendarTaskStatus() {
        val updatedDays = sharedViewModel.calendarDays.value ?: return  // Null인 경우 바로 리턴하여 충돌 방지

        val currentDate = getCurrentDate().trim()  // 날짜 포맷 공백 제거
        val checklistCompleted = checklist.isNotEmpty() && checklist.all { it.isChecked }  // 모든 항목이 체크되었는지 확인

        updatedDays.forEach { day ->
            if (day.date.trim() == currentDate) {
                day.hasTask = checklist.isNotEmpty()  // 체크리스트에 항목이 있으면 hasTask 설정
                day.isTaskCompleted = checklistCompleted  // 모든 체크 완료 상태 반영
                Log.d("HomeFragment updateCalendarTaskStatus", "업데이트된 날짜: ${day.date}, hasTask: ${day.hasTask}, isTaskCompleted: ${day.isTaskCompleted}")
            }
        }

        // 변경된 상태를 ViewModel에 반영
        sharedViewModel.calendarDays.setValue(updatedDays.toMutableList())
        calendarAdapter.notifyDataSetChanged()
    }

    private fun initializeCalendar() {
        // 새로운 달의 날짜 목록 생성
        val generatedDays = generateCalendarDays(
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH) + 1
        )

        // 기존 ViewModel의 데이터 가져오기
        val existingDays = sharedViewModel.calendarDays.value ?: mutableListOf()

        // 병합 로직: 기존의 `hasTask`와 `isTaskCompleted` 상태 유지
        val mergedDays = generatedDays.map { newDay ->
            val existingDay = existingDays.find { it.date == newDay.date }
            if (existingDay != null) {
                // 기존 상태 유지
                newDay.hasTask = existingDay.hasTask
                newDay.isTaskCompleted = existingDay.isTaskCompleted
            } else {
                newDay.isTaskCompleted = false
            }
            newDay
        }

        // 병합된 결과를 ViewModel에 저장
        sharedViewModel.calendarDays.value = mergedDays.toMutableList()

        // 로그로 확인
        Log.d("Home InitializeCalendar", "초기화된 CalendarDays: ${mergedDays.map { "${it.date}: ${it.hasTask}" }}")
    }



    // 카테고리 추가 함수
    private fun addCategory(title: String) {
        val color = generateRandomColor()
        categories.add(Category(title, mutableListOf(), color))
        ctgyAdapter.notifyItemInserted(categories.size - 1)
    }



    private fun updateLayoutBasedOnResult(type: String) {
        when (type) {
            "memo_user" -> {
                binding.homePlannerMemoV.visibility = View.VISIBLE
                binding.homePlannerCtgyV.visibility = View.GONE
            }
            else -> {
                binding.homePlannerMemoV.visibility = View.GONE
                binding.homePlannerCtgyV.visibility = View.VISIBLE
            }
        }
    }


    // 텍스트 전송 함수
    private fun sendTaskToApi() {

        val taskTitle = checklist.map { it.task }  // 체크리스트 항목들을 하나의 텍스트로 결합
        val currentDate = getCurrentDate()  // 현재 날짜 가져오기
        val allChecked = checklist.all { it.isChecked == false }

        savePlannerDate(requireContext(), currentDate)

        updateCalendarTaskStatus()

        // 로그로 출력해서 API에 전달되는 데이터 확인
        Log.d("API SendTask", "Task Title: $taskTitle")
        Log.d("API SendTask", "Current Date: $currentDate")

        updateTaskCompletion(requireContext(), currentDate, allChecked)

        Log.d("API SendTask", "Task Title: $taskTitle, Date: $currentDate")

//        taskApiHelper = TaskApiHelper(requireContext())
//        taskApiHelper.addMultipleTasks(taskTitle, currentDate)
    }

    private fun checkAllItemsChecked() {
        val allChecked = checklist.all { it.isChecked }

        if (allChecked) {
            markCurrentDateCompleted(true)
        } else {
            markCurrentDateCompleted(false)
        }
    }

    private fun markCurrentDateCompleted(isCompleted: Boolean) {
        val currentDate = getCurrentDate()

        calendarDays.forEach { day ->
            if (day.date == currentDate) {
                day.isTaskCompleted = isCompleted
            }
        }
        calendarAdapter.notifyDataSetChanged()
    }

    // SharedPreferences에서 결과값 불러오기
    private fun loadUserPreferences(): Pair<String?, String?> {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)
        val type = sharedPreferences.getString("type", "memo_user") // 기본값 설정
        return Pair(userId, type)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}