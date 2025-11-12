package com.football.kick_board.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.football.kick_board.application.like.LikeService;
import com.football.kick_board.application.post.PostServiceImpl;
import com.football.kick_board.common.security.SecurityUtils;
import com.football.kick_board.common.validation.ValidationGroups;
import com.football.kick_board.domain.member.Member;
import com.football.kick_board.domain.member.MemberRepository;
import com.football.kick_board.domain.post.BoardType;
import com.football.kick_board.domain.post.Post;
import com.football.kick_board.domain.post.PostRepository;
import com.football.kick_board.web.post.model.request.PostCreateRequest;
import com.football.kick_board.web.post.model.request.PostUpdateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

  @InjectMocks
  private PostServiceImpl postService;

  @Mock
  private PostRepository postRepository;
  @Mock
  private MemberRepository memberRepository;
  @Mock
  private Validator validator;

  @Mock
  private LikeService likeService; // (ServiceImpl에 주입되므로 Mock 객체 선언)

  private Member mockAuthor; // 테스트용 '실제' 작성자 객체

  @BeforeEach
  void setUp() {
    // [!!] Member 엔티티의 실제 빌더에 맞게 필수 필드를 채워주세요.
    mockAuthor = Member.builder()
        .userId("testUser")
        .nickname("테스트유저")
        .password("password123")
        .email("test@test.com")
        .favoriteTeam("TeamA")
        .build();
  }

  // --- 1. 게시글 생성 (createdPost) 테스트 ---

  @Test
  @DisplayName("게시글 생성 (일반) - 성공")
  void createdPost_General_Success() {
    // [Given] (준비)
    // 1. PostCreateRequest DTO 준비 (Setter가 있다고 가정)
    PostCreateRequest request = new PostCreateRequest();
    request.setTitle("일반글 제목");
    request.setContent("일반글 내용");
    request.setBoardType(BoardType.GENERAL);

    String currentUserId = mockAuthor.getUserId();

    // 2. [핵심] Validator Mocking (유효성 검증 통과)
    given(validator.validate(request, ValidationGroups.GeneralPost.class))
        .willReturn(Collections.emptySet());

    // 3. SecurityUtils & MemberRepository Mocking
    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
      given(memberRepository.findByUserId(currentUserId)).willReturn(Optional.of(mockAuthor));

      // 4. PostRepository.save() Mocking (ArgumentCaptor로 저장될 객체 캡처)
      ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
      // save가 호출되면, 캡처된 Post 객체를 그대로 반환하도록 설정
      given(postRepository.save(postCaptor.capture())).willAnswer(invocation -> invocation.getArgument(0));

      // [When] (실행)
      Post createdPost = postService.createdPost(request);

      // [Then] (검증)
      assertThat(createdPost).isNotNull();
      assertThat(createdPost.getTitle()).isEqualTo("일반글 제목");
      assertThat(createdPost.getAuthor().getUserId()).isEqualTo(currentUserId);

      // 캡처된 Post 객체 검증
      Post savedPost = postCaptor.getValue();
      assertThat(savedPost.getTitle()).isEqualTo("일반글 제목");
      assertThat(savedPost.getBoardType()).isEqualTo(BoardType.GENERAL);
      assertThat(savedPost.getLocation()).isNull(); // 일반글이므로 null
    }
  }

  @Test
  @DisplayName("게시글 생성 (용병) - 실패 (유효성 검증)")
  void createdPost_Mercenary_ValidationFail() {
    // [Given]
    PostCreateRequest request = new PostCreateRequest();
    request.setTitle("용병글 제목");
    request.setContent("용병글 내용");
    request.setBoardType(BoardType.MERCENARY);
    // request.setLocation(null); <- 필수 필드 누락했다고 가정

    // 1. [핵심] Validator가 실패하도록 Mocking
    // 가짜 ConstraintViolation 객체 생성
    ConstraintViolation<PostCreateRequest> violation = mock(ConstraintViolation.class);
    given(violation.getMessage()).willReturn("위치는 필수입니다."); // 에러 메시지 설정
    Set<ConstraintViolation<PostCreateRequest>> violations = Set.of(violation);

    // validator.validate가 호출되면, 에러 Set 반환
    given(validator.validate(request, ValidationGroups.MercenaryPost.class))
        .willReturn(violations);

    // [When & Then] (실행 및 검증)
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      postService.createdPost(request);
    });

    // 2. 예외 메시지에 Validator의 메시지가 포함되었는지 검증
    assertThat(exception.getMessage()).isEqualTo("게시글 생성 실패: 위치는 필수입니다.");

    // [검증] 유효성 검증 실패 시, SecurityUtils나 Repository가 호출되지 않았는지 확인
    verify(memberRepository, never()).findByUserId(any());
    verify(postRepository, never()).save(any());
  }

  @Test
  @DisplayName("게시글 생성 - 실패 (작성자 없음)")
  void createdPost_Fail_UserNotFound() {
    // [Given]
    PostCreateRequest request = new PostCreateRequest();
    request.setTitle("일반글 제목");
    request.setContent("일반글 내용");
    request.setBoardType(BoardType.GENERAL);

    String currentUserId = "nonExistingUser";

    // 1. Validator는 통과
    given(validator.validate(request, ValidationGroups.GeneralPost.class))
        .willReturn(Collections.emptySet());

    // 2. [핵심] SecurityUtils Mocking & MemberRepository가 Optional.empty() 반환
    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
      given(memberRepository.findByUserId(currentUserId)).willReturn(Optional.empty());

      // [When & Then]
      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        postService.createdPost(request);
      });
      assertThat(exception.getMessage()).isEqualTo("작성자를 찾을 수 없습니다.");
    }
  }

  // --- 2. 게시글 상세 조회 (getPostById) 테스트 ---

  @Test
  @DisplayName("게시글 상세 조회 - 성공 (조회수 증가)")
  void getPostById_Success() {
    // [Given]
    Long postId = 1L;
    // 1. '실제' Post 객체 생성 (active=true)
    Post mockPost = Post.builder()
        .title("테스트 글")
        .content("내용")
        .author(mockAuthor)
        .build();
    // (ReflectionTestUtils로 ID 강제 주입 - Post 엔티티에 id setter가 없다고 가정)
    org.springframework.test.util.ReflectionTestUtils.setField(mockPost, "id", postId);
    org.springframework.test.util.ReflectionTestUtils.setField(mockPost, "viewCount", 0); // 초기 조회수 0
    org.springframework.test.util.ReflectionTestUtils.setField(mockPost, "active", true);


    // 2. Repository Mocking (DB에서 이 '실제' 객체를 반환)
    given(postRepository.findByIdWithComments(postId)).willReturn(Optional.of(mockPost));

    // [When]
    Post foundPost = postService.getPostById(postId);

    // [Then]
    assertThat(foundPost).isNotNull();
    assertThat(foundPost.getTitle()).isEqualTo("테스트 글");

    // 3. [핵심] DB 조회수 증가(incrementViewCount) 메서드가 1번 호출되었는지 검증
    verify(postRepository).incrementViewCount(postId);

    // 4. [핵심] 반환된 '실제' 객체의 조회수(incrementViewCountInMemory)가 증가했는지 '상태' 검증
    assertThat(foundPost.getViewCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("게시글 상세 조회 - 실패 (게시글 없음)")
  void getPostById_Fail_NotFound() {
    // [Given]
    Long postId = 999L;
    given(postRepository.findByIdWithComments(postId)).willReturn(Optional.empty());

    // [When & Then]
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      postService.getPostById(postId);
    });
    assertThat(exception.getMessage()).isEqualTo("게시글을 찾을 수 없습니다.");
  }

  @Test
  @DisplayName("게시글 상세 조회 - 실패 (삭제된 게시글)")
  void getPostById_Fail_Inactive() {
    // [Given]
    Long postId = 1L;
    Post deletedPost = Post.builder().title("삭제된 글").author(mockAuthor).build();
    deletedPost.deactivate(); // active = false 상태로 변경

    given(postRepository.findByIdWithComments(postId)).willReturn(Optional.of(deletedPost));

    // [When & Then]
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      postService.getPostById(postId);
    });
    assertThat(exception.getMessage()).isEqualTo("삭제되었거나 비활성 상태인 게시글입니다.");
  }

  // --- 3. 게시글 수정 (updatePost) 테스트 ---

  @Test
  @DisplayName("게시글 수정 - 성공")
  void updatePost_Success() {
    // [Given]
    Long postId = 1L;
    String currentUserId = mockAuthor.getUserId();

    // 1. 수정 요청 DTO
    PostUpdateRequest request = new PostUpdateRequest();
    request.setTitle("수정된 제목");
    request.setContent("수정된 내용");

    // 2. '실제' 원본 Post 객체 (작성자: mockAuthor, active=true)
    Post originalPost = Post.builder()
        .title("원본 제목")
        .content("원본 내용")
        .author(mockAuthor)
        .build();
    org.springframework.test.util.ReflectionTestUtils.setField(originalPost, "active", true);


    // 3. SecurityUtils & Repository Mocking
    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);

      // 4. postRepository.findById()는 원본 Post 반환
      given(postRepository.findById(postId)).willReturn(Optional.of(originalPost));

      // 5. [중요] postRepository.findByIdWithComments()는 (업데이트된) Post 반환
      //    (이 테스트에서는 편의상 같은 객체를 반환해도 무방)
      given(postRepository.findByIdWithComments(postId)).willReturn(Optional.of(originalPost));

      // [When]
      Post updatedPost = postService.updatePost(postId, request);

      // [Then]
      // 1. 반환된 객체가 re-fetch된 객체인지 확인 (여기서는 originalPost)
      assertThat(updatedPost).isNotNull();

      // 2. [핵심] '실제' originalPost 객체의 '상태'가 변경되었는지 검증
      //    (post.update() 메서드가 잘 호출되었는지)
      assertThat(originalPost.getTitle()).isEqualTo("수정된 제목");
      assertThat(originalPost.getContent()).isEqualTo("수정된 내용");
    }
  }

  @Test
  @DisplayName("게시글 수정 - 실패 (작성자 아님)")
  void updatePost_Fail_NotAuthor() {
    // [Given]
    Long postId = 1L;
    String otherUserId = "otherUser"; // 현재 로그인한 사용자 (타인)

    PostUpdateRequest request = new PostUpdateRequest();
    request.setTitle("수정 시도");
    request.setContent("수정 시도");

    // 1. Post 객체 (작성자: mockAuthor)
    Post originalPost = Post.builder().title("원본").author(mockAuthor).build();
    org.springframework.test.util.ReflectionTestUtils.setField(originalPost, "active", true);

    // 2. SecurityUtils는 'otherUser' 반환, Repository는 'originalPost' 반환
    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(otherUserId);
      given(postRepository.findById(postId)).willReturn(Optional.of(originalPost));

      // [When & Then]
      // currentUserId("otherUser") != post.getAuthor().getUserId("testUser")
      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        postService.updatePost(postId, request);
      });
      assertThat(exception.getMessage()).isEqualTo("게시글을 수정할 권한이 없습니다.");
    }
  }

  // --- 4. 게시글 삭제 (deletePost) 테스트 ---

  @Test
  @DisplayName("게시글 삭제 - 성공 (소프트 삭제)")
  void deletePost_Success() {
    // [Given]
    Long postId = 1L;
    String currentUserId = mockAuthor.getUserId();

    // 1. '실제' Post 객체 (작성자: mockAuthor, active=true)
    Post originalPost = Post.builder().title("원본").author(mockAuthor).build();
    org.springframework.test.util.ReflectionTestUtils.setField(originalPost, "active", true);

    // 2. SecurityUtils & Repository Mocking
    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
      given(postRepository.findById(postId)).willReturn(Optional.of(originalPost));

      // [When]
      postService.deletePost(postId); // (void 반환)

      // [Then]
      // 3. [핵심] '실제' originalPost 객체의 '상태'가 변경되었는지 검증
      //    (post.deactivate()가 잘 호출되었는지)
      assertThat(originalPost.isActive()).isFalse();
      assertThat(originalPost.getDeletedAt()).isNotNull();
    }
  }

  @Test
  @DisplayName("게시글 삭제 - 실패 (작성자 아님)")
  void deletePost_Fail_NotAuthor() {
    // [Given]
    Long postId = 1L;
    String otherUserId = "otherUser"; // 현재 로그인한 사용자 (타인)

    Post originalPost = Post.builder().title("원본").author(mockAuthor).build();
    org.springframework.test.util.ReflectionTestUtils.setField(originalPost, "active", true);

    // 2. SecurityUtils & Repository Mocking
    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(otherUserId);
      given(postRepository.findById(postId)).willReturn(Optional.of(originalPost));

      // [When & Then]
      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        postService.deletePost(postId);
      });
      assertThat(exception.getMessage()).isEqualTo("게시글을 삭제할 권한이 없습니다.");
    }
  }

  @Test
  @DisplayName("게시글 삭제 - 실패 (이미 삭제됨)")
  void deletePost_Fail_AlreadyDeleted() {
    // [Given]
    Long postId = 1L;
    String currentUserId = mockAuthor.getUserId();

    // 1. '이미 삭제된' Post 객체
    Post deletedPost = Post.builder().title("원본").author(mockAuthor).build();
    deletedPost.deactivate(); // active = false

    // 2. SecurityUtils & Repository Mocking
    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
      given(postRepository.findById(postId)).willReturn(Optional.of(deletedPost));

      // [When & Then]
      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        postService.deletePost(postId);
      });
      assertThat(exception.getMessage()).isEqualTo("이미 삭제 된 게시글입니다.");
    }
  }
}