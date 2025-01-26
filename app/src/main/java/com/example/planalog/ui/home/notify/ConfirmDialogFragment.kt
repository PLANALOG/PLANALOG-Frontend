package com.example.planalog.ui.comment.com.example.planalog.ui.home.notify

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.planalog.databinding.FragmentConfirmDialogBinding

class ConfirmDialogFragment : DialogFragment() {
    private var _binding: FragmentConfirmDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConfirmDialogBinding.inflate(inflater, container, false)

        // 취소 버튼
        binding.cancelBtn.setOnClickListener {
            dismiss() // 팝업 닫기
        }

        // 수락 버튼
        binding.acceptBtn.setOnClickListener {
            // 처리 로직 추가
            dismiss() // 팝업 닫기
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // 배경 투명
        return dialog
    }
}
