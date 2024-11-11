package soma.achoom.zigg.invite.dto

import soma.achoom.zigg.space.dto.SpaceUserRequestDto

data class InviteRequestDto(
    val spaceUsers : List<SpaceUserRequestDto>
)
