package soma.achoom.zigg.global.exception

class S3UrlFormatUnMatchException : RuntimeException() {
    override val message: String
        get() = "콘텐츠 저장소 양식을 확인하세요."
}