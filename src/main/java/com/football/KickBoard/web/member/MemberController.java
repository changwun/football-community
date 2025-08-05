package com.football.KickBoard.web.member;

import com.football.KickBoard.application.member.MemberService;
import com.football.KickBoard.web.member.dto.MemberSignupRequestDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {
  private final MemberService memberService;

  @PostMapping("/signup")
  public ResponseEntity<String> signup(@Valid @RequestBody MemberSignupRequestDto requestDto){
    memberService.signup(requestDto);
    return ResponseEntity.ok("회원가입 완료");// 재 풀 리퀘스트 위한 주석 추가.
  }

}
