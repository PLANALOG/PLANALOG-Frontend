package com.example.planalog.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.planalog.databinding.ItemMypageMomentBinding
import com.example.planalog.network.user.MypageMoment

class MypageMomentAdapter(private var moments: List<MypageMoment>) :
    RecyclerView.Adapter<MypageMomentAdapter.MomentViewHolder>() {

    inner class MomentViewHolder(private val binding: ItemMypageMomentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(moment: MypageMoment) {
            binding.postTitle.text = moment.title
        //    binding.postDate.text = moment.createdAt.substring(0, 10)  // 날짜 포맷 (YYYY-MM-DD)
        //    binding.postReply.text = "공감 ${moment.cheeringCount} 댓글 ${moment.commentCount}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MomentViewHolder {
        val binding = ItemMypageMomentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MomentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MomentViewHolder, position: Int) {
        holder.bind(moments[position])
    }

    override fun getItemCount(): Int = moments.size

    fun updateData(newMoments: List<MypageMoment>) {
        moments = newMoments
        notifyDataSetChanged()  // 데이터 변경 시 어댑터 새로고침
    }
}
