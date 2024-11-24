package soma.achoom.zigg.report.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import soma.achoom.zigg.comment.exception.CommentNotFoundException
import soma.achoom.zigg.comment.repository.CommentRepository
import soma.achoom.zigg.post.exception.PostNotFoundException
import soma.achoom.zigg.post.repository.PostRepository
import soma.achoom.zigg.report.dto.ReportRequestDto
import soma.achoom.zigg.report.entity.Report
import soma.achoom.zigg.report.entity.ReportType
import soma.achoom.zigg.report.repository.ReportRepository
import soma.achoom.zigg.s3.entity.S3DataType
import soma.achoom.zigg.s3.service.S3Service
import soma.achoom.zigg.user.exception.UserNotFoundException
import soma.achoom.zigg.user.repository.UserRepository
import kotlin.jvm.optionals.getOrElse

@Service
class ReportService(
    private val s3Service: S3Service,
    private val reportRepository: ReportRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val objectMapper: ObjectMapper
) {
    @Transactional(readOnly = false)
    fun reportPost(authentication: Authentication, postId: Long, reportRequestDto: ReportRequestDto) {
        val post = postRepository.findById(postId).getOrElse { throw PostNotFoundException() }
        val postJsonData = objectMapper.writeValueAsString(post)
        println(postJsonData)
        reportRepository.save(
            Report(
                reportMessage = reportRequestDto.reportMessage,
                reportSpecific = reportRequestDto.reportSpecific,
                reportType = ReportType.POST_REPORT
            )
        )
        s3Service.putJsonDataToS3(S3DataType.POST_REPORT,postJsonData)
    }

    @Transactional(readOnly = false)
    fun reportUser(authentication: Authentication, userId: Long, reportRequestDto: ReportRequestDto) {
        val user = userRepository.findById(userId).getOrElse { throw UserNotFoundException() }
        val userJsonData = objectMapper.writeValueAsString(user)
        println(userJsonData)
        reportRepository.save(
            Report(
                reportMessage = reportRequestDto.reportMessage,
                reportSpecific = reportRequestDto.reportSpecific,
                reportType = ReportType.USER_REPORT
            )
        )
        s3Service.putJsonDataToS3(S3DataType.USER_REPORT,userJsonData)
    }

    @Transactional(readOnly = false)
    fun reportComment(authentication: Authentication, commentId: Long, reportRequestDto: ReportRequestDto) {
        val comment = commentRepository.findById(commentId).getOrElse { throw CommentNotFoundException() }
        val commentJsonData = objectMapper.writeValueAsString(comment)
        println(commentJsonData)
        reportRepository.save(
            Report(
                reportMessage = reportRequestDto.reportMessage,
                reportSpecific = reportRequestDto.reportSpecific,
                reportType = ReportType.COMMENT_REPORT
            )
        )
        s3Service.putJsonDataToS3(S3DataType.COMMENT_REPORT,commentJsonData)
    }
}