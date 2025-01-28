package com.example.planalog.ui.post

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.planalog.databinding.FragmentPostBinding

class PostFragment : Fragment() {
    private lateinit var binding: FragmentPostBinding
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var slidePagerAdapter: SlidePagerAdapter
    private val slideList = mutableListOf<Slide>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostBinding.inflate(inflater, container, false)

        setupViewPager()
        setupImagePicker()

        binding.photoButton.setOnClickListener { openImagePicker() }
        binding.uploadButton.setOnClickListener { uploadPost() }

        return binding.root
    }

    private fun setupViewPager() {
        slidePagerAdapter = SlidePagerAdapter(slideList) { position ->
            // 이미지 클릭 처리 (선택사항)
        }
        binding.viewPager.adapter = slidePagerAdapter
    }

    private fun setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val clipData = result.data?.clipData
                val uriList = mutableListOf<Uri>()

                if (clipData != null) {
                    // 다중 선택 처리
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        uriList.add(uri)
                    }
                } else {
                    // 단일 선택 처리
                    result.data?.data?.let { uri ->
                        uriList.add(uri)
                    }
                }

                // 선택된 URI를 슬라이드로 추가
                uriList.forEach { uri ->
                    val newSlide = Slide(imageResId = uri, postContent = "")
                    slideList.add(newSlide)
                }
                slidePagerAdapter.notifyDataSetChanged()
                binding.viewPager.setCurrentItem(slideList.size - 1, true) // 마지막 슬라이드로 이동
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // 다중 선택 허용
        }
        imagePickerLauncher.launch(intent)
    }

    private fun uploadPost() {
        // 업로드 로직 추가
        val title = binding.postTitle.text.toString()
        val contentSlides = slideList.map { it.postContent }.joinToString("\n")
        // title, contentSlides, 그리고 이미지 데이터 처리
    }
}
