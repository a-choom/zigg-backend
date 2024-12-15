package soma.achoom.zigg.post.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import soma.achoom.zigg.board.entity.Board
import soma.achoom.zigg.content.entity.Image
import soma.achoom.zigg.content.entity.Video
import soma.achoom.zigg.global.BaseEntity
import soma.achoom.zigg.user.entity.User

@Entity(name = "post")
class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val postId: Long? = null,

    @ManyToOne(cascade = [CascadeType.MERGE], fetch = FetchType.LAZY)
    @JsonIgnore
    val board: Board,

    @ManyToOne
    @JoinColumn(name = "creator")
    var creator: User?,

    @Column(name = "title")
    var title: String,

    @Column(name = "text_content")
    @Lob
    var textContent: String,

    @Column(name = "is_anonymous")
    val anonymous : Boolean,
    @Column(name = "like_cnt")
    var likes : Int = 0,
    @Column(name = "scrap_cnt")
    var scraps:  Int = 0,
    @Column(name = "comment_cnt")
    var comments:Int = 0,

    @OneToMany(cascade = [CascadeType.PERSIST,CascadeType.MERGE])
    var imageContents: MutableList<Image> = mutableListOf(),

    @ManyToOne(cascade = [CascadeType.PERSIST,CascadeType.MERGE])
    var videoContent: Video? = null,

    @ManyToOne(cascade = [CascadeType.PERSIST,CascadeType.MERGE])
    var videoThumbnail: Image? = null,

    @Version
    var version: Int = 0

    ) : BaseEntity() {

}