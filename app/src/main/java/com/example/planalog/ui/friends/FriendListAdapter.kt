package com.example.planalog.ui.friends

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.planalog.R
import com.example.planalog.databinding.ItemFriendlistBinding
import com.example.planalog.network.friend.Friend

class FriendListAdapter(private var friendList: List<Friend>) :
    RecyclerView.Adapter<FriendListAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(private val binding: ItemFriendlistBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var currentPopupPosition: Int = -1

        fun bind(friend: Friend) {
            binding.friendName.text = friend.nickname
            val clickListener = View.OnClickListener {
                val bundle = Bundle().apply {
                    putInt("id", friend.id)
                }
                it.findNavController().navigate(R.id.action_friendListFragment_to_friendpageFragment, bundle)

            }

            binding.profileImage.setOnClickListener(clickListener)
            binding.friendName.setOnClickListener(clickListener)


            binding.moreIcon.setOnClickListener {
                // 현재 보이는 팝업을 닫기
                if (currentPopupPosition != -1 && currentPopupPosition != adapterPosition) {
                    notifyItemChanged(currentPopupPosition)  // 이전 팝업 숨기기
                }

                // 새 팝업 상태 토글
                if (binding.deletePopup.visibility == View.GONE) {
                    binding.deletePopup.visibility = View.VISIBLE
                    currentPopupPosition = adapterPosition
                } else {
                    binding.deletePopup.visibility = View.GONE
                    currentPopupPosition = -1
                }
            }

            // 삭제 버튼 클릭 이벤트
            binding.deletePopup.setOnClickListener {
                Toast.makeText(binding.root.context, "${friend.nickname} 삭제됨", Toast.LENGTH_SHORT)
                    .show()
                binding.deletePopup.visibility = View.GONE
                currentPopupPosition = -1
            }

            // 이전 상태를 확인하여 삭제 팝업 상태 설정
            binding.deletePopup.visibility = if (currentPopupPosition == adapterPosition) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemFriendlistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(friendList[position])
    }

    override fun getItemCount(): Int = friendList.size

    fun updateFriends(newFriends: List<Friend>) {
        this.friendList = newFriends
        notifyDataSetChanged()
    }
}
