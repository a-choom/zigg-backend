package soma.achoom.zigg.post.dto

import com.fasterxml.jackson.annotation.JsonInclude
import soma.achoom.zigg.comment.dto.CommentResponseDto
import soma.achoom.zigg.content.dto.ImageResponseDto
import soma.achoom.zigg.content.dto.VideoResponseDto
import soma.achoom.zigg.user.dto.UserResponseDto
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PostResponseDto(
    val boardId: Long,
    val postId: Long,
    val postTitle: String,
    val postCreator: UserResponseDto,
    val postMessage: String? = null,
    val postImageContents: List<ImageResponseDto>? = null,
    val postVideoContent: VideoResponseDto? = null,
    val postThumbnailImage: ImageResponseDto? = null,
    val comments : List<CommentResponseDto>? = null,
    val isAnonymous: Boolean,
    val likeCnt: Int,
    val commentCnt: Int,
    val scrapCnt: Int,
    val isScraped: Boolean,
    val isLiked: Boolean,
    val createdAt: LocalDateTime,
    )