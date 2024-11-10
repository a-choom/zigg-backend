package soma.achoom.zigg.user.dto

import java.time.LocalDateTime

data class UserResponseDto(
    val userId: Long? = null,
    val userName: String?,
    val userNickname: String?,
    val profileImageUrl: String?,
    val profileBannerImageUrl:String? = null,
    val userTags : String? = null,
    val userDescription: String? = null,
    val createdAt:LocalDateTime? = null,

)  {

    override fun toString(): String {
        return "UserResponseDto(\n" +
                "userId=$userId,\n" +
                "userName=$userName,\n" +
                "userNickname=$userNickname,\n" +
                "profileImageUrl=$profileImageUrl\n" +
                ")\n"
    }
}