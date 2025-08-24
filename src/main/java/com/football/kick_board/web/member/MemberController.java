package com.football.kick_board.web.member;

import com.football.kick_board.application.member.MemberService;
import com.football.kick_board.common.security.SecurityUtils;
import com.football.kick_board.web.member.model.request.MemberListRequest;
import com.football.kick_board.web.member.model.response.MemberListResponse;
import com.football.kick_board.web.member.model.request.MemberLoginRequest;
import com.football.kick_board.web.member.model.response.MemberLoginResponse;
import com.football.kick_board.web.member.model.response.MemberResponse;
import com.football.kick_board.web.member.model.request.MemberSignupRequest;

import com.football.kick_board.web.member.model.request.MemberWithdrawRequest;
import com.football.kick_board.web.member.model.request.PasswordChangeRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
public class MemberController {


  private final MemberService memberService;

  //관리자 회원 목록 조회
  @GetMapping("/admin")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<MemberListResponse>> getMemberListAdmin(
      @ModelAttribute MemberListRequest requestDto){
    log.info("관리자 회원 목록 조회 요청 접수: {}",requestDto);
    Page<MemberListResponse> memberList = memberService.getMemberListForAdmin(requestDto);
    return ResponseEntity.ok(memberList);
  }

  //회원 탈퇴
  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/withdraw")
  public ResponseEntity<?> withdrawMember(
      @RequestBody @Valid MemberWithdrawRequest requestDto) {

    log.info("회원 탈퇴 요청 접수: userId={}", SecurityUtils.getCurrentUserId());

    memberService.withdrawMember(requestDto);
    return ResponseEntity.ok().body(Map.of("success", true, "message", "회원 탈퇴가 성공적으로 처리되었습니다."));
  }



  //비밀번호 변경
  @PreAuthorize("isAuthenticated()")
  @PutMapping("/password")
  public ResponseEntity<?> changePassword(
      @RequestBody @Valid PasswordChangeRequest requestDto) {
    log.info("비밀번호 변경 요청 접수: userId={}", SecurityUtils.getCurrentUserId());

    memberService.changePassword(requestDto);
    return ResponseEntity.ok().body(Map.of("success", true, "message", "비밀번호가 성공적으로 변경되었습니다."));
  }


  //관리자용: PK로 회원 조회
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MemberResponse> getMemberByIdForAdmin(@PathVariable Long id) {
    log.info("관리자 회원 상세 조회 요청: id={}", id);
    MemberResponse dto = memberService.getMemberInfoByIdForAdmin(id);
    return ResponseEntity.ok(dto);
  }

  //관리자용: userId로 회원 조회
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MemberResponse> getMemberByUserIdForAdmin(@RequestParam String userId) {
    log.info("관리자 회원 상세 조회 요청: userId{}", userId);
    MemberResponse dto = memberService.getMemberInfoByUserIdForAdmin(userId);
    return ResponseEntity.ok(dto);
  }

  //토큰 아이디 추출 테스트용 getMapping
  @GetMapping("/me")
  public ResponseEntity<MemberResponse> getMyInfo() {
    log.info("현재 로그인한 사용자: {}", SecurityUtils.getCurrentUserId());
    MemberResponse memberInfo = memberService.getMemberInfo();
    return ResponseEntity.ok(memberInfo);
  }


  @PostMapping("/signup")
  public ResponseEntity<String> signup(@Valid @RequestBody MemberSignupRequest requestDto) {
    log.info("회원가입 요청 수신: userId={}", requestDto.getUserId());
    memberService.signup(requestDto);
    return ResponseEntity.ok("회원가입 완료");// 재 풀 리퀘스트 위한 주석 추가.
  }

  @PostMapping("/login")
  public ResponseEntity<MemberLoginResponse> login(
      @Valid @RequestBody MemberLoginRequest requestDto, HttpServletRequest request) {
    log.info("로그인 요청 수신: userId={}", requestDto.getUserId());
    MemberLoginResponse response = memberService.login(requestDto,request);
    return ResponseEntity.ok(response);
  }
}
