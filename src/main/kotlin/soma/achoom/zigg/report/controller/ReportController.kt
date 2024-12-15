package soma.achoom.zigg.report.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import soma.achoom.zigg.report.dto.ReportRequestDto
import soma.achoom.zigg.report.service.ReportService

@RestController
@RequestMapping("/api/v0/reports")
class ReportController(
    private val reportService: ReportService
) {
    @PostMapping("/posts/{postId}")
    fun postReport(authentication: Authentication, @PathVariable postId:Long, @RequestBody reportRequestDto: ReportRequestDto): ResponseEntity<Void>{
        reportService.reportPost(authentication,postId,reportRequestDto)
        return ResponseEntity.noContent().build()
    }
    @PostMapping("/comments/{commentId}")
    fun commentReport(authentication: Authentication, @PathVariable commentId:Long, @RequestBody reportRequestDto: ReportRequestDto): ResponseEntity<Void>{
        reportService.reportComment(authentication,commentId,reportRequestDto)
        return ResponseEntity.noContent().build()
    }
    @PostMapping("/users/{userId}")
    fun userReport(authentication: Authentication, @PathVariable userId:Long, @RequestBody reportRequestDto: ReportRequestDto): ResponseEntity<Void>{
        reportService.reportUser(authentication,userId,reportRequestDto)
        return ResponseEntity.noContent().build()
    }
}