package com.example.planalog.ui.post

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.planalog.R
import com.example.planalog.databinding.FragmentPostDetailBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.user.UserService
import com.example.planalog.network.user.response.FriendProfileResponse
import com.example.planalog.ui.comment.CommentFragment
import com.example.planalog.utils.getCurrentDate
import com.example.planalog.utils.getCurrentPostedDate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostDetailFragment : Fragment() {
    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!

    // 좋아요 상태 및 개수 저장 변수
    private var isLiked = false
    private var likeCount = 0

    private lateinit var slidePagerAdapter: SlidePagerAdapter
    private val slideList = mutableListOf<Slide>()


    private lateinit var userService : UserService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)

        val spf = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val postedId = spf.getString("user_id", "")
        Log.d("PostedDetailFragment", "postedID: ${postedId}")

        userService = RetrofitClient.create(UserService::class.java, requireContext())

        userService.getFriendProfile(postedId.toString()).enqueue(object : Callback<FriendProfileResponse> {
            override fun onResponse(
                p0: Call<FriendProfileResponse>,
                response: Response<FriendProfileResponse>
            ) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    val userProfileImg = response.body()?.success?.profileImage
                    val userNickName = response.body()?.success?.nickname

                    binding.profileName.text = userNickName

                    binding.profileDate.text = getCurrentPostedDate()

                    context?.let {
                        Glide.with(it)
                            .load(userProfileImg ?: R.drawable.ic_myprofile)  // 기본 프로필 이미지 설정
                            .into(binding.postBlogFriendIv)
                    }
                } else {
                    Log.e("PostApiHelper", "모먼트 프로필 업데이트 실패: ${response.body()?.error}")
                }
            }

            override fun onFailure(p0: Call<FriendProfileResponse>, p1: Throwable) {
                TODO("Not yet implemented")
            }

        })

        // 번들로부터 데이터 받기
        val title = arguments?.getString("title")
        val content = arguments?.getString("content")
        val slideContents = arguments?.getStringArrayList("slideContents")
        val imageUris = arguments?.getParcelableArrayList<Uri>("imageUris")

        // 제목 설정
        binding.postTitle.text = title ?: ""
//        binding.postContent.text = content ?: ""
        Log.d("PostDetailFragment", "내용: $content")

        if (imageUris.isNullOrEmpty()) {
            // 슬라이드가 없을 경우
            binding.postContent.visibility = View.VISIBLE
//            binding.postContent.setText(slideContents?.getOrNull(0) ?: "")  // 텍스트 설정
            binding.postContent.text = content ?: ""
            binding.viewPager.visibility = View.GONE  // 슬라이드 숨김
        } else {
            // 슬라이드가 있을 경우 데이터를 슬라이드 리스트에 추가
            if (slideContents != null) {
                for (i in imageUris.indices) {
                    slideList.add(Slide(imageUris[i], slideContents[i]))  // URI만 추가
                }
            }
            setupViewPager()
            binding.postContent.visibility = View.GONE  // 텍스트 숨김
            binding.postContent.text = content ?: ""
        }

        setupLikeButton()
        setupReplyButton() // 댓글 버튼 설정

        return binding.root
    }


    private fun setupViewPager() {
        slidePagerAdapter = SlidePagerAdapter(
            slideList,
            onImageClick = { position ->
                // 슬라이드 클릭 시 추가 동작 (예: 전체 화면 보기)
            },
            onDeleteClick = { position ->
                // 삭제 동작이 필요한 경우 (지금은 PostFragment에서 처리 중이라면 생략 가능)
            }
        )
        binding.viewPager.adapter = slidePagerAdapter
    }

    private fun setupReplyButton() {
        binding.buttonReply.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, CommentFragment()) // CommentFragment로 변경
                .addToBackStack(null) // 뒤로 가기 기능 추가
                .commit()
        }
    }

    private fun setupLikeButton() {
        // 초기 좋아요 개수 표시
        updateLikeVisibility()

        // 좋아요 버튼 클릭 이벤트 처리
        binding.buttonHand.setOnClickListener {
            if (isLiked) {
                likeCount--
                binding.buttonHand.setImageResource(R.drawable.ic_hand)  // 비활성화 아이콘
            } else {
                likeCount++
                binding.buttonHand.setImageResource(R.drawable.ic_hand)  // 활성화 아이콘
            }
            isLiked = !isLiked  // 상태 반전

            // 좋아요 개수 UI 업데이트
            binding.likeCount.text = likeCount.toString()
            updateLikeVisibility()
        }
    }

    // 좋아요 개수 0일 때 숨기고, 1 이상일 때 보이게 설정하는 함수
    private fun updateLikeVisibility() {
        if (likeCount == 0) {
            binding.likeCount.visibility = View.GONE
        } else {
            binding.likeCount.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // 메모리 누수 방지
    }
}