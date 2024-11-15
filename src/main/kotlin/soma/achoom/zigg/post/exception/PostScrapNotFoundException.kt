package soma.achoom.zigg.post.exception

class PostScrapNotFoundException : RuntimeException() {
    override val message: String
        get() = "스크랩을 하지 않은 게시물입니다."
}