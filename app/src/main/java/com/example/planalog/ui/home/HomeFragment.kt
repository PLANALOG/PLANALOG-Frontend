package com.example.planalog.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planalog.R
import com.example.planalog.databinding.FragmentHomeBinding
import com.example.planalog.ui.comment.com.example.planalog.ui.comment.CommentFragment
import com.example.planalog.ui.home.ctgy.Category
import com.example.planalog.ui.home.ctgy.CategoryAdapter
import com.example.planalog.ui.home.ctgy.MemoAdapter
import com.example.planalog.ui.home.memo.ChecklistItem
import com.example.planalog.utils.generateRandomColor

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val checklist = mutableListOf<ChecklistItem>()
    private val categories = mutableListOf<Category>()
    private lateinit var ctgyAdapter : CategoryAdapter
    private lateinit var memoAdapter: MemoAdapter

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

        // 초기 상태 버튼 설정
        setInitialBtnState()

        // 카테고리형 RecyclerView 설정
        ctgyAdapter = CategoryAdapter(categories, {
            updateSaveButtonState() // SAVE 버튼 활성화 로직
        }, {
            updateDeleteButtonState() // DELETE 버튼 상태 갱신
        })

        binding.homePlannerCtgyRv.adapter = ctgyAdapter
        binding.homePlannerCtgyRv.layoutManager = LinearLayoutManager(context)


        // 메모형 RecyclerView 설정
        memoAdapter = MemoAdapter(checklist, {
            // 체크리스트 변경 상태 확인
            updateDeleteButtonState()
            updateSaveButtonState()
        }, { hasSelected ->
            // 삭제 버튼 상태 업데이트
            binding.homePlannerMemoDeleteBtn.isEnabled = hasSelected
        })
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

        // 전달된 result 값 처리
        val result = arguments?.getString("result") ?: ""
        updateLayoutBasedOnResult(result)

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
            checklist.forEach { memo ->
                memo.isSelected = false
            }

            // SAVE 버튼 비활성화
            binding.homePlannerMemoSaveBtn.isEnabled = false

            // DELETE 버튼 활성화
            binding.homePlannerMemoDeleteBtn.isEnabled = true

            // 삭제 모드 비활성화
            memoAdapter.toggleDeleteMode(false) // 삭제 모드 비활성화
        }

        // 메모형 삭제 버튼 클릭 리스너
        binding.homePlannerMemoDeleteBtn.setOnClickListener {

            if (binding.homePlannerMemoDeleteBtn.isEnabled) {
                if (memoAdapter.hasSelectedItems()) {
                    memoAdapter.deleteSelectedItems()
                    binding.homePlannerMemoDeleteBtn.isEnabled = false
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
        checklist.add(ChecklistItem(task, true))
        memoAdapter.notifyItemInserted(checklist.size - 1)
    }


    // 카테고리 추가 함수
    private fun addCategory(title: String) {
        val color = generateRandomColor()
        categories.add(Category(title, mutableListOf(), color))
        ctgyAdapter.notifyItemInserted(categories.size - 1)
    }



    private fun updateLayoutBasedOnResult(result: String) {
        when (result) {
            "a" -> {
                binding.homePlannerMemoV.visibility = View.VISIBLE
                binding.homePlannerCtgyV.visibility = View.GONE
            }
            else -> {
                binding.homePlannerMemoV.visibility = View.GONE
                binding.homePlannerCtgyV.visibility = View.VISIBLE
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}