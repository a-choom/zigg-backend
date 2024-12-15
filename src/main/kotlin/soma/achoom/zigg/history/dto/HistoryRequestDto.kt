package soma.achoom.zigg.history.dto

import jakarta.validation.constraints.NotBlank


data class HistoryRequestDto(
    val historyName: String?,
    @NotBlank
    val historyVideoUrl: String,
    val historyThumbnailUrl:String,
    var videoDuration: String
)