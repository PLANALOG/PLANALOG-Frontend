package com.example.planalog.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class VerticalSpaceItemDecoration(private val spaceHeight: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        // 첫 번째 아이템이 아닌 경우에만 간격 추가
        if (parent.getChildAdapterPosition(view) != 0) {
            outRect.top = spaceHeight  // 아이템 위쪽에 간격 추가
        }
    }
}
