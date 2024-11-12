package soma.achoom.zigg.global.util

class S3UrlParser {
    companion object {
        fun extractionKeyFromUrl(url: String): String {
            val imageKey = url.split("?")[0]
                .split("/")
                .subList(3, url.split("?")[0].split("/").size)
                .joinToString("/")
            return imageKey
        }
    }
}