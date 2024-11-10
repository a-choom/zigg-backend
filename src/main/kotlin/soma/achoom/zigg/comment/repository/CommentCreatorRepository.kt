package soma.achoom.zigg.comment.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import soma.achoom.zigg.comment.entity.CommentCreator
import soma.achoom.zigg.post.entity.Post
import soma.achoom.zigg.user.entity.User

interface CommentCreatorRepository: JpaRepository<CommentCreator, Long> {
    fun findCommentCreatorByPostAndUserAndAnonymous(post: Post, user: User,anonymous:Boolean): CommentCreator?
    @Query("SELECT COUNT(c) FROM CommentCreator c WHERE c.post = :post AND c.anonymous = true AND c.anonymousName like '익명%'")
    fun countAnonymousInPost(post: Post): Int
}