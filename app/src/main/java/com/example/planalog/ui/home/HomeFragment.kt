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
import com.example.planalog.ui.comment.CommentFragment
import com.example.planalog.ui.comment.com.example.planalog.ui.home.calender.CalendarAdapter
import com.example.planalog.ui.comment.com.example.planalog.ui.home.calender.CalendarDay
import com.example.planalog.ui.comment.com.example.planalog.utils.generateCalendarDays
import com.example.planalog.network.api.PlannerApiHelper
import com.example.planalog.network.api.TaskApiHelper
import com.example.planalog.network.api.TaskCtgyApiHelper
import com.example.planalog.ui.home.calender.SharedViewModel
import com.example.planalog.ui.home.ctgy.Category
import com.example.planalog.ui.home.ctgy.CategoryAdapter
import com.example.planalog.ui.home.ctgy.MemoAdapter
import com.example.planalog.ui.home.memo.ChecklistItem
import com.example.planalog.utils.generateRandomColor
import com.example.planalog.utils.getCurrentDate
import com.example.planalog.utils.getCurrentMonth
import com.example.planalog.utils.getCurrentPostedDate
import com.example.planalog.utils.savePlannerDate
import com.example.planalog.utils.updateTaskCompletion
import loadLastSavedDate
import loadPlannerState
import saveLastSavedDate
import savePlannerState
import java.util.Calendar

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // 데이터 리스트
    private val checklist = mutableListOf<ChecklistItem>()
    private val categories = mutableListOf<Category>()

    // 어댑터
    private lateinit var ctgyAdapter: CategoryAdapter
    private lateinit var memoAdapter: MemoAdapter
    private lateinit var calendarAdapter: CalendarAdapter

    // API 헬퍼
    private lateinit var taskApiHelper: TaskApiHelper
    private lateinit var ctgyApiHelper: TaskCtgyApiHelper
    private lateinit var plannerApiHelper: PlannerApiHelper

    //기타 변수
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private val calendarDays = mutableListOf<CalendarDay>()
    private var lastSavedTasks: Set<String> = emptySet()
    private var isDeleteMode = false
    private var isCategoryDeleted = false

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
        val type = arguments?.getString("type") ?: ""
        Log.d("HomeFragment", "argument type: $type")
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", MODE_PRIVATE)
        val plannerType = sharedPreferences.getString("type", "memo_user") // 기본값 설정
        loadUserPreferences()
        updateLayoutBasedOnResult(plannerType.toString())

        val currentDate = getCurrentDate()
        val lastSavedDate = loadLastSavedDate(requireContext(), plannerType.toString()) // 마지막 저장된 날짜 불러오기

        Log.d("HomeFragment", "현재 날짜: $currentDate, 마지막 저장된 날짜: $lastSavedDate")

        if (currentDate != lastSavedDate) {
            Log.d("HomeFragment", " 날짜 변경 감지 → 데이터 초기화")
            categories.clear()
            checklist.clear()

            if (plannerType == "category_user") {
                categories.add(Category(-1, "", mutableListOf(), generateRandomColor()))
            }

            saveLastSavedDate(requireContext(), plannerType.toString(), currentDate)
        } else {
            val (savedCategories, savedChecklist) = loadPlannerState(requireContext(), plannerType.toString())

            if (plannerType == "memo_user") {
                checklist.clear()
                checklist.addAll(savedChecklist)
                Log.d("HomeFragment", " [메모형] 데이터 불러오기 완료: ${checklist.size}개")
            } else {
                categories.clear()
                categories.addAll(savedCategories)
                Log.d("HomeFragment", "[카테고리형] 데이터 불러오기 완료: ${categories.size}개")
            }
        }

        ctgyApiHelper = TaskCtgyApiHelper(requireContext())
        ctgyApiHelper.getCtgys()

        taskApiHelper = TaskApiHelper(requireContext())
        taskApiHelper.getTasks(getCurrentDate())

        initializeVariables()
        initializeUI()
        initializeAdapters()
        setupClickListeners()

        Log.d("HomeFragment", "📋 데이터 로드 완료 - 카테고리: ${categories.size}, 체크리스트: ${checklist.size}")


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

    override fun onResume() {
        super.onResume()
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", MODE_PRIVATE)
        val plannerType = sharedPreferences.getString("type", "memo_user") ?: "memo_user"
        updateLayoutBasedOnResult(plannerType)
    }

    private fun getLastSavedDate(): String {
        val sharedPreferences = requireContext().getSharedPreferences("date_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("last_saved_date", "") ?: ""
    }

    private fun setupClickListeners() {
        // 카테고리 추가 버튼 클릭 리스너
        binding.homePlannerCtgyToolsIc.setOnClickListener {
            addCategory("")
        }

        // 메모 추가 버튼 클릭 리스너
        binding.homePlannerMemoPlusIc.setOnClickListener {
            addCheckListItem("")
            binding.homePlannerMemoSaveBtn.isEnabled = true
        }
    }

    private fun initializeAdapters() {
        val onDayClicked: (CalendarDay) -> Unit = { day ->
            if (!day.isEmpty) {
                Toast.makeText(context, "Clicked: ${day.date}", Toast.LENGTH_SHORT).show()
            }
        }

        calendarAdapter = CalendarAdapter(calendarDays, onDayClicked)

        // 카테고리형 RecyclerView 설정
        ctgyAdapter = CategoryAdapter(requireContext(), categories, {
            updateSaveButtonState() // SAVE 버튼 활성화 로직
        }, {
            updateDeleteButtonState() // DELETE 버튼 상태 갱신
        })


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

        binding.homePlannerCtgyRv.apply {
            adapter = ctgyAdapter
            layoutManager = LinearLayoutManager(context)
        }

        binding.homePlannerMemoRv.apply {
            adapter = memoAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun initializeUI() {
        // 초기 상태 버튼 설정
        setInitialBtnState()

        binding.homePlannerDateTv.text = getCurrentPostedDate()
        binding.homePlannerCtgyDateTv.text = getCurrentPostedDate()

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
    }

    private fun initializeVariables() {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        val date = getCurrentDate()
        val month = getCurrentMonth()
        Log.d("HomeFragment", "date: ${date}, month: $month")

        isDeleteMode = false

        plannerApiHelper = PlannerApiHelper(requireContext())
        plannerApiHelper.getPlanner(userId, date, month) { hasPlanner ->
            if (hasPlanner) {
                Log.d("HomeFragment", "플래너 데이터가 존재합니다.")
                //  플래너가 있을 때 실행할 코드 추가
            } else {
                Log.e("HomeFragment", "플래너가 없습니다.")
                Toast.makeText(requireContext(), "플래너가 없습니다. 먼저 생성해주세요.", Toast.LENGTH_SHORT).show()
            }
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
            if (!isDeleteMode && !isCategoryDeleted) {
                saveCtgyWithTasks()
//                savePlannerState(requireContext(),"category_user", categories, checklist) //  저장 버튼 클릭 시 플래너 상태 저장
            } else {
                Log.d("HomeFragment", "삭제 후 저장 버튼 클릭 - 카테고리 생성 API 호출하지 않음")
            }

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
            isCategoryDeleted = false
        }

        // 카테고리형 삭제 버튼 클릭 리스너
        binding.homePlannerCtgyDeleteBtn.setOnClickListener {
            isDeleteMode = true
            ctgyAdapter.toggleDeleteMode(true)

            if (binding.homePlannerCtgyDeleteBtn.isEnabled) {
                if (ctgyAdapter.hasSelectedItems()) { // 선택된 항목이 있는 경우
                    val selectedCtgyIds = ctgyAdapter.getSelectedCtgyIds()
                    val selectedTaskIds = ctgyAdapter.getSelectedTaskIds()
                    Log.d("HomeFragment", "삭제할 카테고리 ID 목록: $selectedCtgyIds")
                    Log.d("HomeFragment", "삭제할 카테고리 하위 ID 목록: $selectedTaskIds")

                    if (selectedCtgyIds.isNotEmpty()) {
                        ctgyApiHelper = TaskCtgyApiHelper(requireContext())
                        ctgyApiHelper.deleteCtgys(selectedCtgyIds, categories,
                            onSuccess = { deletedCtgyIds ->
                                Log.d("HomeFragment", "삭제 성공한 카테고리 ID 목록: $deletedCtgyIds")

                                //  UI에서 삭제
                                ctgyAdapter.deleteSelectedItems()

                                //  삭제된 상태 저장
                                savePlannerState(requireContext(), "category_user", categories, checklist)

                                isCategoryDeleted = true
                                isDeleteMode = false

                                //  UI 업데이트
                                binding.homePlannerCtgyDeleteBtn.isEnabled = false
                            },
                            onFailure = { errorMessage ->
                                Log.e("HomeFragment", "카테고리 삭제 실패: $errorMessage")
                            }
                        )
                    }

                    if (selectedTaskIds.isNotEmpty()) {
                        // 선택된 할 일만 삭제하는 경우 처리
                        taskApiHelper = TaskApiHelper(requireContext())
                        taskApiHelper.deleteTasks(selectedTaskIds,
                            onSuccess = { deletedTaskIds ->
                                Log.d("HomeFragment", "삭제 성공한 하위 할 일 ID 목록: $deletedTaskIds")

                                //  UI에서 삭제
                                ctgyAdapter.deleteSelectedItems()

                                // UI 업데이트
                                binding.homePlannerCtgyDeleteBtn.isEnabled = false
                            },
                            onFailure = { errorMessage ->
                                Log.e("HomeFragment", "하위 할 일 삭제 실패: $errorMessage")
                            }
                        )
                    }
                }
                ctgyAdapter.toggleDeleteMode(true)
                binding.homePlannerCtgySaveBtn.isEnabled = true // SAVE 버튼 활성화
            }
        }

        // 메모형 플래너 저장 버튼 클릭 리스너
        binding.homePlannerMemoSaveBtn.setOnClickListener {
            val currentTasks = checklist.map { it.task }.toSet()

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

            // 삭제 후 저장 버튼을 눌렀을 때 `sendTaskToApi()` 실행 안 하도록 변경
            if (currentTasks != lastSavedTasks) {
                Log.d("HomeFragment", "변경 사항 감지됨, 새로운 할 일 생성 API 호출")
                sendTaskToApi()
            } else {
                Log.d("HomeFragment", "변경 사항 없음, 할 일 생성 API 호출 안 함")
                savePlannerState(requireContext(), "memo_user", categories, checklist)
            }

            lastSavedTasks = currentTasks
        }

        // 메모형 삭제 버튼 클릭 리스너
        binding.homePlannerMemoDeleteBtn.setOnClickListener {

            if (binding.homePlannerMemoDeleteBtn.isEnabled) {
                if (memoAdapter.hasSelectedItems()) {
                    // 선택된 할 일의 ID 가져오기
                    val selectedTaskIds = memoAdapter.getSelectedTaskIds()
                    Log.d("HomeFragment", "선택된 Task ID 목록: $selectedTaskIds")

                    if (selectedTaskIds.isNotEmpty()) {
                        // API 호출로 선택된 할 일 삭제
                        taskApiHelper = TaskApiHelper(requireContext())
                        taskApiHelper.deleteTasks(selectedTaskIds,
                            onSuccess = {
                                memoAdapter.deleteSelectedItems()
                                Log.d("HomeFragment", "선택된 할 일 삭제 성공")

                                // 삭제 후 상태를 저장하여, 저장 버튼 눌렀을 때 생성 API가 호출되지 않도록 설정
                                savePlannerState(requireContext(), "memo_user", categories, checklist)
                                lastSavedTasks = checklist.map { it.task }.toSet()
                                Log.d("HomeFragment", "삭제 후 lastSavedTasks 업데이트: $lastSavedTasks")
                            },

                            onFailure = { Log.e("HomeFragment", "할 일 삭제 실패") }
                        )
                        binding.homePlannerMemoDeleteBtn.isEnabled = false
                    }

                    memoAdapter.deleteSelectedItems()

                } else {
                    memoAdapter.toggleDeleteMode(true)
                }
            }

            binding.homePlannerMemoSaveBtn.isEnabled = true // SAVE 버튼 활성화
        }
    }

    private fun saveCtgyWithTasks() {
        ctgyApiHelper = TaskCtgyApiHelper(requireContext())
        val plannerDate = getCurrentDate()

        val ctgyTitles = categories.map { it.title }

        //  삭제된 상태 그대로 저장
        savePlannerState(requireContext(), "category_user", categories, checklist)

        // 카테고리 생성 API 호출
        ctgyApiHelper.addMultipleCtgy(ctgyTitles) { success, responseCode, responseBody, errorMessage ->
            if (success && responseBody != null) {
                Log.d("HomeFragment", "카테고리 생성 완료: $responseBody")

                responseBody.forEachIndexed { index, ctgyItem ->
                    if (index < categories.size) {
                        categories[index].id = ctgyItem.id  // 서버에서 받은 ID 업데이트
                    }
                }

                //  서버에서 받은 ID를 적용한 후, UI 갱신
                ctgyAdapter.notifyDataSetChanged()
                saveTasksForCategories(categories, plannerDate)
            } else {
                Log.e("HomeFragment", " 카테고리 생성 실패: $errorMessage")
                Toast.makeText(requireContext(), "카테고리 생성 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveTasksForCategories(categories: List<Category>, plannerDate: String) {
        for (category in categories) {
            val tasks = category.checklists.map { it.task }

            if (tasks.isNotEmpty() && category.id != -1) {
                Log.d("HomeFragment", "카테고리 ID: ${category.id}, 할 일 목록: $tasks")

                ctgyApiHelper.addCtgyMultipleTasks(
                    category.id,
                    tasks,
                    plannerDate,
                    onSuccess = { createdTasks ->
                        Log.d("HomeFragment", "카테고리(${category.id}) 하위 할 일 생성 완료: $createdTasks")

                        category.checklists.clear()
                        category.checklists.addAll(
                            createdTasks.map {
                                ChecklistItem(
                                    task = it.title,
                                    taskId = it.id,
                                    isChecked = it.isCompleted,
                                    isEditable = false
                                )
                            }
                        )
                        Log.d("HomeFragment", "저장된 할 일 목록 (ID 포함): ${category.checklists.map { it.taskId }}")
                    },
                    onFailure = { errorMessage ->
                        Log.e("HomeFragment", "카테고리(${category.id}) 하위 할 일 생성 실패: $errorMessage")
                    }
                )
            }
        }

        ctgyAdapter.notifyDataSetChanged()
    }


    private fun updateSaveButtonState() {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", MODE_PRIVATE)
        val type = sharedPreferences.getString("type", "")

        // 카테고리나 체크리스트에 변경사항이 있는 경우 SAVE 버튼 활성화
        val hasUnsavedChanges = when (type) {
            "memo_user" -> checklist.any { it.task.isNotBlank() } //  메모형: 메모가 입력되었으면 SAVE
            else -> categories.any { category ->
                category.isEditable || category.isSelected || category.checklists.any { it.isEditable || it.isSelected }
            }
        }

        Log.d("HomeFragment", " SAVE 버튼 활성화 여부: $hasUnsavedChanges")
        binding.homePlannerCtgySaveBtn.isEnabled = hasUnsavedChanges
        binding.homePlannerMemoSaveBtn.isEnabled = hasUnsavedChanges
    }



    private fun updateDeleteButtonState() {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", MODE_PRIVATE)
        val type = sharedPreferences.getString("type", "")

        when (type) {
            "memo_user" -> {
                //  메모형 플래너일 때 (메모 어댑터에서 ID 가져오기)
                val selectedTaskIds = memoAdapter.getSelectedTaskIds()
                Log.d("HomeFragment", "[메모형] 선택된 Task ID 목록: $selectedTaskIds")

                // 선택된 메모가 있는지 확인
                val hasSelectedItems = selectedTaskIds.isNotEmpty()

                // 삭제 버튼 활성화 여부 갱신 (메모형)
                binding.homePlannerMemoDeleteBtn.isEnabled = hasSelectedItems
            }

            else -> {
                //  카테고리형 플래너일 때 (카테고리 어댑터에서 ID 가져오기)
                val selectedCtgyIds = ctgyAdapter.getSelectedCtgyIds()
                val selectedTaskIds = ctgyAdapter.getSelectedTaskIds()

                Log.d("HomeFragment", "[카테고리형] 선택된 카테고리 ID 목록: $selectedCtgyIds")
                Log.d("HomeFragment", "[카테고리형] 선택된 할 일 ID 목록: $selectedTaskIds")

                // 선택된 항목이 하나라도 있는지 확인
                val hasSelectedCategories = selectedCtgyIds.isNotEmpty()
                val hasSelectedTasks = selectedTaskIds.isNotEmpty()

                // 삭제 버튼 활성화 여부 갱신 (카테고리형)
                binding.homePlannerCtgyDeleteBtn.isEnabled = hasSelectedTasks || hasSelectedCategories
            }
        }
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
        categories.add(Category(-1, title, mutableListOf(), color))
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
        val taskTitles = checklist.map { it.task }  // 체크리스트 항목들을 리스트로 변환
        val currentDate = getCurrentDate()  // 현재 날짜 가져오기
        val allChecked = checklist.all { it.isChecked }

        savePlannerDate(requireContext(), currentDate)
        updateCalendarTaskStatus()

        // 로그로 API에 전달되는 데이터 확인
        Log.d("API SendTask", "Task Titles: $taskTitles")
        Log.d("API SendTask", "Current Date: $currentDate")

        updateTaskCompletion(requireContext(), currentDate, allChecked)

        // TaskApiHelper 사용하여 서버에 요청
        taskApiHelper = TaskApiHelper(requireContext())
        taskApiHelper.addMultipleTasks(taskTitles, currentDate) { success, responseCode, responseBody, errorMessage ->
            if (success) {
                Log.d("HomeFragment", "할 일이 여러 개 생성되었습니다. (응답 코드: $responseCode)")
                Log.d("HomeFragment", "서버 응답 본문: $responseBody")
                Toast.makeText(requireContext(), "할 일이 여러 개 생성되었습니다.", Toast.LENGTH_SHORT).show()

                (responseBody)?.let { todoItems ->
                    todoItems.forEachIndexed { index, item ->
                        if (index < checklist.size) {
                            checklist[index].taskId = item.id // taskId 업데이트
                        }
                    }
                    memoAdapter.notifyDataSetChanged()

                    savePlannerState(requireContext(),"memo_user", categories, checklist)
                }
                Toast.makeText(requireContext(), "할 일이 여러 개 생성되었습니다.", Toast.LENGTH_SHORT).show()

            } else {
                Log.e("HomeFragment", "할 일 생성 실패 (응답 코드: $responseCode, 에러 메시지: $errorMessage)")
                Log.e("HomeFragment", "서버 응답 본문: $responseBody")
                Toast.makeText(requireContext(), "할 일 생성 실패: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }
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
        val type = sharedPreferences.getString("type", "") // 기본값 설정
        Log.d("HomeFragment", "user_id: $userId, type: $type")
        return Pair(userId, type)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}