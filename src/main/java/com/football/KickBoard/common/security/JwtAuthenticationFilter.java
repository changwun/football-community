package com.football.KickBoard.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  //모든 요청 가로채서 JWT 검증

  private final JwtTokenProvider jwtTokenProvider;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException{

      String token = jwtTokenProvider.resolveToken(request);

    //토큰이 존재하고 유효하면 토큰에서 userId 추출
    if(token !=  null && jwtTokenProvider.validateToken(token)){
      String userId = jwtTokenProvider.getUserIdFromToken(token);

      JwtAuthenticationToken authentication =
          new JwtAuthenticationToken(userId, null, null);
    //JwtAuthenticationToken의 authorities 부분은 권한 정보를 의미
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
  filterChain.doFilter(request,response);
  }

}
