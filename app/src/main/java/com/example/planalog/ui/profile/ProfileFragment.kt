package com.example.planalog.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.planalog.R
import com.example.planalog.databinding.FragmentProfileBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.api.UserApiHelper
import com.example.planalog.network.user.response.MypageMoment
import com.example.planalog.network.user.response.MypageResponse
import com.example.planalog.network.user.MypageService
import com.example.planalog.network.user.response.UserResponse
import com.example.planalog.network.user.UserService

import com.example.planalog.network.user.FriendCountResponse
import com.example.planalog.network.user.FriendcountService
import com.example.planalog.network.user.response.UserInfo
import com.example.planalog.ui.friends.FriendListActivity
import com.google.android.material.tabs.TabLayoutMediator


import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var mypageService: MypageService
    private lateinit var friendService: FriendcountService
    private var mypageMomentAdapter: MypageMomentAdapter? = null  // 어댑터를 한 번만 초기화

    private lateinit var userApiHelper : UserApiHelper

    private var information = arrayListOf("Moment")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Retrofit을 이용해 초기화
        mypageService = RetrofitClient.create(MypageService::class.java, requireContext())
        friendService = RetrofitClient.create(FriendcountService::class.java, requireContext())

        // 유저 정보 가져오기
        loadUserProfile()
        fetchFriendCount()
        setupRecyclerView()
        setupUI()


        binding.friendCountLayout.setOnClickListener {
            // FriendListActivity로 이동하는 Intent 설정
            val intent = Intent(requireContext(), FriendListActivity::class.java)
            startActivity(intent)
        }


        return binding.root
    }
    private fun setupRecyclerView() {
        //뷰 페이저 연결
        val myPageMomentVpAdapter = MyPageMomentVpAdapter(this)
        binding.mypageMomentContentVp.adapter = myPageMomentVpAdapter

        //탭 레이아웃 연결
        TabLayoutMediator(binding.tabLayout, binding.mypageMomentContentVp){
                tab, position ->
            tab.text = information[position]
        }.attach()
    }

    private fun loadUserProfile() {
        userApiHelper = UserApiHelper(requireContext())

        userApiHelper.getUserInfo(
            onSuccess = { userInfo ->
                    updateUI(userInfo)  // UI에 유저 정보 표시
                    Log.d("ProfileFragment", "유저 정보: $userInfo")
            },
            onFailure = { errorMsg ->
                Toast.makeText(requireContext(), "유저 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT)
                    .show()
            }
        )
    }

    private fun updateUI(userInfo: UserInfo) {
        if (!isAdded) {
            // Fragment가 Activity에 붙어 있지 않으면 UI 업데이트를 중단
            return
        }

        binding.userName.text = userInfo.nickname
        binding.introText.text = userInfo.introduction

        context?.let {
            Glide.with(it)
                .load(userInfo.profileImage ?: R.drawable.ic_myprofile)  // 기본 프로필 이미지 설정
                .into(binding.profileImage)
        }
    }


    private fun setupUI() {
        // 프로필 추가 아이콘 클릭 이벤트 설정

        // 프로필 편집 버튼 클릭 이벤트 설정
        binding.editProfileButton.setOnClickListener {
            val intent = Intent(requireContext(), EditprofileActivity::class.java)
            editProfileLauncher.launch(intent)
        }

        // 플래너 설정 버튼 클릭 이벤트 설정
        binding.plannerSettingButton.setOnClickListener {
            val intent = Intent(requireContext(), EditcharacterActivity::class.java)
            editCharacterLauncher.launch(intent)
        }
    }

    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadUserProfile()
        }
    }
    private val editCharacterLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadUserProfile()
        }
    }





    ////////////친구 수 API////////////
    private fun fetchFriendCount() {
        friendService.getFriendCount().enqueue(object : Callback<FriendCountResponse> {
            override fun onResponse(
                call: Call<FriendCountResponse>,
                response: Response<FriendCountResponse>
            ) {
                if (response.isSuccessful) {
                    val friendCount = response.body()?.success?.data?.friendCount ?: 0
                    binding.friendCountNumber.text = friendCount.toString()
                    Log.d("친구 수", "친구 수 조회 성공: $friendCount")

                } else {
                    Log.e("친구 수", "서버 응답 실패: ${response.errorBody()?.string() ?: "알 수 없는 오류"}")
                    Toast.makeText(requireContext(), "친구 수를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FriendCountResponse>, t: Throwable) {
                Log.e("친구 수", "네트워크 요청 실패: ${t.message}", t)
                Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun updatePostCount(count: Int) {
        if (!isAdded) return
        binding.postCountNumber.text = count.toString()
    }


}