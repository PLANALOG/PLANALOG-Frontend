package com.example.planalog.network.SocialLogin

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

data class TokenRequestBody(val accessToken: String?)

data class RefreshTokenRequest(val refreshToken: String?)

interface NaverLoginService {
    @POST("/oauth2/naver/token")
    fun sendAccessToken(
        @Body body: TokenRequestBody,
    ): Call<NaverTokenResponse>

    @POST("/refresh_token")
    fun refreshToken(@Body body: RefreshTokenRequest): Call<TokenRefreshResponse>
}