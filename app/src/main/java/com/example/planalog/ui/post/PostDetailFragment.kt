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

    // 좋아요 상태 및 개수 저장 변수
    private var isLiked = false
    private var likeCount = 0

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

        // 좋아요 개수가 0이면 숨김
        updateLikeVisibility()

        // 좋아요 버튼 클릭 시
        binding.buttonHand.setOnClickListener {
            if (isLiked) {
                likeCount--
                binding.buttonHand.setImageResource(R.drawable.ic_hand)  // 기본 아이콘
            } else {
                likeCount++
                binding.buttonHand.setImageResource(R.drawable.ic_hand)  // 좋아요 활성화 아이콘
            }
            isLiked = !isLiked

            // UI 업데이트
            binding.likeCount.text = likeCount.toString()
            updateLikeVisibility()
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

    // 좋아요 개수 0일 때 숨기고, 1 이상일 때 보이게 설정하는 함수
    private fun updateLikeVisibility() {
        if (likeCount == 0) {
            binding.likeCount.visibility = View.GONE
        } else {
            binding.likeCount.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // 메모리 누수 방지
    }
}