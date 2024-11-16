package soma.achoom.zigg.post.listener

import jakarta.persistence.PrePersist
import jakarta.persistence.PreRemove
import soma.achoom.zigg.post.entity.PostScrap

class PostScrapListener {
    @PrePersist
    fun prePersist(postScrap: PostScrap){
        postScrap.post.scraps += 1
    }
    @PreRemove
    fun preRemove(postScrap: PostScrap){
        postScrap.post.scraps -= 1
    }
}