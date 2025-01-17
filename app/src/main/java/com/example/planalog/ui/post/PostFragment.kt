package com.example.planalog.ui.post

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.planalog.R
import com.example.planalog.databinding.FragmentPostBinding

class PostFragment : Fragment() {
    private lateinit var binding: FragmentPostBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostBinding.inflate(inflater, container, false)

        setupPostContentListener()

        // 글 작성 후 완료 버튼 클릭 시
        binding.uploadButton.setOnClickListener {
            // 입력된 제목과 내용을 가져옴
            val title = binding.postTitle.text.toString()
            val content = binding.postContent.text.toString()

            // PostDetailFragment로 데이터 전달
            val bundle = Bundle().apply {
                putString("title", title)
                putString("content", content)
            }

            // PostDetailFragment 생성 및 인자 전달
            val postDetailFragment = PostDetailFragment().apply {
                arguments = bundle
            }

            // 프래그먼트 전환
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, postDetailFragment)  // 여기서 fragment_container는 실제 컨테이너 ID로 변경
                .addToBackStack(null)  // 뒤로가기 기능
                .commit()
        }

        return binding.root
    }

    private fun setupPostContentListener() {
        binding.postContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 글자 수 업데이트
                val currentLength = s?.length ?: 0
                binding.postContentCount.text = "$currentLength/500"
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }
}
