package com.example.planalog.ui.comment.com.example.planalog.ui.home.notify

import NotifyViewModel
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planalog.databinding.FragmentNotifyBinding
import com.example.planalog.network.api.NoticeApiHelper
import com.example.planalog.ui.home.notify.NotificationAdapter
import com.example.planalog.ui.home.notify.NotificationItem
import com.example.planalog.utils.DisplayUtil.dpToPx
import com.example.planalog.utils.VerticalSpaceItemDecoration
import com.example.planalog.utils.convertDateToLabel

class NotifyFragment : Fragment() {
    private var _binding: FragmentNotifyBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationAdapter: NotificationAdapter
    private val notificationList = mutableListOf<NotificationItem>()
    private lateinit var notifyViewModel: NotifyViewModel
    private lateinit var noticeApiHelper: NoticeApiHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotifyBinding.inflate(inflater, container, false)

        setupRecyclerView()

        noticeApiHelper = NoticeApiHelper(requireContext())
        noticeApiHelper.getNotices()

        // ✅ ViewModel을 Application 범위에서 가져오기
        notifyViewModel = NotifyViewModel.getInstance(requireActivity().application)

        Log.d("NotifyFragment", "📌 NotifyFragment ViewModel 해시코드: ${notifyViewModel.hashCode()}")

        notifyViewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            Log.d("NotifyFragment", "📌 LiveData 변경 감지됨. 알림 개수: ${notifications.size}")

            notifications.forEachIndexed { index, notification ->
                Log.d("NotifyFragment", "📌 [$index] ${notification.userId}: ${notification.message}")
            }

            notificationAdapter.updateNotifications(notifications)
        }

        // 예제 날짜 (추후 서버에서 불러온 날짜로 변경 가능)
        val todayDate = "2025-02-12" // 오늘 날짜 예시
        val yesterdayDate = "2025-02-11" // 어제 날짜 예시
        val olderDate = "2025-02-09" // 그 이전 날짜 예시

        // 날짜 변환 함수 적용
        binding.notifyTodayTv.text = convertDateToLabel(todayDate)
        binding.notifyYesterdayTv.text = convertDateToLabel(yesterdayDate)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.agreeBtn.setOnClickListener {
            ConfirmDialogFragment().show(parentFragmentManager, "ConfirmDialog")
        }

        binding.agreeBtn2.setOnClickListener {
            ConfirmDialogFragment().show(parentFragmentManager, "ConfirmDialog")
        }
    }

    private fun setupRecyclerView() {

        noticeApiHelper = NoticeApiHelper(requireContext())

        notificationAdapter = NotificationAdapter(this,mutableListOf(), noticeApiHelper)
        binding.notifyRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter

            val pxValue = requireContext().dpToPx(16)
            addItemDecoration(VerticalSpaceItemDecoration(pxValue))
        }
    }
}