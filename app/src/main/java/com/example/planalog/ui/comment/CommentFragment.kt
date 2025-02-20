package com.example.planalog.ui.comment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planalog.R
import com.example.planalog.databinding.FragmentCommentBinding
import com.example.planalog.network.api.CommentApiHelper
import com.example.planalog.ui.comment.com.example.planalog.ui.comment.Comment
import android.widget.Toast
import com.example.planalog.network.comment.SuccessResponse

class CommentFragment : Fragment() {

    private lateinit var commentAdapter: CommentAdapter
    private val commentList = mutableListOf<Comment>()
    private lateinit var binding: FragmentCommentBinding

    private lateinit var commentApiHelper: CommentApiHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommentBinding.inflate(inflater, container, false)

        commentApiHelper = CommentApiHelper(requireContext())

        // RecyclerView 초기화
        binding.commentRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            commentAdapter = CommentAdapter(commentList)
            adapter = commentAdapter
        }

        // ✅ 댓글 추가 버튼 클릭 이벤트 수정
        binding.commentPostButton.setOnClickListener {
            val content = binding.commentInput.text.toString().trim()

            if (content.isNotEmpty()) {
                // ✅ CommentApiHelper를 통해 댓글을 서버에 등록
                commentApiHelper.handleCommentPostButtonClick(content, object : CommentApiHelper.CommentCallback {
                    override fun onSuccess(comment: SuccessResponse) {
                        requireActivity().runOnUiThread {
                            Log.d("CommentFragment", "🟢 댓글 등록 성공: ${comment.content}")
                            val newComment = Comment(
                                userName = "사용자",
                                content = comment.content, // ✅ 서버에서 받은 content 반영
                                profileImage = R.drawable.ic_profile
                            )

                            commentAdapter.addComment(newComment) // ✅ 기존 리스트에 추가하는 대신 addComment 호출
                            binding.commentInput.text.clear()
                            binding.commentRecyclerView.scrollToPosition(commentAdapter.itemCount - 1) // ✅ 가장 아래로 스크롤 이동
                        }
                    }

                    override fun onFailure(errorMessage: String) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "댓글 등록 실패: $errorMessage", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            } else {
                Toast.makeText(requireContext(), "댓글을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
}
