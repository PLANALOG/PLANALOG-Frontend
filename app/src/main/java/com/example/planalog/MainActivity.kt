package com.example.planalog

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.planalog.databinding.ActivityMainBinding
import com.example.planalog.ui.comment.com.example.planalog.ui.home.calender.CalendarFragment
import com.example.planalog.ui.comment.com.example.planalog.ui.home.notify.NotifyFragment
import com.example.planalog.ui.home.HomeFragment
import com.example.planalog.ui.post.PostFragment
import com.example.planalog.ui.profile.ProfileFragment
import com.example.planalog.ui.search.SearchFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //초기 화면 설정
        replaceFragment(HomeFragment())

        // 바텀 내비게이션 클릭 리스너
        binding.mainBottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_fragment -> replaceFragment(HomeFragment())
                R.id.search_fragment -> {
                    replaceFragment(SearchFragment())
                    hideCalendarFragment()  // 캘린더 숨기기
                    hideNotificationFragment()
                }
                R.id.post_fragment -> {
                    replaceFragment(PostFragment())
                    hideCalendarFragment()  // 캘린더 숨기기
                    hideNotificationFragment()
                }
                R.id.profile_fragment -> {
                    replaceFragment(ProfileFragment())
                    hideCalendarFragment()  // 캘린더 숨기기
                    hideNotificationFragment()
                }
            }
            true
        }

        // 캘린더 이동 (홈 화면에서 캘린더 아이콘 클릭 시)
        showCalendarFragment()
        showrNotifyFragment()
    }

    private fun showCalendarFragment() {
        binding.mainCalendarIc.setOnClickListener {
            // 캘린더 프래그먼트를 홈 화면에서만 표시
            val calendarFragment =
                supportFragmentManager.findFragmentByTag(CalendarFragment::class.java.simpleName)
            if (calendarFragment == null) {
                val transaction = supportFragmentManager.beginTransaction()
                val newCalendarFragment = CalendarFragment()
                transaction.replace(
                    R.id.fragment_calendar,
                    newCalendarFragment,
                    CalendarFragment::class.java.simpleName
                )
                transaction.addToBackStack(null)  // 뒤로 가기 스택에 추가
                transaction.commit()
            }
            binding.fragmentCalendar.visibility = View.VISIBLE
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.main_frm, fragment).commit()
    }

    private fun hideCalendarFragment() {
        // 캘린더 프래그먼트를 숨긴다.
        binding.fragmentCalendar.visibility = View.GONE
    }

    private fun showrNotifyFragment() {
        binding.mainBellIc.setOnClickListener {
            // 캘린더 프래그먼트를 홈 화면에서만 표시
            val notifyFragment =
                supportFragmentManager.findFragmentByTag(NotifyFragment::class.java.simpleName)
            if (notifyFragment == null) {
                val transaction = supportFragmentManager.beginTransaction()
                val newNotifyFragment = NotifyFragment()
                transaction.replace(
                    R.id.fragment_notify,
                    newNotifyFragment,
                    CalendarFragment::class.java.simpleName
                )
                transaction.addToBackStack(null)  // 뒤로 가기 스택에 추가
                transaction.commit()
            }
            binding.fragmentNotify.visibility = View.VISIBLE
        }
    }

    private fun hideNotificationFragment() {
        // 알림 프래그먼트가 있는 경우 숨기기
        binding.fragmentNotify.visibility = View.GONE
    }
}