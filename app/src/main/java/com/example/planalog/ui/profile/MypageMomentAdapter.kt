package com.example.planalog.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.planalog.R
import com.example.planalog.databinding.ItemMypageMomentBinding
import com.example.planalog.network.api.PostApiHelper
import com.example.planalog.network.user.response.MypageMoment
import com.example.planalog.ui.post.PostDetailFragment

class MypageMomentAdapter(
    private val context: Context,
    private var moments: List<MypageMoment>,
    private var momentId: Int = -1
) : RecyclerView.Adapter<MypageMomentAdapter.MomentViewHolder>() {

    inner class MomentViewHolder(private val binding: ItemMypageMomentBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {


        fun bind(moment: MypageMoment) {
            momentId = moment.momentId
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

            // 클릭 이벤트 설정 (PostDetailFragment 이동)
            binding.postTitle.setOnClickListener(this)
            binding.postUserName.setOnClickListener(this)
            binding.postDate.setOnClickListener(this)
            binding.postReply.setOnClickListener(this)

            binding.moreIv.setOnClickListener {
                showPopupMenu(it)
            }
        }

        override fun onClick(view: View?) {
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
            transaction.addToBackStack(null) // 뒤로 가기 기능 추가
            transaction.commit()
        }
    }


    private fun showPopupMenu(anchor: View) {
        val popupView = LayoutInflater.from(context).inflate(R.layout.comment_popup_menu, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        anchor.post {
            val location = IntArray(2)
            anchor.getLocationOnScreen(location) // 버튼의 화면 위치 가져오기

            val anchorWidth = anchor.width
            val anchorHeight = anchor.height

            // 팝업의 크기를 측정하기 위해 필요
            popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            val popupWidth = popupView.measuredWidth
            val popupHeight = popupView.measuredHeight

            // 팝업을 버튼의 왼쪽 위로 정렬
            val xOffset = location[0] - popupWidth -20 // 버튼의 왼쪽으로 이동
            val yOffset = location[1] - popupHeight / 2 + anchorHeight / 2 - 60// 너무 위로 안 가게 조정

            popupWindow.showAtLocation(anchor, android.view.Gravity.NO_GRAVITY, xOffset, yOffset)
        }
        // 버튼 클릭 리스너 설정
        val editButton = popupView.findViewById<TextView>(R.id.edit_button)
        val deleteButton = popupView.findViewById<TextView>(R.id.delete_button)

        editButton.setOnClickListener {
            popupWindow.dismiss()
            // 편집 버튼 클릭 시 처리할 로직 추가
            // 예: 편집 화면으로 이동
        }

        deleteButton.setOnClickListener {
            popupWindow.dismiss()

            val postApiHelper = PostApiHelper(context)
            postApiHelper.deleteMoment(momentId) { success, errorMessage ->
                if (success) {
                    Toast.makeText(context, "모먼트가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    removeMomentFromList(momentId) // ✅ 삭제 후 어댑터 갱신 (bind() 재호출)
                } else {
                    Toast.makeText(context, "삭제 실패: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
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

    private fun removeMomentFromList(momentId: Int) {
        val position = moments.indexOfFirst { it.momentId == momentId }
        if (position != -1) {
            moments = moments.toMutableList().apply { removeAt(position) }
            notifyItemRemoved(position) // ✅ 특정 아이템만 삭제하여 성능 최적화
            notifyItemRangeChanged(position, moments.size) // ✅ 삭제된 이후의 아이템 인덱스 갱신
        }
    }

}
