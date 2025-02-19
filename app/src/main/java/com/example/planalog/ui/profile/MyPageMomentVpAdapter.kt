package com.example.planalog.ui.profile

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MyPageMomentVpAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int  = 1

    override fun createFragment(position: Int): Fragment {
        return MyPageMomentFragment()
    }

}