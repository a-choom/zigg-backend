package soma.achoom.zigg.post.exception

class AlreadyLikedPostException : RuntimeException() {
    override val message: String
        get() = "이미 좋아요를 누를 게시물입니다."
}