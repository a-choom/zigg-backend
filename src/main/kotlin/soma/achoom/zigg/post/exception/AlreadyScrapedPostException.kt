package soma.achoom.zigg.post.exception

class AlreadyScrapedPostException : RuntimeException() {
    override val message: String
        get() = "이미 스크랩한 게시물입니다."
}