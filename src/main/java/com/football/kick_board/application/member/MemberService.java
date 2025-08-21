package com.football.kick_board.application.member;

import com.football.kick_board.web.member.model.request.MemberListRequest;
import com.football.kick_board.web.member.model.response.MemberListResponse;
import com.football.kick_board.web.member.model.request.MemberLoginRequest;
import com.football.kick_board.web.member.model.response.MemberLoginResponse;
import com.football.kick_board.web.member.model.response.MemberResponse;
import com.football.kick_board.web.member.model.request.MemberSignupRequest;
import com.football.kick_board.web.member.model.request.MemberWithdrawRequest;
import com.football.kick_board.web.member.model.request.PasswordChangeRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface MemberService {

  void signup(MemberSignupRequest requestDto);

  MemberLoginResponse login(MemberLoginRequest requestDto, HttpServletRequest request);

  MemberResponse getMemberInfo(String userId);

  MemberResponse getMemberInfoByIdForAdmin(Long id);//관리자 전용 id조회

  MemberResponse getMemberInfoByUserIdForAdmin(String userId);//관리자 전용 유저id 조회

  String getCurrentUserId();

  Page<MemberListResponse> getMemberListForAdmin(MemberListRequest requestDto);

  // 반환값이 없으므로 void 통한 메서드 처리.
  void changePassword(String userId, PasswordChangeRequest requestDto);

  void withdrawMember(String userId, MemberWithdrawRequest requestDto);
}
