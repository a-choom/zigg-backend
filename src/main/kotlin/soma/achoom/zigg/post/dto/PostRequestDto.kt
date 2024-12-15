package soma.achoom.zigg.post.dto

import soma.achoom.zigg.content.dto.VideoRequestDto

data class PostRequestDto(
    val postTitle: String,
    val postMessage: String,
    val postImageContent: MutableSet<String> = mutableSetOf(),
    val anonymous: Boolean = true,
    val postVideoContent: VideoRequestDto? = null,
    val postVideoThumbnail: String? = null,
    val historyId: Long? = null
)