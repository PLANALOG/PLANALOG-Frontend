package com.example.planalog.ui.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.planalog.R
import com.example.planalog.databinding.ItemMypageMomentBinding
import com.example.planalog.network.user.response.MypageMoment

class MypageMomentAdapter(
    private val context: Context,
    private var moments: List<MypageMoment>
) :
    RecyclerView.Adapter<MypageMomentAdapter.MomentViewHolder>() {

    inner class MomentViewHolder(private val binding: ItemMypageMomentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(moment: MypageMoment) {
            binding.postTitle.text = moment.title

            val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val nickname = sharedPreferences.getString("nickname", "")
            binding.postUserName.text = nickname
            binding.postDate.text = moment.date
            binding.postReply.text = "공감 ${moment.likingCount} 댓글 ${moment.commentCount}"

            Glide.with(binding.postImg.context)
                .load(moment.thumbnailURL)  // 서버에서 받은 URL을 로드
                .placeholder(R.drawable.ic_logo_dark) // 로딩 중 보여줄 이미지
                .error(R.drawable.ic_logo_dark) // 에러 발생 시 보여줄 이미지
                .into(binding.postImg)
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
