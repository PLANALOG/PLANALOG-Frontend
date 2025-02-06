package com.example.planalog.network

interface TokenRefreshCallback {
    fun onSuccess(newAccessToken: String?)
    fun onFailure()
}
