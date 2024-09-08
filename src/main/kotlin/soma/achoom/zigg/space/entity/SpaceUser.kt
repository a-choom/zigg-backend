package soma.achoom.zigg.space.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import soma.achoom.zigg.feedback.entity.Feedback
import soma.achoom.zigg.global.BaseEntity
import soma.achoom.zigg.user.entity.User
import java.util.*

@Entity
@Table(name = "space_user")
class SpaceUser(
    @Id
    var spaceUserId: UUID = UUID.randomUUID(),

    @ManyToOne
    @JoinColumn(name = "space")
    @JsonBackReference
    var space: Space,

    @ManyToOne
    @JoinColumn(name = "user")
    @JsonBackReference
    var user: User?,

    var userNickname: String = user?.userNickname!!,

    var userName: String = user?.userName!!,

    var profileImageUrl: String = user?.profileImageKey ?: "",

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    var spaceRole: SpaceRole?,


    @Column(name = "is_deleted")
    var isDeleted: Boolean = false,
    ) : BaseEntity() {
    @get:JsonInclude
    val userId: UUID?
        get() = user?.userId


}

