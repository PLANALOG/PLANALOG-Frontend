package com.example.planalog.ui.search

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.planalog.R
import com.example.planalog.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {
    lateinit var binding: FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        setupEditTextActions()

        return binding.root
    }

    private fun setupEditTextActions() {
        binding.searchEt.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                // DrawableStart 클릭 영역 감지
                val drawableStartWidth = binding.searchEt.compoundDrawables[0]?.bounds?.width() ?: 0
                if (event.rawX <= (binding.searchEt.left + drawableStartWidth + binding.searchEt.paddingStart)) {
                    performSearch(binding.searchEt.text.toString())
                    return@setOnTouchListener true
                }

                // DrawableEnd 클릭 영역 감지
                val drawableEndWidth = binding.searchEt.compoundDrawables[2]?.bounds?.width() ?: 0
                if (event.rawX >= (binding.searchEt.right - drawableEndWidth - binding.searchEt.paddingEnd)) {
                    clearSearchField()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    // 검색 동작
    private fun performSearch(query: String) {
        if (!TextUtils.isEmpty(query)) {
            // TODO: 검색 동작 구현
            println("검색 수행: $query")
        }
    }

    // 검색 필드 초기화
    private fun clearSearchField() {
        binding.searchEt.text.clear()
        binding.searchEt.hint = getString(R.string.hint)
    }
}