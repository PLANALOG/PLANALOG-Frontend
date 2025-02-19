package com.example.planalog

import NotifyViewModel
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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

        //  ViewModel을 Application 범위에서 가져오기
        val notifyViewModel = NotifyViewModel.getInstance(application)
        val type = intent.getStringExtra("type") ?: ""
        val existingFragment = supportFragmentManager.findFragmentById(R.id.main_frm)

        Log.d("MainActivity", "📌 MainActivity ViewModel 해시코드: ${notifyViewModel.hashCode()}")

        if (existingFragment == null) {
            val homeFragment = HomeFragment().apply {
                arguments = Bundle().apply {
                    putString("type", type)
                }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, homeFragment, "HomeFragment")
                .commit()
        }

        // 📌 UI 업데이트 트리거 전달
        val result = Bundle().apply { putString("type", type) }
        supportFragmentManager.setFragmentResult("update_ui", result)

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
            replaceFragment(CalendarFragment(), addToBackStack = false) // 백 스택에 추가 X
        }

        // 알림 아이콘 클릭 리스너
        binding.mainBellIc.setOnClickListener {
            replaceFragment(NotifyFragment(), addToBackStack = false) // 백 스택에 추가 X
        }
    }

    // 프래그먼트 변경 메서드 (캘린더 & 알림은 백 스택에 추가하지 않음)
    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.main_frm)

        // 🛠 동일한 프래그먼트를 중복 추가 방지
        if (currentFragment?.javaClass == fragment.javaClass) {
            Log.d("MainActivity", "❌ 중복된 프래그먼트 추가 방지: ${fragment.javaClass.simpleName}")
            return
        }

        val transaction = supportFragmentManager.beginTransaction().replace(R.id.main_frm, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commitAllowingStateLoss()
    }

    // 뒤로 가기 버튼 오버라이드 (캘린더 & 알림에서 뒤로 가기 시 홈으로 이동)
    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.main_frm)

        if (currentFragment is CalendarFragment || currentFragment is NotifyFragment) {
            //  백 스택에서 한 단계 뒤로 가기 (홈이 자동으로 남도록)
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                //  현재 프래그먼트가 HomeFragment인지 확인하고 중복 추가 방지
                val existingHomeFragment = supportFragmentManager.findFragmentByTag("HomeFragment")
                if (existingHomeFragment == null) {
                    replaceFragment(HomeFragment(), addToBackStack = false)
                }
            }

            //  바텀 내비게이션의 선택된 아이템을 '홈'으로 변경
            binding.mainBottomNav.selectedItemId = R.id.home_fragment
        } else {
            //  일반적인 경우 백 스택이 남아있다면 popBackStack() 실행
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                super.onBackPressed() // 앱 종료
            }
        }
    }
}
