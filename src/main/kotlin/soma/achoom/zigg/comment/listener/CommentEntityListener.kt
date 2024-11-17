package soma.achoom.zigg.comment.listener

import jakarta.persistence.PrePersist
import jakarta.persistence.PreRemove
import soma.achoom.zigg.comment.entity.Comment

class CommentEntityListener {
    @PrePersist
    fun prePersist(comment: Comment){
        comment.post.comments += 1
    }
}