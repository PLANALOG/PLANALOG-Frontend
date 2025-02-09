package com.example.planalog.ui.post

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.planalog.R
import com.example.planalog.databinding.FragmentPostBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.post.MomentContent
import com.example.planalog.network.post.MomentRequest
import com.example.planalog.network.post.PostApiService
import kotlinx.coroutines.launch

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
            requireActivity().lifecycleScope.launch {
                uploadPost()
            }
        }

        return binding.root
    }

    private fun navigateToPostDetailFragment() {
        val postDetailFragment = PostDetailFragment()

        // 전달할 데이터 담기
        val bundle = Bundle().apply {
            putString("title", binding.postTitle.text.toString())  // 제목 전달
            putString("content", binding.postContent.text.toString())

            // 슬라이드에서 오직 이미지 URI만 추출해서 전달
            val imageUris = slideList.map { it.imageResId }.toCollection(ArrayList())
            val slideContents = slideList.map { it.postContent }.toCollection(ArrayList())

            putParcelableArrayList("imageUris", imageUris)  // 이미지 URI만 전달
            putStringArrayList("slideContents", slideContents)  // 텍스트 내용 전달
        }

        postDetailFragment.arguments = bundle  // 데이터 전달

        // 프래그먼트 전환
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, postDetailFragment)
            .addToBackStack(null)
            .commit()
    }


    private fun setupViewPager() {
        slidePagerAdapter = SlidePagerAdapter(
            slideList,
            onImageClick = { position ->
                // 필요 시 이미지 클릭 처리 로직
            },
            onDeleteClick = { position ->
                // 슬라이드 삭제 로직
                slideList.removeAt(position)  // 데이터 직접 관리
                slidePagerAdapter.notifyItemRemoved(position)  // RecyclerView 갱신

                // 슬라이드가 비었으면 UI 변경
                if (slideList.isEmpty()) {
                    binding.viewPager.visibility = View.GONE
                    binding.postContent.visibility = View.VISIBLE
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

    private suspend fun uploadPost() {
        val title = binding.postTitle.text.toString().trim()
        val plannerId = 1

        if (title.isBlank() || (binding.postContent.text.isBlank() && slideList.isEmpty())) {
            showToast("제목과 내용을 입력해 주세요.")
            return
        }

        val momentContents = slideList.mapIndexed { index, slide ->
            MomentContent(
                sortOrder = index + 1,
                content = slide.postContent,
                url = slide.imageResId.toString()
            )
        }.toMutableList()

        if (slideList.isEmpty()) {
            momentContents.add(
                MomentContent(
                    sortOrder = 1,
                    content = binding.postContent.text.toString(),
                    url = ""
                )
            )
        }

        val postRequest = MomentRequest(
            title = title,
            plannerId = plannerId,
            momentContents = momentContents
        )

        val postApiService = RetrofitClient.create(PostApiService::class.java, requireContext())
        try {
            val response = postApiService.createMoment(postRequest)

            // HTTP 상태 코드 확인
            val statusCode = response.code()
            Log.d("PostFragment", "HTTP 상태 코드: $statusCode")

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.resultType == "SUCCESS") {
                    showToast("게시물 작성 성공")
                    Log.d("PostFragment", "게시물 데이터: ${body.success?.data}")

                    // 프래그먼트 전환 추가
                    navigateToPostDetailFragment()
                } else {
                    val errorReason = body?.error?.reason ?: "알 수 없는 오류"
                    Log.e("PostFragment", "게시물 작성 실패: $errorReason")
                    showToast("게시물 작성 실패: $errorReason")
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("PostFragment", "서버 오류 발생: $errorBody")
                showToast("서버 오류 발생: $errorBody (HTTP 상태 코드: $statusCode)")
            }
        } catch (e: Exception) {
            Log.e("PostFragment", "네트워크 오류 발생", e)
            showToast("네트워크 오류 발생: ${e.localizedMessage}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
