package soma.achoom.zigg.auth.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import soma.achoom.zigg.auth.dto.NicknameValidResponseDto

import soma.achoom.zigg.auth.dto.OAuth2MetaDataRequestDto
import soma.achoom.zigg.auth.dto.OAuth2UserRequestDto
import soma.achoom.zigg.auth.dto.OAuthProviderEnum
import soma.achoom.zigg.auth.filter.JwtTokenProvider
import soma.achoom.zigg.user.dto.UserExistsResponseDto
import soma.achoom.zigg.user.entity.User
import soma.achoom.zigg.user.exception.UserAlreadyExistsException
import soma.achoom.zigg.user.repository.UserRepository

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Service
class AuthenticationService @Autowired constructor(
    private var jwtTokenProvider: JwtTokenProvider,
    @Value("\${user.default.profile.images}")
    private val defaultProfileImages: List<String>,
    private val userRepository: UserRepository
)  {

    fun userExistsCheckByOAuthPlatformAndProviderId(oAuth2MetaDataRequestDto: OAuth2MetaDataRequestDto): UserExistsResponseDto {

        runCatching {
            userRepository.findUserByPlatformAndProviderId(
                OAuthProviderEnum.valueOf(oAuth2MetaDataRequestDto.platform), oAuth2MetaDataRequestDto.providerId
            ) ?: return UserExistsResponseDto(false)
        }.onFailure {
            println(it.message)
            return UserExistsResponseDto(false)
        }
        return UserExistsResponseDto(true)
    }

    fun checkNickname(nickname:String): NicknameValidResponseDto {
        return NicknameValidResponseDto(userRepository.existsUserByUserNickname(nickname))
    }

    fun generateJWTToken(oAuth2UserRequestDto: OAuth2UserRequestDto): HttpHeaders {
        val user = userRepository.findUserByPlatformAndProviderId(
            OAuthProviderEnum.valueOf(oAuth2UserRequestDto.platform), oAuth2UserRequestDto.providerId
        ) ?: throw IllegalArgumentException("register first")
        val accessToken = jwtTokenProvider.createTokenWithUserInfo(user, oAuth2UserRequestDto.userInfo)
        val header = HttpHeaders()
        header.set("Authorization", accessToken)
        header.set("platform", oAuth2UserRequestDto.platform)
        user.jwtToken = accessToken

            userRepository.save(user)

        return header
    }

    fun registers(oAuth2UserRequestDto: OAuth2UserRequestDto): HttpHeaders {
        oAuth2UserRequestDto.userNickname?: throw IllegalArgumentException("userNickname is required")
        userRepository.findUserByUserNickname(oAuth2UserRequestDto.userNickname)?.let {
            throw UserAlreadyExistsException()
        }
        when (oAuth2UserRequestDto.platform) {
            OAuthProviderEnum.GOOGLE.name -> {
                val user = saveOrUpdate(oAuth2UserRequestDto)

                if (verifyGoogleToken(oAuth2UserRequestDto.idToken)) {
                    val accessToken = jwtTokenProvider.createTokenWithUserInfo(user, oAuth2UserRequestDto.userInfo)
                    val header = HttpHeaders()
                    header.set("Authorization", accessToken)
                    header.set("platform", oAuth2UserRequestDto.platform)
                    user.jwtToken = accessToken

                    userRepository.save(user)


                    return header
                } else {
                    throw IllegalArgumentException("Invalid access token")
                }
            }

            OAuthProviderEnum.KAKAO.name -> {
                if (verifyKakaoToken(oAuth2UserRequestDto.idToken)) {
                    val user = saveOrUpdate(oAuth2UserRequestDto)

                    val accessToken = jwtTokenProvider.createTokenWithUserInfo(user, oAuth2UserRequestDto.userInfo)
                    val header = HttpHeaders()
                    header.set("Authorization", accessToken)
                    header.set("platform", oAuth2UserRequestDto.platform)
                    user.jwtToken = accessToken
                    userRepository.save(user)
                    return header
                } else {
                    throw IllegalArgumentException("Invalid access token")
                }
            }

            OAuthProviderEnum.APPLE.name -> {
                val user = saveOrUpdate(oAuth2UserRequestDto)

                val accessToken = jwtTokenProvider.createTokenWithUserInfo(user, oAuth2UserRequestDto.userInfo)
                val header = HttpHeaders()
                header.set("Authorization", accessToken)
                header.set("platform", oAuth2UserRequestDto.platform)
                user.jwtToken = accessToken

                    userRepository.save(user)

                return header
            }

            else -> {
                throw IllegalArgumentException("Unsupported platform")
            }
        }

    }

    private fun verifyGoogleToken(idToken: String): Boolean {
        val client =
            HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(java.time.Duration.ofSeconds(20)).build()

        val request =
            HttpRequest.newBuilder().uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=$idToken"))
                .timeout(java.time.Duration.ofSeconds(20)).GET().build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.statusCode() == 200
    }

    private fun verifyKakaoToken(idToken: String): Boolean {
        val client = HttpClient.newHttpClient()
        val tokenData = "id_token=$idToken"
        val request = HttpRequest.newBuilder().uri(URI.create("https://kauth.kakao.com/oauth/tokeninfo"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(tokenData)).build()


        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.statusCode() == 200
    }


    private fun saveOrUpdate(oAuth2UserRequestDto: OAuth2UserRequestDto): User {
        val user: User = userRepository.findUserByPlatformAndProviderId(
            OAuthProviderEnum.valueOf(oAuth2UserRequestDto.platform), oAuth2UserRequestDto.providerId
        ) ?: User(
            userNickname = oAuth2UserRequestDto.userNickname,
            userName = oAuth2UserRequestDto.userName,
            providerId = oAuth2UserRequestDto.providerId,
            platform = OAuthProviderEnum.valueOf(oAuth2UserRequestDto.platform),
            jwtToken = "",
            profileImageKey = defaultProfileImages.random()
        )

        return userRepository.save(user)

    }

}