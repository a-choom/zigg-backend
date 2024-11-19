package soma.achoom.zigg.global.util

import soma.achoom.zigg.global.exception.S3UrlFormatUnMatchException

class S3UrlParser {
    companion object {
        fun extractionKeyFromUrl(url: String): String {
            val imageKey = url.split("?")[0]
                .split("/")
                .subList(3, url.split("?")[0].split("/").size)
                .joinToString("/")
            if (imageKey.isEmpty()){
                throw S3UrlFormatUnMatchException()
            }
            return imageKey
        }
    }
}