package com.example.planalog.ui.post

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.planalog.R

class SlidePagerAdapter(
    private val slideList: MutableList<Slide>,
    private val onImageClick: (position: Int) -> Unit
) : RecyclerView.Adapter<SlidePagerAdapter.SlideViewHolder>() {

    inner class SlideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val postContent: EditText = itemView.findViewById(R.id.postContent)

        fun bind(slide: Slide) {
            // 이미지와 초기 텍스트 설정
            slide.imageResId?.let { uri -> imageView.setImageURI(uri) }
            postContent.setText(slide.postContent)

            // 텍스트 변경 리스너 추가
            postContent.addTextChangedListener(object : android.text.TextWatcher {
                override fun afterTextChanged(s: android.text.Editable?) {
                    // 텍스트 변경 즉시 리스트에 반영
                    slide.postContent = s.toString()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_slide, parent, false)
        return SlideViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlideViewHolder, position: Int) {
        holder.bind(slideList[position])
    }

    override fun getItemCount(): Int = slideList.size
}
