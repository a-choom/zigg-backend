package soma.achoom.zigg.content.entity

import jakarta.persistence.*
import org.jetbrains.annotations.TestOnly
import soma.achoom.zigg.global.BaseEntity
import soma.achoom.zigg.global.util.S3UrlParser
import soma.achoom.zigg.user.entity.User

@Entity(name = "video")
class Video private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val videoId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "uploader")
    var uploader: User?,
    @Column(name = "video_key")
    val videoKey: String,
    @Column(name = "duration")
    val duration: String,

    ) : BaseEntity() {

    @TestOnly
    constructor(uploader: User?, videoKey: String, duration: String) : this(
        videoId = null,
        uploader = uploader,
        videoKey = videoKey,
        duration = duration
    )
    companion object {
        fun fromUrl(videoUrl: String, uploader: User, duration: String): Video {
            val videoKey = S3UrlParser.extractionKeyFromUrl(videoUrl)
            return Video(uploader = uploader, videoKey = videoKey, duration = duration)
        }
        fun fromKey(videoKey : String,uploader: User ,duration: String) : Video{
            return Video(uploader = uploader, videoKey = videoKey, duration = duration)
        }
    }
}