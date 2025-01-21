package com.example.planalog.ui.start

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.TypefaceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.example.planalog.R
import com.example.planalog.databinding.FragmentStartsetBinding

class StartsetFragment : Fragment() {
    private lateinit var binding: FragmentStartsetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStartsetBinding.inflate(inflater, container, false)
        setupCombinedTextView()
        return binding.root
    }










    private fun setupCombinedTextView() {
        val spannable1 = SpannableStringBuilder()
        val spannable2 = SpannableStringBuilder()


        val text1 = getString(R.string.app_name)
        val text2 = getString(R.string.hello2)
        val font1 = ResourcesCompat.getFont(requireContext(), R.font.impact)
        val font2 = ResourcesCompat.getFont(requireContext(), R.font.pretendard_medium)
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
        spannable2.setSpan(span3, 0, text1.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable2.append(text4)
        spannable2.setSpan(span4, text3.length, text3.length + text4.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.hello2.text = spannable1
        binding.hello3.text = spannable2
    }
}