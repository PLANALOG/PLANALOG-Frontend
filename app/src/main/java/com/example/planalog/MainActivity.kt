package com.example.planalog

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.planalog.databinding.ActivityMainBinding
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
                R.id.search_fragment -> replaceFragment(SearchFragment())
                R.id.post_fragment -> replaceFragment(PostFragment())
                R.id.profile_fragment -> replaceFragment(ProfileFragment())
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.main_frm, fragment).commit()
    }
}