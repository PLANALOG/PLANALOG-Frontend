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
import androidx.fragment.app.commit
import com.example.planalog.R
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

        // 초기 상태: 뷰페이저를 숨김 처리
        binding.viewPager.visibility = View.GONE

        // 사진 추가 버튼 클릭 시 내용은 유지하고 뷰만 전환
        binding.photoButton.setOnClickListener {
            binding.postContent.visibility = View.GONE  // 내용은 숨기지만 초기화하지 않음
            binding.viewPager.visibility = View.VISIBLE  // 뷰페이저 표시
            openImagePicker()
        }

        // 등록 버튼 클릭 시 PostDetailFragment로 이동
        binding.uploadButton.setOnClickListener {
            uploadPost()
        }

        return binding.root
    }

    private fun setupViewPager() {
        slidePagerAdapter = SlidePagerAdapter(
            slideList,
            onImageClick = { position ->
                // 이미지 클릭 시 추가 작업 (예: 확대보기)
                // 필요 없으면 비워둘 수 있습니다
            },
            onDeleteClick = { position ->
                slideList.removeAt(position)  // 선택된 슬라이드 삭제
                slidePagerAdapter.notifyDataSetChanged()  // 어댑터 갱신

                // 슬라이드가 비었으면 뷰페이저 숨기기
                if (slideList.isEmpty()) {
                    binding.viewPager.visibility = View.GONE
                    binding.postContent.visibility = View.VISIBLE  // 텍스트 입력란 다시 보이기
                }
            }
        )
        binding.viewPager.adapter = slidePagerAdapter
    }



    private fun setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val clipData = result.data?.clipData
                val uriList = mutableListOf<Uri>()

                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        uriList.add(uri)
                    }
                } else {
                    result.data?.data?.let { uri ->
                        uriList.add(uri)
                    }
                }

                // 선택된 URI를 슬라이드로 추가하고 뷰페이저 업데이트
                if (uriList.isNotEmpty()) {
                    uriList.forEach { uri ->
                        val newSlide = Slide(imageResId = uri, postContent = "")
                        slideList.add(newSlide)
                    }
                    slidePagerAdapter.notifyDataSetChanged()
                    binding.viewPager.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        imagePickerLauncher.launch(intent)
    }

    private fun uploadPost() {
        val title = binding.postTitle.text.toString().trim()
        val content = binding.postContent.text.toString().trim()

        // 제목이나 내용이 비어 있으면 경고 메시지
        if (title.isBlank() || (content.isBlank() && slideList.isEmpty())) {
            showToast("제목과 내용을 입력해 주세요.")
            return
        }

        // 모든 슬라이드 이미지 URI와 텍스트를 ArrayList로 변환
        val imageUris = ArrayList<Uri>()
        val slideContents = ArrayList<String>()

        slideList.forEach {
            imageUris.add(it.imageResId!!)
            slideContents.add(it.postContent)
        }

        // 슬라이드가 없을 경우에는 EditText의 텍스트를 번들에 포함
        if (slideList.isEmpty()) {
            slideContents.add(content)
        }

        val bundle = Bundle().apply {
            putString("title", title)
            putStringArrayList("slideContents", slideContents)
            putParcelableArrayList("imageUris", imageUris)
        }

        parentFragmentManager.commit {
            replace(R.id.main_frm, PostDetailFragment().apply { arguments = bundle })
            addToBackStack(null)
        }
    }


    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
