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
import com.example.planalog.network.user.response.MypageMoment
import com.example.planalog.network.user.response.MypageResponse
import com.example.planalog.network.user.MypageService
import com.example.planalog.network.user.response.UserResponse
import com.example.planalog.network.user.UserService

import com.example.planalog.network.user.FriendCountResponse
import com.example.planalog.network.user.FriendcountService
import com.example.planalog.network.user.response.UserInfo


import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var userService: UserService
    private lateinit var mypageService: MypageService
    private lateinit var friendService: FriendcountService
    private var mypageMomentAdapter: MypageMomentAdapter? = null  // 어댑터를 한 번만 초기화

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Retrofit을 이용해 초기화
        userService = RetrofitClient.create(UserService::class.java, requireContext())
        mypageService = RetrofitClient.create(MypageService::class.java, requireContext())
        friendService = RetrofitClient.create(FriendcountService::class.java, requireContext())

        // RecyclerView 설정
        binding.recyclerViewMoments.layoutManager = LinearLayoutManager(requireContext())

        // 유저 정보 가져오기
        fetchUserProfile()
        fetchMypageMoments()
        fetchFriendCount()

        setupUI()

        return binding.root
    }
    private fun setupRecyclerView() {
        binding.recyclerViewMoments.layoutManager = LinearLayoutManager(requireContext())
        mypageMomentAdapter = MypageMomentAdapter(emptyList())  // 초기 데이터는 빈 리스트로 설정
        binding.recyclerViewMoments.adapter = mypageMomentAdapter
    }

    private fun fetchUserProfile() {
        userService.getUserInfo().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val userInfo = response.body()?.success
                    if (userInfo != null) {
                        updateUI(userInfo)  // UI에 유저 정보 표시
                        Log.d("ProfileFragment", "유저 정보: $userInfo")
                    } else {
                        Toast.makeText(requireContext(), "유저 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "에러: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("ProfileFragment", "네트워크 오류", t)
            }
        })
    }

    private fun fetchMypageMoments() {
        mypageService.getMypageMoments().enqueue(object : Callback<MypageResponse> {
            override fun onResponse(call: Call<MypageResponse>, response: Response<MypageResponse>) {
                if (!isAdded) {
                    // Fragment가 Activity에 붙어 있지 않으면 종료
                    return
                }

                context?.let { ctx ->
                    if (response.isSuccessful) {
                        val moments = response.body()?.success?.data ?: emptyList()

                        Log.d("MypageService", "성공적으로 가져온 Moments 개수: ${moments.size}")
                        moments.forEach { moment ->
                            Log.d("MypageService", "Moment ID: ${moment.momentId}, Title: ${moment.title}")
                        }

                        if (moments.isNotEmpty()) {
                            updateMoments(moments)
                        } else {
                            Toast.makeText(ctx, "Moment 게시글이 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("MypageService", "서버 응답 오류: ${response.errorBody()?.string()}")
                        Toast.makeText(ctx, "에러 발생: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<MypageResponse>, t: Throwable) {
                if (!isAdded) {
                    // Fragment가 Activity에 붙어 있지 않으면 종료
                    return
                }
                context?.let { ctx ->
                    Log.e("MypageService", "네트워크 오류: ${t.message}", t)
                    Toast.makeText(ctx, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun updateMoments(moments: List<MypageMoment>) {
        // 어댑터의 데이터를 새로고침하여 표시
        mypageMomentAdapter?.updateData(moments)
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
            startActivity(intent)
        }

        // 플래너 설정 버튼 클릭 이벤트 설정
        binding.plannerSettingButton.setOnClickListener {
            val intent = Intent(requireContext(), EditcharacterActivity::class.java)
            editCharacterLauncher.launch(intent)
        }
    }

    private val editCharacterLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            fetchUserProfile()
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

}