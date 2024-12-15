package soma.achoom.zigg.post.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import soma.achoom.zigg.history.dto.UploadContentTypeRequestDto
import soma.achoom.zigg.post.dto.PostRequestDto
import soma.achoom.zigg.post.dto.PostResponseDto
import soma.achoom.zigg.post.service.PostService
import soma.achoom.zigg.s3.entity.S3DataType
import soma.achoom.zigg.s3.service.S3Service
import java.util.*

@RestController
@RequestMapping("/api/v0/boards/posts")
class PostController(
    private val s3Service: S3Service,
    private val postService: PostService
) {
    @PostMapping("/pre-signed-url/{value}")
    fun getPreSignedUrl(
        @RequestBody uploadContentTypeRequestDto: UploadContentTypeRequestDto, @PathVariable value: String
    ): ResponseEntity<String> {
        if (value.trim() == "video") {
            val preSignedUrl = s3Service.getPreSignedPutUrl(S3DataType.POST_VIDEO, UUID.randomUUID(), uploadContentTypeRequestDto)
            return ResponseEntity.ok(preSignedUrl)
        }
        else if (value.trim() == "image") {
            val preSignedUrl = s3Service.getPreSignedPutUrl(S3DataType.POST_IMAGE, UUID.randomUUID(), uploadContentTypeRequestDto)
            return ResponseEntity.ok(preSignedUrl)
        }
        else if (value.trim() == "video_thumbnail") {
            val preSignedUrl = s3Service.getPreSignedPutUrl(S3DataType.POST_THUMBNAIL, UUID.randomUUID(), uploadContentTypeRequestDto)
            return ResponseEntity.ok(preSignedUrl)
        }
        else return ResponseEntity.badRequest().build()
    }
    @GetMapping("/random/{boardId}")
    fun getRandomPostsIncludeInBoard(authentication: Authentication, @PathVariable boardId: Long) : ResponseEntity<List<PostResponseDto>>{
        return ResponseEntity.ok(postService.getRandomPostsFromBoard(authentication,boardId))
    }

    @GetMapping("/{boardId}")
    fun getPosts(authentication: Authentication, @PathVariable boardId:Long,  @RequestParam("page") page: Int) : ResponseEntity<List<PostResponseDto>>{
        val posts = postService.getPosts(authentication, boardId, page)
        return ResponseEntity.ok(posts)
    }
    @PostMapping("/{boardId}")
    fun createPost(authentication: Authentication, @PathVariable boardId:Long, @RequestBody postRequestDto: PostRequestDto) : ResponseEntity<PostResponseDto>{
        val post = postService.createPost(authentication, boardId, postRequestDto)
        return ResponseEntity.ok(post)
    }
    @GetMapping("/{boardId}/{postId}")
    fun getPost(authentication : Authentication, @PathVariable boardId : Long, @PathVariable postId: Long) : ResponseEntity<PostResponseDto>{
        val post = postService.getPost(authentication,boardId,postId)
        return ResponseEntity.ok(post)
    }
    @PatchMapping("/{boardId}/{postId}")
    fun updatePost(authentication: Authentication, @PathVariable boardId : Long,@PathVariable postId: Long, @RequestBody postRequestDto: PostRequestDto) : ResponseEntity<PostResponseDto>{
        val post = postService.updatePost(authentication, postId, postRequestDto)
        return ResponseEntity.ok(post)
    }
    @DeleteMapping("/{boardId}/{postId}")
    fun deletePost(authentication: Authentication, @PathVariable boardId : Long,@PathVariable postId: Long) : ResponseEntity<Unit> {
        postService.deletePost(authentication, postId)
        return ResponseEntity.noContent().build()
    }
    @GetMapping("/search/{boardId}")
    fun searchPosts(authentication: Authentication,@PathVariable boardId:Long, @RequestParam("page") page: Int, @RequestParam("keyword") keyword: String) : ResponseEntity<List<PostResponseDto>>{
        val posts = postService.searchPosts(authentication, boardId, keyword, page)
        return ResponseEntity.ok(posts)
    }
    @GetMapping("/my")
    fun getMyPosts(authentication: Authentication) : ResponseEntity<List<PostResponseDto>>{
        val posts = postService.getMyPosts(authentication)
        return ResponseEntity.ok(posts)
    }
    @GetMapping("/likes/{boardId}/{postId}")
    fun likePost(authentication: Authentication, @PathVariable boardId : Long,@PathVariable postId: Long) : ResponseEntity<PostResponseDto>{
        val post = postService.likePost(authentication,boardId, postId)
        return ResponseEntity.ok(post)
    }
    @DeleteMapping("/likes/{boardId}/{postId}")
    fun unlikePost(authentication: Authentication, @PathVariable boardId : Long,@PathVariable postId: Long) : ResponseEntity<PostResponseDto>{
        val post = postService.unlikePost(authentication,boardId, postId)
        return ResponseEntity.ok(post)
    }
    @GetMapping("/scraps/{boardId}/{postId}")
    fun scrapPost(authentication: Authentication, @PathVariable boardId: Long, @PathVariable postId: Long) : ResponseEntity<PostResponseDto>{
        val posts = postService.scrapPost(authentication,boardId, postId)
        return ResponseEntity.ok(posts)
    }
    @DeleteMapping("/scraps/{boardId}/{postId}")
    fun unScrapPost(authentication: Authentication, @PathVariable boardId: Long, @PathVariable postId: Long) : ResponseEntity<PostResponseDto>{
        val posts = postService.unScrapPost(authentication,boardId, postId)
        return ResponseEntity.ok(posts)
    }
    @GetMapping("/scraps")
    fun getMyScraps(authentication: Authentication) : ResponseEntity<List<PostResponseDto>>{
        val posts = postService.getScrapedPosts(authentication)
        return ResponseEntity.ok(posts)
    }
    @GetMapping("/likes")
    fun getMyLikes(authentication: Authentication) : ResponseEntity<List<PostResponseDto>>{
        val posts = postService.getLikedPosts(authentication)
        return ResponseEntity.ok(posts)
    }
    @GetMapping("/commented")
    fun getMyCommentedPosts(authentication: Authentication) : ResponseEntity<List<PostResponseDto>>{
        val posts = postService.getCommentedPosts(authentication)
        return ResponseEntity.ok(posts)
    }
    @GetMapping("/hottest")
    fun getHottestPosts(authentication: Authentication) : ResponseEntity<List<PostResponseDto>>{
        val posts = postService.getPopularPosts(authentication)
        return ResponseEntity.ok(posts)
    }
}