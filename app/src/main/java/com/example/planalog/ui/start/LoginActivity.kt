package com.example.planalog.ui.start
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.planalog.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNaverLogin.setOnClickListener {
            Toast.makeText(this, "Naver Login Clicked", Toast.LENGTH_SHORT).show()
            // Add your Naver login logic here
        }

        binding.btnKakaoLogin.setOnClickListener {
            Toast.makeText(this, "Kakao Login Clicked", Toast.LENGTH_SHORT).show()
            // Add your Kakao login logic here
        }

        binding.btnGoogleLogin.setOnClickListener {
            Toast.makeText(this, "Google Login Clicked", Toast.LENGTH_SHORT).show()
            // Add your Google login logic here
        }
    }
}