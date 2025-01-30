package com.example.planalog

import android.os.Bundle
import android.util.Log
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

        // 초기 프래그먼트 설정
        val type = intent.getStringExtra("type") ?: ""
        replaceFragment(HomeFragment().apply {
            arguments = Bundle().apply {
                putString("type", type)
                Log.d("타입", "Received 타입: $type")
            }
        })

        // 바텀 내비게이션 리스너 설정
        binding.mainBottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_fragment -> replaceFragment(HomeFragment())
                R.id.search_fragment -> replaceFragment(SearchFragment())
                R.id.post_fragment -> replaceFragment(PostFragment())
                R.id.profile_fragment -> replaceFragment(ProfileFragment())
            }
            true
        }

        // 캘린더 아이콘 클릭 리스너
        binding.mainCalendarIc.setOnClickListener {
            showFragment(CalendarFragment(), R.id.fragment_calendar)
        }

        // 알림 아이콘 클릭 리스너
        binding.mainBellIc.setOnClickListener {
            showFragment(NotifyFragment(), R.id.fragment_notify)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, fragment)
            .commit()
    }

    private fun showFragment(fragment: Fragment, containerId: Int) {
        val tag = fragment::class.java.simpleName
        val existingFragment = supportFragmentManager.findFragmentByTag(tag)
        if (existingFragment == null) {
            supportFragmentManager.beginTransaction()
                .replace(containerId, fragment, tag)
                .addToBackStack(null)
                .commit()
        }
    }
}
