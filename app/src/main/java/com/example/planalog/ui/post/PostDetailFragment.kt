package com.example.planalog.ui.post

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.planalog.R
import com.example.planalog.databinding.FragmentPostDetailBinding

class PostDetailFragment : Fragment() {
    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!

    // 좋아요 상태 및 개수 저장 변수
    private var isLiked = false
    private var likeCount = 0

    private lateinit var slidePagerAdapter: SlidePagerAdapter
    private val slideList = mutableListOf<Slide>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)

        // 번들로부터 데이터 받기
        val title = arguments?.getString("title")
        val slideContents = arguments?.getStringArrayList("slideContents")
        val imageUris = arguments?.getParcelableArrayList<Uri>("imageUris")

        // 제목 설정
        binding.postTitle.text = title ?: ""

        if (imageUris.isNullOrEmpty()) {
            // 슬라이드가 없을 경우
            binding.postContent.visibility = View.VISIBLE
            binding.postContent.setText(slideContents?.getOrNull(0) ?: "")  // 텍스트 설정
            binding.viewPager.visibility = View.GONE  // 슬라이드 숨김
        } else {
            // 슬라이드가 있을 경우
            if (slideContents != null) {
                for (i in imageUris.indices) {
                    slideList.add(Slide(imageUris[i], slideContents[i]))
                }
            }
            setupViewPager()
            binding.postContent.visibility = View.GONE  // 텍스트 숨김
        }

        setupLikeButton()

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