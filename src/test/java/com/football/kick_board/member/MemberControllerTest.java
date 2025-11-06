package com.football.kick_board.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.football.kick_board.domain.member.Member;
import com.football.kick_board.domain.member.MemberRepository;
import com.football.kick_board.domain.member.Role;
import com.football.kick_board.web.member.model.request.MemberLoginRequest;
import com.football.kick_board.web.member.model.request.MemberSignupRequest;
import com.football.kick_board.web.member.model.request.MemberWithdrawRequest;
import com.football.kick_board.web.member.model.request.PasswordChangeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 테스트 후 DB 롤백
class MemberControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private PasswordEncoder passwordEncoder; // [!!중요!!] 실제 암호화기 주입

  // --- 테스트용 데이터 ---
  private Member testUser;
  private Member adminUser;
  private String testUserRawPassword = "testPassword123!"; // 테스트용 원본 비밀번호

  @BeforeEach
  void setUp() {
    // 1. 테스트용 일반 유저 (testUser) 저장
    testUser = memberRepository.save(
        Member.builder()
            .userId("testUser")
            .password(passwordEncoder.encode(testUserRawPassword)) // [!!] 암호화해서 저장
            .email("testUser@test.com")
            .nickname("테스트유저")
            .favoriteTeam("TeamA")
            .role(Role.USER)
            .active(true)
            .build()
    );

    // 2. 테스트용 관리자 유저 (adminUser) 저장
    adminUser = memberRepository.save(
        Member.builder()
            .userId("adminUser")
            .password(passwordEncoder.encode("adminPassword123!"))
            .email("admin@test.com")
            .nickname("관리자")
            .favoriteTeam("TeamB")
            .role(Role.ADMIN) // [!!] ADMIN 롤
            .active(true)
            .build()
    );
  }

  // --- 1. 회원가입 (POST /members/signup) ---

  @Test
  @DisplayName("[성공] 회원가입")
  void signup_Success() throws Exception {
    // [Given]
    MemberSignupRequest request = new MemberSignupRequest();
    request.setUserId("newUser");
    request.setPassword("newPassword123!"); // DTO의 @Pattern 규칙에 맞게
    request.setEmail("newUser@test.com");
    request.setNickname("새유저");
    request.setFavoriteTeam("TeamC");
    String requestBody = objectMapper.writeValueAsString(request);

    // [When & Then]
    mockMvc.perform(
            post("/members/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isOk()) // 200 OK
        .andExpect(content().string("회원가입 완료")) // 컨트롤러 반환값
        .andDo(print());

    // [DB 검증]
    Member savedMember = memberRepository.findByUserId("newUser").orElseThrow();
    assertThat(savedMember.getNickname()).isEqualTo("새유저");
    assertThat(passwordEncoder.matches("newPassword123!", savedMember.getPassword())).isTrue(); // 암호화 검증
  }

  @Test
  @DisplayName("[실패] 회원가입 - ID 중복")
  void signup_Fail_DuplicateUserId() throws Exception {
    // [Given]
    MemberSignupRequest request = new MemberSignupRequest();
    request.setUserId("testUser"); // @BeforeEach에서 이미 생성한 ID
    request.setPassword("newPassword123!");
    request.setEmail("newUser@test.com");
    request.setNickname("새유저");
    request.setFavoriteTeam("TeamC");
    String requestBody = objectMapper.writeValueAsString(request);

    // [When & Then]
    mockMvc.perform(
            post("/members/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isConflict()) // (409 Conflict 가정)
        .andDo(print());
  }

  @Test
  @DisplayName("[실패] 회원가입 - Validation (비밀번호 규칙 위반)")
  void signup_Fail_Validation() throws Exception {
    // [Given]
    MemberSignupRequest request = new MemberSignupRequest();
    request.setUserId("newUser");
    request.setPassword("1234"); // @Pattern 위반
    request.setEmail("newUser@test.com");
    request.setNickname("새유저");
    request.setFavoriteTeam("TeamC");
    String requestBody = objectMapper.writeValueAsString(request);

    // [When & Then]
    mockMvc.perform(
            post("/members/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isBadRequest()) // @Valid가 400 반환
        .andDo(print());
  }

  // --- 2. 로그인 (POST /members/login) ---

  @Test
  @DisplayName("[성공] 로그인")
  void login_Success() throws Exception {
    // [Given]
    MemberLoginRequest request = new MemberLoginRequest();
    ReflectionTestUtils.setField(request, "userId", "testUser");
    ReflectionTestUtils.setField(request, "password", testUserRawPassword); // 원본 비밀번호
    String requestBody = objectMapper.writeValueAsString(request);

    // [When & Then]
    mockMvc.perform(
            post("/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value("testUser"))
        .andExpect(jsonPath("$.token").exists()) // 토큰이 발급되었는지
        .andDo(print());
  }

  @Test
  @DisplayName("[실패] 로그인 - 존재하지 않는 아이디")
  void login_Fail_UserNotFound() throws Exception {
    // [Given]
    MemberLoginRequest request = new MemberLoginRequest();
    ReflectionTestUtils.setField(request, "userId", "nonExistingUser");
    ReflectionTestUtils.setField(request, "password", "1234");
    String requestBody = objectMapper.writeValueAsString(request);

    // [When & Then]
    mockMvc.perform(
            post("/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isConflict()) // (409 Conflict 가정)
        .andDo(print());
  }

  @Test
  @DisplayName("[실패] 로그인 - 비밀번호 불일치")
  void login_Fail_PasswordMismatch() throws Exception {
    // [Given]
    MemberLoginRequest request = new MemberLoginRequest();
    ReflectionTestUtils.setField(request, "userId", "testUser");
    ReflectionTestUtils.setField(request, "password", "wrongPassword"); // 틀린 비밀번호
    String requestBody = objectMapper.writeValueAsString(request);

    // [When & Then]
    mockMvc.perform(
            post("/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isConflict()) // (409 Conflict 가정)
        .andDo(print());
  }

  // --- 3. 내 정보 조회 (GET /members/me) ---

  @Test
  @DisplayName("[성공] 내 정보 조회")
  @WithMockUser(username = "testUser") // 'testUser'로 로그인
  void getMyInfo_Success() throws Exception {
    // [When & Then]
    mockMvc.perform(
            get("/members/me")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value("testUser"))
        .andExpect(jsonPath("$.nickname").value("테스트유저"))
        .andDo(print());
  }

  @Test
  @DisplayName("[실패] 내 정보 조회 - 인증되지 않은 사용자")
  void getMyInfo_Fail_Unauthorized() throws Exception {
    // [When & Then]
    // @WithMockUser가 없으므로 '비로그인' 상태
    mockMvc.perform(
            get("/members/me")
        )
        .andExpect(status().isUnauthorized()) // (401 Unauthorized 가정)
        .andDo(print());
  }

  // --- 4. 비밀번호 변경 (PUT /members/password) ---

  @Test
  @DisplayName("[성공] 비밀번호 변경")
  @WithMockUser(username = "testUser") // 'testUser'로 로그인
  void changePassword_Success() throws Exception {
    // [Given]
    PasswordChangeRequest request = new PasswordChangeRequest();
    request.setCurrentPassword(testUserRawPassword); // 현재 비밀번호
    request.setNewPassword("newPassword456!"); // 새 비밀번호
    String requestBody = objectMapper.writeValueAsString(request);

    // [When & Then]
    mockMvc.perform(
            put("/members/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andDo(print());

    // [DB 검증]
    Member updatedUser = memberRepository.findByUserId("testUser").orElseThrow();
    assertThat(passwordEncoder.matches("newPassword456!", updatedUser.getPassword())).isTrue();
  }

  @Test
  @DisplayName("[실패] 비밀번호 변경 - 현재 비밀번호 불일치")
  @WithMockUser(username = "testUser") // 'testUser'로 로그인
  void changePassword_Fail_PasswordMismatch() throws Exception {
    // [Given]
    PasswordChangeRequest request = new PasswordChangeRequest();
    request.setCurrentPassword("wrongPassword"); // 틀린 현재 비밀번호
    request.setNewPassword("newPassword456!");
    String requestBody = objectMapper.writeValueAsString(request);

    // [When & Then]
    mockMvc.perform(
            put("/members/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isConflict()) // (409 Conflict 가정)
        .andDo(print());
  }

  // --- 5. 회원 탈퇴 (DELETE /members/withdraw) ---

  @Test
  @DisplayName("[성공] 회원 탈퇴")
  @WithMockUser(username = "testUser") // 'testUser'로 로그인
  void withdrawMember_Success() throws Exception {
    // [Given]
    MemberWithdrawRequest request = new MemberWithdrawRequest();
    request.setPassword(testUserRawPassword); // 올바른 비밀번호
    String requestBody = objectMapper.writeValueAsString(request);

    // [When & Then]
    mockMvc.perform(
            delete("/members/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andDo(print());

    // [DB 검증]
    Member withdrawnUser = memberRepository.findByUserId("testUser").orElseThrow();
    assertThat(withdrawnUser.isActive()).isFalse(); // active=false (soft delete) 검증
  }

  // --- 6. 관리자 기능 (GET /members/admin) ---

  @Test
  @DisplayName("[성공] 관리자 - 회원 목록 조회")
  @WithMockUser(username = "adminUser", roles = "ADMIN") // 'adminUser'로 로그인
  void getMemberListAdmin_Success() throws Exception {
    // [When & Then]
    mockMvc.perform(
            get("/members/admin")
                .param("page", "0")
                .param("size", "10")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(15)) // testUser, adminUser,DB에 13개
        .andExpect(jsonPath("$.content[0].userId").exists())
        .andDo(print());
  }

  @Test
  @DisplayName("[실패] 관리자 기능 - 일반 유저 접근")
  @WithMockUser(username = "testUser") // [!!] 'testUser' (Role: USER)로 로그인
  void getMemberListAdmin_Fail_Forbidden() throws Exception {
    // [When & Then]
    mockMvc.perform(
            get("/members/admin")
        )
        .andExpect(status().isForbidden()) // 403 Forbidden
        .andDo(print());
  }
}