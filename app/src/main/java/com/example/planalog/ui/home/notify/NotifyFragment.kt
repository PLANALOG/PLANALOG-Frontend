package com.example.planalog.ui.comment.com.example.planalog.ui.home.notify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.planalog.databinding.FragmentNotifyBinding

class NotifyFragment : Fragment() {
    private var _binding: FragmentNotifyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotifyBinding.inflate(inflater, container, false)

        binding.agreeBtn.setOnClickListener {
            //팝업창 호출
            ConfirmDialogFragment().show(parentFragmentManager, "ConfirmDialog")
        }

        binding.agreeBtn2.setOnClickListener {
            //팝업창 호출
            ConfirmDialogFragment().show(parentFragmentManager, "ConfirmDialog")
        }

        return binding.root
    }
}