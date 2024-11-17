package soma.achoom.zigg.comment.exception

class AlreadyChildCommentException : RuntimeException() {
    override val message: String
        get() = "대댓글에는 대댓글을 생성할 수 없습니다."
}