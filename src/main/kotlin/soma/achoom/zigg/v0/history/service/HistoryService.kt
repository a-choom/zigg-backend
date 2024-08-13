package soma.achoom.zigg.v0.history.service

import jakarta.transaction.Transactional
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import soma.achoom.zigg.v0.space.exception.SpaceNotFoundException
import soma.achoom.zigg.global.util.BaseService
import soma.achoom.zigg.global.infra.GCSService
import soma.achoom.zigg.v0.ai.dto.GenerateThumbnailRequestDto
import soma.achoom.zigg.v0.ai.service.AIService
import soma.achoom.zigg.v0.feedback.dto.FeedbackResponseDto
import soma.achoom.zigg.v0.history.dto.HistoryRequestDto
import soma.achoom.zigg.v0.history.dto.HistoryResponseDto
import soma.achoom.zigg.v0.history.entity.History
import soma.achoom.zigg.v0.history.repository.HistoryRepository
import soma.achoom.zigg.v0.space.repository.SpaceRepository
import java.util.UUID

@Service
class HistoryService @Autowired constructor(
    private val historyRepository: HistoryRepository,
    private val spaceRepository: SpaceRepository,
    private val gcsService: GCSService,
    private val aiService: AIService,
    @Value("\${gcp.bucket.name}")
    private val bucketName: String,
) : BaseService() {
    suspend fun createHistory(
        authentication: Authentication,
        spaceId: UUID,
        historyRequestDto: HistoryRequestDto
    ): HistoryResponseDto = coroutineScope {

        getAuthUser(authentication)

        val space = spaceRepository.findSpaceBySpaceId(spaceId)
            ?: throw SpaceNotFoundException()

        historyRequestDto.historyVideoUrl
            ?: throw IllegalArgumentException("historyVideoUrl is required")

        val bucketKey = gcsService.convertPreSignedUrlToGeneralKey(historyRequestDto.historyVideoUrl)


        val response = aiService.createThumbnailRequest(
                GenerateThumbnailRequestDto(
                    bucketName = bucketName,
                    historyVideoKey = bucketKey
                )
        )

        val uuid = getLastPathSegment(bucketKey).split(".")[0]

        val history = History(
            historyId = UUID.fromString(uuid),
            historyVideoKey = bucketKey,
            historyName = historyRequestDto.historyName,
            space = space,
            historyVideoThumbnailUrl = response.historyThumbnailKey
        )

        println(response.historyThumbnailKey)
        historyRepository.save(history)

        return@coroutineScope HistoryResponseDto(
            historyId = history.historyId,
            historyName = history.historyName,
            historyVideoPreSignedUrl = gcsService.getPreSignedGetUrl(history.historyVideoKey),
            feedbacks = history.feedbacks.map { feedback ->
                FeedbackResponseDto.from(feedback)
            }.toMutableSet(),
            historyVideoThumbnailPreSignedUrl = gcsService.getPreSignedGetUrl(history.historyVideoThumbnailUrl!!)
        )
    }

    fun getHistories(authentication: Authentication, spaceId: UUID): List<HistoryResponseDto> {
        getAuthUser(authentication)
        val space = spaceRepository.findSpaceBySpaceId(spaceId)
            ?: throw SpaceNotFoundException()
        val histories = historyRepository.findHistoriesBySpace(space)
        return histories.filter { !it.isDeleted }.map {
            HistoryResponseDto(
                historyId = it.historyId,
                historyName = it.historyName,
                historyVideoPreSignedUrl = gcsService.getPreSignedGetUrl(it.historyVideoKey),
                feedbacks = it.feedbacks.map { feedback ->
                    FeedbackResponseDto.from(feedback)
                }.toMutableSet(),
                historyVideoThumbnailPreSignedUrl = gcsService.getPreSignedGetUrl(it.historyVideoThumbnailUrl!!)
            )
        }
    }

    fun getHistory(authentication: Authentication, spaceId: UUID, historyId: UUID): HistoryResponseDto {
        getAuthUser(authentication)
        val space = spaceRepository.findSpaceBySpaceId(spaceId)
            ?: throw SpaceNotFoundException()
        val history = historyRepository.findHistoryByHistoryId(historyId)
            ?: throw SpaceNotFoundException()
        return HistoryResponseDto(
            historyId = history.historyId,
            historyName = history.historyName,
            historyVideoPreSignedUrl = gcsService.getPreSignedGetUrl(history.historyVideoKey),
            feedbacks = history.feedbacks.map { feedback ->
                FeedbackResponseDto.from(feedback)
            }.toMutableSet(),
            historyVideoThumbnailPreSignedUrl = gcsService.getPreSignedGetUrl(history.historyVideoThumbnailUrl!!)
        )
    }

    suspend fun updateHistory(
        authentication: Authentication,
        spaceId: UUID,
        historyId: UUID,
        historyRequestDto: HistoryRequestDto
    ): HistoryResponseDto  = coroutineScope{
        getAuthUser(authentication)
        val space = spaceRepository.findSpaceBySpaceId(spaceId)
            ?: throw SpaceNotFoundException()
        val history = historyRepository.findHistoryByHistoryId(historyId)
            ?: throw SpaceNotFoundException()
        history.isDeleted = true
        historyRepository.save(history)
        val newHistory = createHistory(authentication, spaceId, historyRequestDto)
        return@coroutineScope newHistory
    }

    fun deleteHistory(authentication: Authentication, spaceId: UUID, historyId: UUID) {
        getAuthUser(authentication)
        val space = spaceRepository.findSpaceBySpaceId(spaceId)
            ?: throw SpaceNotFoundException()
        val history = historyRepository.findHistoryByHistoryId(historyId)
            ?: throw SpaceNotFoundException()
        history.isDeleted = true
        historyRepository.save(history)
    }
    private fun getLastPathSegment(path: String):String{
        return path.split("/").last()
    }
}