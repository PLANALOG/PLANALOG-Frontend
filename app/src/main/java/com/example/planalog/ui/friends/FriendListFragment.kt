package com.example.planalog.ui.friends

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planalog.databinding.FragmentFriendlistBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.friend.response.Friend
import com.example.planalog.network.friend.FriendService
import com.example.planalog.network.friend.response.FriendResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FriendListFragment : Fragment() {
    private var _binding: FragmentFriendlistBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: FriendListAdapter
    private var tabType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabType = arguments?.getString("TAB_TITLE")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        fetchFriends()
    }

    private fun setupRecyclerView() {
        adapter = FriendListAdapter(emptyList())
        binding.friendRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.friendRecyclerView.adapter = adapter
    }

    private fun fetchFriends() {
        val apiService = RetrofitClient.create(FriendService::class.java, requireContext())
        val call = if (tabType == "내가 응원하는") {
            apiService.getFollowing()
        } else {
            apiService.getFollowers()
        }

        call.enqueue(object : Callback<FriendResponse> {
            override fun onResponse(call: Call<FriendResponse>, response: Response<FriendResponse>) {
                if (response.isSuccessful) {
                    val successData = response.body()?.success

                    // success가 List<Friend>인지 확인하고 처리
                    val friends = if (successData is List<*>) {
                        successData.filterIsInstance<Friend>()
                    } else {
                        emptyList()
                    }

                    if (isAdded && _binding != null) {
                        updateFriendCountText(friends.size)
                        if (friends.isNotEmpty()) {
                            adapter.updateFriends(friends)
                        } else {
                            Toast.makeText(requireContext(), "친구 목록이 비어 있습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    val errorResponse = response.body()?.error
                    val errorMessage = errorResponse?.reason ?: "알 수 없는 오류가 발생했습니다."
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FriendResponse>, t: Throwable) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun updateFriendCountText(count: Int) {
        val countText = "$count 명"
        binding.friendCount.text = countText
        Log.d("FriendListFragment", "업데이트된 친구 수: $count 명")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(tabType: String) = FriendListFragment().apply {
            arguments = Bundle().apply {
                putString("TAB_TITLE", tabType)
            }
        }
    }
}
