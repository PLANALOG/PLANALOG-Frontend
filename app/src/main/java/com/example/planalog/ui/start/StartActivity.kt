package com.example.planalog.ui.start

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.planalog.MainActivity
import com.example.planalog.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val spf = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = spf.getString("user_id", null)
        val type = spf.getString("type", null)

        binding.btnStart.setOnClickListener {
//            if (!userId.isNullOrEmpty()) {
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
//                finish()  // 현재 액티비티 종료
//            } else {
                val intent = Intent(this, StartsetActivity::class.java)
                startActivity(intent)
                finish()  // 현재 액티비티 종료
//            }

        }
    }
}