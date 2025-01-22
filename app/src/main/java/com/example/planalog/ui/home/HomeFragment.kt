package com.example.planalog.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planalog.R
import com.example.planalog.databinding.FragmentCalendarBinding
import com.example.planalog.databinding.FragmentHomeBinding
import com.example.planalog.ui.comment.CommentFragment
import com.example.planalog.ui.comment.com.example.planalog.ui.home.calender.CalendarFragment
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

        // RecyclerView 설정
        ctgyAdapter = CategoryAdapter(categories)
        binding.homePlannerCtgyRv.layoutManager = LinearLayoutManager(context)
        binding.homePlannerCtgyRv.adapter = ctgyAdapter

        memoAdapter = MemoAdapter(checklist)
        binding.homePlannerMemoRv.layoutManager = LinearLayoutManager(context)
        binding.homePlannerMemoRv.adapter = memoAdapter

        // 초기 카테고리 추가
        if (categories.isEmpty()) {
            addCategory("") // 기본 카테고리 제목 설정
        }

        // Tools 버튼: 카테고리 추가
        binding.homePlannerCtgyToolsIc.setOnClickListener {
            addCategory("")
        }

        // 메모형 체크리스트 추가 버튼 클릭 리스너
        binding.homePlannerMemoPlusIc.setOnClickListener {
            addCheckListItem("")
        }

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
}