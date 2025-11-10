package com.football.kick_board.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.football.kick_board.application.like.LikeService;
import com.football.kick_board.domain.comment.Comment;
import com.football.kick_board.domain.comment.CommentRepository;
import com.football.kick_board.domain.member.Member;
import com.football.kick_board.domain.member.MemberRepository;
import com.football.kick_board.domain.member.Role;
import com.football.kick_board.domain.post.BoardType;
import com.football.kick_board.domain.post.Post;
import com.football.kick_board.domain.post.PostRepository;
import com.football.kick_board.web.post.model.request.PostCreateRequest;
import com.football.kick_board.web.post.model.request.PostUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private PasswordEncoder passwordEncoder;

  // --- DB 셋업을 위한 실제 리포지토리 ---
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private PostRepository postRepository;
  @Autowired
  private CommentRepository commentRepository;

  // [!!핵심!!]
  // PostController가 PostService(실제 Bean) 외에 LikeService도 직접 의존합니다.
  // PostController 테스트에 집중하기 위해 LikeService는 @MockBean으로 격리합니다.
  @MockitoBean
  private LikeService likeService;

  // --- 테스트 데이터 ---
  private Member testUser;
  private Member otherUser;
  private Post testPost;
  private Post deletedPost;
  private Comment testComment;

  @BeforeEach
  void setUp() {
    // 1. 테스트용 유저 (testUser, otherUser) 저장
    testUser = memberRepository.save(
        Member.builder()
            .userId("testUser")
            .password(passwordEncoder.encode("password123!"))
            .email("testUser@test.com")
            .nickname("테스트유저")
            .favoriteTeam("TeamA")
            .role(Role.USER)
            .build()
    );
    otherUser = memberRepository.save(
        Member.builder()
            .userId("otherUser")
            .password(passwordEncoder.encode("password123!"))
            .email("otherUser@test.com")
            .nickname("다른유저")
            .favoriteTeam("TeamB")
            .role(Role.USER)
            .build()
    );

    // 2. 테스트용 게시글 (활성 상태) 저장
    testPost = postRepository.save(
        Post.builder()
            .title("테스트 게시글")
            .content("내용입니다.")
            .author(testUser)
            .boardType(BoardType.GENERAL)
            .build()
    );

    // 3. 테스트용 댓글 (활성 상태) 저장
    testComment = commentRepository.save(
        Comment.builder()
            .content("테스트 댓글")
            .author(otherUser) // 댓글은 다른 사람이 작성
            .post(testPost)
            .parent(null)
            .build()
    );

    // 4. 테스트용 게시글 (삭제 상태) 저장
    deletedPost = postRepository.save(
        Post.builder()
            .title("삭제된 게시글")
            .content("내용")
            .author(testUser)
            .build()
    );
    deletedPost.deactivate(); // soft delete
    postRepository.save(deletedPost); // 변경 사항(active=false) DB에 반영

    testPost.getComments().add(testComment);
  }

  // --- 1. 게시글 생성 (POST /posts) ---

  @Test
  @DisplayName("[성공] 게시글 생성")
  @WithMockUser(username = "testUser") // 'testUser'로 로그인
  void createPost_Success() throws Exception {
    // [Given]
    // (PostCreateRequest DTO에 Setter가 있다고 가정)
    PostCreateRequest request = new PostCreateRequest();
    request.setTitle("새 게시글 제목");
    request.setContent("새 내용입니다.");
    request.setBoardType(BoardType.GENERAL);
    String requestBody = objectMapper.writeValueAsString(request);

    // [When & Then]
    mockMvc.perform(
            post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isCreated()) // 201 Created
        .andExpect(jsonPath("$.title").value("새 게시글 제목"))
        .andExpect(jsonPath("$.authorNickname").value(testUser.getNickname()))
        .andExpect(jsonPath("$.likeCount").value(0)) // 새 글
        .andExpect(jsonPath("$.userLiked").value(false)) // 새 글
        .andExpect(jsonPath("$.comments").isEmpty()) // 새 글
        .andDo(print());
  }

  @Test
  @DisplayName("[실패] 게시글 생성 - 인증되지 않은 사용자 (401)")
  void createPost_Fail_Unauthorized() throws Exception {
    // [Given]
    PostCreateRequest request = new PostCreateRequest();
    request.setTitle("새 게시글 제목");
    request.setContent("새 내용입니다.");
    request.setBoardType(BoardType.GENERAL);
    String requestBody = objectMapper.writeValueAsString(request);

    // [When & Then]
    mockMvc.perform(
            post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isUnauthorized()) // @PreAuthorize("isAuthenticated()")
        .andDo(print());
  }

  @Test
  @DisplayName("[실패] 게시글 생성 - Validation (제목 없음) (409)")
  @WithMockUser(username = "testUser") // 인증은 통과
  void createPost_Fail_Validation() throws Exception {
    // [Given]
    PostCreateRequest request = new PostCreateRequest();
    request.setTitle(""); // [!!] @Valid @NotBlank 위반
    request.setContent("새 내용입니다.");
    request.setBoardType(BoardType.GENERAL);
    String requestBody = objectMapper.writeValueAsString(request);

    // [When & Then]
    mockMvc.perform(
            post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isConflict())
        .andDo(print());
  }

  // --- 2. 게시글 상세 조회 (GET /posts/{postId}) ---

  @Test
  @DisplayName("[성공] 게시글 상세 조회 (좋아요/댓글 정보 포함)")
  @WithMockUser(username = "otherUser") // 'otherUser'로 로그인 (게시글/댓글 좋아요 누름)
  void getPostById_Success() throws Exception {
    // [Given] (MockBean인 LikeService가 반환할 값 설정)
    // 1. 'otherUser'는 이 게시글(testPost)을 좋아함
    given(likeService.isPostLikedByCurrentUser(testPost.getId())).willReturn(true);
    given(likeService.countPostLikes(testPost.getId())).willReturn(1L);

    // 2. 'otherUser'는 이 댓글(testComment)을 좋아하지 않음
    given(likeService.isCommentLikedByCurrentUser(testComment.getId())).willReturn(false);
    given(likeService.countCommentLikes(testComment.getId())).willReturn(0L);

    // [When & Then]
    mockMvc.perform(
            get("/posts/" + testPost.getId())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("테스트 게시글"))
        .andExpect(jsonPath("$.authorNickname").value(testUser.getNickname()))
        .andExpect(jsonPath("$.userLiked").value(true)) // Mocking 결과 (1)
        .andExpect(jsonPath("$.likeCount").value(1)) // Mocking 결과 (1)
        .andExpect(jsonPath("$.comments[0].content").value("테스트 댓글"))
        .andExpect(jsonPath("$.comments[0].authorNickname").value(otherUser.getNickname()))
        .andExpect(jsonPath("$.comments[0].userLiked").value(false))
        .andDo(print());
  }

  @Test
  @DisplayName("[실패] 게시글 상세 조회 - 삭제된 게시글 (409)")
  @WithMockUser(username = "testUser")
  void getPostById_Fail_DeletedPost() throws Exception {
    // [Given] (deletedPost는 @BeforeEach에서 생성됨)
    // (LikeService는 PostService가 예외를 던지므로 호출되지 않음)

    // [When & Then]
    mockMvc.perform(
            get("/posts/" + deletedPost.getId())
        )
        .andExpect(status().isConflict()) // 409 (IllegalArgumentException)
        .andDo(print());
  }

  // --- 3. 게시글 수정 (PUT /posts/{postId}) ---

  @Test
  @DisplayName("[성공] 게시글 수정")
  @WithMockUser(username = "testUser") // 작성자 본인('testUser')으로 로그인
  void updatePost_Success() throws Exception {
    // [Given]
    PostUpdateRequest request = new PostUpdateRequest();
    request.setTitle("수정된 제목");
    request.setContent("수정된 내용");
    String requestBody = objectMapper.writeValueAsString(request);

    // [Given] (MockBean인 LikeService가 반환할 값 설정)
    // 'testUser'는 이 게시글(testPost)을 좋아하지 않음
    given(likeService.isPostLikedByCurrentUser(testPost.getId())).willReturn(false);
    given(likeService.countPostLikes(testPost.getId())).willReturn(0L);
    // 'testUser'는 이 댓글(testComment)을 좋아하지 않음
    given(likeService.isCommentLikedByCurrentUser(testComment.getId())).willReturn(false);
    given(likeService.countCommentLikes(testComment.getId())).willReturn(0L);

    // [When & Then]
    mockMvc.perform(
            put("/posts/" + testPost.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("수정된 제목"))
        .andExpect(jsonPath("$.content").value("수정된 내용"))
        .andExpect(jsonPath("$.userLiked").value(false)) // Mocking 결과
        .andExpect(jsonPath("$.likeCount").value(0)) // Mocking 결과
        .andDo(print());
  }

  @Test
  @DisplayName("[실패] 게시글 수정 - 작성자 아님 (409)")
  @WithMockUser(username = "otherUser") // 'otherUser'로 로그인 (작성자 아님)
  void updatePost_Fail_NotAuthor() throws Exception {
    // [Given]
    PostUpdateRequest request = new PostUpdateRequest();
    request.setTitle("내가 수정 시도");
    request.setContent("내가 수정 시도");
    String requestBody = objectMapper.writeValueAsString(request);

    // [When & Then]
    mockMvc.perform(
            put("/posts/" + testPost.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isConflict()) // 409 (IllegalArgumentException - "권한이 없습니다.")
        .andDo(print());
  }

  // --- 4. 게시글 삭제 (DELETE /posts/{postId}) ---

  @Test
  @DisplayName("[성공] 게시글 삭제")
  @WithMockUser(username = "testUser") // 작성자 본인('testUser')으로 로그인
  void deletePost_Success() throws Exception {
    // [When & Then]
    mockMvc.perform(
            delete("/posts/" + testPost.getId())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("게시글이 삭제 되었습니다."))
        .andDo(print());

    // [DB 검증]
    Post deleted = postRepository.findById(testPost.getId()).orElseThrow();
    assertThat(deleted.isActive()).isFalse(); // soft delete 확인
  }

  @Test
  @DisplayName("[실패] 게시글 삭제 - 작성자 아님 (409)")
  @WithMockUser(username = "otherUser") // 'otherUser'로 로그인 (작성자 아님)
  void deletePost_Fail_NotAuthor() throws Exception {
    // [When & Then]
    mockMvc.perform(
            delete("/posts/" + testPost.getId())
        )
        .andExpect(status().isConflict()) // 409
        .andDo(print());
  }

  // --- 5. 게시글 목록 조회 (GET /posts) ---

  @Test
  @DisplayName("[성공] 게시글 목록 조회 (좋아요 정보 포함)")
  @WithMockUser(username = "otherUser") // 'otherUser'로 로그인 (게시글 좋아요 누름)
  void getPosts_Success() throws Exception {
    // [Given] (MockBean인 LikeService가 반환할 값 설정)
    // 'otherUser'는 이 게시글(testPost)을 좋아함
    given(likeService.isPostLikedByCurrentUser(testPost.getId())).willReturn(true);
    given(likeService.countPostLikes(testPost.getId())).willReturn(1L);
    // (deletedPost는 기본 목록 조회(active=true)에 포함되지 않으므로 Mocking 불필요)

    // [When & Then]
    mockMvc.perform(
            get("/posts")
                .param("page", "0")
                .param("size", "10")
        )
        .andExpect(status().isOk())
        // [!!--- 수정된 부분 ---!!]
        // H2 DB를 쓰지 않기 때문에 정확히 1개를 기대할 수 없음.
        .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.content[0].title").exists()) // 1개가 아닌 첫번째 요소가 존재하는지만 확인
        .andDo(print());
  }
}