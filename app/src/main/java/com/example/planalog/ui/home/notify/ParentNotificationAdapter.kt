package com.example.planalog.ui.home.notify

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.planalog.databinding.ItemNotificationDateBinding
import com.example.planalog.network.api.NoticeApiHelper
import com.example.planalog.network.notice.NotificationItem
import com.example.planalog.utils.DisplayUtil.dpToPx
import com.example.planalog.utils.VerticalSpaceItemDecoration
import java.text.SimpleDateFormat
import java.util.*

class ParentNotificationAdapter(
    private var context: Context,
    private val fragment: Fragment,
    private var groupedNotifications: Map<String, List<NotificationItem>>,
    var onNotificationDeleted: ((NotificationItem) -> Unit)? = null
) : RecyclerView.Adapter<ParentNotificationAdapter.DateSectionViewHolder>() {

    inner class DateSectionViewHolder(private val binding: ItemNotificationDateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(date: String, notifications: List<NotificationItem>) {
            binding.notifyTodayTv.text = date

            // 내부 RecyclerView 설정
            val noticeApiHelper = NoticeApiHelper(context)
            val childAdapter = NotificationAdapter( fragment, notifications.toMutableList(), noticeApiHelper)

            binding.notifyRv.apply {
                layoutManager = LinearLayoutManager(binding.root.context)
                adapter = childAdapter

                val pxValue = context.dpToPx(16)
                if (itemDecorationCount == 0) {
                    addItemDecoration(VerticalSpaceItemDecoration(pxValue))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateSectionViewHolder {
        val binding = ItemNotificationDateBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DateSectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DateSectionViewHolder, position: Int) {
        val date = groupedNotifications.keys.toList()[position]
        val notifications = groupedNotifications[date] ?: emptyList()
        holder.bind(date, notifications)
    }

    override fun getItemCount(): Int = groupedNotifications.size

    fun updateData(newNotifications: List<NotificationItem>) {
        groupedNotifications = groupNotificationsByDate(newNotifications)
        notifyDataSetChanged()
    }

    private fun groupNotificationsByDate(notifications: List<NotificationItem>): Map<String, List<NotificationItem>> {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()) // 출력용 포맷
        val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()) // 입력용 포맷
        iso8601Format.timeZone = TimeZone.getTimeZone("UTC") // UTC 시간 변환

        // 오늘과 어제의 날짜 가져오기
        val calendar = Calendar.getInstance()
        val today = dateFormat.format(calendar.time) // 오늘 날짜

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = dateFormat.format(calendar.time) // 어제 날짜

        val groupedMap = mutableMapOf<String, MutableList<NotificationItem>>()

        notifications.forEach { notification ->
            try {
                val date = iso8601Format.parse(notification.createdAt) ?: Date()
                val formattedDate = dateFormat.format(date) // "yyyy.MM.dd" 형식으로 변환

                //  오늘/어제 변환 적용
                val displayDate = when (formattedDate) {
                    today -> "오늘"
                    yesterday -> "어제"
                    else -> formattedDate
                }

                //  날짜별로 그룹화
                if (!groupedMap.containsKey(displayDate)) {
                    groupedMap[displayDate] = mutableListOf()
                }
                groupedMap[displayDate]?.add(notification)

                // 디버깅 로그 추가
                Log.d("ParentNotificationAdapter", " 알림 날짜 변환: ${notification.createdAt} → $displayDate")

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ParentNotificationAdapter", " 날짜 변환 실패: ${notification.createdAt}", e)
            }
        }

        return groupedMap
    }
}