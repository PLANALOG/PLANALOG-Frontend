package com.example.planalog.utils

import android.util.Base64
import android.util.Log
import org.json.JSONObject

fun isTokenExpired(accessToken: String): Boolean {
    try {
        val parts = accessToken.split(".")
        if (parts.size == 3) {
            val payload = String(Base64.decode(parts[1], Base64.DEFAULT))
            val jsonPayload = JSONObject(payload)
            val exp = jsonPayload.optLong("exp", 0)
            val currentTime = System.currentTimeMillis() / 1000
            return exp < currentTime
        }
    } catch (e: Exception) {
        Log.e("TokenCheck", "토큰 만료 확인 중 오류 발생: ${e.message}")
    }
    return true  // 오류 발생 시 만료된 것으로 간주
}
