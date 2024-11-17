package soma.achoom.zigg.post.entity

import jakarta.persistence.*
import soma.achoom.zigg.global.BaseEntity
import soma.achoom.zigg.post.listener.PostScrapListener
import soma.achoom.zigg.user.entity.User

@Entity
@EntityListeners(PostScrapListener::class)
class PostScrap(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    val post: Post
) : BaseEntity() {
}
