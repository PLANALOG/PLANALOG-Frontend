package com.example.planalog.ui.start

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
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.res.ResourcesCompat
import com.example.planalog.R
import com.example.planalog.databinding.ActivityStartsetBinding
import com.example.planalog.network.ApiService
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.startset.Idcheck
import com.example.planalog.network.startset.NicknameCheckResponse
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StartsetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartsetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartsetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nickname = intent.getStringExtra("nickname") ?: ""
        Log.d("StartsetActivity", "Received nickname: $nickname")

        setupCombinedTextView()


        binding.writeNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val nickname = s?.toString()?.trim() ?: ""

                if (nickname.length > 15) {
                    binding.blocknickname.visibility = View.VISIBLE
                    binding.blocknickname.text = "닉네임은 15글자를 초과할 수 없습니다."
                    binding.writeNickname.setBackgroundResource(R.drawable.btn_round_error)
                    binding.logincheck.visibility = View.INVISIBLE
                    binding.ok.isEnabled = false
                } else if (nickname.isNotEmpty()) {
                    checkNicknameAvailability(nickname)
                } else {
                    binding.blocknickname.visibility = View.INVISIBLE
                    binding.writeNickname.setBackgroundResource(R.drawable.btn_round)
                    binding.logincheck.visibility = View.INVISIBLE
                    binding.ok.isEnabled = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        binding.ok.setOnClickListener {
            binding.ok.setOnClickListener {
                val nickname = binding.writeNickname.text.toString().trim()

                if (nickname.isNotEmpty() && nickname.length <= 15) {
                    Log.d("StartsetActivity", "전송할 닉네임: $nickname")

                    val intent = Intent(this, QnaActivity::class.java)
                    intent.putExtra("nickname", nickname)  // 닉네임 전달
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "유효한 닉네임을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkNicknameAvailability(nickname: String) {
        val service = ApiService.idcheckService

        service.idcheck(nickname).enqueue(object : Callback<NicknameCheckResponse> {
            override fun onResponse(
                call: Call<NicknameCheckResponse>,
                response: Response<NicknameCheckResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        when (body.resultType) {
                            "SUCCESS" -> handleSuccessResponse(body.success?.isDuplicated ?: true)
                            else -> showErrorMessage(body.error ?: "알 수 없는 오류가 발생했습니다.")
                        }
                    }
                } else {
                    showErrorMessage("서버 응답 실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<NicknameCheckResponse>, t: Throwable) {
                showErrorMessage("네트워크 오류: ${t.localizedMessage}")
            }
        })
    }

    private fun handleSuccessResponse(isDuplicated: Boolean) {
        if (isDuplicated) {
            binding.blocknickname.visibility = View.VISIBLE
            binding.blocknickname.text = "중복된 닉네임입니다."
            binding.writeNickname.setBackgroundResource(R.drawable.btn_round_error)
            binding.logincheck.visibility = View.INVISIBLE
            binding.ok.isEnabled = false
        } else {
            binding.blocknickname.visibility = View.INVISIBLE
            binding.writeNickname.setBackgroundResource(R.drawable.btn_round)
            binding.logincheck.visibility = View.VISIBLE
            binding.ok.isEnabled = true
        }
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(this@StartsetActivity, message, Toast.LENGTH_SHORT).show()
    }










    private fun setupCombinedTextView() {
        val spannable1 = SpannableStringBuilder()
        val spannable2 = SpannableStringBuilder()

        val text1 = getString(R.string.app_name)
        val text2 = getString(R.string.hello2)
        val font1 = ResourcesCompat.getFont(this, R.font.impact)
        val font2 = ResourcesCompat.getFont(this, R.font.pretendard_medium)
        val span1 = TypefaceSpan(font1!!)
        val span2 = TypefaceSpan(font2!!)

        spannable1.append(text1)
        spannable1.setSpan(span1, 0, text1.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable1.append(text2)
        spannable1.setSpan(span2, text1.length, text1.length + text2.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)

        val text3 = getString(R.string.app_name)
        val text4 = getString(R.string.hello2)
        val span3 = TypefaceSpan(font1)
        val span4 = TypefaceSpan(font2)

        spannable2.append(text3)
        spannable2.setSpan(span3, 0, text3.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable2.append(text4)
        spannable2.setSpan(span4, text3.length, text3.length + text4.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.hello2.text = spannable1
        binding.hello3.text = spannable2
    }
}