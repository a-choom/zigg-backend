package soma.achoom.zigg.v0.space.dto

import com.fasterxml.jackson.annotation.JsonInclude
import soma.achoom.zigg.v0.history.dto.HistoryResponseDto
import soma.achoom.zigg.v0.space.entity.Space
import soma.achoom.zigg.v0.space.entity.SpaceUser
import java.util.UUID
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SpaceResponseDto(
    val spaceId: UUID?,
    val spaceName: String?,
    val spaceImageUrl: String?,
    val spaceUsers: MutableSet<String>?,

    val history: MutableSet<HistoryResponseDto>? = null,

    ){

}