package soma.achoom.zigg.report.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import soma.achoom.zigg.comment.exception.CommentNotFoundException
import soma.achoom.zigg.comment.repository.CommentRepository
import soma.achoom.zigg.post.exception.PostNotFoundException
import soma.achoom.zigg.post.repository.PostRepository
import soma.achoom.zigg.report.dto.CommentReportMetaDto
import soma.achoom.zigg.report.dto.PostReportMetaDto
import soma.achoom.zigg.report.dto.ReportRequestDto
import soma.achoom.zigg.report.dto.UserReportMetaDto
import soma.achoom.zigg.report.entity.Report
import soma.achoom.zigg.report.entity.ReportType
import soma.achoom.zigg.report.repository.ReportRepository
import soma.achoom.zigg.s3.entity.S3DataType
import soma.achoom.zigg.s3.service.S3Service
import soma.achoom.zigg.user.exception.UserNotFoundException
import soma.achoom.zigg.user.repository.UserRepository
import soma.achoom.zigg.user.service.UserService
import kotlin.jvm.optionals.getOrElse

@Service
class ReportService(
    private val s3Service: S3Service,
    private val reportRepository: ReportRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val objectMapper: ObjectMapper,
    private val userService: UserService,

    ) {

    @Transactional(readOnly = false)
    fun reportPost(authentication: Authentication, postId: Long, reportRequestDto: ReportRequestDto) {
        ObjectMapper().apply {
            registerModule(JavaTimeModule())
            val post = postRepository.findById(postId).getOrElse { throw PostNotFoundException() }
            val postJsonData = objectMapper.writeValueAsString(
                PostReportMetaDto(
                    postId = post.postId!!,
                    userId = post.creator?.userId!!,
                    imageContent = post.imageContents.map { it.imageId!! },
                    videoContent = post.videoContent?.videoId,
                    textContent = post.textContent
                )
            )
            println(postJsonData)
            reportRepository.save(
                Report(
                    reportMessage = reportRequestDto.reportMessage,
                    reportSpecific = reportRequestDto.reportSpecific,
                    reportType = ReportType.POST_REPORT
                )
            )
            s3Service.putJsonDataToS3(S3DataType.POST_REPORT, postJsonData)
        }
    }

    @Transactional(readOnly = false)
    fun reportUser(authentication: Authentication, userId: Long, reportRequestDto: ReportRequestDto) {
        ObjectMapper().apply {
            registerModule(JavaTimeModule())
        }
        val reporter = userService.authenticationToUser(authentication)
        val user = userRepository.findById(userId).getOrElse { throw UserNotFoundException() }
        val userJsonData = objectMapper.writeValueAsString(
            UserReportMetaDto(
                userId = user.userId!!,
                profileImage = user.profileImageKey.imageId!!
            )
        )
        println(userJsonData)
        reportRepository.save(
            Report(
                reportMessage = reportRequestDto.reportMessage,
                reportSpecific = reportRequestDto.reportSpecific,
                reportType = ReportType.USER_REPORT
            )
        )
        reporter.ignoreUsers.add(user)
        userRepository.save(reporter)
        s3Service.putJsonDataToS3(S3DataType.USER_REPORT, userJsonData)
    }

    @Transactional(readOnly = false)
    fun reportComment(authentication: Authentication, commentId: Long, reportRequestDto: ReportRequestDto) {
        ObjectMapper().apply {
            registerModule(JavaTimeModule())
        }
        val comment = commentRepository.findById(commentId).getOrElse { throw CommentNotFoundException() }
        val commentJsonData = objectMapper.writeValueAsString(
            CommentReportMetaDto(
                userId = comment.creator.user?.userId!!,
                commentId = comment.commentId!!,
                textContent = comment.textComment
            )
        )
        println(commentJsonData)
        reportRepository.save(
            Report(
                reportMessage = reportRequestDto.reportMessage,
                reportSpecific = reportRequestDto.reportSpecific,
                reportType = ReportType.COMMENT_REPORT
            )
        )
        s3Service.putJsonDataToS3(S3DataType.COMMENT_REPORT, commentJsonData)
    }
}
