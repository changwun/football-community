package com.football.kick_board.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.football.kick_board.application.member.MemberServiceImpl;
import com.football.kick_board.common.security.JwtTokenProvider;
import com.football.kick_board.common.security.SecurityUtils;
import com.football.kick_board.domain.member.Member;
import com.football.kick_board.domain.member.MemberRepository;
import com.football.kick_board.domain.member.Role;
import com.football.kick_board.web.member.model.request.MemberLoginRequest;
import com.football.kick_board.web.member.model.request.MemberSignupRequest;
import com.football.kick_board.web.member.model.request.PasswordChangeRequest;
import com.football.kick_board.web.member.model.request.MemberWithdrawRequest;
import com.football.kick_board.web.member.model.response.MemberLoginResponse;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

  @InjectMocks
  private MemberServiceImpl memberService;
  @Mock
  private MemberRepository memberRepository;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private JwtTokenProvider jwtTokenProvider;

  // --- 1. 회원가입 (signup) 테스트 ---

  @Test
  @DisplayName("회원가입 - 성공")
  void signup_Success() {
    // [Given] (준비)
    // 생성자 대신 Setter를 이용해 객체 생성
    MemberSignupRequest request = new MemberSignupRequest();
    request.setUserId("testUser");
    request.setPassword("password123!");
    request.setEmail("test@test.com");
    request.setNickname("닉네임");
    request.setFavoriteTeam("팀이름");

    given(memberRepository.existsByUserId(request.getUserId())).willReturn(false);
    given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");

    // [When]
    memberService.signup(request);

    // [Then]
    ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
    verify(memberRepository).save(captor.capture());
    Member savedMember = captor.getValue();
    assertThat(savedMember.getUserId()).isEqualTo(request.getUserId());
    assertThat(savedMember.getPassword()).isEqualTo("encodedPassword");
    assertThat(savedMember.getNickname()).isEqualTo(request.getNickname());
    assertThat(savedMember.getRole()).isEqualTo(Role.USER);
  }

  @Test
  @DisplayName("회원가입 - 실패 (ID 중복)")
  void signup_Fail_DuplicateUserId() {
    // [Given]
    MemberSignupRequest request = new MemberSignupRequest();
    request.setUserId("testUser");
    request.setPassword("password123!");
    request.setEmail("test@test.com");
    request.setNickname("닉네임");
    request.setFavoriteTeam("팀이름");

    given(memberRepository.existsByUserId(request.getUserId())).willReturn(true);

    // [When & Then]
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      memberService.signup(request);
    });
    assertThat(exception.getMessage()).isEqualTo("이미 사용 중인 아이디입니다.");
  }

  // --- 2. 로그인 (login) 테스트 ---

  @Test
  @DisplayName("로그인 - 성공")
  void login_Success() {
    // [Given]
    // (MemberLoginRequest.java에 @Setter가 필요합니다)
    MemberLoginRequest request = new MemberLoginRequest();
    // ReflectionTestUtils를 사용해 private 필드에 값을 강제 주입
    ReflectionTestUtils.setField(request, "userId", "testUser");
    ReflectionTestUtils.setField(request, "password", "password123!");
    String encodedPassword = "encodedPassword";
    String fakeToken = "fake.jwt.token";

    Member mockMember = Member.builder()
        .userId("testUser")
        .password(encodedPassword)
        .active(true)
        .build();

    given(memberRepository.findByUserId(request.getUserId())).willReturn(Optional.of(mockMember));
    given(passwordEncoder.matches(request.getPassword(), encodedPassword)).willReturn(true);
    given(jwtTokenProvider.generateToken(mockMember.getUserId())).willReturn(fakeToken);

    // [When]
    MemberLoginResponse response = memberService.login(request, null);

    // [Then]
    assertThat(response).isNotNull();
    assertThat(response.getUserId()).isEqualTo("testUser");
    assertThat(response.getToken()).isEqualTo(fakeToken);
    verify(memberRepository).save(mockMember);
  }

  @Test
  @DisplayName("로그인 - 실패 (존재하지 않는 아이디)")
  void login_Fail_UserNotFound() {
    // [Given]
    MemberLoginRequest request = new MemberLoginRequest();
    // ReflectionTestUtils를 사용해 private 필드에 값을 강제 주입
    ReflectionTestUtils.setField(request, "userId", "testUser");
    ReflectionTestUtils.setField(request, "password", "password123!");

    given(memberRepository.findByUserId(request.getUserId())).willReturn(Optional.empty());

    // [When & Then]
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      memberService.login(request, null);
    });
    assertThat(exception.getMessage()).isEqualTo("존재하지 않는 아이디 입니다.");
  }

  @Test
  @DisplayName("로그인 - 실패 (비활성 계정)")
  void login_Fail_InactiveUser() {
    // [Given]
    MemberLoginRequest request = new MemberLoginRequest();
    // ReflectionTestUtils를 사용해 private 필드에 값을 강제 주입
    ReflectionTestUtils.setField(request, "userId", "testUser");
    ReflectionTestUtils.setField(request, "password", "password123!");

    Member inactiveMember = Member.builder().userId("testUser").active(false).build();
    given(memberRepository.findByUserId(request.getUserId())).willReturn(
        Optional.of(inactiveMember));

    // [When & Then]
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      memberService.login(request, null);
    });
    assertThat(exception.getMessage()).isEqualTo("탈퇴되었거나 비활성 상태인 계정입니다.");
  }

  @Test
  @DisplayName("로그인 - 실패 (비밀번호 불일치)")
  void login_Fail_PasswordMismatch() {
    // [Given]
    MemberLoginRequest request = new MemberLoginRequest();
    // ReflectionTestUtils를 사용해 private 필드에 값을 강제 주입
    ReflectionTestUtils.setField(request, "userId", "testUser");
    ReflectionTestUtils.setField(request, "password", "password123!");

    String encodedPassword = "encodedPassword";
    Member mockMember = Member.builder()
        .userId("testUser")
        .password(encodedPassword)
        .active(true)
        .build();

    given(memberRepository.findByUserId(request.getUserId())).willReturn(Optional.of(mockMember));
    given(passwordEncoder.matches(request.getPassword(), encodedPassword)).willReturn(false);

    // [When & Then]
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      memberService.login(request, null);
    });
    assertThat(exception.getMessage()).isEqualTo("비밀번호가 일치하지 않습니다.");
  }

  // --- 3. 비밀번호 변경 (changePassword) 테스트 ---

  @Test
  @DisplayName("비밀번호 변경 - 성공")
  void changePassword_Success() {
    // [Given]
    String currentUserId = "testUser";
    String currentPassword = "currentPassword123!";
    String newPassword = "newPassword456!";
    String encodedCurrentPassword = "encodedCurrentPassword";
    String encodedNewPassword = "encodedNewPassword";

    PasswordChangeRequest request = new PasswordChangeRequest();
    request.setCurrentPassword(currentPassword);
    request.setNewPassword(newPassword);

    Member mockMember = Member.builder()
        .userId(currentUserId)
        .password(encodedCurrentPassword)
        .build();

    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
      given(memberRepository.findByUserId(currentUserId)).willReturn(Optional.of(mockMember));
      given(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).willReturn(true);
      given(passwordEncoder.encode(newPassword)).willReturn(encodedNewPassword);

      // [When]
      memberService.changePassword(request);

      // [Then]
      assertThat(mockMember.getPassword()).isEqualTo(encodedNewPassword);
    }
  }

  @Test
  @DisplayName("비밀번호 변경 - 실패 (현재 비밀번호 불일치)")
  void changePassword_Fail_PasswordMismatch() {
    // [Given]
    String currentUserId = "testUser";
    String currentPassword = "wrongPassword";
    String newPassword = "newPassword456!";
    String encodedCurrentPassword = "encodedCurrentPassword";

    PasswordChangeRequest request = new PasswordChangeRequest();
    request.setCurrentPassword(currentPassword);
    request.setNewPassword(newPassword);

    Member mockMember = Member.builder()
        .userId(currentUserId)
        .password(encodedCurrentPassword)
        .build();

    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
      given(memberRepository.findByUserId(currentUserId)).willReturn(Optional.of(mockMember));
      given(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).willReturn(false);

      // [When & Then]
      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        memberService.changePassword(request);
      });
      assertThat(exception.getMessage()).isEqualTo("현재 비밀번호가 일치하지 않습니다.");
    }
  }

  // --- 4. 회원 탈퇴 (withdrawMember) 테스트 ---

  @Test
  @DisplayName("회원 탈퇴 - 성공")
  void withdrawMember_Success() {
    // [Given]
    String currentUserId = "testUser";
    String password = "password123!";
    String encodedPassword = "encodedPassword";

    MemberWithdrawRequest request = new MemberWithdrawRequest();
    request.setPassword(password);

    Member mockMember = Member.builder()
        .userId(currentUserId)
        .password(encodedPassword)
        .build();

    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
      given(memberRepository.findByUserId(currentUserId)).willReturn(Optional.of(mockMember));
      given(passwordEncoder.matches(password, encodedPassword)).willReturn(true);

      // [When]
      memberService.withdrawMember(request);

      // [Then]
      assertThat(mockMember.isActive()).isFalse();
      assertThat(mockMember.getDeletedAt()).isNotNull();
    }
  }

  @Test
  @DisplayName("회원 탈퇴 - 실패 (비밀번호 불일치)")
  void withdrawMember_Fail_PasswordMismatch() {
    // [Given]
    String currentUserId = "testUser";
    String wrongPassword = "wrongPassword";
    String encodedPassword = "encodedPassword";

    MemberWithdrawRequest request = new MemberWithdrawRequest();
    request.setPassword(wrongPassword);

    Member mockMember = Member.builder()
        .userId(currentUserId)
        .password(encodedPassword)
        .build();

    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
      given(memberRepository.findByUserId(currentUserId)).willReturn(Optional.of(mockMember));
      given(passwordEncoder.matches(wrongPassword, encodedPassword)).willReturn(false);

      // [When & Then]
      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        memberService.withdrawMember(request);
      });
      assertThat(exception.getMessage()).isEqualTo("비밀번호가 일치하지 않습니다.");
    }
  }
}