package com.football.KickBoard.web.member;

import com.football.KickBoard.application.member.MemberService;
import com.football.KickBoard.application.member.MemberServiceImpl;
import com.football.KickBoard.web.member.dto.MemberLoginRequestDto;
import com.football.KickBoard.web.member.dto.MemberLoginResponseDto;
import com.football.KickBoard.web.member.dto.MemberSignupRequestDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

  private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

  private final MemberService memberService;

  //토큰 아이디 추출 테스트용 getMapping
  @GetMapping("/me")
  public ResponseEntity<String> getCurrentUser() {
    logger.debug("/members/me 요청 수신");
    String userId = memberService.getCurrentUserId();
    logger.info("현재 로그인한 사용자: {}", userId);
    return ResponseEntity.ok("현재 로그인한 사용자: " + userId);
  }


  @PostMapping("/signup")
  public ResponseEntity<String> signup(@Valid @RequestBody MemberSignupRequestDto requestDto) {
    logger.info("회원가입 요청 수신: userId={}", requestDto.getUserId());
    memberService.signup(requestDto);
    logger.info("회원가입 성공: userId={}", requestDto.getUserId());
    return ResponseEntity.ok("회원가입 완료");// 재 풀 리퀘스트 위한 주석 추가.
  }

  @PostMapping("/login")
  public ResponseEntity<MemberLoginResponseDto> login(
      @Valid @RequestBody MemberLoginRequestDto requestDto) {
    logger.info("로그인 요청 수신: userId={}", requestDto.getUserId());
    MemberLoginResponseDto response = memberService.login(requestDto);
    logger.info("로그인 성공: userId={}", requestDto.getUserId());
    return ResponseEntity.ok(response);
  }
}
