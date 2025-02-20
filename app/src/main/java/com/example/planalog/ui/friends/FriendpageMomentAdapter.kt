package com.example.planalog.ui.friends

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.planalog.R
import com.example.planalog.databinding.ItemUserpageMomentBinding
import com.example.planalog.network.friend.response.FriendpageMoment

class FriendpageMomentAdapter(
    private val context: Context,
    private var moments: List<FriendpageMoment>
) : RecyclerView.Adapter<FriendpageMomentAdapter.MomentViewHolder>() {

    inner class MomentViewHolder(private val binding: ItemUserpageMomentBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private var momentId: Int = -1

        fun bind(moment: FriendpageMoment) {
            this.momentId = moment.momentId

            binding.postTitle.text = moment.title
            binding.postUserName.text = moment.userName

            val formattedDate = moment.date ?: "0000-00-00"
            binding.postDate.text = formattedDate.substring(0, 10)

            binding.postReply.text = "공감 ${moment.likingCount} 댓글 ${moment.commentCount}"

            Glide.with(binding.postImg.context)
                .load(moment.thumbnailURL)
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
            Log.d("FriendpageMomentAdapter", "Clicked momentId: $momentId")
            (context as? FriendpageActivity)?.openPostDetailFragment(momentId)  // ✅ `FriendpageActivity`의 메서드 호출
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

    fun updateData(newMoments: List<FriendpageMoment>) {
        moments = newMoments
        notifyDataSetChanged()
        Log.d("FriendpageMomentAdapter", "Updated moment list size: ${moments.size}") // ✅ 모먼트 개수 로그 출력
    }
}
