package soma.achoom.zigg.comment.exception

class AlreadyLikedCommentException : RuntimeException() {
    override val message: String
        get() = "이미 좋아요를 누른 댓글입니다."
}