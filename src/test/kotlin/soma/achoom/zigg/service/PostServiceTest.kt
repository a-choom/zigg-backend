package soma.achoom.zigg.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import soma.achoom.zigg.TestConfig.Companion.SPACE_IMAGE_URL
import soma.achoom.zigg.board.entity.Board
import soma.achoom.zigg.board.repository.BoardRepository
import soma.achoom.zigg.comment.dto.CommentRequestDto
import soma.achoom.zigg.comment.repository.CommentRepository
import soma.achoom.zigg.comment.service.CommentService
import soma.achoom.zigg.data.DummyDataUtil
import soma.achoom.zigg.post.dto.PostRequestDto
import soma.achoom.zigg.post.entity.Post
import soma.achoom.zigg.post.repository.PostRepository
import soma.achoom.zigg.post.service.PostService
import soma.achoom.zigg.s3.service.S3Service

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PostServiceTest {
    @Autowired
    private lateinit var commentRepository: CommentRepository

    @Autowired
    private lateinit var commentService: CommentService

    @Autowired
    private lateinit var postService: PostService

    @Autowired
    private lateinit var dummyDataUtil: DummyDataUtil

    @Autowired
    private lateinit var s3Service: S3Service

    @Autowired
    private lateinit var boardRepository: BoardRepository

    @Autowired
    private lateinit var postRepository: PostRepository

    private lateinit var board: Board

    private lateinit var post: Post

    @BeforeEach
    fun setup() {
        Mockito.`when`(s3Service.getPreSignedGetUrl(anyString())).thenReturn(SPACE_IMAGE_URL)


        val user = dummyDataUtil.createDummyUser()
        board = Board(name = "test board")
        boardRepository.save(board)

        post = Post(board = board, title = "test post", textContent = "test content", creator = user, anonymous = false)
        postRepository.save(post)
    }

    @Test
    fun `create post`() {
        val user = dummyDataUtil.createDummyUser()
        val auth = dummyDataUtil.createDummyAuthentication(user)
        val postResponse = postService.createPost(
            auth, board.boardId!!, PostRequestDto(
                postTitle = "test post",
                postMessage = "test content",
            )
        )
        assert(postResponse.postTitle == "test post")
    }
    @Test
    fun `update post`() {
        val user = dummyDataUtil.createDummyUser()
        val auth = dummyDataUtil.createDummyAuthentication(user)
        val newPost = Post(board = board, title = "test post", textContent = "test content", creator = user, anonymous = false)
        postRepository.save(newPost)
        val postResponse = postService.updatePost(
            auth, newPost.postId!!, PostRequestDto(
                postTitle = "update post",
                postMessage = "update content",
            )
        )
        assert(postResponse.postTitle == "update post")
        assert(postResponse.postMessage == "update content")
    }
    @Test
    fun `delete post`(){
        val user = dummyDataUtil.createDummyUser()
        val auth = dummyDataUtil.createDummyAuthentication(user)
        val newPost = Post(board = board, title = "test post", textContent = "test content", creator = user, anonymous = false)
        postRepository.save(newPost)
        postService.deletePost(auth, newPost.postId!!)
        assert(postRepository.findById(newPost.postId!!).isEmpty)
    }
    @Test
    fun `like unlike post`(){
        val user = dummyDataUtil.createDummyUser()
        val auth = dummyDataUtil.createDummyAuthentication(user)
        postService.likePost(auth, board.boardId!!,post.postId!!)

        postService.unlikePost(auth, board.boardId!! ,post.postId!!)
    }
    @Test
    fun `scrap unscrap post`() {
        val user = dummyDataUtil.createDummyUser()
        val auth = dummyDataUtil.createDummyAuthentication(user)
        postService.scrapPost(auth,board.boardId!!, post.postId!!)

        postService.unScrapPost(auth, board.boardId!!,post.postId!!)
    }
    @Test
    fun `get post`(){
        val user = dummyDataUtil.createDummyUser()
        val auth = dummyDataUtil.createDummyAuthentication(user)
        val postResponse = postService.getPost(auth, board.boardId!!, post.postId!!)
        assert(postResponse.postId == post.postId)
    }
    @Test
    fun `get scraps`(){
        val user = dummyDataUtil.createDummyUser()
        val auth = dummyDataUtil.createDummyAuthentication(user)
        postService.scrapPost(auth,board.boardId!!, post.postId!!)
        val scraps = postService.getScrapedPosts(auth)
        assert(scraps[0].postId == post.postId)
    }
    @Test
    fun `get my posts`(){
        val user = dummyDataUtil.createDummyUser()
        val auth = dummyDataUtil.createDummyAuthentication(user)
        val postResponse = postService.createPost(
            auth, board.boardId!!, PostRequestDto(
                postTitle = "test post",
                postMessage = "test content",
            )
        )
        val posts = postService.getMyPosts(auth)
        assert(posts[0].postId == postResponse.postId)
    }
    @Test
    fun `get my commented posts`(){
        val user = dummyDataUtil.createDummyUser()
        val auth = dummyDataUtil.createDummyAuthentication(user)
        val posts = postService.getCommentedPosts(auth)
        assert(posts.isEmpty())

        commentService.createComment(auth, board.boardId!!, post.postId!!, CommentRequestDto("test comment"))
        val commentedPosts = postService.getCommentedPosts(auth)
        assert(commentedPosts[0].postId == post.postId)
    }
    @Test
    fun `delete post with comments`(){
        val user = dummyDataUtil.createDummyUser()
        val auth = dummyDataUtil.createDummyAuthentication(user)
        val newPost = postService.createPost(
            auth, board.boardId!!, PostRequestDto(
                postTitle = "test post",
                postMessage = "test content",
            )
        )
        commentService.createComment(auth, board.boardId!!, newPost.postId, CommentRequestDto("test comment"))
        assert(commentRepository.findAll().size == 1)
    }
    @Test
    fun `create anonymous post`(){
        val user = dummyDataUtil.createDummyUser()
        val auth = dummyDataUtil.createDummyAuthentication(user)
        val postResponse = postService.createPost(
            auth, board.boardId!!, PostRequestDto(
                postTitle = "test post",
                postMessage = "test content",
                anonymous = true
            )
        )
        assert(postResponse.postTitle == "test post")
        println(postResponse.postCreator.userName)
    }
    @Test
    fun `create post with comments`(){
        val user = dummyDataUtil.createDummyUser()
        val auth = dummyDataUtil.createDummyAuthentication(user)
        val postResponse = postService.createPost(
            auth, board.boardId!!, PostRequestDto(
                postTitle = "test post",
                postMessage = "test content",
            )
        )
        val commentResponse = commentService.createComment(auth, board.boardId!!, postResponse.postId, CommentRequestDto("test comment"))
//        println(commentResponse.commentCreator.userName)
//        assert(commentResponse.commentCreator.userName == "글쓴이(익명)")
        val commenter1 = dummyDataUtil.createDummyUser()
        val commenter1Auth = dummyDataUtil.createDummyAuthentication(commenter1)

        val commenter2 = dummyDataUtil.createDummyUser()
        val commenter2Auth = dummyDataUtil.createDummyAuthentication(commenter2)

        commentService.createComment(commenter1Auth, board.boardId!!, postResponse.postId, CommentRequestDto("test comment"))
        commentService.createComment(commenter2Auth, board.boardId!!, postResponse.postId, CommentRequestDto("test comment"))
//        commentService.createChildComment(commenter1Auth, board.boardId!!, postResponse.postId, commentResponse.commentId!!, CommentRequestDto("test comment"))
//        commentService.createChildComment(commenter2Auth, board.boardId!!, postResponse.postId, commentResponse.commentId!!, CommentRequestDto("test comment"))
//
//        val postWithComments = postService.getPost(auth, board.boardId!!, postResponse.postId)
//        assert(postWithComments.comments?.size == 3)
    }
    @Test
    fun `post 200 line text`(){
        val user = dummyDataUtil.createDummyUser()
        val auth = dummyDataUtil.createDummyAuthentication(user)
        val postResponse = postService.createPost(
            auth, board.boardId!!, PostRequestDto(
                postTitle = "test post",
                postMessage = "* 테스터에서 웹뷰말고 Link로\n" +
                        "* 히스토리에서 신기환폰 튕김\n" +
                        "* 영상 업로드중에 나가면 경고 및 취소\n" +
                        "* 커뮤니티 비디오플레이어 회전 안됨\n" +
                        "* 커뮤니티에서 북마크, 좋아요누르면 영상 다시 로드됨\n" +
                        "* 커뮤니티에서 모달창 위치 이상"
            )
        )
    }
}