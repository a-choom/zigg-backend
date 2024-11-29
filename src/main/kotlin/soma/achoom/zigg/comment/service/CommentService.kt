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
import soma.achoom.zigg.comment.exception.*
import soma.achoom.zigg.comment.repository.CommentCreatorRepository
import soma.achoom.zigg.comment.repository.CommentLikeRepository
import soma.achoom.zigg.comment.repository.CommentRepository
import soma.achoom.zigg.post.exception.PostNotFoundException
import soma.achoom.zigg.post.repository.PostRepository
import soma.achoom.zigg.s3.service.S3Service
import soma.achoom.zigg.user.dto.UserResponseDto
import soma.achoom.zigg.user.entity.User
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

        return commentRepository.findCommentsByPost(post).filter { it.commentType == CommentType.COMMENT }.map{
            generateCommentResponse(it,user)
        }
    }
    @Transactional(readOnly = false)
    fun createChildComment(authentication: Authentication,boardId:Long, postId:Long,commentId: Long, commentRequestDto: CommentRequestDto) : List<CommentResponseDto>{
        val user = userService.authenticationToUser(authentication)
        boardRepository.findById(boardId).orElseThrow { BoardNotFoundException() }
        val post = postRepository.findById(postId).orElseThrow { PostNotFoundException() }
        val parentComment = commentRepository.findById(commentId).orElseThrow { CommentNotFoundException() }
        if(parentComment.commentType == CommentType.REPLY){
            throw AlreadyChildCommentException()
        }

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

        return commentRepository.findCommentsByPost(post).filter { it.commentType == CommentType.COMMENT }.map{
            generateCommentResponse(it,user)
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

        return commentRepository.findCommentsByPost(post).filter { it.commentType == CommentType.COMMENT }.map{
            generateCommentResponse(it,user)
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
        commentRepository.save(comment)
        post.comments-=1
        postRepository.save(post)
        return commentRepository.findCommentsByPost(post).filter { it.commentType == CommentType.COMMENT }.map{
            generateCommentResponse(it,user)
        }
    }
    @Transactional(readOnly = false)
    fun likeComment(authentication: Authentication,boardId: Long,postId: Long, commentId: Long):CommentResponseDto{
        val user = userService.authenticationToUser(authentication)
        boardRepository.findById(boardId).orElseThrow{BoardNotFoundException()}
        val post = postRepository.findById(postId).orElseThrow{PostNotFoundException()}
        val comment = commentRepository.findById(commentId).orElseThrow{CommentNotFoundException()}
        if (commentLikeRepository.existsCommentLikesByCommentAndUser(comment,user)){
            throw AlreadyLikedCommentException()
        }
        val commentLike = CommentLike(
            comment = comment,
            user = user
        )
        commentLikeRepository.save(commentLike)
        return generateCommentResponse(comment,user)
    }

    @Transactional(readOnly = false)
    fun unlikeComment(authentication: Authentication, boardId: Long, postId: Long, commentId: Long):CommentResponseDto{
        val user = userService.authenticationToUser(authentication)
        boardRepository.findById(boardId).orElseThrow{BoardNotFoundException()}
        val post = postRepository.findById(postId).orElseThrow{PostNotFoundException()}
        val comment = commentRepository.findById(commentId).orElseThrow{CommentNotFoundException()}
        if (!commentLikeRepository.existsCommentLikesByCommentAndUser(comment,user)){
            throw CommentLikeNotFoundException()
        }
        commentLikeRepository.deleteCommentLikeByCommentAndUser(comment,user)
        return generateCommentResponse(comment,user)
    }

     fun generateCommentResponse(comment: Comment,user: User): CommentResponseDto{
        return CommentResponseDto(
            commentId = comment.commentId,
            commentLike = comment.likes,
            commentMessage = if(comment.isDeleted) "삭제된 메세지입니다." else if (comment.creator.user in user.ignoreUsers) "차단되어 숨김 처리된 메세지입니다." else comment.textComment,
            commentCreator = UserResponseDto(
                userId = comment.creator.user?.userId,
                userName = if(comment.isDeleted || comment.creator.user == null) "알수없음" else if (comment.creator.user in user.ignoreUsers) "차단된 유저" else if (comment.creator.anonymous) comment.creator.anonymousName else comment.creator.user?.name,
                profileImageUrl = if (comment.isDeleted || comment.creator.anonymous || comment.creator.user == null || comment.creator.user in user.ignoreUsers) null else s3Service.getPreSignedGetUrl(comment.creator.user?.profileImageKey?.imageKey),
            ),
            createdAt = comment.createAt,
            isAnonymous = comment.creator.anonymous,
            isLiked = commentLikeRepository.existsCommentLikesByCommentAndUser(comment,user),
            childComment = comment.replies.map { reply->
                CommentResponseDto(
                    commentId = reply.commentId,
                    commentLike = reply.likes,
                    commentMessage = if(reply.isDeleted) "삭제된 메세지입니다." else if (reply.creator.user in user.ignoreUsers) "차단되어 숨김 처리된 메세지입니다." else reply.textComment,
                    commentCreator = UserResponseDto(
                        userId = reply.creator.user?.userId,
                        userName = if(reply.creator.user == null || reply.isDeleted) "알수없음" else if (reply.creator.user in user.ignoreUsers) "차단된 유저" else if(reply.creator.anonymous) reply.creator.anonymousName else reply.creator.user?.name,
                        profileImageUrl = if(reply.isDeleted || reply.creator.anonymous || reply.creator.user == null || reply.creator.user in user.ignoreUsers) null else s3Service.getPreSignedGetUrl(reply.creator.user?.profileImageKey?.imageKey)
                    ),
                    createdAt = reply.createAt,
                    isAnonymous = reply.creator.anonymous,
                    isLiked = commentLikeRepository.existsCommentLikesByCommentAndUser(reply,user)
                )
            }.toMutableList()
        )
    }
}