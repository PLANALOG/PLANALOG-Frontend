package com.example.planalog.ui.profile

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.planalog.R
import com.example.planalog.databinding.FragmentProfileBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.user.response.MypageMoment
import com.example.planalog.network.user.response.MypageResponse
import com.example.planalog.network.user.MypageService
import com.example.planalog.network.user.response.UserResponse
import com.example.planalog.network.user.UserService
import android.Manifest
import com.example.planalog.network.user.response.UserInfo
import com.example.planalog.network.user.response.UserProfileImgResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var userService: UserService
    private lateinit var mypageService: MypageService
    private var mypageMomentAdapter: MypageMomentAdapter? = null  // 어댑터를 한 번만 초기화
    private val galleryRequestCode = 100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Retrofit을 이용해 초기화
        userService = RetrofitClient.create(UserService::class.java, requireContext())
        mypageService = RetrofitClient.create(MypageService::class.java, requireContext())

        // RecyclerView 설정
        binding.recyclerViewMoments.layoutManager = LinearLayoutManager(requireContext())

        // 유저 정보 가져오기
        fetchUserProfile()
        fetchMypageMoments()

        setupUI()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerViewMoments.layoutManager = LinearLayoutManager(requireContext())
        mypageMomentAdapter = MypageMomentAdapter(emptyList())  // 초기 데이터는 빈 리스트로 설정
        binding.recyclerViewMoments.adapter = mypageMomentAdapter
    }

    private fun fetchUserProfile() {
        userService.getUserInfo().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val userInfo = response.body()?.success
                    if (userInfo != null) {
                        updateUI(userInfo)  // UI에 유저 정보 표시
                        Log.d("ProfileFragment", "유저 정보: $userInfo")
                    } else {
                        Toast.makeText(requireContext(), "유저 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "에러: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("ProfileFragment", "네트워크 오류", t)
            }
        })
    }

    private fun fetchMypageMoments() {
        mypageService.getMypageMoments().enqueue(object : Callback<MypageResponse> {
            override fun onResponse(call: Call<MypageResponse>, response: Response<MypageResponse>) {
                if (!isAdded) {
                    // Fragment가 Activity에 붙어 있지 않으면 종료
                    return
                }

                context?.let { ctx ->
                    if (response.isSuccessful) {
                        val moments = response.body()?.success?.data ?: emptyList()

                        Log.d("MypageService", "성공적으로 가져온 Moments 개수: ${moments.size}")
                        moments.forEach { moment ->
                            Log.d("MypageService", "Moment ID: ${moment.momentId}, Title: ${moment.title}")
                        }

                        if (moments.isNotEmpty()) {
                            updateMoments(moments)
                        } else {
                            Toast.makeText(ctx, "Moment 게시글이 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("MypageService", "서버 응답 오류: ${response.errorBody()?.string()}")
                        Toast.makeText(ctx, "에러 발생: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<MypageResponse>, t: Throwable) {
                if (!isAdded) {
                    // Fragment가 Activity에 붙어 있지 않으면 종료
                    return
                }
                context?.let { ctx ->
                    Log.e("MypageService", "네트워크 오류: ${t.message}", t)
                    Toast.makeText(ctx, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun updateMoments(moments: List<MypageMoment>) {
        // 어댑터의 데이터를 새로고침하여 표시
        mypageMomentAdapter?.updateData(moments)
    }

    private fun updateUI(userInfo: UserInfo) {
        if (!isAdded) {
            // Fragment가 Activity에 붙어 있지 않으면 UI 업데이트를 중단
            return
        }

        binding.userName.text = userInfo.nickname
        binding.introText.text = userInfo.introduction

        context?.let {
            Glide.with(it)
                .load(userInfo.profileImage ?: R.drawable.ic_myprofile)  // 기본 프로필 이미지 설정
                .into(binding.profileImage)
        }
    }


    private fun setupUI() {
        // 프로필 추가 아이콘 클릭 이벤트 설정
        binding.icAddprofile.setOnClickListener {
            if (hasGalleryPermission()) {
                openGallery()
            } else {
                requestGalleryPermission()
            }
        }

        // 프로필 편집 버튼 클릭 이벤트 설정
        binding.editProfileButton.setOnClickListener {
            val intent = Intent(requireContext(), EditprofileActivity::class.java)
            startActivity(intent)
        }

        // 플래너 설정 버튼 클릭 이벤트 설정
        binding.plannerSettingButton.setOnClickListener {
            val intent = Intent(requireContext(), EditcharacterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun hasGalleryPermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }


    private fun requestGalleryPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                galleryRequestCode
            )
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                galleryRequestCode
            )
        }
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"  // 모든 이미지 타입 설정
        galleryLauncher.launch(intent)
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val selectedImageUri: Uri? = result.data?.data
            if (selectedImageUri != null) {
                updateProfileImage(selectedImageUri)
            }
        }
    }

    private fun updateProfileImage(imageUri: Uri) {

        val imageFile = getFileFromUri(imageUri)

        if (imageFile != null) {
            // 이미지 데이터를 바이너리로 변환하여 RequestBody 생성
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
            val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

            Log.d("ProfileImageUpload", "업로드 시작 - 이미지 파일 경로: ${imageFile.path}")

            // basicImage는 빈 값으로 설정
            val basicImageBody = RequestBody.create("text/plain".toMediaTypeOrNull(), "")

            userService.uploadProfileImage(imagePart, basicImageBody).enqueue(object : Callback<UserProfileImgResponse> {
                    override fun onResponse(call: Call<UserProfileImgResponse>, response: Response<UserProfileImgResponse>) {
                        if (response.isSuccessful) {
                            Log.d("ProfileImageUpload", "업로드 성공 - 상태 코드: ${response.code()}")
                            Toast.makeText(requireContext(), "프로필 이미지가 업데이트되었습니다.", Toast.LENGTH_SHORT).show()

                            val savedUrl = response.body()?.success?.savedUrl
                            // UI에 새 프로필 이미지 반영
                            savedUrl?.let {
                                Glide.with(requireContext())
                                    .load(it)
                                    .placeholder(R.drawable.ic_myprofile)
                                    .into(binding.profileImage)
                            }
                        } else {
                            Log.e("ProfileImageUpload", "업로드 실패 - 상태 코드: ${response.code()}, 메시지: ${response.message()}")
                            Toast.makeText(requireContext(), "서버 오류: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<UserProfileImgResponse>, t: Throwable) {
                        Log.e("ProfileImageUpload", "업로드 실패 - 네트워크 오류: ${t.message}", t)
                        Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            Log.e("ProfileImageUpload", "이미지 파일 변환 실패")
            Toast.makeText(requireContext(), "이미지 파일 변환에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }





    // Helper function to convert Uri to File
    private fun getFileFromUri(uri: Uri): File? {
        return try {
            // 임시 파일을 캐시 디렉토리에 생성
            val tempFile = File.createTempFile("temp_image", ".jpg", requireContext().cacheDir)
            // Uri를 사용하여 InputStream(파일의 내용을 읽는 스트림) 가져오기
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            inputStream?.use { input ->  // inputStream이 null이 아니면 실행
                tempFile.outputStream().use { output ->  // 임시 파일로 데이터를 복사
                    input.copyTo(output)  // 이미지 데이터를 임시 파일로 복사
                }
            }
            tempFile  // 임시 파일 반환
        } catch (e: Exception) {
            e.printStackTrace()
            null  // 오류 발생 시 null 반환
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == galleryRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("PermissionResult", "갤러리 권한 허용됨")
                openGallery()
            } else {
                Log.e("PermissionResult", "갤러리 권한 거부됨")
                Toast.makeText(requireContext(), "갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}