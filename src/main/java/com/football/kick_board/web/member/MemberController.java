package com.football.kick_board.web.member;

import com.football.kick_board.application.member.MemberService;
import com.football.kick_board.common.exception.ErrorResponse;
import com.football.kick_board.common.security.SecurityUtils;
import com.football.kick_board.domain.member.Member;
import com.football.kick_board.web.member.model.request.MemberListRequest;
import com.football.kick_board.web.member.model.response.MemberListResponse;
import com.football.kick_board.web.member.model.request.MemberLoginRequest;
import com.football.kick_board.web.member.model.response.MemberLoginResponse;
import com.football.kick_board.web.member.model.response.MemberResponse;
import com.football.kick_board.web.member.model.request.MemberSignupRequest;

import com.football.kick_board.web.member.model.request.MemberWithdrawRequest;
import com.football.kick_board.web.member.model.request.PasswordChangeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/members")
@Tag(name = "1. 회원 (Member) API", description = "회원 가입, 로그인, 정보 관리 등 핵심 인증/인가 API")
public class MemberController {


  private final MemberService memberService;

  //관리자 회원 목록 조회
  @GetMapping("/admin")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "관리자 - 회원 목록 조회", description = "(ADMIN 권한 필요) 모든 회원 목록을 검색 조건과 함께 페이징하여 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "회원 목록 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패(토큰 없음/만료)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "403", description = "접근 권한 없음(ADMIN이 아님)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<Page<MemberListResponse>> getMemberListAdmin(
      @ModelAttribute MemberListRequest requestDto) {
    Page<Member> memberPage = memberService.getMemberListForAdmin(requestDto);
    log.info("관리자 회원 목록 조회 요청 접수: {}", requestDto);

    Page<MemberListResponse> responsePage = memberPage.map(MemberListResponse::from);
    return ResponseEntity.ok(responsePage);
  }

  //회원 탈퇴
  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/withdraw")
  @Operation(summary = "회원 탈퇴", description = "현재 로그인한 회원이 비밀번호를 확인하여 탈퇴(비활성) 처리합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공",
      content = @Content(schema = @Schema(example = "{\"success\": true, \"message\": \"회원 탈퇴가 성공적으로 처리되었습니다.\"}"))),
      @ApiResponse(responseCode = "400", description = "유효성 검증 실패 (비밀번호 누락)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "비밀번호 불일치 (Business Error)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<?> withdrawMember(
      @RequestBody @Valid MemberWithdrawRequest requestDto) {

    log.info("회원 탈퇴 요청 접수: userId={}", SecurityUtils.getCurrentUserId());

    memberService.withdrawMember(requestDto);
    return ResponseEntity.ok().body(Map.of("success", true, "message", "회원 탈퇴가 성공적으로 처리되었습니다."));
  }


  //비밀번호 변경
  @PreAuthorize("isAuthenticated()")
  @PutMapping("/password")
  @Operation(summary = "비밀번호 변경", description = "현재 로그인한 회원의 기존 비밀번호를 확인한 후 새 비밀번호로 변경합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공",
          content = @Content(schema = @Schema(example = "{\"success\": true, \"message\": \"비밀번호가 성공적으로 변경되었습니다.\"}"))),
      @ApiResponse(responseCode = "400", description = "유효성 검증 실패 (e.g., 새 비밀번호 패턴 오류)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "현재 비밀번호 불일치 (Business Error)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<?> changePassword(
      @RequestBody @Valid PasswordChangeRequest requestDto) {
    log.info("비밀번호 변경 요청 접수: userId={}", SecurityUtils.getCurrentUserId());

    memberService.changePassword(requestDto);
    return ResponseEntity.ok().body(Map.of("success", true, "message", "비밀번호가 성공적으로 변경되었습니다."));
  }


  //관리자용: PK로 회원 조회
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "관리자 - ID로 회원 상세 조회", description = "(ADMIN 권한 필요) 회원의 고유 ID(PK)로 특정 회원의 상세 정보를 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "회원 조회 성공",
          content = @Content(schema = @Schema(implementation = MemberResponse.class))),
      @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "403", description = "접근 권한 없음 (ADMIN이 아님)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "존재하지 않는 회원 (Business Error)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<MemberResponse> getMemberByIdForAdmin(@PathVariable Long id) {
    Member member = memberService.getMemberInfoByIdForAdmin(id);
    log.info("관리자 회원 상세 조회 요청: id={}", id);

    MemberResponse response = MemberResponse.fromMember(member);
    return ResponseEntity.ok(response);
  }

  //관리자용: userId로 회원 조회
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "관리자 - UserID로 회원 상세 조회", description = "(ADMIN 권한 필요) 회원의 로그인 ID(UserID)로 특정 회원의 상세 정보를 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "회원 조회 성공",
          content = @Content(schema = @Schema(implementation = MemberResponse.class))),
      @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "403", description = "접근 권한 없음 (ADMIN이 아님)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "존재하지 않는 회원 (Business Error)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<MemberResponse> getMemberByUserIdForAdmin(@RequestParam String userId) {
    Member member = memberService.getMemberInfoByUserIdForAdmin(userId);
    log.info("관리자 회원 상세 조회 요청: userId{}", userId);
    MemberResponse response = MemberResponse.fromMember(member);
    return ResponseEntity.ok(response);
  }

  //토큰 아이디 추출 테스트용 getMapping
  @GetMapping("/me")
  @Operation(summary = "토큰에서 아이디 추출용 테스트", description = "현재 로그인한 사용자의 상세 정보를 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(schema = @Schema(implementation = MemberResponse.class))),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<MemberResponse> getMyInfo() {
    Member member = memberService.getMemberInfo();
    log.info("현재 로그인한 사용자: {}", SecurityUtils.getCurrentUserId());

    MemberResponse memberInfo = MemberResponse.fromMember(member);
    return ResponseEntity.ok(memberInfo);
  }


  @PostMapping("/signup")
  @Operation(summary = "회원가입", description = "신규 회원 정보를 받아 가입을 처리합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "회원가입 성공"),
      // 400 에러를 2가지 케이스로 분리 (GlobalExceptionHandler 기준)
      @ApiResponse(responseCode = "400 (Validation)", description = "유효성 검증 실패 (e.g., 이메일 형식, 비밀번호 패턴 오류)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "400 (Data)", description = "데이터 무결성 위반 (e.g., 이메일/닉네임 중복)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "ID 중복 (Business Error)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<String> signup(@Valid @RequestBody MemberSignupRequest requestDto) {
    log.info("회원가입 요청 수신: userId={}", requestDto.getUserId());
    memberService.signup(requestDto);
    return ResponseEntity.ok("회원가입 완료");// 재 풀 리퀘스트 위한 주석 추가.
  }

  @PostMapping("/login")
  @Operation(summary = "로그인", description = "아이디와 비밀번호로 로그인을 처리하고 JWT 토큰을 발급합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "로그인 성공 (JWT 토큰 발급)",
          content = @Content(schema = @Schema(implementation = MemberLoginResponse.class))),
      @ApiResponse(responseCode = "409", description = "로그인 실패 (e.g., 아이디 없음, 비밀번호 불일치, 비활성 계정)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<MemberLoginResponse> login(
      @Valid @RequestBody MemberLoginRequest requestDto, HttpServletRequest request) {
    log.info("로그인 요청 수신: userId={}", requestDto.getUserId());
    MemberLoginResponse response = memberService.login(requestDto, request);
    return ResponseEntity.ok(response);
  }
}
