package com.example.planalog.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planalog.databinding.FragmentFriendpageBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.user.FriendpageService
import com.example.planalog.network.user.FriendpageResponse
import com.example.planalog.network.user.response.MypageMoment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FriendpageFragment : Fragment() {

    private lateinit var binding: FragmentFriendpageBinding
    private lateinit var friendService: FriendpageService
    private var momentAdapter: FriendpageMomentAdapter? = null  // 어댑터 초기화

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendpageBinding.inflate(inflater, container, false)

        // Retrofit 서비스 초기화
        friendService = RetrofitClient.create(FriendpageService::class.java, requireContext())

        // RecyclerView 설정
        setupRecyclerView()

        // Moments 가져오기
        fetchFriendpageMoments()

        setupUI()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerViewMoments.layoutManager = LinearLayoutManager(requireContext())
        momentAdapter = FriendpageMomentAdapter(emptyList())  // 초기 데이터는 빈 리스트로 설정
        binding.recyclerViewMoments.adapter = momentAdapter
    }

    private fun fetchFriendpageMoments() {
        friendService.getFriendpageMoments().enqueue(object : Callback<FriendpageResponse> {
            override fun onResponse(
                call: Call<FriendpageResponse>,
                response: Response<FriendpageResponse>
            ) {
                if (response.isSuccessful) {
                    val moments = response.body()?.success?.data ?: emptyList()

                    Log.d("FriendpageService", "가져온 Moments 수: ${moments.size}")
                    moments.forEach { moment ->
                        Log.d("FriendpageService", "Moment ID: ${moment.momentId}, Title: ${moment.title}")
                    }

                    if (moments.isNotEmpty()) {
                        updateMomentsList(moments)
                    } else {
                        Toast.makeText(requireContext(), "Moments가 비어 있습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("FriendpageService", "서버 응답 오류: ${response.errorBody()?.string()}")
                    Toast.makeText(requireContext(), "에러 발생: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FriendpageResponse>, t: Throwable) {
                Log.e("FriendpageService", "네트워크 오류: ${t.message}", t)
            }
        })
    }

    private fun updateMomentsList(moments: List<MypageMoment>) {
        // 어댑터의 데이터를 새로고침하여 Moments 목록 표시
        momentAdapter?.updateData(moments)
    }

    private fun setupUI() {

        // 응원하기/응원중 버튼 클릭 이벤트 설정
        binding.followBtn.setOnClickListener {
            binding.followBtn.visibility = View.INVISIBLE
            binding.unfollowBtn.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "응원하기 클릭", Toast.LENGTH_SHORT).show()
        }

        binding.unfollowBtn.setOnClickListener {
            binding.unfollowBtn.visibility = View.INVISIBLE
            binding.followBtn.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "응원중 해제", Toast.LENGTH_SHORT).show()
        }
    }
}
