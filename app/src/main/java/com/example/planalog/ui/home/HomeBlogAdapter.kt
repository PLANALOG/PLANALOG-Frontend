package com.example.planalog.ui.home

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.planalog.R
import com.example.planalog.databinding.ItemHomeBlogBinding
import com.example.planalog.ui.comment.CommentFragment

class HomeBlogAdapter(
    private val context: Context,
    private val blogList: List<HomeBlogItem>,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<HomeBlogAdapter.HomeBlogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeBlogViewHolder {
        val binding = ItemHomeBlogBinding.inflate(LayoutInflater.from(context), parent, false)
        return HomeBlogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeBlogViewHolder, position: Int) {
        val item = blogList[position]
        with(holder.binding) {
            homeBlogUserName.text = item.userName
            homeBlogDateTv.text = item.blogDate
            homeBlogDetailTv.text = item.blogDetail
            homeLikeCountTv.text = item.likeCount.toString()

            homeBlogFriendIv.setImageResource(item.profileImageRes)
            homeBlogImgExIv.setImageResource(item.blogImageRes)
            homeLikeIv.setImageResource(item.likeImageRes)
            homeReplyIv.setImageResource(item.replyImageRes)

            // 기존 간격을 유지하면서 마지막 아이템에 추가 여백 설정
//            val params = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
//            val defaultBottomMargin = 20 // 기존 아이템 간 기본 여백
//            val extraBottomMargin = 60 // 바텀 내비게이션 높이만큼 추가 여백
//
//            params.bottomMargin = if (position == blogList.size - 1) {
//                defaultBottomMargin + extraBottomMargin
//            } else {
//                defaultBottomMargin
//            }
//            holder.itemView.layoutParams = params

            // home_reply_iv 클릭 리스너 추가
            homeReplyIv.setOnClickListener {
                // CommentFragment 이동
                val transaction = fragmentManager.beginTransaction()
                val fragment = CommentFragment()
                transaction.replace(R.id.main_frm, fragment)
                transaction.addToBackStack(null)  // 🔹 뒤로 가기 기능 추가
                transaction.commit()
            }
        }
    }

    override fun getItemCount(): Int = blogList.size

    inner class HomeBlogViewHolder(val binding: ItemHomeBlogBinding) : RecyclerView.ViewHolder(binding.root)
}