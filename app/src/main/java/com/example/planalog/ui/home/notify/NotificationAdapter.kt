package com.example.planalog.ui.home.notify

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.planalog.databinding.ItemNotificationBinding
import com.example.planalog.databinding.ItemNotificationDateBinding
import com.example.planalog.network.api.FriendApiHelper
import com.example.planalog.network.api.NoticeApiHelper
import com.example.planalog.network.api.UserApiHelper
import com.example.planalog.network.friend.FriendRequestCallback
import com.example.planalog.network.notice.NotificationItem
import com.example.planalog.ui.comment.com.example.planalog.ui.home.notify.ConfirmDialogFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class NotificationAdapter(
    private val fragment: Fragment,
    private val notificationList: MutableList<NotificationItem>,
    private val noticeApiHelper: NoticeApiHelper,
    var onNotificationDeleted: ((NotificationItem) -> Unit)? = null
) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(notification: NotificationItem) {

            binding.notifyTv.text = notification.message

            // 수락 버튼
            binding.agreeBtn.setOnClickListener {
                notification.onAccept?.invoke(notification.fromUserId)
                Log.d("NotificationAdapter", "수락 버튼 클릭됨: ${notification.fromUserId}")
                ConfirmDialogFragment().show(fragment.parentFragmentManager, "ConfirmDialog")
            }

            // 거절 버튼
            binding.disagreeBtn.setOnClickListener {
//                notification.onReject?.invoke(notification.fromUserId, notification.id.toString())
                Log.d("NotificationAdapter", "거절 버튼 클릭됨: ${notification.fromUserId}, ID: ${notification.id}")

                noticeApiHelper.deleteNotification(notification.id.toString())
                Log.d("NotificationAdapter", "📌 서버에 삭제 요청 보냄: ${notification.id}")

                // 상대 기기에서 요청한 friendId 정보를 가져올 수 없어서 다른 기기의 ui 업데이트는 보류
//                val friendApiHelper = FriendApiHelper(context)
//
//                //  저장된 friendId 가져오기
//                val spf = context.getSharedPreferences("follow_status", Context.MODE_PRIVATE)
//                val friendFollowId = spf.getString("follow_id_${notification.fromUserId}", null)
//
//                if (friendFollowId != null) {
//                    //  올바른 friendId를 사용하여 친구 삭제 요청
//                    friendApiHelper.deleteFollowers(friendFollowId, object : FriendRequestCallback {
//                        override fun onRequestSuccess(friendId: String?) {
//                            Log.d("NotificationAdapter", "친구 삭제 성공")
//                            sendUpdateToFriendPage(notification.fromUserId)  // 상대방 화면 업데이트
//                        }
//
//                        override fun onRequestFailure(message: String) {
//                            Log.e("NotificationAdapter", "친구 삭제 실패: $message")
//                        }
//                    })
//                } else {
//                    Log.e("NotificationAdapter", "저장된 friendId가 없음")
//                }

                // 현재 클릭된 아이템을 리스트에서 삭제
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && position < notificationList.size) {
                    val removedNotification = notificationList[position]

                    // ✅ ViewModel에서 해당 알림 삭제
                    onNotificationDeleted?.invoke(removedNotification)

                    // ✅ RecyclerView에서 아이템 삭제
                    notificationList.removeAt(position)
                    notifyItemRemoved(position)

                    Log.d("NotificationAdapter", "✅ UI에서 아이템 삭제됨. 현재 남은 개수: ${notificationList.size}")
                } else {
                    Log.e("NotificationAdapter", "❌ 잘못된 삭제 요청: position=$position, size=${notificationList.size}")
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
