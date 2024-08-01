package soma.achoom.zigg.v0.repository

import org.springframework.data.jpa.repository.JpaRepository
import soma.achoom.zigg.v0.model.SpaceUser
import soma.achoom.zigg.v0.model.User

interface SpaceUserRepository : JpaRepository<SpaceUser,Long> {
    fun findSpaceUsersByUser(user: User): List<SpaceUser>
}