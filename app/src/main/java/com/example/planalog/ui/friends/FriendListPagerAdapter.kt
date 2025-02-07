package com.example.planalog.ui.friends

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class FriendListPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FriendListFragment.newInstance("내가 응원하는")
            else -> FriendListFragment.newInstance("나를 응원하는")
        }
    }
}