package com.example.planalog.network.search

import com.example.planalog.network.search.response.SearchPostResponse
import com.example.planalog.network.search.request.SearchPostRequest
import com.example.planalog.network.search.response.SearchDeleteResponse
import com.example.planalog.network.search.response.SearchRecordsResponse
import com.example.planalog.network.search.response.SearchUsersResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SearchService {
    @GET("/searches/users")
    fun getSearch(
        @Query ("query") query: String
    ) : Call<SearchUsersResponse>

    @GET("/searches/records")
    fun getSearchRecords() : Call<SearchRecordsResponse>

    @POST("/searches")
    fun postSearch(
        @Body request: SearchPostRequest
    ) : Call<SearchPostResponse>

    @DELETE("/searches/records/{recordId}")
    fun deleteSearchRecords(
        @Path("recordId") recordId: Int
    ) : Call<SearchDeleteResponse>
}