package com.example.planalog.network.user

import com.example.planalog.network.user.request.UserUpdateRequest
import com.example.planalog.network.user.response.UserProfileImgResponse
import com.example.planalog.network.user.response.UserResponse
import com.example.planalog.network.user.response.UserUpdateResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface UserService {

    //회원 정보 수정 api
    @PATCH("/users/profile")
    fun updateUser(
        @Body request: UserUpdateRequest,
    ): Call<UserUpdateResponse>

    // 본인 회원 조회 api
    @GET("/users")
    fun getUserInfo(): Call<UserResponse>

    // 프로필 이미지 업데이트 api
    @Multipart
    @POST("/users/profile/image")
    fun uploadProfileImage(
        @Part image: MultipartBody.Part?,  // 업로드할 이미지 파일 (nullable)
        @Part("basicImage") basicImage: RequestBody  // 기본 플래닛 이미지 번호
    ): Call<UserProfileImgResponse>
}