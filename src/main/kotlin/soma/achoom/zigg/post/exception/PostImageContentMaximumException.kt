package soma.achoom.zigg.post.exception

class PostImageContentMaximumException(
    val maximumImageCount: Int
) : RuntimeException() {

    override val message : String
        get () = "게시글 당 이미지는 최대 $maximumImageCount 개까지만 등록 가능합니다."
}