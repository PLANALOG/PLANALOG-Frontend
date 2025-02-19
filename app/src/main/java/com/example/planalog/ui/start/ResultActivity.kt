package com.example.planalog.ui.start

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.TypefaceSpan
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.planalog.MainActivity
import com.example.planalog.R
import com.example.planalog.databinding.ActivityResultBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.api.UserApiHelper
import com.example.planalog.network.user.UserService
import com.example.planalog.network.user.request.UserUpdateRequest
import com.example.planalog.network.user.response.UserUpdateResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupCombinedTextView()

        val nickname = intent.getStringExtra("nickname") ?: "사용자"
        Log.d("ResultActivity", "받은 닉네임: $nickname")

        val result = intent.getStringExtra("result") ?: "null"
        Log.d("ResultActivity", "받은 타입: $result")

        val userInfo = intent.getStringExtra("user_info") ?: ""
        Log.d("ResultActivity", "받은 유저 정보: $userInfo")

        if (result == "memo_user") {
            binding.iconImageView.setImageResource(R.drawable.ic_memotype)
            binding.userTypeTextView.text = "${nickname}님은\n메모형 사용자입니다."
        } else {
            binding.iconImageView.setImageResource(R.drawable.ic_categorytype)
            binding.userTypeTextView.text = "${nickname}님은\n카테고리형 사용자입니다."
        }


        binding.startButton.setOnClickListener {
            val type = if (result == "memo_user") "memo_user" else "category_user"

            val intent = Intent(this@ResultActivity, MainActivity::class.java)
            intent.putExtra("nickname", nickname)
            intent.putExtra("type", type)
            updateUserInfo(nickname, type = result)
            startActivity(intent)
            finish()
        }
    }


    private fun updateUserInfo(nickname: String, type: String) {
        val userApiHelper = UserApiHelper(this)

        // 업데이트할 필드만 포함된 Map 생성
        val updatedFields = mutableMapOf<String, Any>(
            "nickname" to nickname,
            "type" to type
        )

        userApiHelper.updateUserInfo(
            context = this,
            updatedFields = updatedFields,
            onSuccess = {
                Log.d("ResultActivity", "프로필 업데이트 성공")
                Toast.makeText(this, "프로필이 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
            },
            onFailure = { errorMsg ->
                Log.e("ResultActivity", "프로필 업데이트 실패: $errorMsg")
                Toast.makeText(this, "업데이트 실패: $errorMsg", Toast.LENGTH_SHORT).show()
            }
        )
    }


    private fun setupCombinedTextView() {
        val spannable = SpannableStringBuilder()

        val text1 = getString(R.string.app_name)
        val text2 = getString(R.string.on_ready)
        val font1 = ResourcesCompat.getFont(this, R.font.impact)
        val font2 = ResourcesCompat.getFont(this, R.font.pretendard_medium)
        val span1 = TypefaceSpan(font1!!)
        val span2 = TypefaceSpan(font2!!)

        spannable.append(text1)
        spannable.setSpan(span1, 0, text1.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.append(" ")
        spannable.append(text2)
        spannable.setSpan(span2, text1.length + 1, text1.length + 1 + text2.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.titleTextView.text = spannable
    }
}
