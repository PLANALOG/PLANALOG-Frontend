package com.example.planalog.ui.start

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.TypefaceSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.res.ResourcesCompat
import com.example.planalog.R
import com.example.planalog.databinding.ActivityStartsetBinding

class StartsetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartsetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartsetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCombinedTextView()


        binding.writeNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isNotEmpty = s?.isNotEmpty() == true
                binding.logincheck.visibility = if (isNotEmpty) View.VISIBLE else View.INVISIBLE
                binding.ok.isEnabled = isNotEmpty
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.ok.setOnClickListener {
            Toast.makeText(this, "ok", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, QnaActivity::class.java)
            startActivity(intent)
            finish()
        }
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