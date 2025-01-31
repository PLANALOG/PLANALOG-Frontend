package com.example.planalog.ui.start

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.TypefaceSpan
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.planalog.R
import com.example.planalog.databinding.ActivityQnaBinding

class QnaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQnaBinding
    private var option1Count = 0
    private var option2Count = 0
    private var currentQuestion = 1
    private var nickname: String? = null  // null 허용 타입으로 변경

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQnaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("QnaActivity", "받은 닉네임: $nickname")

        setupCombinedTextView()
        // 초기 질문 설정
        binding.tvQuestion.text = "곧 시험 기간이다!\n" + "이제 슬슬 준비해야지..."

        // 각 옵션에 클릭 리스너 설정
        binding.tvOption1.setOnClickListener { handleOptionSelection(1) }
        binding.tvFollowUpOption1.setOnClickListener { handleOptionSelection(1) }
        binding.tvFollow2UpOption1.setOnClickListener { handleOptionSelection(1) }

        binding.tvOption2.setOnClickListener { handleOptionSelection(2) }
        binding.tvFollow2UpOption2.setOnClickListener { handleOptionSelection(2) }
        binding.tvFollow2UpOption2.setOnClickListener { handleOptionSelection(2) }

    }


    private fun setupCombinedTextView() {
        val spannable = SpannableStringBuilder()

        val text1 = getString(R.string.app_name)
        val text2 = getString(R.string.ready)
        val font1 = ResourcesCompat.getFont(this, R.font.impact)
        val font2 = ResourcesCompat.getFont(this, R.font.pretendard_medium)
        val span1 = TypefaceSpan(font1!!)
        val span2 = TypefaceSpan(font2!!)

        spannable.append(text1)
        spannable.setSpan(span1, 0, text1.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.append(" ")
        spannable.append(text2)
        spannable.setSpan(span2, text1.length + 1, text1.length + 1 + text2.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.planalogReady.text = spannable
    }




    private fun handleOptionSelection(option: Int) {
        if (option == 1) {
            option1Count++
        } else {
            option2Count++
        }

        when (currentQuestion) {
            1 -> {
                binding.tvQuestion.text = "이제 PPT를 만들어볼까?\n아자아자 파이팅!"
                binding.tvOption1.text = "1. 아이디어가 떠오르는 대로 추가하며 만든다."
                binding.tvOption2.text = "2. 초안을 시작으로 단계별로 만들어 나간다."
                currentQuestion++
            }
            2 -> {
                binding.tvQuestion.text = "오늘 정말 말도 안 되는 하루를 보냈다!\n" + "이런 건 좀 적어둬야겠어."
                binding.tvOption1.text = "1. 오늘 일어난 일들을 생각나는 대로 적는다."
                binding.tvOption2.text = "2. 오늘 일어난 일들을 시간 순으로 정리한다."
                currentQuestion++
            }
            3 -> {
                navigateToResult()
            }
        }
    }

    private fun navigateToResult() {
        val nickname = intent.getStringExtra("nickname") ?: "사용자"
        val result = if (option1Count >= 2) "memo" else "category"
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("result", result)
            putExtra("nickname", nickname ?: "사용자")  // null 체크
        }

        Log.d("QnaActivity", "ResultActivity로 전달할 닉네임: $nickname")
        startActivity(intent)
        finish()
    }
}