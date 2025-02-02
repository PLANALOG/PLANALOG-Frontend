package com.example.planalog.ui.start

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.TypefaceSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.planalog.R
import com.example.planalog.databinding.ActivityStartBinding
import com.example.planalog.databinding.ActivityStartsetBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.startset.IdcheckService
import com.example.planalog.network.startset.NicknameCheckResponse
import com.example.planalog.network.user.response.UserResponse
import com.example.planalog.network.user.UserService
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStart.setOnClickListener {
            val intent = Intent(this, StartsetActivity::class.java)
            startActivity(intent)
            finish()  // 현재 액티비티 종료
        }
    }
}