package soma.achoom.zigg.service

import org.joda.time.DateTime
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import soma.achoom.zigg.TestConfig
import soma.achoom.zigg.data.DummyDataUtil
import soma.achoom.zigg.feedback.service.FeedbackService
import soma.achoom.zigg.space.entity.Space
import soma.achoom.zigg.user.entity.User
import soma.achoom.zigg.user.repository.UserRepository
import kotlin.random.Random

@SpringBootTest(
    classes = [TestConfig::class]
)
@ActiveProfiles("test")
@Transactional
class FeedbackServiceTest {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var feedbackService: FeedbackService

    @Autowired
    private lateinit var dummyDataUtil: DummyDataUtil

    private lateinit var user: User

    private lateinit var space:Space

    @BeforeEach
    fun setup(){
        user = dummyDataUtil.createDummyUserWithMultiFCMToken(Random.nextInt(1, 5))
        space = dummyDataUtil.createDummySpace()
        user.spaces.add(dummyDataUtil.createDummySpaceUser(space, user))
        userRepository.save(user)

    }


}