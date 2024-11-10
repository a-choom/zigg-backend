package soma.achoom.zigg.comment.dto

data class CommentRequestDto(
    val message:String,
    val anonymous:Boolean = true,
)
