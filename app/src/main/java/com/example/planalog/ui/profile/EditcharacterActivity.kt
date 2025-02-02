package com.example.planalog.ui.profile

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.planalog.R
import com.example.planalog.databinding.ActivityEditcharacterBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.user.UserService
import com.example.planalog.network.user.response.UserProfileImgResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditcharacterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditcharacterBinding
    private lateinit var userService: UserService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 초기화
        binding = ActivityEditcharacterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("EditcharacterActivity", "Activity created and binding initialized.")

        // Retrofit 서비스 초기화
        userService = RetrofitClient.create(UserService::class.java, this)
        Log.d("EditcharacterActivity", "Retrofit ProfileimageService initialized.")

        // 캐릭터 이미지 배열
        val characterImages = arrayOf(
            R.drawable.ic_character1,
            R.drawable.ic_character2,
            R.drawable.ic_character3,
            R.drawable.ic_character4,
            R.drawable.ic_character5,
            R.drawable.ic_character6,
            R.drawable.ic_character7,
            R.drawable.ic_character8
        )

        // 각 캐릭터 클릭 리스너 설정
        val characterViews = arrayOf(
            binding.character1,
            binding.character2,
            binding.character3,
            binding.character4,
            binding.character5,
            binding.character6,
            binding.character7,
            binding.character8
        )

        characterViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                Log.d("EditcharacterActivity", "Character ${index + 1} clicked.")
                // 선택한 캐릭터로 프로필 이미지 변경
                binding.profileImage.setImageResource(characterImages[index])
                Log.d("EditcharacterActivity", "Profile image updated to character ${index + 1}.")

                // 서버로 basicImage 값 전송
                sendBasicImageToServer(index + 1) // 서버에 1부터 시작하는 번호 전송
            }
        }

        // 뒤로가기 버튼 클릭 리스너
        binding.backButton.setOnClickListener {
            Log.d("EditcharacterActivity", "Back button clicked, finishing activity.")
            finish()
        }
    }

    private fun sendBasicImageToServer(basicImage: Int) {
        Log.d("EditcharacterActivity", "Sending basicImage value to server: $basicImage")
        // 빈 이미지로 설정 (null 처리)
        val imagePart: MultipartBody.Part? = null

        // 선택한 기본 이미지 번호를 요청 바디에 추가
        val basicImageBody = RequestBody.create("text/plain".toMediaTypeOrNull(), basicImage.toString())

        userService.uploadProfileImage(imagePart, basicImageBody).enqueue(object : Callback<UserProfileImgResponse> {
            override fun onResponse(call: Call<UserProfileImgResponse>, response: Response<UserProfileImgResponse>) {
                if (response.isSuccessful) {
                    Log.d("EditcharacterActivity", "Profile image updated successfully. Response code: ${response.code()}")
                    Toast.makeText(this@EditcharacterActivity, "프로필 이미지가 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("EditcharacterActivity", "Profile image update failed. Response code: ${response.code()}, message: ${response.message()}")
                    Toast.makeText(this@EditcharacterActivity, "업데이트 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserProfileImgResponse>, t: Throwable) {
                Log.e("EditcharacterActivity", "Network error while updating profile image: ${t.message}", t)
                Toast.makeText(this@EditcharacterActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
