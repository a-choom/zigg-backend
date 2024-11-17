package soma.achoom.zigg.comment.listener

import jakarta.persistence.PrePersist
import jakarta.persistence.PreRemove
import soma.achoom.zigg.comment.entity.CommentLike

class CommentLikeEntityListener {
    @PrePersist
    fun prePersist(commentLike: CommentLike) {
        commentLike.comment.likes += 1
    }
    @PreRemove
    fun preRemove(commentLike: CommentLike) {
        commentLike.comment.likes -= 1
    }
}