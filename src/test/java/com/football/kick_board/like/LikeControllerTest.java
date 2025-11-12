package com.football.kick_board.like;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.football.kick_board.domain.comment.Comment;
import com.football.kick_board.domain.comment.CommentRepository;
import com.football.kick_board.domain.like.Like;
import com.football.kick_board.domain.like.LikeRepository;
import com.football.kick_board.domain.member.Member;
import com.football.kick_board.domain.member.MemberRepository;
import com.football.kick_board.domain.member.Role;
import com.football.kick_board.domain.post.Post;
import com.football.kick_board.domain.post.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional // H2 DB 롤백
@TestPropertySource(properties = { // H2 DB 강제
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@SpringBootTest
@AutoConfigureMockMvc
class LikeControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired // DB 셋업용
  private MemberRepository memberRepository;
  @Autowired
  private PostRepository postRepository;
  @Autowired
  private CommentRepository commentRepository;
  @Autowired
  private LikeRepository likeRepository; // '좋아요 취소' 테스트용

  private Member testUser;
  private Post testPost;
  private Comment testComment;

  @BeforeEach
  void setUp() {
    // 1. 테스트 유저 저장
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

    // 2. 테스트 게시글 저장
    testPost = postRepository.save(
        Post.builder().title("테스트 글").content("테스트 내용").author(testUser).build()
    );

    // 3. 테스트 댓글 저장
    testComment = commentRepository.save(
        Comment.builder().content("테스트 댓글").author(testUser).post(testPost).build()
    );

    // 4. (참고) testPost의 댓글 리스트에 수동 추가 (JPA 1차 캐시 문제 방지)
    testPost.getComments().add(testComment);
  }

  // --- 1. 게시글 좋아요 (POST /likes/posts/{postId}) ---

  @Test
  @DisplayName("[성공] 게시글 좋아요 - 추가")
  @WithMockUser(username = "testUser") // 'testUser'로 로그인
  void togglePostLike_Success_Add() throws Exception {
    // [Given] - (좋아요가 없는 상태)

    // [When & Then]
    mockMvc.perform(
            post("/likes/posts/" + testPost.getId())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.likeCount").value(1))
        .andExpect(jsonPath("$.userLiked").value(true))
        .andDo(print());
  }

  @Test
  @DisplayName("[성공] 게시글 좋아요 - 취소")
  @WithMockUser(username = "testUser") // 'testUser'로 로그인
  void togglePostLike_Success_Remove() throws Exception {
    // [Given] - (미리 좋아요를 눌러둠)
    likeRepository.save(Like.ofPost(testUser, testPost));

    // [When & Then]
    mockMvc.perform(
            post("/likes/posts/" + testPost.getId())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.likeCount").value(0))
        .andExpect(jsonPath("$.userLiked").value(false))
        .andDo(print());
  }

  @Test
  @DisplayName("[실패] 게시글 좋아요 - 인증되지 않은 사용자 (401)")
  void togglePostLike_Fail_Unauthorized() throws Exception {
    // [Given] - (@WithMockUser 없음)
    // [When & Then]
    mockMvc.perform(
            post("/likes/posts/" + testPost.getId())
        )
        .andExpect(status().isUnauthorized()) // @PreAuthorize("isAuthenticated()")
        .andDo(print());
  }

  // --- 2. 댓글 좋아요 (POST /likes/comments/{commentId}) ---

  @Test
  @DisplayName("[성공] 댓글 좋아요 - 추가")
  @WithMockUser(username = "testUser")
  void toggleCommentLike_Success_Add() throws Exception {
    // [Given] - (좋아요 없는 상태)

    // [When & Then]
    mockMvc.perform(
            post("/likes/comments/" + testComment.getId())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.likeCount").value(1))
        .andExpect(jsonPath("$.userLiked").value(true))
        .andDo(print());
  }

  // --- 3. 좋아요 상태 조회 (GET /likes/...) ---

  @Test
  @DisplayName("[성공] 게시글 좋아요 상태 조회 (비로그인)")
  void getPostLikeStatus_Success_NoAuth() throws Exception {
    // [Given] - (다른 유저가 좋아요를 1개 눌러둠)
    Member otherUser = memberRepository.save(
        Member.builder().userId("otherUser").password("123").email("o@o.com").nickname("o").favoriteTeam("o").role(Role.USER).build()
    );
    likeRepository.save(Like.ofPost(otherUser, testPost));

    // [When & Then]
    mockMvc.perform(
            get("/likes/posts/" + testPost.getId())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.likeCount").value(1))
        .andExpect(jsonPath("$.userLiked").value(false)) // (비로그인 상태이므로 false)
        .andDo(print());
  }
}
