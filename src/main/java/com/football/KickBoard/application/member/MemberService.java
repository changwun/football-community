package com.football.KickBoard.application.member;

import com.football.KickBoard.web.member.dto.MemberLoginRequestDto;
import com.football.KickBoard.web.member.dto.MemberLoginResponseDto;
import com.football.KickBoard.web.member.dto.MemberResponseDto;
import com.football.KickBoard.web.member.dto.MemberSignupRequestDto;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public interface MemberService {

  void signup(MemberSignupRequestDto requestDto);

  MemberLoginResponseDto login(MemberLoginRequestDto requestDto);

  MemberResponseDto getMemberInfo(String userId);

  MemberResponseDto getMemberInfoByIdForAdmin(Long id);//관리자 전용 id조회

  MemberResponseDto getMemberInfoByUserIdForAdmin(String userId);//관리자 전용 유저id 조회

  String getCurrentUserId();
}
