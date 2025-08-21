package com.football.KickBoard.common.aop;

import com.football.KickBoard.common.WebPropertyProvider;
import com.football.KickBoard.domain.member.LoginHistory;
import com.football.KickBoard.domain.member.LoginHistoryRepository;
import com.football.KickBoard.domain.member.Member;
import com.football.KickBoard.domain.member.MemberRepository;
import com.football.KickBoard.web.member.model.response.MemberLoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class LoginHistoryAspect {

  private final LoginHistoryRepository loginHistoryRepository;
  private final HttpServletRequest request;
  private final MemberRepository memberRepository;

  @AfterReturning(
      pointcut = "execution(* com.football.KickBoard.application.member.MemberServiceImpl.login(..))",
      returning = "result")

  public void saveLoginHistory(Object result) {
    if (!(result instanceof MemberLoginResponse)) {
      return; // 로그인 결과가 우리가 기대한 DTO가 아니면 스킵
    }

    MemberLoginResponse response = (MemberLoginResponse) result;
// 로그인한 멤버 찾기 (userId 기준 - 네 도메인에 따라 PK or 아이디 문자열)
    Member member = memberRepository.findByUserId(response.getUserId())
        .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

    String ipAddress = WebPropertyProvider.getClientIp(request);
    String userAgent = WebPropertyProvider.getUserAgent(request);

    LoginHistory history = LoginHistory.builder()
        .member(member)
        .loginAt(LocalDateTime.now())
        .ipAddress(ipAddress)
        .userAgent(userAgent)
        .build();

    loginHistoryRepository.save(history);
  }
}
