data class SearchPostResponse(
    val resultType: String,
    val error: ErrorDetail?,  // 객체로 처리
    val success: PostSuccess?
)

data class ErrorDetail(
    val reason: String?  // 에러 메시지를 담는 필드
)

data class PostSuccess(
    val message: String,
    val data: PostSearchItem  // 단일 객체로 수정
)

data class PostSearchItem(
    val id: Int,
    val email: String,
    val createdAt: String,
    val introduction: String,
    val link: String,
    val nickname: String
)
