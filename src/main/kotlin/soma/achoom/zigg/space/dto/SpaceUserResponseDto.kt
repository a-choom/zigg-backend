package soma.achoom.zigg.space.dto

import soma.achoom.zigg.space.entity.SpaceRole

data class SpaceUserResponseDto(
    val userId: Long? = null,
    val userNickname: String?,
    val userName:String?,
    val spaceUserId: Long?,
    val spaceRole: SpaceRole?,
    val profileImageUrl: String?
){
    override fun toString(): String {
        return "SpaceUserResponseDto(\n" +
                "userNickname=$userNickname,\n" +
                "userName=$userName,\n" +
                "spaceUserId=$spaceUserId,\n" +
                "spaceRole=$spaceRole,\n" +
                "profileImageUrl=$profileImageUrl\n" +
                ")\n"
    }
}
