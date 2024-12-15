package soma.achoom.zigg.service

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import soma.achoom.zigg.board.entity.Board
import soma.achoom.zigg.board.repository.BoardRepository
import soma.achoom.zigg.data.DummyDataUtil
import soma.achoom.zigg.post.entity.Post
import soma.achoom.zigg.post.repository.PostRepository
import soma.achoom.zigg.report.dto.ReportRequestDto
import soma.achoom.zigg.report.service.ReportService
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReportServiceTest {
    @Autowired
    private lateinit var reportService: ReportService

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var dummyDataUtil: DummyDataUtil

    @Autowired
    private lateinit var boardRepository : BoardRepository

}