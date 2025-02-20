package com.example.planalog.ui.post

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.example.planalog.network.api.PostApiHelper
import com.example.planalog.network.post.MomentRequestContent
import kotlinx.coroutines.launch

class PostFragment : Fragment() {
    private lateinit var binding: FragmentPostBinding
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var slidePagerAdapter: SlidePagerAdapter
    private val slideList = mutableListOf<Slide>()

    private var plannerId: Int? = null

    private lateinit var postApiHelper : PostApiHelper

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

        // **전달받은 플래너 이미지를 슬라이드에 추가**
        val plannerImageUri = arguments?.getParcelable<Uri>("image_uri")
        plannerImageUri?.let {
            addPlannerImageToSlides(it)
        }

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

        plannerId = getPlannerIdFromSPF()
        Log.d("PostFragment", "저장된 플래너 ID: $plannerId")

        return binding.root
    }

    // **SharedPreferences에서 플래너 ID 불러오는 함수**
    fun getPlannerIdFromSPF(): Int? {
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("PlannerSPF", Context.MODE_PRIVATE)
        val plannerId = sharedPreferences.getInt("PLANNER_ID", -1) // 기본값: -1 (값이 없을 때)

        return if (plannerId == -1) null else plannerId
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
            isDetailView = false,  // ✅ PostFragment에서는 삭제 버튼 보이기
            onImageClick = { position ->
                // 이미지 클릭 이벤트
            },
            onDeleteClick = { position ->
                slideList.removeAt(position)
                slidePagerAdapter.notifyItemRemoved(position)
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
                    updateViewVisibility()
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
        postApiHelper = PostApiHelper(requireContext())

        val title = binding.postTitle.text.toString().trim()

        if (title.isBlank() || (binding.postContent.text.isBlank() && slideList.isEmpty())) {
            showToast("제목과 내용을 입력해 주세요.")
            return
        }

        val momentRequestList = mutableListOf<MomentRequestContent>()
        val content = binding.postContent.text.toString().trim()

        if (content.isNotEmpty()) {
            momentRequestList.add(MomentRequestContent(sortOrder = 1, content = content, url = ""))
        }

        slideList.forEachIndexed { index, slide ->
            val imageUrl = slide.imageResId?.toString() ?: ""
            momentRequestList.add(
                MomentRequestContent(
                    sortOrder = index + 2,
                    content = slide.postContent,
                    url = imageUrl
                )
            )
        }

        postApiHelper.uploadPost(
            title,
            plannerId,
            momentRequestList,
            object : PostApiHelper.UploadPostCallback {
                override fun onSuccess(postedId: Int?, momentId: Int?) {
                    showToast("게시물이 성공적으로 업로드되었습니다.")
                    Log.d("PostFragment", "작성한 유저 아이디: $postedId")
                    Log.d("PostFragment", "저장된 모먼트 ID: $momentId")

                    // momentId 저장
                    val sharedPreferences = requireContext().getSharedPreferences("MomentData", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putInt("MOMENT_ID", momentId ?: -1)
                    editor.apply()

                    navigateToPostDetailFragment()
                }

                override fun onFailure(errorMessage: String) {
                    showToast("게시물 업로드 실패: $errorMessage")
                }
            }
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun addPlannerImageToSlides(imageUri: Uri) {
        val newSlide = Slide(imageResId = imageUri, postContent = "")
        slideList.add(0, newSlide)  // 플래너 사진을 리스트 맨 앞에 추가
        slidePagerAdapter.notifyDataSetChanged()
        updateViewVisibility()
    }

    private fun updateViewVisibility() {
        if (slideList.isNotEmpty()) {
            binding.postContent.visibility = View.GONE  // EditText 숨기기
            binding.viewPager.visibility = View.VISIBLE  // ViewPager 표시
        } else {
            binding.postContent.visibility = View.VISIBLE  // EditText 표시
            binding.viewPager.visibility = View.GONE  // ViewPager 숨기기
        }
    }


}