package soma.achoom.zigg.comment.repository

import org.springframework.data.jpa.repository.JpaRepository
import soma.achoom.zigg.comment.entity.Comment
import soma.achoom.zigg.comment.entity.CommentLike
import soma.achoom.zigg.post.entity.Post
import soma.achoom.zigg.user.entity.User

interface CommentLikeRepository : JpaRepository<CommentLike,Long> {
    fun findCommentLikesByCommentPost(post:Post) : List<CommentLike>
    fun findCommentLikeByCommentAndUser(comment: Comment, user: User): CommentLike?
    fun existsCommentLikesByCommentAndUser(comment: Comment,user: User):Boolean
    fun deleteCommentLikeByCommentAndUser(comment: Comment,user: User)
    fun findCommentLikesByUser(user: User):List<CommentLike>
    fun deleteCommentLikesByUser(user: User)
    fun deleteCommentLikesByCommentPost(post:Post)

}