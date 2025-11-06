package com.football.kick_board.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.football.kick_board.domain.comment.Comment;
import com.football.kick_board.domain.comment.CommentRepository;
import com.football.kick_board.domain.member.Member;
import com.football.kick_board.domain.member.MemberRepository;
import com.football.kick_board.domain.member.Role; // [!!] Role 임포트
import com.football.kick_board.domain.post.Post;
import com.football.kick_board.domain.post.PostRepository;
import com.football.kick_board.web.comment.model.request.CommentCreateRequest;
import com.football.kick_board.web.comment.model.request.CommentUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private PostRepository postRepository;
  @Autowired
  private CommentRepository commentRepository;

  private Member testUser;
  private Member otherUser;
  private Post testPost;
  private Comment testComment;

  @BeforeEach
  void setUp() {
    // [!!--- 수정된 부분 시작 ---!!]
    // Member 엔티티의 모든 nullable=false 필드를 채워줍니다.

    // 1. 테스트용 유저 1 (댓글/게시글 작성자)
    testUser = memberRepository.save(
        Member.builder()
            .userId("testUser")
            .password("password123") // (실제 암호화가 필요하다면 PasswordEncoder Bean을 주입받아야 하나, 여기서는 텍스트로 저장)
            .email("testUser@test.com")
            .nickname("테스트유저")
            .favoriteTeam("Manchester United") // [!!] 누락되었던 필수 필드
            .role(Role.USER) // [!!] 누락되었던 필수 필드
            .active(true)
            .build()
    );

    // 2. 테스트용 유저 2 (타인)
    otherUser = memberRepository.save(
        Member.builder()
            .userId("otherUser")
            .password("password123")
            .email("otherUser@test.com")
            .nickname("다른유저")
            .favoriteTeam("Liverpool") // [!!] 누락되었던 필수 필드
            .role(Role.USER) // [!!] 누락되었던 필수 필드
            .active(true)
            .build()
    );

    // 3. 테스트용 게시글 1개 (작성자: testUser)
    // Post 엔티티의 빌더는 title, content, author만 있어도 BoardType.GENERAL로 잘 생성됩니다.
    testPost = postRepository.save(
        Post.builder()
            .title("테스트 게시글")
            .content("내용입니다.")
            .author(testUser)
            .build()
    );
    // [!!--- 수정된 부분 끝 ---!!]

    // 4. 테스트용 댓글 1개 (작성자: testUser)
    testComment = commentRepository.save(
        Comment.builder()
            .content("원본 댓글")
            .author(testUser)
            .post(testPost)
            .parent(null)
            .build()
    );
  }


  // --- 4. 댓글 생성 (POST /comments) 테스트 ---

  @Test
  @DisplayName("[성공] 댓글 생성")
  @WithMockUser(username = "testUser") // 'testUser'로 로그인
  void createComment_Success() throws Exception {
    // [Given]
    CommentCreateRequest request = new CommentCreateRequest("새 댓글 내용", testPost.getId(), null);
    String requestBody = objectMapper.writeValueAsString(request);

    // [When & Then]
    mockMvc.perform(
            post("/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isOk()) // 200 OK
        .andExpect(jsonPath("$.content").value("새 댓글 내용"))
        .andExpect(jsonPath("$.authorNickname").value(testUser.getNickname()))
        .andDo(print());
  }

  @Test
  @DisplayName("[실패] 댓글 생성 - 인증되지 않은 사용자")
  void createComment_Fail_Unauthorized() throws Exception {
    // [Given]
    CommentCreateRequest request = new CommentCreateRequest("내용", testPost.getId(), null);
    String requestBody = objectMapper.writeValueAsString(request);

    // [When & Then]
    // @WithMockUser가 없으므로 '비로그인' 상태로 요청
    mockMvc.perform(
            post("/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isUnauthorized()) // 401 Unauthorized
        .andDo(print());
  }

  @Test
  @DisplayName("[실패] 댓글 생성 - Validation (내용 없음)")
  @WithMockUser(username = "testUser") // 인증은 통과
  void createComment_Fail_Validation() throws Exception {
    // [Given]
    // (CommentCreateRequest의 'content' 필드에 @Valid를 위한 @NotBlank 등이 있다고 가정)
    CommentCreateRequest request = new CommentCreateRequest("", testPost.getId(), null); // 내용 비움
    String requestBody = objectMapper.writeValueAsString(request);

    // [When & Then]
    mockMvc.perform(
            post("/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isBadRequest()) // @Valid가 400 Bad Request 반환
        .andDo(print());
  }

  // --- 5. 댓글 목록 조회 (GET /comments/posts/{postId}) 테스트 ---

  @Test
  @DisplayName("[성공] 게시글 댓글 목록 조회")
  @WithMockUser(username = "testUser")
  void getCommentsByPostId_Success() throws Exception {
    // [Given] - @BeforeEach에서 testComment가 이미 생성됨

    // [When & Then]
    mockMvc.perform(
            get("/comments/posts/" + testPost.getId())
                .param("page", "0")
                .param("size", "10")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].content").value(testComment.getContent()))
        .andExpect(jsonPath("$.content[0].authorNickname").value(testUser.getNickname()))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andDo(print());
  }

  // --- 6. 댓글 수정 (PUT /comments/{commentId}) 테스트 ---

  @Test
  @DisplayName("[성공] 댓글 수정")
  @WithMockUser(username = "testUser") // 작성자 본인('testUser')으로 로그인
  void updateComment_Success() throws Exception {
    // [Given]
    CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글 내용");
    String requestBody = objectMapper.writeValueAsString(request);

    // [When & Then]
    mockMvc.perform(
            put("/comments/" + testComment.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("수정된 댓글 내용"))
        .andDo(print());
  }

  @Test
  @DisplayName("[실패] 댓글 수정 - 작성자 아님 (권한 없음)")
  @WithMockUser(username = "otherUser") // 'otherUser'로 로그인 (작성자 아님)
  void updateComment_Fail_NotAuthor() throws Exception {
    // [Given]
    CommentUpdateRequest request = new CommentUpdateRequest("내가 수정 시도");
    String requestBody = objectMapper.writeValueAsString(request);

    // [When & Then]
    mockMvc.perform(
            put("/comments/" + testComment.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        // (Service에서 'IllegalArgumentException' 발생 시 400으로 처리한다고 가정)
        .andExpect(status().isConflict())
        .andDo(print());
  }

  // --- 7. 댓글 삭제 (DELETE /comments/{commentId}) 테스트 ---

  @Test
  @DisplayName("[성공] 댓글 삭제")
  @WithMockUser(username = "testUser") // 작성자 본인('testUser')으로 로그인
  void deleteComment_Success() throws Exception {
    // [When & Then]
    mockMvc.perform(
            delete("/comments/" + testComment.getId())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("댓글이 삭제되었습니다."))
        .andDo(print());

    // [DB 검증]
    Comment deletedComment = commentRepository.findById(testComment.getId())
        .orElseThrow(() -> new AssertionError("댓글이 DB에서 조회되지 않음"));

    assertThat(deletedComment.isActive()).isFalse(); // 'active'가 false가 되었는지 검증
  }

  @Test
  @DisplayName("[실패] 댓글 삭제 - 작성자 아님 (권한 없음)")
  @WithMockUser(username = "otherUser") // 'otherUser'로 로그인
  void deleteComment_Fail_NotAuthor() throws Exception {
    // [When & Then]
    mockMvc.perform(
            delete("/comments/" + testComment.getId())
        )
        .andExpect(status().isConflict()) // (400으로 가정)
        .andDo(print());
  }
}