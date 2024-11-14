package soma.achoom.zigg.comment.service

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import soma.achoom.zigg.board.exception.BoardNotFoundException
import soma.achoom.zigg.board.repository.BoardRepository
import soma.achoom.zigg.comment.dto.CommentRequestDto
import soma.achoom.zigg.comment.dto.CommentResponseDto
import soma.achoom.zigg.comment.entity.Comment
import soma.achoom.zigg.comment.entity.CommentCreator
import soma.achoom.zigg.comment.entity.CommentLike
import soma.achoom.zigg.comment.entity.CommentType
import soma.achoom.zigg.comment.exception.AlreadyChildCommentException
import soma.achoom.zigg.comment.exception.CommentNotFoundException
import soma.achoom.zigg.comment.exception.CommentUserMissMatchException
import soma.achoom.zigg.comment.repository.CommentCreatorRepository
import soma.achoom.zigg.comment.repository.CommentLikeRepository
import soma.achoom.zigg.comment.repository.CommentRepository
import soma.achoom.zigg.post.exception.PostNotFoundException
import soma.achoom.zigg.post.repository.PostRepository
import soma.achoom.zigg.s3.service.S3Service
import soma.achoom.zigg.user.dto.UserResponseDto
import soma.achoom.zigg.user.service.UserService

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
    private val userService: UserService,
    private val boardRepository: BoardRepository,
    private val commentLikeRepository: CommentLikeRepository,
    private val commentCreatorRepository: CommentCreatorRepository,
    private val s3Service: S3Service
){

    @Transactional(readOnly = false)
    fun createComment(authentication: Authentication,boardId:Long, postId:Long, commentRequestDto : CommentRequestDto) : List<CommentResponseDto>{
        val user = userService.authenticationToUser(authentication)
        boardRepository.findById(boardId).orElseThrow { BoardNotFoundException() }

        val post = postRepository.findById(postId).orElseThrow { PostNotFoundException() }

        val commentCreator = commentCreatorRepository.findCommentCreatorByPostAndUserAndAnonymous(post, user, commentRequestDto.anonymous) ?: CommentCreator(
            post = post,
            user = user,
            anonymous = commentRequestDto.anonymous,
            anonymousName = if(post.creator == user) "글쓴이(익명)" else if(commentRequestDto.anonymous) "익명 " + (commentCreatorRepository.countAnonymousInPost(post) + 1).toString() else null
        )

        val comment = Comment(
            creator = commentCreator,
            textComment = commentRequestDto.message,
            post = post,
            commentType = CommentType.COMMENT
        )
        commentRepository.save(comment)
        return commentRepository.findCommentsByPost(post).map{
            CommentResponseDto(
                commentId = it.commentId,
                commentLike = it.likes,
                commentMessage = it.textComment,
                commentCreator = UserResponseDto(
                    userId = it.creator.user.userId,
                    userName = if(it.creator.anonymous) it.creator.anonymousName else user.name,
                    profileImageUrl = if(it.creator.anonymous) null else s3Service.getPreSignedGetUrl(it.creator.user.profileImageKey.imageKey),
                ),
                createdAt = it.createAt,
                isAnonymous = it.creator.anonymous,
            )
        }
    }
    @Transactional(readOnly = false)
    fun createChildComment(authentication: Authentication,boardId:Long, postId:Long,commentId: Long, commentRequestDto: CommentRequestDto) : List<CommentResponseDto>{
        val user = userService.authenticationToUser(authentication)
        boardRepository.findById(boardId).orElseThrow { BoardNotFoundException() }
        val post = postRepository.findById(postId).orElseThrow { PostNotFoundException() }
        val parentComment = commentRepository.findById(commentId).orElseThrow { CommentNotFoundException() }

        val commentCreator = commentCreatorRepository.findCommentCreatorByPostAndUserAndAnonymous(post, user, commentRequestDto.anonymous) ?: CommentCreator(
            post = post,
            user = user,
            anonymous = commentRequestDto.anonymous,
            anonymousName = if(post.creator == user) "글쓴이(익명)" else if(commentRequestDto.anonymous) "익명 " + (commentCreatorRepository.countAnonymousInPost(post) + 1).toString() else null
        )
        val childComment = Comment(
            creator = commentCreator,
            commentType = CommentType.REPLY,
            textComment = commentRequestDto.message,
            post = post,
        )

        parentComment.replies.add(childComment)
        commentRepository.save(parentComment)


        return commentRepository.findCommentsByPost(post).map{
            CommentResponseDto(
                commentId = it.commentId,
                commentLike = it.likes,
                commentMessage = it.textComment,
                commentCreator = UserResponseDto(
                    userId = it.creator.user.userId,
                    userName = if(it.creator.anonymous) it.creator.anonymousName else user.name,
                    profileImageUrl = if(it.creator.anonymous) null else s3Service.getPreSignedGetUrl(it.creator.user.profileImageKey.imageKey),
                ),
                createdAt = it.createAt,
                isAnonymous = it.creator.anonymous,
            )
        }
    }
    @Transactional(readOnly = false)
    fun updateComment(authentication: Authentication,boardId: Long, postId:Long,commentId:Long, commentRequestDto: CommentRequestDto) : List<CommentResponseDto>{
        val user = userService.authenticationToUser(authentication)

        val comment = commentRepository.findById(commentId).orElseThrow { CommentNotFoundException() }
        val post = postRepository.findById(postId).orElseThrow { PostNotFoundException() }
        boardRepository.findById(boardId).orElseThrow { BoardNotFoundException() }

        if(comment.creator != user){
            throw CommentUserMissMatchException()
        }
        comment.textComment = commentRequestDto.message
        commentRepository.save(comment)

        return commentRepository.findCommentsByPost(post).map{
            CommentResponseDto(
                commentId = it.commentId,
                commentLike = it.likes,
                commentMessage = it.textComment,
                commentCreator = UserResponseDto(
                    userId = it.creator.user.userId,
                    userName = if(it.creator.anonymous) it.creator.anonymousName else user.name,
                    profileImageUrl = if(it.creator.anonymous) null else s3Service.getPreSignedGetUrl(it.creator.user.profileImageKey.imageKey),
                ),
                createdAt = it.createAt,
                isAnonymous = it.creator.anonymous,
            )
        }
    }
    @Transactional(readOnly = false)
    fun deleteComment(authentication: Authentication,boardId: Long, postId: Long, commentId: Long) : List<CommentResponseDto> {
        val user = userService.authenticationToUser(authentication)
        boardRepository.findById(boardId).orElseThrow { BoardNotFoundException() }
        val post = postRepository.findById(postId).orElseThrow { PostNotFoundException() }
        val comment = commentRepository.findById(commentId).orElseThrow { CommentNotFoundException() }
        if(comment.creator.user != user){
            throw CommentUserMissMatchException()
        }
        comment.isDeleted = true
        comment.creator.anonymous = true
        comment.creator.anonymousName = "알 수 없음."
        comment.textComment = "삭제된 댓글입니다."
        commentRepository.save(comment)
        return commentRepository.findCommentsByPost(post).map{
            CommentResponseDto(
                commentId = it.commentId,
                commentLike = it.likes,
                commentMessage = it.textComment,
                commentCreator = UserResponseDto(
                    userId = it.creator.user.userId,
                    userName = if(it.creator.anonymous) it.creator.anonymousName else user.name,
                    profileImageUrl = if(it.creator.anonymous) null else s3Service.getPreSignedGetUrl(it.creator.user.profileImageKey.imageKey),
                ),
                createdAt = it.createAt,
                isAnonymous = it.creator.anonymous,
            )
        }
    }
    @Transactional(readOnly = false)
    fun likeOrUnlikeComment(authentication: Authentication, boardId:Long, postId:Long, commentId: Long) : CommentResponseDto{
        val user = userService.authenticationToUser(authentication)
        boardRepository.findById(boardId).orElseThrow { BoardNotFoundException() }
        postRepository.findById(postId).orElseThrow { PostNotFoundException() }
        val comment = commentRepository.findById(commentId).orElseThrow { CommentNotFoundException() }

        commentLikeRepository.findCommentLikeByCommentAndUser(comment, user)?.let {
            commentLikeRepository.delete(it)
            return CommentResponseDto(
                commentId = comment.commentId,
                commentLike = comment.likes,
                commentMessage = comment.textComment,
                commentCreator = UserResponseDto(
                    userId = comment.creator.user.userId,
                    userName = if(comment.creator.anonymous) comment.creator.anonymousName else comment.creator.user.name,
                    profileImageUrl = if (comment.creator.anonymous) null else s3Service.getPreSignedGetUrl(comment.creator.user.profileImageKey.imageKey),
                ),
                createdAt = comment.createAt,
                isAnonymous = comment.creator.anonymous,
            )
        }

        val commentLike = CommentLike(
            user = user,
            comment = comment
        )
        commentLikeRepository.save(commentLike)
        return CommentResponseDto(
            commentId = comment.commentId,
            commentLike = comment.likes,
            commentMessage = comment.textComment,
            commentCreator = UserResponseDto(
                userId = comment.creator.user.userId,
                userName = if (comment.creator.anonymous) comment.creator.anonymousName else comment.creator.user.name,
                profileImageUrl = if (comment.creator.anonymous) null else s3Service.getPreSignedGetUrl(comment.creator.user.profileImageKey.imageKey),
            ),
            createdAt = comment.createAt,
            isAnonymous = comment.creator.anonymous,
        )
    }
}