package com.example.planalog.ui.search

import SearchAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planalog.R
import com.example.planalog.databinding.FragmentSearchBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.planner.PlannerResponse
import com.example.planalog.network.search.User
import com.example.planalog.network.user.UserService
import com.example.planalog.network.user.response.FriendProfileResponse
import com.example.planalog.ui.friends.FriendpageActivity
import com.example.planalog.ui.home.api.SearchApiHelper
import com.example.planalog.utils.VerticalSpaceItemDecoration
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchAdapter: SearchAdapter
    private lateinit var searchApiHelper: SearchApiHelper
    private lateinit var userService: UserService
    private lateinit var sharedPreferences: SharedPreferences
    private val searchHistory = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("search_data", Context.MODE_PRIVATE)
        searchApiHelper = SearchApiHelper(requireContext())
        val spf = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = spf.getString("user_id", null)
        Log.d("유저 id","${userId}")

        setupRecyclerView()
        loadSavedSearchResults()  // 이전 검색 결과 불러오기
        loadSearchHistory()  // 검색 기록 불러오기
        setupSearchInput()
        setupStartDrawableClick()

        userService = RetrofitClient.create(UserService::class.java, requireContext())

        userService.getFriendProfile(userId).enqueue(object : Callback<FriendProfileResponse> {
            override fun onResponse(
                call: Call<FriendProfileResponse>,
                response: Response<FriendProfileResponse>
            ) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {

                        val users = response.body()?.success  // 필요한 데이터만 추출
                        Toast.makeText(context, "조회 성공", Toast.LENGTH_SHORT).show()
                        Log.d("다른 유저 정보 조회", "조회 성공: $users")
                } else {
                    Log.e("다른 유저 정보 조회", "서버 오류: ${response.code()}, ${response.message()}")
                    Toast.makeText(context, "조회 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FriendProfileResponse>, t: Throwable) {
                Log.e("다른 유저 정보 조회", "네트워크 오류: ${t.message}", t)
                Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            }
        })

        return binding.root
    }

    private fun setupRecyclerView() {
        searchAdapter = SearchAdapter(mutableListOf()) { item ->
            when (item) {
                is String -> {
                    // JSON 데이터를 FriendpageActivity로 전달
                    val intent = Intent(requireContext(), FriendpageActivity::class.java)
                    intent.putExtra("friendData", item)
                    startActivity(intent)
                }
                else -> Toast.makeText(requireContext(), "알 수 없는 데이터 유형입니다.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.searchRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
            addItemDecoration(VerticalSpaceItemDecoration(16.dpToPx()))  // 아이템 간격 추가
        }
    }

    private fun getFriendIdFromSearchResult(result: String): Int {
        return try {
            val parts = result.split(":")
            if (parts.size == 2) parts[1].toInt() else -1  // 올바른 형식이 아니면 -1 반환
        } catch (e: Exception) {
            e.printStackTrace()
            -1  // 오류 발생 시 기본값 반환
        }
    }



    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun setupSearchInput() {
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    performSearch(s.toString())  // 검색 결과만 실시간으로 UI에 표시
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupStartDrawableClick() {
        binding.searchEt.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableStartWidth = binding.searchEt.compoundDrawables[0]?.bounds?.width() ?: 0
                val drawableEndWidth = binding.searchEt.compoundDrawables[2]?.bounds?.width() ?: 0

                // Start Drawable 클릭 시 검색 처리 및 기록 저장
                if (event.rawX <= (binding.searchEt.left + drawableStartWidth + binding.searchEt.paddingStart)) {
                    val query = binding.searchEt.text.toString().trim()
                    if (query.isNotBlank()) {
                        saveSearchHistory(query)  // 검색어를 SharedPreferences에 저장
                        searchAdapter.addSearchHistory(query, binding.searchRv)  // 검색 기록에 추가 및 스크롤 이동
                        // 검색 기록 생성 API 호출
                        searchApiHelper.createSearchRecord(query,
                            onSuccess = {
                                Toast.makeText(requireContext(), "검색 기록이 서버에 저장되었습니다.", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = {
                                Toast.makeText(requireContext(), "검색 기록 저장 실패", Toast.LENGTH_SHORT).show()
                            }
                        )
                        binding.searchEt.text.clear()  // 입력 초기화
                    }
                    return@setOnTouchListener true
                }

                // End Drawable 클릭 시 EditText 내용 삭제
                if (event.rawX >= (binding.searchEt.right - drawableEndWidth - binding.searchEt.paddingEnd)) {
                    binding.searchEt.text.clear()
                    binding.searchEt.hint = getString(R.string.hint)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }


    private fun performSearch(query: String) {
        searchApiHelper.getSearch(query) { results ->
            // 검색 결과 업데이트 (전체 사용자 정보 포함)
            searchAdapter.updateSearchResults(results)
            saveSearchResults(query, results)
        }
    }


    /** 검색 기록 저장 */
    private fun saveSearchHistory(query: String) {
        if (!searchHistory.contains(query)) {
            searchHistory.add(0, query)  // 최신 검색어를 맨 위에 추가
            sharedPreferences.edit().putStringSet("history", searchHistory.toSet()).apply()
        }
    }

    /** 검색 결과 저장 */
    private fun saveSearchResults(query: String, results: List<Map<String, Any>>) {
        val existingResults = sharedPreferences.getStringSet("saved_results", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        val gson = Gson()

        results.forEach { result ->
            val jsonResult = gson.toJson(result)
            existingResults.add(jsonResult)
        }

        sharedPreferences.edit().putStringSet("saved_results", existingResults).apply()
    }

    private fun loadSearchHistory() {
        searchApiHelper.fetchSearchHistory(
            onSuccess = { historyItems ->
                val sortedHistory = historyItems.sortedByDescending { it.createdAt }  // 최신순으로 정렬
                sortedHistory.forEach { item ->
                    searchAdapter.addSearchHistory(item.content, binding.searchRv)  // 리사이클러뷰 전달
                }
                Log.d("검색 기록 조회: ", "검색 목록: $sortedHistory")
            },
            onFailure = {
                println("검색 기록 조회 실패")
            }
        )
    }


    private fun loadSavedSearchResults() {
        val savedResults = sharedPreferences.getStringSet("saved_results", emptySet()) ?: emptySet()
        val gson = Gson()

        // 저장된 검색어와 결과를 맵에 정리
        val parsedResults = savedResults.mapNotNull { json ->
            try {
                gson.fromJson<Map<String, Any>>(json, object : TypeToken<Map<String, Any>>() {}.type)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        // 저장된 결과를 어댑터에 업데이트
        searchAdapter.updateSearchResults(parsedResults)

        // 리사이클러뷰를 맨 위로 스크롤
        binding.searchRv.scrollToPosition(0)
    }

    private fun navigateToFriendPage(friendId: Int) {
        val intent = Intent(requireContext(), FriendpageActivity::class.java)
        intent.putExtra("friendId", friendId)
        startActivity(intent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}