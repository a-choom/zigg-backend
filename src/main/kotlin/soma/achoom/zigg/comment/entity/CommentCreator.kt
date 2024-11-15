package soma.achoom.zigg.comment.entity

import jakarta.persistence.*
import soma.achoom.zigg.global.BaseEntity
import soma.achoom.zigg.post.entity.Post
import soma.achoom.zigg.user.entity.User

@Entity
class CommentCreator(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    var user : User?,

    @ManyToOne(fetch = FetchType.LAZY)
    val post: Post,

    @Column(name = "is_anonymous")
    var anonymous: Boolean,

    var anonymousName : String? = null

) : BaseEntity()