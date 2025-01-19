package com.example.planalog.ui.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.planalog.databinding.FragmentCommentBinding

class CommentFragment : Fragment() {

    private lateinit var commentAdapter: CommentAdapter
    private val commentList = mutableListOf<Comment>()
    private lateinit var binding: FragmentCommentBinding  // ViewBinding 사용

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // ViewBinding을 이용한 레이아웃 인플레이트
        binding = FragmentCommentBinding.inflate(inflater, container, false)

        // RecyclerView 설정
        val recyclerView: RecyclerView = binding.commentRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        commentAdapter = CommentAdapter(commentList)
        recyclerView.adapter = commentAdapter

        // 댓글 입력과 버튼 설정
        val commentPostButton: Button = binding.commentPostButton
        val commentInput: EditText = binding.commentInput

        commentPostButton.setOnClickListener {
            val content = commentInput.text.toString()
            if (content.isNotEmpty()) {
                // 댓글 추가
                val newComment = Comment(userName = "사용자", content = content)
                commentAdapter.addComment(newComment)
                commentInput.text.clear()  // 댓글 입력창 비우기
            }
        }

        return binding.root  // Fragment의 뷰 반환
    }
}
