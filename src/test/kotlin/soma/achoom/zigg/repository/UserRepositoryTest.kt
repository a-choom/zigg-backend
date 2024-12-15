package soma.achoom.zigg.repository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import soma.achoom.zigg.auth.dto.OAuthProviderEnum
import soma.achoom.zigg.content.entity.Image
import soma.achoom.zigg.user.entity.User
import soma.achoom.zigg.user.repository.UserRepository
import kotlin.test.Test

@SpringBootTest()
@ActiveProfiles("test")
@Transactional
class UserRepositoryTest {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `find user by username`() {

        val user = User(
            name = "test",
            nickname = "test",
            profileImageKey = Image(
                uploader = null,
                imageKey = "test"
            ),
            jwtToken = "test",
            providerId = "test",
            platform = OAuthProviderEnum.TEST
        )
        userRepository.save(user)

        val user1 = userRepository.findUsersByUserNameLike(userName = user.name!!, pageable = Pageable.ofSize(10))
        for (u in user1) {
            assert(u.name == user.name)
        }

    }
}