package com.example.planalog.network.friend.response

data class FriendpageResponse(
    val resultType: String,
    val error: String?,
    val success: FriendpageData?
)

data class FriendpageData(
    val data: List<FriendpageMoment>
)

data class FriendpageMoment(
    val momentId: Int,
    val title: String,
    val date: String,  // ✅ createdAt → date 로 변경
    val userName: String,  // ✅ 추가: JSON에서 제공되는 userName 반영
    val likingCount: Int,  // ✅ 추가: 좋아요 개수 반영
    val commentCount: Int,  // ✅ 추가: 댓글 개수 반영
    val thumbnailURL: String  // ✅ 필드명 통일
)
