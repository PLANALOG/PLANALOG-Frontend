package com.example.planalog.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.planalog.databinding.ActivityEditprofileBinding

class EditprofileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditprofileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ViewBinding 초기화
        binding = ActivityEditprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 버튼 클릭 리스너 설정
        binding.backButton.setOnClickListener {
            finish()
        }
        binding.exit.setOnClickListener {
            val intent = Intent(this, ExitpopupActivity::class.java)
            startActivity(intent)
        }

        binding.icAddprofile.setOnClickListener{
            val intent = Intent(this, EditcharacterActivity::class.java)
            startActivity(intent)
        }

        binding.confirmButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val intro = binding.introEditText.text.toString()
            val link = binding.linkEditText.text.toString()

            // 저장 로직 추가
            Toast.makeText(
                this@EditprofileActivity,
                "저장됨: 이름=$name, 소개=$intro, 링크=$link",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
