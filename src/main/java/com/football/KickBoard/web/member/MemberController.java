package com.football.KickBoard.web.member;

import com.football.KickBoard.application.member.MemberService;
import com.football.KickBoard.application.member.MemberServiceImpl;
import com.football.KickBoard.web.member.dto.MemberListRequestDto;
import com.football.KickBoard.web.member.dto.MemberListResponseDto;
import com.football.KickBoard.web.member.dto.MemberLoginRequestDto;
import com.football.KickBoard.web.member.dto.MemberLoginResponseDto;
import com.football.KickBoard.web.member.dto.MemberResponseDto;
import com.football.KickBoard.web.member.dto.MemberSignupRequestDto;

import com.football.KickBoard.web.member.dto.MemberWithdrawRequestDto;
import com.football.KickBoard.web.member.dto.PasswordChangeRequestDto;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
@RequestMapping("/members")
public class MemberController {

  private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

  private final MemberService memberService;

  @GetMapping("/admin")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<MemberListResponseDto>> getMemberListAdmin(
      @ModelAttribute MemberListRequestDto requestDto){
    logger.info("관리자 회원 목록 조회 요청 접수: {}",requestDto);
    Page<MemberListResponseDto> memberList = memberService.getMemberListForAdmin(requestDto);
    return ResponseEntity.ok(memberList);
  }

  @DeleteMapping("/withdraw")
  public ResponseEntity<?> withdrawMember(
      @RequestBody @Valid MemberWithdrawRequestDto requestDto,
      Authentication authentication) {
    String userId = authentication.getName();
    logger.info("회원 탈퇴 요청 접수: userId={}, confirmText='{}'", userId, requestDto.getConfirmText());

    memberService.withdrawMember(userId, requestDto);

    return ResponseEntity.ok().body(Map.of("seccess", true, "message", "회원 탈퇴가 성공적으로 처리되었습니다."));
  }




  @PutMapping("/password")
  public ResponseEntity<?> changePassword(
      @RequestBody @Valid PasswordChangeRequestDto requestDto,
      Authentication authentication) {
    String userId = authentication.getName();
    logger.info("비밀번호 변경 요청 접수: userId={}", userId);

    memberService.changePassword(userId, requestDto);

    return ResponseEntity.ok().body(Map.of("success", true, "message", "비밀번호가 성공적으로 변경되었습니다."));
  }


  //관리자용: PK로 회원 조회
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MemberResponseDto> getMemberByIdForAdmin(@PathVariable Long id) {
    logger.info("관리자 회원 상세 조회 요청: id={}", id);
    MemberResponseDto dto = memberService.getMemberInfoByIdForAdmin(id);
    return ResponseEntity.ok(dto);
  }

  //관리자용: userId로 회원 조회
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MemberResponseDto> getMemberByUserIdForAdmin(@RequestParam String userId) {
    logger.info("관리자 회원 상세 조회 요청: userId{}", userId);
    MemberResponseDto dto = memberService.getMemberInfoByUserIdForAdmin(userId);
    return ResponseEntity.ok(dto);
  }

  //토큰 아이디 추출 테스트용 getMapping
  @GetMapping("/me")
  public ResponseEntity<MemberResponseDto> getMyInfo() {
    String userId = memberService.getCurrentUserId();
    logger.info("현재 로그인한 사용자: {}", userId);
    MemberResponseDto memberInfo = memberService.getMemberInfo(userId);
    return ResponseEntity.ok(memberInfo);
  }


  @PostMapping("/signup")
  public ResponseEntity<String> signup(@Valid @RequestBody MemberSignupRequestDto requestDto) {
    logger.info("회원가입 요청 수신: userId={}", requestDto.getUserId());
    memberService.signup(requestDto);
    return ResponseEntity.ok("회원가입 완료");// 재 풀 리퀘스트 위한 주석 추가.
  }

  @PostMapping("/login")
  public ResponseEntity<MemberLoginResponseDto> login(
      @Valid @RequestBody MemberLoginRequestDto requestDto) {
    logger.info("로그인 요청 수신: userId={}", requestDto.getUserId());
    MemberLoginResponseDto response = memberService.login(requestDto);
    return ResponseEntity.ok(response);
  }
}
