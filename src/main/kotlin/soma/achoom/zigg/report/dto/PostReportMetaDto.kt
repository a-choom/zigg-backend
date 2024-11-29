package soma.achoom.zigg.report.dto

data class PostReportMetaDto(
    val postId:Long,
    val userId:Long,
    val contents: List<Long>,
    val textContent : String
) {
}