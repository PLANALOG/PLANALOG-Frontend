package com.example.planalog.ui.comment.com.example.planalog.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planalog.databinding.DropdownRecyclerBinding
import com.example.planalog.ui.comment.com.example.planalog.ui.home.calender.DropdownAdapter

fun showDropdownMenu(anchorView: View, items: List<String>, onItemSelected: (String) -> Unit) {
    // PopupWindow에서 사용할 바인딩 객체 생성
    val binding = DropdownRecyclerBinding.inflate(LayoutInflater.from(anchorView.context))

    // RecyclerView 설정
    val recyclerView = binding.dropdownMenuLayout
    recyclerView.layoutManager = LinearLayoutManager(anchorView.context)

    // PopupWindow 생성 (먼저 정의)
    val popupWindow = PopupWindow(
        binding.root, // 바인딩된 루트 뷰를 팝업에 설정
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        true // 외부 터치 시 닫힘
    )

    // 어댑터 설정
    recyclerView.adapter = DropdownAdapter(items) { selectedItem ->
        onItemSelected(selectedItem) // 선택된 아이템 콜백 호출
        popupWindow.dismiss() // 팝업 닫기
    }

    // 팝업 위치 설정 (anchorView 바로 아래에 표시)
    popupWindow.showAsDropDown(anchorView)
}