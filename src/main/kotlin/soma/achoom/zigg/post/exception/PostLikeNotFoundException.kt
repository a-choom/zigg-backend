package soma.achoom.zigg.post.exception

class PostLikeNotFoundException : RuntimeException() {
    override val message: String
        get() = "좋아요를 누르지 않은 게시물입니다."
}