package com.example.planalog.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planalog.databinding.FragmentMypageMomentBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.user.MypageService
import com.example.planalog.network.user.response.MypageMoment
import com.example.planalog.network.user.response.MypageResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPageMomentFragment : Fragment() {
    private lateinit var binding: FragmentMypageMomentBinding
    private var mypageMomentAdapter: MypageMomentAdapter? = null  // 어댑터를 한 번만 초기화
    private lateinit var mypageService: MypageService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMypageMomentBinding.inflate(inflater, container, false)

        fetchMypageMoments()

        binding.recyclerViewMoments.layoutManager = LinearLayoutManager(requireContext())
        mypageMomentAdapter = MypageMomentAdapter(requireContext(), emptyList())  // 초기 데이터는 빈 리스트로 설정
        binding.recyclerViewMoments.adapter = mypageMomentAdapter

        return binding.root
    }

    private fun fetchMypageMoments() {
        mypageService = RetrofitClient.create(MypageService::class.java, requireContext())
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
                            Log.d("MypageService", "Moment Id: ${moment.momentId}, Title: ${moment.title}")
                        }

                        (parentFragment as? ProfileFragment)?.updatePostCount(moments.size)

                        if (moments.isNotEmpty()) {
                            updateMoments(moments)
                        } else {
                            Toast.makeText(ctx, "Moment 게시글이 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("MypageService", "서버 응답 오류: ${response.errorBody()?.string()}")
                        Toast.makeText(ctx, "서버 응답 오류: ${response.message()}", Toast.LENGTH_SHORT).show()
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
}