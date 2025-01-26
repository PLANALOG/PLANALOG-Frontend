package com.example.planalog.ui.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.planalog.R
import com.example.planalog.databinding.ActivityEditcharacterBinding

class EditcharacterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditcharacterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 초기화
        binding = ActivityEditcharacterBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                // 선택한 캐릭터로 프로필 이미지 변경
                binding.profileImage.setImageResource(characterImages[index])
            }
        }

        // 뒤로가기 버튼 클릭 리스너
        binding.backButton.setOnClickListener {
            finish()
        }
    }
}