package soma.achoom.zigg.post.service

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import soma.achoom.zigg.board.repository.BoardRepository
import soma.achoom.zigg.comment.dto.CommentResponseDto
import soma.achoom.zigg.comment.entity.Comment
import soma.achoom.zigg.comment.entity.CommentType
import soma.achoom.zigg.comment.repository.CommentRepository
import soma.achoom.zigg.content.dto.ImageResponseDto
import soma.achoom.zigg.content.dto.VideoResponseDto
import soma.achoom.zigg.content.entity.Image
import soma.achoom.zigg.content.entity.Video
import soma.achoom.zigg.global.util.S3UrlParser
import soma.achoom.zigg.history.repository.HistoryRepository
import soma.achoom.zigg.post.dto.PostRequestDto
import soma.achoom.zigg.post.dto.PostResponseDto
import soma.achoom.zigg.post.entity.Post
import soma.achoom.zigg.post.entity.PostLike
import soma.achoom.zigg.post.entity.PostScrap
import soma.achoom.zigg.post.exception.PostCreatorMismatchException
import soma.achoom.zigg.post.exception.PostImageContentMaximumException
import soma.achoom.zigg.post.repository.PostLikeRepository
import soma.achoom.zigg.post.repository.PostRepository
import soma.achoom.zigg.post.repository.PostScrapRepository
import soma.achoom.zigg.s3.service.S3Service
import soma.achoom.zigg.user.dto.UserResponseDto
import soma.achoom.zigg.user.entity.User
import soma.achoom.zigg.user.service.UserService

@Service
class PostService(
    private val postRepository: PostRepository,
    private val userService: UserService,
    private val boardRepository: BoardRepository,
    private val historyRepository: HistoryRepository,
    private val postLikeRepository: PostLikeRepository,
    private val postScrapRepository: PostScrapRepository,
    private val s3Service: S3Service,
    private val commentRepository: CommentRepository
) {

    companion object {
        private const val POST_PAGE_SIZE = 15
        private const val POST_BEST_SIZE = 2
        private const val POST_IMAGE_MAX = 5
    }

    @Transactional(readOnly = false)
    fun createPost(authentication: Authentication, boardId: Long, postRequestDto: PostRequestDto): PostResponseDto {
        val user = userService.authenticationToUser(authentication)
        val board = boardRepository.findById(boardId).orElseThrow { IllegalArgumentException("Board not found") }

        if(postRequestDto.postImageContent.size > POST_IMAGE_MAX) {
            throw PostImageContentMaximumException(POST_IMAGE_MAX)
        }

        val history = postRequestDto.historyId?.let {
            historyRepository.findHistoryByHistoryId(it)
        }
        val post = Post(
            title = postRequestDto.postTitle,
            textContent = postRequestDto.postMessage,
            imageContents = postRequestDto.postImageContent.map {
                Image.fromUrl(
                    uploader = user, imageUrl = it
                )
            }.toMutableList(),
            videoContent = postRequestDto.postVideoContent?.let {
                history?.videoKey ?: Video.fromUrl(
                    uploader = user, videoUrl = it.videoKey, duration = it.videoDuration
                )
            },
            videoThumbnail = postRequestDto.postVideoThumbnail?.let {
                history?.videoThumbnailUrl ?: Image.fromUrl(
                    uploader = user, imageUrl = it
                )
            },
            board = board,
            creator = user,
            anonymous = postRequestDto.anonymous
        )
        postRepository.save(post)
        return generatePostResponse(post, user)
    }

    @Transactional(readOnly = true)
    fun getPosts(authentication: Authentication, boardId: Long, page: Int): List<PostResponseDto> {
        val user = userService.authenticationToUser(authentication)
        val board = boardRepository.findById(boardId).orElseThrow { IllegalArgumentException("Board not found") }
        val sort = Sort.by(Sort.Order.desc("createAt"))
        val posts = postRepository.findPostsByBoard(board, PageRequest.of(page, POST_PAGE_SIZE, sort))
        return posts.map {
            generatePostResponse(it, user)
        }.toList()

    }

    @Transactional(readOnly = true)
    fun getPost(authentication: Authentication, boardId: Long, postId: Long): PostResponseDto {
        val user = userService.authenticationToUser(authentication)
        val post = postRepository.findById(postId).orElseThrow { IllegalArgumentException("Post not found") }
        val comments = commentRepository.findCommentsByPost(post)
        return generatePostResponse(post, comments, user)
    }

    @Transactional(readOnly = true)
    fun searchPosts(
        authentication: Authentication,
        boardId: Long,
        keyword: String,
        page: Int,
    ): List<PostResponseDto> {
        val user = userService.authenticationToUser(authentication)
        val sort = Sort.by(Sort.Order.desc("createAt"))
        val board = boardRepository.findById(boardId).orElseThrow { IllegalArgumentException("Board not found") }
        val posts = postRepository.findPostsByBoardAndTitleContaining(
            board,
            keyword,
            PageRequest.of(page, POST_PAGE_SIZE, sort)
        )
        return posts.map {
            generatePostResponse(it, user)
        }.toList()
    }

    @Transactional(readOnly = false)
    fun updatePost(authentication: Authentication, postId: Long, postRequestDto: PostRequestDto): PostResponseDto {
        val user = userService.authenticationToUser(authentication)

        val post = postRepository.findById(postId).orElseThrow { IllegalArgumentException("Post not found") }

        if (postRequestDto.postImageContent.size > POST_IMAGE_MAX) {
            throw PostImageContentMaximumException(POST_IMAGE_MAX)
        }

        if (post.creator.userId != user.userId) {
            throw PostCreatorMismatchException()
        }

        post.title = postRequestDto.postTitle
        post.textContent = postRequestDto.postMessage

        postRequestDto.postVideoContent?.let {
            val extractedVideoKey = S3UrlParser.extractionKeyFromUrl(it.videoKey)
            if (post.videoContent?.videoKey != extractedVideoKey) {
                post.videoThumbnail = postRequestDto.postVideoThumbnail?.let { thumbnailUrl ->
                    Image.fromUrl(uploader = post.creator, imageUrl = thumbnailUrl)
                }
                post.videoContent = Video.fromUrl(
                    uploader = post.creator,
                    videoUrl = extractedVideoKey,
                    duration = it.videoDuration
                )
            }
        } ?: run {
            post.videoContent = null
            post.videoThumbnail = null
        }

        post.imageContents.removeIf { existingImage ->
            existingImage.imageKey !in postRequestDto.postImageContent.map(S3UrlParser::extractionKeyFromUrl)
        }

        postRequestDto.postImageContent.forEach { imageUrl ->
            val imageKey = S3UrlParser.extractionKeyFromUrl(imageUrl)
            if (post.imageContents.none { it.imageKey == imageKey }) {
                post.imageContents.add(
                    Image.fromUrl(uploader = post.creator, imageUrl = imageKey)
                )
            }
        }

        postRepository.save(post)
        val comments = commentRepository.findCommentsByPost(post)
        return generatePostResponse(post, comments, user)
    }


    @Transactional(readOnly = false)
    fun deletePost(authentication: Authentication, postId: Long) {
        val user = userService.authenticationToUser(authentication)
        val post = postRepository.findById(postId).orElseThrow { IllegalArgumentException("Post not found") }
        if (post.creator.userId != user.userId) {
            throw PostCreatorMismatchException()
        }
        postRepository.delete(post)
    }

    @Transactional(readOnly = false)
    fun likeOrUnlikePost(authentication: Authentication, postId: Long): PostResponseDto {
        val user = userService.authenticationToUser(authentication)
        val post = postRepository.findById(postId).orElseThrow { IllegalArgumentException("Post not found") }
        val postLike = postLikeRepository.findByPostAndUser(post, user)
        if (postLike == null) {
            postLikeRepository.save(PostLike(user = user, post = post))
        } else {
            postLikeRepository.delete(postLike)
        }
        return generatePostResponse(post, user)
    }

    @Transactional(readOnly = false)
    fun scrapOrUnscrapPost(authentication: Authentication, postId: Long): PostResponseDto {
        val user = userService.authenticationToUser(authentication)
        val post = postRepository.findById(postId).orElseThrow { IllegalArgumentException("Post not found") }
        val postScrap = postScrapRepository.findByPostAndUser(post, user)
        if (postScrap == null) {
            postScrapRepository.save(PostScrap(user = user, post = post))
        } else {
            postScrapRepository.delete(postScrap)
        }
        return generatePostResponse(post, user)
    }

    @Transactional(readOnly = true)
    fun getMyPosts(authentication: Authentication): List<PostResponseDto> {
        val user = userService.authenticationToUser(authentication)
        val post = postRepository.findPostsByCreator(user)
        return post.map {
            generatePostResponse(it, user)
        }.toList()

    }

    @Transactional(readOnly = true)
    fun getScrapedPosts(authentication: Authentication): List<PostResponseDto> {
        val user = userService.authenticationToUser(authentication)
        val post = postScrapRepository.findByUser(user).map { it.post }
        return post.map {
            generatePostResponse(it, user)
        }.toList()
    }

    @Transactional(readOnly = true)
    fun getLikedPosts(authentication: Authentication): List<PostResponseDto> {
        val user = userService.authenticationToUser(authentication)
        val post = postLikeRepository.findByUser(user).map { it.post }
        return post.map {
            generatePostResponse(it, user)
        }.toList()
    }

    @Transactional(readOnly = true)
    fun getCommentedPosts(authentication: Authentication): List<PostResponseDto> {
        val user = userService.authenticationToUser(authentication)
        val post = commentRepository.findCommentsByCreatorUser(user).filter { !it.isDeleted }.map { it.post }
        return post.map {
            generatePostResponse(it, user)
        }.toSet().toList()
    }

    @Transactional(readOnly = true)
    fun getPopularPosts(authentication: Authentication): List<PostResponseDto> {
        val user = userService.authenticationToUser(authentication)
        val posts = postRepository.findBestPosts(Pageable.ofSize(POST_BEST_SIZE))
        return posts.map {
            generatePostResponse(it, user)
        }.toList()
    }


    private fun generatePostResponse(post: Post, user: User): PostResponseDto {
        return PostResponseDto(postId = post.postId!!,
            postTitle = post.title,
            postMessage = post.textContent,
            postImageContents = post.imageContents.map { ImageResponseDto(s3Service.getPreSignedGetUrl(it.imageKey)) }
                .toList(),
            postThumbnailImage = post.videoThumbnail?.let { ImageResponseDto(s3Service.getPreSignedGetUrl(it.imageKey)) }
                ?: post.imageContents.firstOrNull()
                    ?.let { ImageResponseDto(s3Service.getPreSignedGetUrl(it.imageKey)) },

            postVideoContent = post.videoContent?.let {
                VideoResponseDto(
                    videoUrl = s3Service.getPreSignedGetUrl(post.videoContent!!.videoKey),
                    videoDuration = it.duration
                )
            },
            likeCnt = postLikeRepository.countPostLikesByPost(post),
            scrapCnt = postScrapRepository.countPostScrapsByPost(post),
            isScraped = postScrapRepository.existsPostScrapByPostAndUser(post, user),
            isLiked = postLikeRepository.existsPostLikeByPostAndUser(post, user),
            commentCnt = commentRepository.countCommentsByPost(post),
            createdAt = post.createAt,
            postCreator = UserResponseDto(
                userId = post.creator.userId,
                userName = if (post.anonymous) "익명" else post.creator.name,
                userNickname = post.creator.nickname,
                profileImageUrl = if (post.anonymous) null else s3Service.getPreSignedGetUrl(post.creator.profileImageKey.imageKey)
            ),
            isAnonymous = post.anonymous
        )
    }

    private fun generatePostResponse(post: Post, comments: List<Comment>, user: User): PostResponseDto {
        return PostResponseDto(postId = post.postId!!,
            postTitle = post.title,
            postMessage = post.textContent,
            postImageContents = post.imageContents.map { ImageResponseDto(s3Service.getPreSignedGetUrl(it.imageKey)) }
                .toList(),
            postThumbnailImage = post.videoThumbnail?.let { ImageResponseDto(s3Service.getPreSignedGetUrl(it.imageKey)) }
                ?: post.imageContents.firstOrNull()
                    ?.let { ImageResponseDto(s3Service.getPreSignedGetUrl(it.imageKey)) },
            postVideoContent = post.videoContent?.let {
                VideoResponseDto(
                    videoUrl = s3Service.getPreSignedGetUrl(post.videoContent!!.videoKey),
                    videoDuration = it.duration
                )
            },
            likeCnt = postLikeRepository.countPostLikesByPost(post),
            scrapCnt = postScrapRepository.countPostScrapsByPost(post),
            isScraped = postScrapRepository.existsPostScrapByPostAndUser(post, user),
            isLiked = postLikeRepository.existsPostLikeByPostAndUser(post, user),
            commentCnt = commentRepository.countCommentsByPost(post),
            createdAt = post.createAt,
            postCreator = UserResponseDto(
                userId = post.creator.userId,
                userName = if (post.anonymous) "익명" else post.creator.name,
                profileImageUrl = if (post.anonymous) null else s3Service.getPreSignedGetUrl(post.creator.profileImageKey.imageKey)
            ),
            isAnonymous = post.anonymous,
            comments = commentRepository.findCommentsByPost(post).filter {
                it.commentType == CommentType.COMMENT
            }.map {
                CommentResponseDto(
                    commentId = it.commentId,
                    commentMessage = it.textComment,
                    commentLike = it.likes,
                    commentCreator = UserResponseDto(
                        userId = it.creator.user.userId,
                        userName = if (it.creator.anonymous) it.creator.anonymousName else it.creator.user.name,
                        profileImageUrl = if (post.anonymous) null else s3Service.getPreSignedGetUrl(post.creator.profileImageKey.imageKey),
                    ),
                    createdAt = it.createAt,
                    childComment = it.replies.map { comment ->
                        CommentResponseDto(
                            commentId = comment.commentId,
                            commentMessage = comment.textComment,
                            commentLike = comment.likes,
                            commentCreator = UserResponseDto(
                                userId = comment.creator.user.userId,
                                userName = if (comment.creator.anonymous) comment.creator.anonymousName else comment.creator.user.name,
                                profileImageUrl = if (post.anonymous) null else s3Service.getPreSignedGetUrl(post.creator.profileImageKey.imageKey),
                            ),
                            createdAt = comment.createAt,
                            isAnonymous = comment.creator.anonymous
                        )
                    }.toMutableList(),
                    isAnonymous = it.creator.anonymous
                )
            }
        )
    }
}