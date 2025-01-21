package com.example.planalog.ui.start

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.TypefaceSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.planalog.MainActivity
import com.example.planalog.R
import com.example.planalog.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCombinedTextView()
        // Get the result value from the intent
        val result = intent.getStringExtra("result") ?: "b"

        // Update the views based on the result value
        if (result == "a") {
            binding.iconImageView.setImageResource(R.drawable.ic_memotype)
            binding.userTypeTextView.text = "oo님은\n메모형 사용자입니다."
        } else {
            binding.iconImageView.setImageResource(R.drawable.ic_categorytype)
            binding.userTypeTextView.text = "oo님은\n카테고리형 사용자입니다."
        }

        // Button click event
        binding.startButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
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
