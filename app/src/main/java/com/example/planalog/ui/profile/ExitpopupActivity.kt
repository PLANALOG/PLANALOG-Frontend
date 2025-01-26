package com.example.planalog.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.Display
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.planalog.databinding.ActivityExitpopupBinding

class ExitpopupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExitpopupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState)
        binding = ActivityExitpopupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val display = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val width = (display.width * 1.0).toInt()
        val params = window.attributes
        params.width = width
        window.attributes = params

        // 확인 버튼 클릭 리스너
        binding.leaveButton.setOnClickListener {
            // 종료 로직 추가 (예: 앱 종료)
            finishAffinity() // 모든 액티비티 종료
        }

        // 취소 버튼 클릭 리스너
        binding.cancelButton.setOnClickListener {
            finish() // 팝업 닫기
        }
    }
}
