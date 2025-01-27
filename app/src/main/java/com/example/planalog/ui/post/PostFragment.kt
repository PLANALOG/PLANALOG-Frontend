package com.example.planalog.ui.post

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.planalog.R
import com.example.planalog.databinding.FragmentPostBinding

class PostFragment : Fragment() {
    private lateinit var binding: FragmentPostBinding
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostBinding.inflate(inflater, container, false)

        setupImagePicker()
        setupPostContentListener()

        // 사진 선택 버튼 클릭 시
        binding.photoButton.setOnClickListener {
            openImagePicker()
        }

        // 글 작성 후 완료 버튼 클릭 시
        binding.uploadButton.setOnClickListener {
            // 입력된 제목과 내용을 가져옴
            val title = binding.postTitle.text.toString()
            val content = binding.postContent.text.toString()

            // PostDetailFragment로 데이터 전달
            val bundle = Bundle().apply {
                putString("title", title)
                putString("content", content)
                putParcelable("imageUri", selectedImageUri)
            }

            // PostDetailFragment 생성 및 인자 전달
            val postDetailFragment = PostDetailFragment().apply {
                arguments = bundle
            }

            // 프래그먼트 전환
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, postDetailFragment)
                .addToBackStack(null)  // 뒤로가기 기능
                .commit()
        }

        return binding.root
    }

    private fun setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedImageUri = uri
                    binding.selectedImageView.setImageURI(uri)
                    binding.selectedImageView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private fun setupPostContentListener() {
        binding.postContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }
}
