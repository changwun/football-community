package com.football.KickBoard.application.member;

import com.football.KickBoard.web.member.dto.MemberLoginRequestDto;
import com.football.KickBoard.web.member.dto.MemberLoginResponseDto;
import com.football.KickBoard.web.member.dto.MemberSignupRequestDto;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public interface MemberService {
  void signup(MemberSignupRequestDto requestDto);

  MemberLoginResponseDto login(MemberLoginRequestDto requestDto);

  String getCurrentUserId();
}
