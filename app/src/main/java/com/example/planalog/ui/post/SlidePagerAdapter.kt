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
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val postContent: EditText = itemView.findViewById(R.id.postContent)

        init {
            imageView.setOnClickListener {
                onImageClick(adapterPosition)
            }
        }

        fun bind(slide: Slide) {
            slide.imageResId?.let { uri ->
                imageView.setImageURI(uri) // 이미지 설정
            }
            postContent.setText(slide.postContent)

            // EditText 내용 변경 감지
            postContent.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) { // 포커스 잃었을 때
                    slide.postContent = postContent.text.toString()
                }
            }
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
