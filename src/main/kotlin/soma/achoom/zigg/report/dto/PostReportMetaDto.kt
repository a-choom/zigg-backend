package soma.achoom.zigg.report.dto

data class PostReportMetaDto(
    val postId:Long,
    val userId:Long,
    val imageContent: List<Long>,
    val videoContent : Long?,
    val textContent : String
) {
}