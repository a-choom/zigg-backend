package soma.achoom.zigg.comment.entity

import jakarta.persistence.*
import soma.achoom.zigg.global.BaseEntity
import soma.achoom.zigg.post.entity.Post

@Entity(name = "comment")
@EntityListeners(CommentEntityListener::class)
class Comment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val commentId : Long? = null,

    @ManyToOne
    val post:Post,

    @ManyToOne
    val parentComment: Comment? = null,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "creator")
    var creator: CommentCreator,
    @Column(name = "text_comment")
    var textComment: String,
    @Column(name = "likes")
    var likes : Int = 0,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    val replies: MutableList<Comment> = mutableListOf(),
    @Column(name = "is_deleted")
    var isDeleted: Boolean = false
    ) : BaseEntity() {

}