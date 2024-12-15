package soma.achoom.zigg.space.dto

data class SpaceRequestDto(
    val spaceId: Long?,
    val spaceName: String,
    val spaceUsers: List<SpaceUserRequestDto> = emptyList(),
    val spaceImageUrl: String?,
)
