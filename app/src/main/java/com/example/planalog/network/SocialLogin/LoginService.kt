package com.example.planalog.network.SocialLogin

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class TokenRequestBody(val accessToken: String?, val refreshToken: String?)

data class RefreshTokenRequest(val refreshToken: String?)

interface LoginService {
    @POST("/oauth2/naver/token")
    fun sendAccessToken(
        @Body body: TokenRequestBody,
    ): Call<TokenResponse>

    @GET("/logout")
    fun logout() : Call<LogoutResponse>

    @POST("/refresh_token")
    fun refreshToken(@Body body: RefreshTokenRequest): Call<TokenRefreshResponse>
}