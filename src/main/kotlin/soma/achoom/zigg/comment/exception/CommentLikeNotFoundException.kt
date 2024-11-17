package soma.achoom.zigg.comment.exception

class CommentLikeNotFoundException : RuntimeException() {
    override val message: String
        get() = "좋아요를 누르지 않은 댓글입니다."
}