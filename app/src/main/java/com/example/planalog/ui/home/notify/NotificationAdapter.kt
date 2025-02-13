package com.example.planalog.ui.home.notify

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.planalog.databinding.ItemNotificationBinding
import com.example.planalog.network.api.NoticeApiHelper
import com.example.planalog.ui.comment.com.example.planalog.ui.home.notify.ConfirmDialogFragment

class NotificationAdapter(
    private val fragment: Fragment,
    private val notificationList: MutableList<NotificationItem>,
    private val noticeApiHelper: NoticeApiHelper,
) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(notification: NotificationItem) {
            binding.profileIv.setImageResource(notification.profileImageRes)
            binding.notifyTv.text = notification.message

            // 수락 버튼
            binding.agreeBtn.setOnClickListener {
                notification.onAccept?.invoke(notification.userId)
                Log.d("NotificationAdapter", "수락 버튼 클릭됨: ${notification.userId}")
                ConfirmDialogFragment().show(fragment.parentFragmentManager, "ConfirmDialog")
            }

            // 거절 버튼
            binding.disagreeBtn.setOnClickListener {
                notification.onReject?.invoke(notification.userId, notification.noticeId)
                Log.d("NotificationAdapter", "거절 버튼 클릭됨: ${notification.userId}, ID: ${notification.noticeId}")

                noticeApiHelper.deleteNotification(notification.noticeId)

                // 현재 클릭된 아이템을 리스트에서 삭제
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    notificationList.removeAt(position)
                    notifyItemRemoved(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notificationList[position])
    }

    override fun getItemCount(): Int = notificationList.size

    fun updateNotifications(newNotifications: List<NotificationItem>) {
        val oldSize = notificationList.size
        notificationList.clear()
        notificationList.addAll(newNotifications)

        if (oldSize == 0) {
            notifyDataSetChanged()  // 첫 데이터 추가 시 전체 갱신
        } else {
            notifyItemInserted(0)  // 새로운 아이템만 갱신하여 UI 성능 개선
        }

        Log.d("NotificationAdapter", "RecyclerView 데이터 갱신 완료: ${notificationList.size}개")
    }
}
