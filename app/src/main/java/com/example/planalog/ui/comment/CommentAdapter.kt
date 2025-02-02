package com.example.planalog.ui.comment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.planalog.databinding.ItemCommentBinding
import com.example.planalog.ui.comment.com.example.planalog.ui.comment.Comment

class CommentAdapter(private val comments: MutableList<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]

        // 데이터 바인딩
        holder.binding.commentUserName.text = comment.userName
        holder.binding.commentContent.text = comment.content
        holder.binding.commentProfileImage.setImageResource(comment.profileImage)
    }

    override fun getItemCount(): Int = comments.size

    // 댓글 추가 메서드
    fun addComment(comment: Comment) {
        comments.add(comment)
        notifyItemInserted(comments.size - 1) // 새 댓글이 추가된 위치만 갱신
    }
}
