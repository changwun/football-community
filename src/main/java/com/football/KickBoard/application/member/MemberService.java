package com.football.KickBoard.application.member;

import com.football.KickBoard.web.member.dto.MemberSignupRequestDto;
import org.springframework.stereotype.Service;

@Service
public interface MemberService {
  void signup(MemberSignupRequestDto requestDto);

}
