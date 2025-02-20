package com.example.planalog.ui.post

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.planalog.R
import com.example.planalog.databinding.FragmentPostDetailBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.api.PostApiHelper
import com.example.planalog.network.post.UserPageMomentResponse
import com.example.planalog.network.user.UserService
import com.example.planalog.network.user.response.FriendProfileResponse
import com.example.planalog.ui.comment.CommentFragment
import com.example.planalog.utils.getCurrentPostedDate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostDetailFragment : Fragment() {
    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!

    private var isLiked = false
    private var likeCount = 0
    private lateinit var slidePagerAdapter: SlidePagerAdapter
    private val slideList = mutableListOf<Slide>()

    private lateinit var userService: UserService
    private lateinit var postApiHelper: PostApiHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        postApiHelper = PostApiHelper(requireContext())

        val spf = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val postedId = spf.getString("user_id", "")
        Log.d("PostDetailFragment", "postedID: $postedId")

        userService = RetrofitClient.create(UserService::class.java, requireContext())
        userService.getFriendProfile(postedId.toString()).enqueue(object : Callback<FriendProfileResponse> {
            override fun onResponse(
                call: Call<FriendProfileResponse>, response: Response<FriendProfileResponse>
            ) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    val userProfileImg = response.body()?.success?.profileImage
                    val userNickName = response.body()?.success?.nickname

                    binding.profileName.text = userNickName
                    binding.profileDate.text = getCurrentPostedDate()

                    context?.let {
                        Glide.with(it)
                            .load(userProfileImg ?: R.drawable.ic_myprofile)
                            .into(binding.postBlogFriendIv)
                    }
                } else {
                    Log.e("PostApiHelper", "모먼트 프로필 업데이트 실패: ${response.body()?.error}")
                }
            }
            override fun onFailure(call: Call<FriendProfileResponse>, t: Throwable) {
                Log.e("PostDetailFragment", "프로필 불러오기 실패: ${t.message}")
            }
        })

        val momentId = arguments?.getInt("momentId", -1)
        Log.d("PostDetailFragment", "받은 momentId: $momentId")

        if (momentId != -1) {
            if (momentId != null) {
                fetchUserPageMomentDetails(momentId)
            }
        } else {
            loadArgumentsData()
        }

        setupLikeButton()
        setupReplyButton()

        return binding.root
    }

    private fun loadArgumentsData() {
        val title = arguments?.getString("title")
        val content = arguments?.getString("content")
        val slideContents = arguments?.getStringArrayList("slideContents")
        val imageUris = arguments?.getParcelableArrayList<Uri>("imageUris")

        binding.postTitle.text = title ?: ""
        binding.profileDate.text = getCurrentPostedDate()

        if (imageUris.isNullOrEmpty()) {
            binding.postContent.visibility = View.VISIBLE
            binding.postContent.text = content ?: ""
            binding.viewPager.visibility = View.GONE
        } else {
            slideList.clear()
            if (slideContents != null) {
                for (i in imageUris.indices) {
                    slideList.add(Slide(imageUris[i], slideContents[i]))
                }
            }
            setupViewPager()
            binding.postContent.visibility = View.GONE
        }
    }

    private fun fetchUserPageMomentDetails(momentId: Int) {
        postApiHelper.fetchUserPageMomentDetail(momentId, object : PostApiHelper.UserPageMomentCallback {
            override fun onSuccess(response: UserPageMomentResponse) {
                val momentData = response.success?.data
                Log.d("PostDetailFragment", "불러온 데이터: $momentData")

                binding.postTitle.text = momentData?.title ?: binding.postTitle.text
                binding.profileDate.text = momentData?.date ?: binding.profileDate.text

                slideList.clear()
                momentData?.momentContents?.forEach { content ->
                    slideList.add(Slide(imageResId = Uri.parse(content.url), postContent = content.content))
                }

                setupViewPager()
            }
            override fun onFailure(errorMessage: String) {
                Log.e("PostDetailFragment", "Moment 불러오기 실패: $errorMessage")
                loadArgumentsData()
            }
        })
    }

    private fun setupViewPager() {
        slidePagerAdapter = SlidePagerAdapter(
            slideList,
            isDetailView = true,
            onImageClick = { position ->
                // TODO: 이미지 클릭 시 추가 기능
            },
            onDeleteClick = { position ->
                // TODO: 이미지 삭제 기능 (현재는 미사용)
            }
        )

        binding.viewPager.adapter = slidePagerAdapter
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
    }

    private fun setupReplyButton() {
        binding.buttonReply.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, CommentFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupLikeButton() {
        updateLikeVisibility()
        binding.buttonHand.setOnClickListener {
            if (isLiked) {
                likeCount--
                binding.buttonHand.setImageResource(R.drawable.ic_hand)
            } else {
                likeCount++
                binding.buttonHand.setImageResource(R.drawable.ic_hand)
            }
            isLiked = !isLiked
            binding.likeCount.text = likeCount.toString()
            updateLikeVisibility()
        }
    }

    private fun updateLikeVisibility() {
        binding.likeCount.visibility = if (likeCount == 0) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
