package com.example.planalog.ui.friends

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.planalog.R
import com.example.planalog.databinding.ItemUserpageMomentBinding
import com.example.planalog.network.friend.response.FriendpageMoment
import com.example.planalog.ui.post.PostDetailFragment

class FriendpageMomentAdapter(
    private val context: Context,
    private var moments: List<FriendpageMoment>
) : RecyclerView.Adapter<FriendpageMomentAdapter.MomentViewHolder>() {

    inner class MomentViewHolder(private val binding: ItemUserpageMomentBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        fun bind(moment: FriendpageMoment) {
            binding.postTitle.text = moment.title
            binding.postUserName.text = "친구"
            binding.postDate.text = moment.createdAt.substring(0, 10)  // "YYYY-MM-DD"
            binding.postReply.text = "공감 0 댓글 0"

            // 썸네일 이미지 로드
            Glide.with(binding.postImg.context)
                .load(moment.thumbnailUrl)  // ✅ API 응답에 맞게 thumbnailUrl 사용
                .placeholder(R.drawable.ic_logo_dark)
                .error(R.drawable.ic_logo_dark)
                .into(binding.postImg)

            // 클릭 이벤트 추가
            binding.postImg.setOnClickListener(this)
            binding.postTitle.setOnClickListener(this)
            binding.postUserName.setOnClickListener(this)
            binding.postDate.setOnClickListener(this)
            binding.postReply.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            val momentId = moments[adapterPosition].momentId
            openPostDetailFragment(momentId)
        }

        private fun openPostDetailFragment(momentId: Int) {
            val fragment = PostDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt("momentId", momentId)
                }
            }

            val transaction = (context as FragmentActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.main_frm, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MomentViewHolder {
        val binding = ItemUserpageMomentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MomentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MomentViewHolder, position: Int) {
        holder.bind(moments[position])
    }

    override fun getItemCount(): Int = moments.size

    fun updateData(newMoments: List<FriendpageMoment>) {  // ✅ `FriendpageMoment` 사용
        moments = newMoments
        notifyDataSetChanged()
    }
}
