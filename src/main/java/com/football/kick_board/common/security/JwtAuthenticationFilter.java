package com.football.kick_board.common.security;

import com.football.kick_board.domain.member.Member;
import com.football.kick_board.domain.member.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  //모든 요청 가로채서 JWT 검증
  private final JwtTokenProvider jwtTokenProvider;
  private final MemberRepository memberRepository;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    String token = jwtTokenProvider.resolveToken(request);

    //토큰이 존재하고 유효하면 토큰에서 userId 추출
    if (token != null && jwtTokenProvider.validateToken(token)) {
      String userId = jwtTokenProvider.getUserIdFromToken(token);

      Member member = memberRepository.findByUserId(userId)
          .orElseThrow(() -> new IllegalArgumentException("회원아이디가 유효하지 않습니다."));

      if (member != null) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()));

        JwtAuthenticationToken authentication =
            new JwtAuthenticationToken(userId, null, authorities);
        //JwtAuthenticationToken의 authorities 부분은 권한 정보를 의미
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }
    filterChain.doFilter(request, response);
  }

}
