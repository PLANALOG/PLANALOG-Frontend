package com.example.planalog.ui.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planalog.R
import com.example.planalog.databinding.FragmentCommentBinding
import com.example.planalog.ui.comment.com.example.planalog.ui.comment.Comment

class CommentFragment : Fragment() {

    private lateinit var commentAdapter: CommentAdapter
    private val commentList = mutableListOf<Comment>()
    private lateinit var binding: FragmentCommentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommentBinding.inflate(inflater, container, false)

        // RecyclerView 초기화
        binding.commentRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            commentAdapter = CommentAdapter(commentList)
            adapter = commentAdapter
        }

        // 댓글 추가 로직
        binding.commentPostButton.setOnClickListener {
            val content = binding.commentInput.text.toString()
            if (content.isNotEmpty()) {
                val newComment = Comment(
                    userName = "사용자",
                    content = content,
                    profileImage = R.drawable.ic_profile
                )
                commentAdapter.addComment(newComment)
                binding.commentInput.text.clear()
                binding.commentRecyclerView.scrollToPosition(commentList.size - 1)
            }
        }

        return binding.root
    }
}
