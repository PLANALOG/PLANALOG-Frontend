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
import androidx.fragment.app.commit
import com.example.planalog.R
import com.example.planalog.databinding.FragmentPostBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.planner.PlannerResponse
import com.example.planalog.network.post.PostApiService
import com.example.planalog.network.post.PostContent
import com.example.planalog.network.post.PostRequest
import com.example.planalog.network.post.PostResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        val status = "draft"
        val plannerId = 1

        // 제목이나 내용이 비어 있으면 경고 메시지
        if (title.isBlank() || (content.isBlank() && slideList.isEmpty())) {
            showToast("제목과 내용을 입력해 주세요.")
            return
        }

        // 모든 슬라이드 이미지 URI와 텍스트를 ArrayList로 변환
        val imageUris = ArrayList<Uri>()
        val slideContents = ArrayList<String>()

        // momentContents를 담을 리스트 생성
        val momentContents = mutableListOf<PostContent>()

        // 슬라이드가 없으면 메인 텍스트를 하나의 페이지로 추가
        if (slideList.isEmpty()) {
            momentContents.add(
                PostContent(
                    sortOrder = 1,
                    content = content,
                    url = ""  // 이미지가 없는 경우 빈 URL
                )
            )
        }

        // PostRequest 생성
        val postRequest = PostRequest(
            title = title,
            status = status,
            plannerId = plannerId,
            momentContents = momentContents
        )

        val bundle = Bundle().apply {
            putString("title", title)
            putStringArrayList("slideContents", slideContents)
            putParcelableArrayList("imageUris", imageUris)
        }

        parentFragmentManager.commit {
            replace(R.id.main_frm, PostDetailFragment().apply { arguments = bundle })
            addToBackStack(null)
        }

        // 게시물 생성 api 연결
        val postApiService = RetrofitClient.create(PostApiService::class.java, requireContext())
//        val requestBody = PostRequest(title, status, plannerId, postContents)

        postApiService.createPost(postRequest).enqueue(object : Callback<PostResponse> {
            override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    val post = response.body()?.success

                    Toast.makeText(requireContext(), "게시물 작성 성공", Toast.LENGTH_SHORT).show()
                    Log.d("Post", "Completed: $post")
                } else {
                    Log.e("Post", "서버 오류: ${response.code()}, ${response.message()}")
                    Toast.makeText(requireContext(), "게시물 작성 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                Log.e("Planner", "네트워크 오류: ${t.message}", t)
                Toast.makeText(requireContext(), "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
