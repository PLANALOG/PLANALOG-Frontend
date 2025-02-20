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
import com.example.planalog.ui.home.notify.ParentNotificationAdapter
import com.example.planalog.utils.DisplayUtil.dpToPx
import com.example.planalog.utils.VerticalSpaceItemDecoration
import com.example.planalog.utils.convertDateToLabel

class NotifyFragment : Fragment() {
    private var _binding: FragmentNotifyBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var notifyViewModel: NotifyViewModel
    private lateinit var noticeApiHelper: NoticeApiHelper

    private lateinit var parentAdapter: ParentNotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotifyBinding.inflate(inflater, container, false)

        setupRecyclerView()

        //  ViewModel을 Application 범위에서 가져오기
        notifyViewModel = NotifyViewModel.getInstance(requireActivity().application)

        Log.d("NotifyFragment", "📌 NotifyFragment ViewModel 해시코드: ${notifyViewModel.hashCode()}")

        notifyViewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            Log.d("NotifyFragment", "📌 LiveData 변경 감지됨. 알림 개수: ${notifications.size}")

            notifications.forEachIndexed { index, notification ->
                Log.d("NotifyFragment", "📌 [$index] ${notification.fromUserId}: ${notification.message}")
            }

                if (notifications.isEmpty()) {
                    binding.notifyRv.visibility = View.GONE
                } else {
                    binding.notifyRv.visibility = View.VISIBLE
                    parentAdapter.updateData(notifications)
                }
        }

        noticeApiHelper = NoticeApiHelper(requireContext())
        noticeApiHelper.getNotices { notices ->
            notifyViewModel.refreshNotifications(notices)
        }

        return binding.root
    }

    private fun setupRecyclerView() {

        noticeApiHelper = NoticeApiHelper(requireContext())

//        notificationAdapter = NotificationAdapter(requireContext(),this,mutableListOf(), noticeApiHelper)
//        binding.notifyRv.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = notificationAdapter
//
//            val pxValue = requireContext().dpToPx(16)
//            addItemDecoration(VerticalSpaceItemDecoration(pxValue))
//        }

        parentAdapter = ParentNotificationAdapter(requireContext(), this, emptyMap()).apply {
            onNotificationDeleted = { deletedNotification ->
                notifyViewModel.removeNotification(deletedNotification)
            }
        }
        binding.notifyRv.layoutManager = LinearLayoutManager(requireContext())
        binding.notifyRv.adapter = parentAdapter

        // 알림 데이터를 가져와 업데이트
    }
}