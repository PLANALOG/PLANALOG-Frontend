package com.example.planalog.ui.home.api

import SearchPostResponse
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.search.Records
import com.example.planalog.network.search.SearchDeleteResponse
import com.example.planalog.network.search.SearchPostRequest
import com.example.planalog.network.search.SearchRecordsResponse
import com.example.planalog.network.search.SearchService
import com.example.planalog.network.search.SearchUsersResponse
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchApiHelper(private val context: Context) {

    private val searchService: SearchService by lazy {
        RetrofitClient.create(SearchService::class.java, context)
    }

    fun getSearch(name: String, callback: (List<Map<String, Any>>) -> Unit) {
        searchService.getSearch(name).enqueue(object : Callback<SearchUsersResponse> {
            override fun onResponse(
                call: Call<SearchUsersResponse>,
                response: Response<SearchUsersResponse>
            ) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    response.body()?.let { responseBody ->
                        val searchResults = responseBody.success?.data?.map { user ->
                            mapOf(
                                "id" to user.id,
                                "name" to user.name,
                                "email" to user.email,
                                "introduction" to user.introduction,
                                "link" to user.link,
                                "nickname" to user.nickname
                            )
                        } ?: emptyList()

                        // Gson으로 JSON 형식으로 변환해서 전체 출력
                        val jsonResult = Gson().toJson(searchResults)
                        Log.d("검색 기능", "검색 성공: $jsonResult")
                        Toast.makeText(context, "검색 성공", Toast.LENGTH_SHORT).show()
                        callback(searchResults)
                    }
                } else {
                    Log.e("검색 기능", "서버 오류: ${response.code()} - ${response.message()}")
                    Toast.makeText(context, "검색 실패", Toast.LENGTH_SHORT).show()
                    callback(emptyList())
                }
            }

            override fun onFailure(call: Call<SearchUsersResponse>, t: Throwable) {
                Log.e("검색 기능", "네트워크 오류 발생: ${t.localizedMessage}", t)
                Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
                callback(emptyList())
            }
        })
    }

    fun fetchSearchHistory(onSuccess: (List<Records>) -> Unit, onFailure: () -> Unit) {
        searchService.getSearchRecords().enqueue(object : Callback<SearchRecordsResponse> {
            override fun onResponse(
                call: Call<SearchRecordsResponse>,
                response: Response<SearchRecordsResponse>
            ) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    val historyItems = response.body()?.success?.data ?: emptyList()
                    Log.d("검색 기록 조회", "성공: ${historyItems.size}개의 기록 로드됨")
                    onSuccess(historyItems)
                } else {
                    Log.e("검색 기록 조회", "서버 오류: ${response.code()} - ${response.message()}")
                    onFailure()
                }
            }

            override fun onFailure(call: Call<SearchRecordsResponse>, t: Throwable) {
                Log.e("검색 기록 조회", "네트워크 오류 발생: ${t.localizedMessage}", t)
                onFailure()
            }
        })
    }

    fun createSearchRecord (query: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val request = SearchPostRequest(query)
        val call = searchService.postSearch(request)
        call.enqueue(object : Callback<SearchPostResponse> {
            override fun onResponse(call: Call<SearchPostResponse>, response: Response<SearchPostResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("검색 기록 생성", "전체 응답: ${responseBody.toString()}")  // 응답 전체 확인

                    if (responseBody?.resultType == "SUCCESS") {
                        Log.d("검색 기록 생성", "검색어 '$query' 성공적으로 서버에 저장됨")
                        onSuccess()
                    } else {
                        val errorReason = responseBody?.error?.reason ?: "알 수 없는 오류"
                        Log.e("검색 기록 생성", "서버 오류: ${response.code()} - $errorReason")
                        onFailure()
                    }
                } else {
                    Log.e("검색 기록 생성", "응답 오류: ${response.code()} - ${response.message()}")
                    onFailure()
                }
            }


            override fun onFailure(call: Call<SearchPostResponse>, t: Throwable) {
                Log.e("검색 기록 생성", "네트워크 오류 발생: ${t.localizedMessage}", t)
                onFailure()
            }
        })
    }

    fun deleteSearchRecord(recordId: Int, onSuccess: () -> Unit, onFailure: () -> Unit) {
        searchService.deleteSearchRecords(recordId).enqueue(object : Callback<SearchDeleteResponse> {
            override fun onResponse(call: Call<SearchDeleteResponse>, response: Response<SearchDeleteResponse>) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    Log.d("검색 기록 삭제", "기록 ID $recordId 삭제 성공")
                    onSuccess()
                } else {
                    Log.e("검색 기록 삭제", "서버 오류: ${response.code()} - ${response.message()}")
                    onFailure()
                }
            }

            override fun onFailure(call: Call<SearchDeleteResponse>, t: Throwable) {
                Log.e("검색 기록 삭제", "네트워크 오류 발생: ${t.localizedMessage}", t)
                onFailure()
            }
        })
    }
}