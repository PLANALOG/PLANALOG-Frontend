package com.example.planalog.ui.post

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.planalog.R
import com.example.planalog.databinding.FragmentPostDetailBinding
import com.example.planalog.ui.comment.CommentFragment

class PostDetailFragment : Fragment() {
    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)

        // 전달된 데이터 받기
        val title = arguments?.getString("title")
        val content = arguments?.getString("content")
        val imageUri: Uri? = arguments?.getParcelable("imageUri")

        // 받은 데이터 UI에 설정
        binding.postTitle.text = title ?: ""
        binding.postContent.setText(content ?: "") // EditText에 값 설정

        // 이미지가 전달된 경우 보여주기
        if (imageUri != null) {
            binding.selectedImageView.setImageURI(imageUri)
            binding.selectedImageView.visibility = View.VISIBLE
        } else {
            binding.selectedImageView.visibility = View.GONE
        }

        // 댓글 버튼 클릭 시 CommentFragment로 이동
        binding.buttonReply.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.main_frm, CommentFragment())  // main_frm은 MainActivity의 프래그먼트 컨테이너
                addToBackStack(null)  // 뒤로 가기 버튼 처리
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // 메모리 누수 방지
    }
}
