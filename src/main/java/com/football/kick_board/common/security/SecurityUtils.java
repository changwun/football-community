package com.football.kick_board.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

  //현재 인증된 사용자의 Id를 반환.
  //@return 인증된 사용자의 Id(userId)
  //@throws IllegalStateException 인증 정보가 없는 경우

  public static String getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || "anonymousUser".equals(authentication.getName())) {
      return null; // 예외를 던지는 대신 null을 반환하여 서비스 로직에서 비로그인 상태를 처리하도록 함
    }
    return authentication.getName();
  }

}
