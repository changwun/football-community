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
      throw new IllegalArgumentException("인증된 사용자 정보를 찾을 수 없습니다.");
    }
    return authentication.getName();
  }

}
