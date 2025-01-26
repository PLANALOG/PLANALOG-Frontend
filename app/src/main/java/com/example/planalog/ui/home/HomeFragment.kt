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
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 카테고리형 RecyclerView 설정
        ctgyAdapter = CategoryAdapter(categories) {
            binding.homePlannerCtgySaveBtn.isEnabled = true // 변경사항 발생 시 SAVE 버튼 활성화
            updateDeleteButtonState() // 상태 변경 시 호출
        }
        binding.homePlannerCtgyRv.adapter = ctgyAdapter
        binding.homePlannerCtgyRv.layoutManager = LinearLayoutManager(context)

        // 메모형 RecyclerView 설정
        memoAdapter = MemoAdapter(checklist) {
            // 체크된 항목이 있는지 확인
            val hasCheckedItems = checklist.any { it.isChecked }
            binding.homePlannerMemoDeleteBtn.isEnabled = hasCheckedItems

            // 다른 변경 사항 처리
            val hasEditableMemos = checklist.any { it.isEditable }
            binding.homePlannerMemoSaveBtn.isEnabled = hasEditableMemos
        }

        binding.homePlannerMemoRv.adapter = memoAdapter
        binding.homePlannerMemoRv.layoutManager = LinearLayoutManager(context)

        // 초기 카테고리 추가
        if (categories.isEmpty()) {
            addCategory("") // 기본 카테고리 제목 설정
        }

        binding.homePlannerMemoDeleteBtn.isEnabled = checklist.any { it.isChecked }

        // 플래너 항목 추가 함수
        addPlannerList()

        // 플래너 내용 저장 함수
        saveButtonClickListener()

        // 플래너 삭제 버튼 클릭 리스너
        deleteButtonClickListener()

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

    private fun deleteButtonClickListener() {
        // 카테고리형 삭제 버튼 클릭 리스너
        binding.homePlannerCtgyDeleteBtn.setOnClickListener {
            val categoryIterator = categories.iterator()
            while (categoryIterator.hasNext()) {
                val category = categoryIterator.next()

                // 선택된 체크리스트 삭제
                val checklistIterator = category.checklists.iterator()
                while (checklistIterator.hasNext()) {
                    val checklistItem = checklistIterator.next()
                    if (checklistItem.isChecked) {
                        checklistIterator.remove()
                    }
                }

                // 선택된 카테고리 삭제
                if (category.isSelected) {
                    categoryIterator.remove()
                }
            }

            // 어댑터에 변경 사항 적용
            ctgyAdapter.notifyDataSetChanged()

            // 삭제 버튼 상태 업데이트
            updateDeleteButtonState()
        }

        // 메모형 삭제 버튼 클릭 리스너
        binding.homePlannerMemoDeleteBtn.setOnClickListener {
            val iterator = checklist.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (item.isChecked) {
                    iterator.remove() // 체크된 항목 삭제
                }
            }

            // 어댑터 갱신
            memoAdapter.notifyDataSetChanged()

            // 삭제 버튼 비활성화
            binding.homePlannerMemoDeleteBtn.isEnabled = checklist.any { it.isChecked }
        }
    }

    private fun addPlannerList() {
        // Tools 버튼: 카테고리 추가
        binding.homePlannerCtgyToolsIc.setOnClickListener {
            addCategory("")
        }

        // 메모형 체크리스트 추가 버튼 클릭 리스너
        binding.homePlannerMemoPlusIc.setOnClickListener {
            addCheckListItem("")
        }
    }

    private fun saveButtonClickListener() {
        // 카테고리형 플래너 저장 버튼 클릭 리스너
        binding.homePlannerCtgySaveBtn.setOnClickListener {
            // 모든 카테고리 제목 및 체크리스트 수정 상태 고정
            categories.forEach { category ->
                category.isEditable = false // 제목 수정 불가능
                category.checklists.forEach { checklist ->
                    checklist.isEditable = false // 체크리스트 수정 불가능
                }
            }

            // 어댑터에 변경 사항 반영
            ctgyAdapter.notifyDataSetChanged()

            // SAVE 버튼 비활성화 및 스타일 업데이트
            binding.homePlannerCtgySaveBtn.isEnabled = false
        }

        // 메모형 플래너 저장 버튼 클릭 리스너
        binding.homePlannerMemoSaveBtn.setOnClickListener {
            // 모든 항목 수정 불가능으로 변경
            checklist.forEach { it.isEditable = false }
            memoAdapter.notifyDataSetChanged()

            // 저장 버튼 비활성화
            binding.homePlannerMemoSaveBtn.isEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addCheckListItem(task: String) {
        checklist.add(ChecklistItem(task, true))
        memoAdapter.notifyItemInserted(checklist.size - 1)
    }

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

    private fun updateDeleteButtonState() {
        // 선택된 카테고리나 체크리스트가 있는지 확인
        val hasSelectedItems = categories.any { it.isSelected || it.checklists.any { checklist -> checklist.isChecked } }

        // 삭제 버튼 활성화 여부 갱신
        binding.homePlannerCtgyDeleteBtn.isEnabled = hasSelectedItems
    }
}