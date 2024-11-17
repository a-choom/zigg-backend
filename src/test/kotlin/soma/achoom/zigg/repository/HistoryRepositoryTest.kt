package soma.achoom.zigg.repository

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import soma.achoom.zigg.content.entity.Image
import soma.achoom.zigg.content.entity.Video
import soma.achoom.zigg.data.DummyDataUtil
import soma.achoom.zigg.history.entity.History
import soma.achoom.zigg.history.repository.HistoryRepository
import soma.achoom.zigg.space.entity.Space
import soma.achoom.zigg.space.entity.SpaceUser
import soma.achoom.zigg.space.repository.SpaceRepository
import soma.achoom.zigg.space.repository.SpaceUserRepository

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class HistoryRepositoryTest {
    @Autowired
    private lateinit var spaceUserRepository: SpaceUserRepository

    @Autowired
    private lateinit var historyRepository: HistoryRepository

    @Autowired
    private lateinit var spaceRepository: SpaceRepository

    @Autowired
    private lateinit var dummyDataUtil: DummyDataUtil

    @Test
    fun `get Recent Histories`() {
        val user = dummyDataUtil.createDummyUser()
        val space = spaceRepository.save(
            Space(
                name = "Test",
                imageKey = Image(
                    imageKey = "Test",
                    uploader = user
                )
            )
        )
        val spaceUser = spaceUserRepository.save(
            SpaceUser(
                space = space,
                user = user
            )
        )
        val history = historyRepository.save(
            History(
                name = "history",
                videoKey = Video(
                    videoKey = "Tesssst",
                    uploader = user,
                    duration = "10:00:00"
                ),
                videoThumbnailUrl = Image(
                    imageKey = "Testssst",
                    uploader = user
                )
            )
        )
    }

}