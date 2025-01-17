package com.example.planalog.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.planalog.databinding.FragmentPostDetailBinding

class PostDetailFragment : Fragment() {
    private lateinit var binding: FragmentPostDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostDetailBinding.inflate(inflater, container, false)

        // 전달된 데이터 받기
        val title = arguments?.getString("title")
        val content = arguments?.getString("content")

        // 받은 데이터 UI에 설정
        binding.postTitle.text = title
        binding.postContent.text = content

        return binding.root
    }
}
