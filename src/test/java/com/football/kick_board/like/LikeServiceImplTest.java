package com.football.kick_board.like;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.football.kick_board.application.like.LikeServiceImpl;
import com.football.kick_board.common.security.SecurityUtils;
import com.football.kick_board.domain.comment.Comment;
import com.football.kick_board.domain.comment.CommentRepository;
import com.football.kick_board.domain.like.Like;
import com.football.kick_board.domain.like.LikeRepository;
import com.football.kick_board.domain.member.Member;
import com.football.kick_board.domain.member.MemberRepository;
import com.football.kick_board.domain.post.Post;
import com.football.kick_board.domain.post.PostRepository;
import com.football.kick_board.web.like.model.LikeStatusResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LikeServiceImplTest {

  @InjectMocks
  private LikeServiceImpl likeService;

  @Mock
  private LikeRepository likeRepository;
  @Mock
  private MemberRepository memberRepository;
  @Mock
  private PostRepository postRepository;
  @Mock
  private CommentRepository commentRepository;

  // --- 테스트용 '실제' 객체들 ---
  private Member mockMember;
  private Post mockPost;
  private Comment mockComment;
  private String currentUserId = "testUser";

  @BeforeEach
  void setUp() {
    // [!!] Member/Post/Comment 엔티티의 실제 빌더에 맞게 필수 필드를 채워주세요.

    // 1. 테스트용 Member 객체
    mockMember = Member.builder()
        .userId(currentUserId)
        .nickname("테스트유저")
        .password("password123")
        .email("test@test.com")
        .favoriteTeam("TeamA")
        .build();
    ReflectionTestUtils.setField(mockMember, "id", 1L);

    // 2. 테스트용 Post 객체 (active=true)
    mockPost = Post.builder()
        .title("테스트 게시글")
        .content("내용")
        .author(mockMember)
        .build();
    ReflectionTestUtils.setField(mockPost, "id", 1L);
    ReflectionTestUtils.setField(mockPost, "active", true); // [중요]

    // 3. 테스트용 Comment 객체 (active=true, mockPost에 속함)
    mockComment = Comment.builder()
        .content("테스트 댓글")
        .author(mockMember)
        .post(mockPost) // [중요]
        .parent(null)
        .build();
    ReflectionTestUtils.setField(mockComment, "id", 1L);
    ReflectionTestUtils.setField(mockComment, "active", true); // [중요]
  }

  // --- 1. 게시글 좋아요 토글 (togglePostLike) 테스트 ---

  @Test
  @DisplayName("게시글 좋아요 토글 - 성공 (좋아요 추가)")
  void togglePostLike_Success_AddLike() {
    // [Given] (준비)
    Long postId = mockPost.getId();

    // 1. [핵심] SecurityUtils Mocking
    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);

      // 2. Repository Mocking
      given(memberRepository.findByUserId(currentUserId)).willReturn(Optional.of(mockMember));
      given(postRepository.findById(postId)).willReturn(Optional.of(mockPost));

      // 3. [핵심] '좋아요'가 없는 상태(empty)를 반환
      given(likeRepository.findByMemberAndPost(mockMember, mockPost)).willReturn(Optional.empty());

      // 4. '좋아요' 추가 후, 카운트는 1이 될 것임
      given(likeRepository.countByPostAndPostActiveTrue(mockPost)).willReturn(1L);

      // [When] (실행)
      LikeStatusResponse response = likeService.togglePostLike(postId);

      // [Then] (검증)
      // 1. 'save'가 호출되었는지 검증 (좋아요 추가)
      verify(likeRepository).save(any(Like.class));
      // 2. 'delete'는 호출되지 않았는지 검증
      verify(likeRepository, never()).delete(any());

      // 3. 응답 DTO 검증
      assertThat(response.isUserLiked()).isTrue();
      assertThat(response.getLikeCount()).isEqualTo(1L);
    }
  }

  @Test
  @DisplayName("게시글 좋아요 토글 - 성공 (좋아요 취소)")
  void togglePostLike_Success_RemoveLike() {
    // [Given]
    Long postId = mockPost.getId();
    Like existingLike = Like.ofPost(mockMember, mockPost); // '실제' 좋아요 객체

    // 1. SecurityUtils Mocking
    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
      given(memberRepository.findByUserId(currentUserId)).willReturn(Optional.of(mockMember));
      given(postRepository.findById(postId)).willReturn(Optional.of(mockPost));

      // 2. [핵심] '좋아요'가 있는 상태(existingLike)를 반환
      given(likeRepository.findByMemberAndPost(mockMember, mockPost)).willReturn(Optional.of(existingLike));

      // 3. '좋아요' 삭제 후, 카운트는 0이 될 것임
      given(likeRepository.countByPostAndPostActiveTrue(mockPost)).willReturn(0L);

      // [When]
      LikeStatusResponse response = likeService.togglePostLike(postId);

      // [Then]
      // 1. 'delete'가 호출되었는지 검증 (좋아요 취소)
      verify(likeRepository).delete(existingLike);
      // 2. 'save'는 호출되지 않았는지 검증
      verify(likeRepository, never()).save(any());

      // 3. 응답 DTO 검증
      assertThat(response.isUserLiked()).isFalse();
      assertThat(response.getLikeCount()).isEqualTo(0L);
    }
  }

  @Test
  @DisplayName("게시글 좋아요 토글 - 실패 (비활성 게시글)")
  void togglePostLike_Fail_InactivePost() {
    // [Given]
    Long postId = mockPost.getId();
    mockPost.deactivate(); // Post를 '비활성' 상태로 변경

    // 1. SecurityUtils Mocking
    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
      given(memberRepository.findByUserId(currentUserId)).willReturn(Optional.of(mockMember));

      // 2. [핵심] '비활성' 상태의 Post 반환
      given(postRepository.findById(postId)).willReturn(Optional.of(mockPost));

      // [When & Then] (실행 및 검증)
      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        likeService.togglePostLike(postId);
      });

      // 3. 예외 메시지 검증
      assertThat(exception.getMessage()).isEqualTo("비활성 상태의 게시글에는 좋아요를 할 수 없습니다.");
    }
  }

  // --- 2. 댓글 좋아요 토글 (toggleCommentLike) 테스트 ---

  @Test
  @DisplayName("댓글 좋아요 토글 - 성공 (좋아요 추가)")
  void toggleCommentLike_Success_AddLike() {
    // [Given]
    Long commentId = mockComment.getId();

    // 1. SecurityUtils Mocking
    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
      given(memberRepository.findByUserId(currentUserId)).willReturn(Optional.of(mockMember));

      // 2. [중요] Comment가 active, Comment의 Post도 active 상태
      // (setUp에서 mockComment는 mockPost(active=true)를 가지도록 이미 설정됨)
      given(commentRepository.findById(commentId)).willReturn(Optional.of(mockComment));

      // 3. [핵심] '좋아요'가 없는 상태(empty)를 반환
      given(likeRepository.findByMemberAndComment(mockMember, mockComment)).willReturn(Optional.empty());

      // 4. '좋아요' 추가 후, 카운트는 1이 될 것임
      given(likeRepository.countByCommentAndCommentActiveTrue(mockComment)).willReturn(1L);

      // [When]
      LikeStatusResponse response = likeService.toggleCommentLike(commentId);

      // [Then]
      verify(likeRepository).save(any(Like.class)); // 'save' 호출 검증
      assertThat(response.isUserLiked()).isTrue();
      assertThat(response.getLikeCount()).isEqualTo(1L);
    }
  }

  @Test
  @DisplayName("댓글 좋아요 토글 - 실패 (댓글 자체가 비활성)")
  void toggleCommentLike_Fail_InactiveComment() {
    // [Given]
    Long commentId = mockComment.getId();
    mockComment.deactivate(); // [!!] 댓글을 '비활성' 상태로 변경

    // 1. SecurityUtils Mocking
    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
      given(memberRepository.findByUserId(currentUserId)).willReturn(Optional.of(mockMember));

      // 2. [핵심] '비활성' 댓글 반환 (이 댓글의 Post는 '활성' 상태)
      given(commentRepository.findById(commentId)).willReturn(Optional.of(mockComment));

      // [When & Then]
      // Service의 두 번째 if (!comment.isActive()) 에서 걸림
      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        likeService.toggleCommentLike(commentId);
      });

      assertThat(exception.getMessage()).isEqualTo("비활성 상태의 댓글에는 좋아요를 할 수 없습니다.");
    }
  }

  @Test
  @DisplayName("댓글 좋아요 토글 - 실패 (댓글의 게시글이 비활성)")
  void toggleCommentLike_Fail_InactivePost() {
    // [Given]
    Long commentId = mockComment.getId();
    mockPost.deactivate(); // [!!] 댓글이 속한 '게시글'을 '비활성' 상태로 변경

    // 1. SecurityUtils Mocking
    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
      given(memberRepository.findByUserId(currentUserId)).willReturn(Optional.of(mockMember));

      // 2. [핵심] '활성' 댓글이지만, '비활성' 게시글을 가진 댓글 반환
      given(commentRepository.findById(commentId)).willReturn(Optional.of(mockComment));

      // [When & Then]
      // Service의 첫 번째 if (!comment.getPost().isActive()) 에서 걸림
      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        likeService.toggleCommentLike(commentId);
      });

      assertThat(exception.getMessage()).isEqualTo("비활성 상태의 게시글 댓글에는 좋아요를 할 수 없습니다.");
    }
  }

  // --- 3. 상태 조회 (is... / count...) 테스트 ---

  @Test
  @DisplayName("댓글 좋아요 여부 - 성공 (비로그인 사용자)")
  void isCommentLikedByCurrentUser_Success_NotLoggedIn() {
    // [Given]
    Long commentId = mockComment.getId();

    // 1. [핵심] SecurityUtils가 null 반환
    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(null);

      // [When]
      boolean userLiked = likeService.isCommentLikedByCurrentUser(commentId);

      // [Then]
      assertThat(userLiked).isFalse();
      // [검증] DB 조회를 아예 안 했는지 확인 (if (currentUserId == null) { return false; })
      verify(memberRepository, never()).findByUserId(any());
      verify(likeRepository, never()).existsByMemberAndComment(any(), any());
    }
  }

  @Test
  @DisplayName("댓글 좋아요 여부 - 성공 (로그인 사용자)")
  void isCommentLikedByCurrentUser_Success_LoggedIn() {
    // [Given]
    Long commentId = mockComment.getId();

    // 1. SecurityUtils Mocking
    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
      given(memberRepository.findByUserId(currentUserId)).willReturn(Optional.of(mockMember));
      given(commentRepository.findById(commentId)).willReturn(Optional.of(mockComment));

      // 2. [핵심] '좋아요'가 존재한다고(true) 반환
      given(likeRepository.existsByMemberAndComment(mockMember, mockComment)).willReturn(true);

      // [When]
      boolean userLiked = likeService.isCommentLikedByCurrentUser(commentId);

      // [Then]
      assertThat(userLiked).isTrue();
    }
  }

  @Test
  @DisplayName("게시글 좋아요 수 - 성공")
  void countPostLikes_Success() {
    // [Given]
    Long postId = mockPost.getId();
    given(postRepository.findById(postId)).willReturn(Optional.of(mockPost));

    // 1. [핵심] 카운트가 5라고 반환
    given(likeRepository.countByPostAndPostActiveTrue(mockPost)).willReturn(5L);

    // [When]
    long likeCount = likeService.countPostLikes(postId);

    // [Then]
    assertThat(likeCount).isEqualTo(5L);
  }
}