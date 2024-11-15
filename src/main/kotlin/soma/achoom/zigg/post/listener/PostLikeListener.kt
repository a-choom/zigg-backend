package soma.achoom.zigg.post.listener

import jakarta.persistence.PrePersist
import jakarta.persistence.PreRemove
import soma.achoom.zigg.post.entity.PostLike

class PostLikeListener {
    @PrePersist
    fun prePersist(postLike: PostLike){
        postLike.post.likes += 1
    }
    @PreRemove
    fun preRemove(postLike: PostLike){
        postLike.post.likes -= 1
    }
}