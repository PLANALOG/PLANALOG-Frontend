package com.example.planalog.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planalog.databinding.FragmentHomeBinding
import com.example.planalog.ui.home.ctgy.Category
import com.example.planalog.ui.home.ctgy.CategoryAdapter
import com.example.planalog.ui.home.ctgy.MemoAdapter
import com.example.planalog.ui.home.memo.ChecklistItem
import com.example.planalog.utils.generateRandomColor

class HomeFragment : Fragment() {

    private lateinit var binding : FragmentHomeBinding

    private val checklist = mutableListOf<ChecklistItem>()
    private val categories = mutableListOf<Category>()
    private lateinit var ctgyAdapter : CategoryAdapter
    private lateinit var memoAdapter: MemoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

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
}