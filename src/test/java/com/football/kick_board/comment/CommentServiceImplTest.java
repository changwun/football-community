package com.football.kick_board.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.football.kick_board.application.comment.CommentServiceImpl;
import com.football.kick_board.application.like.LikeService;
import com.football.kick_board.common.security.SecurityUtils;
import com.football.kick_board.domain.comment.Comment;
import com.football.kick_board.domain.comment.CommentRepository;
import com.football.kick_board.domain.member.Member;
import com.football.kick_board.domain.member.MemberRepository;
import com.football.kick_board.domain.post.Post;
import com.football.kick_board.domain.post.PostRepository;
import com.football.kick_board.web.comment.model.request.CommentCreateRequest;
import com.football.kick_board.web.comment.model.request.CommentUpdateRequest;
import com.football.kick_board.web.comment.model.response.CommentResponse;
import java.util.Optional;
import org.hibernate.sql.ast.tree.from.CorrelatedTableGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {

  @InjectMocks //테스트 대상 (Mock 객체들을 여기에 주입)
  private CommentServiceImpl commentService;

  @Mock //가짜(Mock) 객체로 만들 의존성
  private CommentRepository commentRepository;
  @Mock
  private MemberRepository memberRepository;
  @Mock
  private PostRepository postRepository;
  @Mock
  private LikeService likeService;

  @Test
  @DisplayName("댓글 생성 - 성공")
  void createComment_Success() {
    //given
    CommentCreateRequest request = new CommentCreateRequest(
        "테스트 댓글 내용",
        1L,
        null
    );
    String currentUserId = "testId";

    //가짜(Mock) 객체들 준비
    Member mockAuthor = Member.builder().userId(currentUserId).nickname("테스트유저").build();
    Post mockPost = Post.builder()
        .title("테스트 게시글")
        .content("테스트 내용")
        .build();
    org.springframework.test.util.ReflectionTestUtils.setField(
        mockPost, // 값을 주입할 객체
        "id",     // 필드 이름
        1L        // 주입할 값 (request의 postId와 동일하게)
    );

    Comment savedComment = Comment.builder()
        .content(request.getContent())
        .author(mockAuthor)
        .post(mockPost)
        .parent(null)
        .build();
    // (실제로는 ID가 세팅되겠지만, 테스트에서는 from 메서드가 이 객체를 사용하므로)

    // 4. Mocking 설정 (이게 핵심입니다)
    // 4-1. SecurityUtils.getCurrentUserId()가 "testUser"를 반환하도록 Mocking
    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);

      // 4-2. memberRepository.findByUserId(...)가 mockAuthor를 반환하도록 설정
      given(memberRepository.findByUserId(currentUserId)).willReturn(Optional.of(mockAuthor));

      // 4-3. postRepository.findById(...)가 mockPost를 반환하도록 설정
      given(postRepository.findById(request.getPostId())).willReturn(Optional.of(mockPost));

      // 4-4. commentRepository.save(...)가 호출되면 savedComment를 반환하도록 설정
      given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

      // [When] (실행)
      CommentResponse response = commentService.createComment(request);

      // [Then] (검증)
      assertThat(response).isNotNull();
      assertThat(response.getContent()).isEqualTo(request.getContent());
      assertThat(response.getAuthorNickname()).isEqualTo(mockAuthor.getNickname());

      // (검증) commentRepository의 save 메서드가 1번 호출되었는지 확인
      verify(commentRepository).save(any(Comment.class));
    }
  }

  @Test
  @DisplayName("댓글 생성 - 실패(게시글 없음)")
  void createComment_Fail_PostNotFound() {
    //given
    CommentCreateRequest request = new CommentCreateRequest(
        "테스트 댓글 내용",
        999L,
        null
    );
    String currentUserId = "testId";

    Member mockAuthor = Member.builder().userId(currentUserId).build();
    // [Mocking] (설정)
    // 1. SecurityUtils Mocking
    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);

      // 2. MemberRepository Mocking (작성자는 찾음)
      given(memberRepository.findByUserId(currentUserId)).willReturn(Optional.of(mockAuthor));

      // 3. [핵심] PostRepository Mocking (게시글을 못 찾음)
      // 999L로 조회 시, Optional.empty()를 반환하도록 설정
      given(postRepository.findById(request.getPostId())).willReturn(Optional.empty());

      // [When & Then] (실행 및 검증)
      // commentService.createComment(request)를 실행할 때
      // IllegalArgumentException이 발생하는지 확인
      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        commentService.createComment(request);
      });

      // (추가 검증) 예외 메시지가 "게시글을 찾을 수 없습니다."인지 확인
      assertThat(exception.getMessage()).isEqualTo("게시글을 찾을 수 없습니다.");
    }
  }

  @Test
  @DisplayName("댓글 수정 - 성공")
  void updateComment_Success() {
    //given 준비
    Long commentId = 1L;
    String currentUserId = "testId";
    CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글 내용");

    Member mockAuthor = Member.builder().userId(currentUserId).build();
    Comment existingComment = Comment.builder()
        .content("원본 댓글 내용")
        .author(mockAuthor)
        .post(null)
        .parent(null)
        .build();

    // (ReflectionTestUtils로 ID 강제 주입)
    org.springframework.test.util.ReflectionTestUtils.setField(existingComment, "id", commentId);

    //Mocking 설정
    try (MockedStatic<SecurityUtils> mockedSecurrity = mockStatic(SecurityUtils.class)) {
      // 1. SecurityUtils Mocking (현재 사용자를 "testId"로 설정)
      mockedSecurrity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);

      // 2. commentRepository.findById Mocking(1L로 조회 시 existingComment 반환)
      given(commentRepository.findById(commentId)).willReturn(Optional.of(existingComment));

      // 3.likeService Mocking (CommentResponse.from 호출 대비)
      given(likeService.isCommentLikedByCurrentUser(commentId)).willReturn(false);
      given(likeService.countCommentLikes(commentId)).willReturn(0L);

      //when 실행
      CommentResponse response = commentService.updateComment(commentId, request);

      // [Then] (검증)
      // 1. 반환된 DTO의 내용이 요청한 "수정된 내용"과 일치하는지 확인
      assertThat(response.getContent()).isEqualTo(request.getContent());

      // 2. [핵심] existingComment 객체의 내용이 실제로 변경되었는지 확인
      assertThat(existingComment.getContent()).isEqualTo(request.getContent());
    }
  }

  @Test
  @DisplayName("댓글 수정 - 실패(댓글 없음)")
  void updateComment_Fail_NotFound() {
    // given 준비
    Long commentId = 999L;//미 존재 Id
    String currentUserId = "testId";
    CommentUpdateRequest request = new CommentUpdateRequest("수정 시도");

    // Mocking 설정
    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);

      given(commentRepository.findById(commentId)).willReturn(Optional.empty());

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        commentService.updateComment(commentId, request);
      });
      assertThat(exception.getMessage()).isEqualTo("댓글을 찾을 수 없습니다.");
    }
  }

  @Test
  @DisplayName("댓글 수정 - 실패 (이미 삭제된 댓글)")
  void updateComment_Fail_Inactive() {
    // [Given] (준비)
    Long commentId = 1L;
    String currentUserId = "testId"; // 작성자와 현재 사용자가 동일
    CommentUpdateRequest request = new CommentUpdateRequest("수정 시도");

    // 1. 원본 댓글 작성자(Member) Mock
    Member mockAuthor = Member.builder().userId(currentUserId).build();

    // 2. 'DB에 저장된 원본 댓글' Mock
    Comment existingComment = Comment.builder()
        .content("원본 댓글 내용")
        .author(mockAuthor)
        .build();
    org.springframework.test.util.ReflectionTestUtils.setField(existingComment, "id", commentId);

    // 3. [핵심] 이 댓글을 '비활성' 상태로 미리 변경합니다.
    existingComment.deactivate(); // active = false, deletedAt = now()

    // [Mocking] (설정)
    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      // 1. SecurityUtils Mocking (권한 검사 통과 목적)
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);

      // 2. commentRepository.findById Mocking (비활성 상태의 댓글 반환)
      given(commentRepository.findById(commentId)).willReturn(Optional.of(existingComment));

      // [When & Then] (실행 및 검증)
      // 서비스 코드의 if (!comment.isActive()) { ... } 구문에서 예외가 터져야 함
      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        commentService.updateComment(commentId, request);
      });

      // 3. 예외 메시지 검증
      assertThat(exception.getMessage()).isEqualTo("이미 삭제되었거나 비활성 상태인 댓글입니다.");
    }
  }

  @Test
  @DisplayName("댓글 수정 - 실패 (작성자 아님)")
  void updateComment_Fail_NotAuthor() {
    // [Given] (준비)
    Long commentId = 1L;
    String authorUserId = "authorId"; // 댓글 원본 작성자
    String otherUserId = "otherUserId"; // 현재 로그인한 사용자 (타인)
    CommentUpdateRequest request = new CommentUpdateRequest("수정 시도");

    // 1. 원본 댓글 작성자(Member) Mock
    Member mockAuthor = Member.builder().userId(authorUserId).build();

    // 2. 'DB에 이미 저장되어 있던 원본 댓글' Mock (작성자는 "authorId")
    Comment existingComment = Comment.builder()
        .content("원본 댓글 내용")
        .author(mockAuthor)
        .build();
    org.springframework.test.util.ReflectionTestUtils.setField(existingComment, "id", commentId);

    // [Mocking] (설정)
    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      // 1. [핵심] SecurityUtils Mocking (현재 사용자를 "otherUserId"로 설정)
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(otherUserId);

      // 2. [핵심] commentRepository.findById Mocking (댓글은 찾음)
      //    이 댓글의 작성자는 "authorId" 입니다.
      given(commentRepository.findById(commentId)).willReturn(Optional.of(existingComment));

      // [When & Then] (실행 및 검증)
      // 현재 사용자("otherUserId")와 댓글 작성자("authorId")가 다르므로
      // "댓글을 수정할 권한이 없습니다." 라는 예외가 터져야 함
      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        commentService.updateComment(commentId, request);
      });

      // 3. 예외 메시지 검증
      assertThat(exception.getMessage()).isEqualTo("댓글을 수정할 권한이 없습니다.");
    }
  }

  @Test
  @DisplayName("댓글 삭제 - 성공")
  void deleteComment_Success() {
    Long commentId = 1L;
    String currentUserId = "testId";

    Member mockAuthor = Member.builder().userId(currentUserId).build();

    Comment existingComment = Comment.builder()
        .content("원본 댓글 내용")
        .author(mockAuthor)
        .build();
    org.springframework.test.util.ReflectionTestUtils.setField(existingComment, "id", commentId);

    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);

      given(commentRepository.findById(commentId)).willReturn(Optional.of(existingComment));

      commentService.deleteComment(commentId);
      // [Then] (검증)
      // [!!핵심!!]
      // 반환값이 없으므로, 'existingComment' 객체의 deactivate()가 호출되어
      // 'active' 상태가 'false'로 변경되었는지 직접 검증합니다.
      assertThat(existingComment.isActive()).isFalse();
      assertThat(existingComment.getDeletedAt()).isNotNull();
    }
  }

  @Test
  @DisplayName("댓글 삭제 - 실패 (작성자 아님)")
  void deleteComment_Fail_NotAuthor() {
    // [Given] (준비)
    Long commentId = 1L;
    String authorUserId = "authorId"; // 댓글 원본 작성자
    String otherUserId = "otherUserId"; // 현재 로그인한 사용자 (타인)

    // 1. 원본 댓글 작성자(Member) Mock
    Member mockAuthor = Member.builder().userId(authorUserId).build();

    // 2. 'DB에 저장된 원본 댓글' Mock (작성자는 "authorId")
    Comment existingComment = Comment.builder()
        .content("원본 댓글 내용")
        .author(mockAuthor)
        .build();
    org.springframework.test.util.ReflectionTestUtils.setField(existingComment, "id", commentId);

    // [Mocking] (설정)
    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      // 1. SecurityUtils Mocking (현재 사용자를 "otherUserId"로 설정)
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(otherUserId);

      // 2. commentRepository.findById Mocking (댓글은 찾음)
      given(commentRepository.findById(commentId)).willReturn(Optional.of(existingComment));

      // [When & Then] (실행 및 검증)
      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        // [!!수정된 부분!!]
        commentService.deleteComment(commentId);
      });

      // 3. 예외 메시지 검증 (메시지만 다름)
      assertThat(exception.getMessage()).isEqualTo("댓글을 삭제할 권한이 없습니다.");
    }
  }

  @Test
  @DisplayName("댓글 삭제 - 실패 (댓글 없음)")
  void deleteComment_Fail_NotFound() {
    Long commentId = 999L; // 존재하지 않는 ID
    String currentUserId = "testId";

    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);

      given(commentRepository.findById(commentId)).willReturn(Optional.empty());
      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        commentService.deleteComment(commentId);
      });
      assertThat(exception.getMessage()).isEqualTo("댓글을 찾을 수 없습니다.");
    }
  }

  @Test
  @DisplayName("댓글 삭제 - 실패 (이미 삭제된 댓글)")
  void deleteComment_Fail_Inactive() {
    Long commentId = 1L;
    String currentUserId = "testId";

    Member mockAuthor = Member.builder().userId(currentUserId).build();

    Comment existingComment = Comment.builder()
        .content("원본 댓글 내용")
        .author(mockAuthor)
        .build();
    org.springframework.test.util.ReflectionTestUtils.setField(existingComment, "id", commentId);

    existingComment.deactivate(); // active = false

    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);

      given(commentRepository.findById(commentId)).willReturn(Optional.of(existingComment));

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        commentService.deleteComment(commentId);
      });
      assertThat(exception.getMessage()).isEqualTo("이미 삭제된 댓글입니다.");
    }
  }
}
