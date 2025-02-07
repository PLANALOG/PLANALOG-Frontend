package com.example.planalog.ui.friends

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.planalog.databinding.ActivityFriendlistBinding
import com.google.android.material.tabs.TabLayoutMediator

class FriendListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFriendlistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendlistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tab 및 ViewPager 초기화
        setupViewPagerWithTabs()
    }

    private fun setupViewPagerWithTabs() {
        val tabTitles = listOf("내가 응원하는", "나를 응원하는")
        val adapter = FriendListPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
}