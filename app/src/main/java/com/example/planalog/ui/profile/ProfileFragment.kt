package com.example.planalog.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.planalog.R
import com.example.planalog.databinding.FragmentProfileBinding
import com.google.android.material.tabs.TabLayout

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        setupUI()

        return binding.root
    }

    private fun setupUI() {
        // 프로필 추가 아이콘 클릭 이벤트 설정
        binding.icAddprofile.setOnClickListener {
            val intent = Intent(requireContext(), EditcharacterActivity::class.java)
            startActivity(intent)
        }

        // 프로필 편집 버튼 클릭 이벤트 설정
        binding.editProfileButton.setOnClickListener {
            val intent = Intent(requireContext(), EditprofileActivity::class.java)
            startActivity(intent)
        }

        // 플래너 설정 버튼 클릭 이벤트 설정
        binding.plannerSettingButton.setOnClickListener {
            // 플래너 설정 버튼 클릭 시 실행될 동작
        }

        // TabLayout에 탭 추가
        setupTabLayout()

        // TabLayout 탭 선택 이벤트 설정
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (it.position) {
                        0 -> binding.tabLayout.setBackgroundResource(R.color.main_2)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // 탭 선택 해제 시 동작
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // 이미 선택된 탭 다시 클릭 시 동작
            }
        })
    }

    private fun setupTabLayout() {
        // TabLayout에 탭 추가
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.moment))
    }
}
