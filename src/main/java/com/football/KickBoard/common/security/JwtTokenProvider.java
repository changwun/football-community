package com.football.KickBoard.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

  //TODO 로그아웃 처리 (JWT 블랙리스트 or 클라이언트 삭제),Refresh Token 도입 (토큰 재발급)


  //SECRET_KEY 하드코딩 실무에서는 시스템 환경변수 등에 키를 저장
  @Value("${jwt.secret}")
  private String SECRET_KEY;
  private final long EXPIRATION_TIME = 1000 * 60 * 60;//토큰 생성 후 유지 시간
  private Key key;

  @PostConstruct
  public void init() {
    this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    logger.info("JWT 키 초기화 완료. 키 길이: {} bytes", SECRET_KEY.getBytes().length);
  }

  public String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  //토큰 유효성 검증
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder()
          .setSigningKey(key)
          .build()
          .parseClaimsJws(token);
      return true;
    } catch (ExpiredJwtException e) {
      logger.warn("토큰 만료: " + e.getMessage());
    } catch (UnsupportedJwtException e) {
      logger.error("지원하지 않는 JWT; " + e.getMessage());
    } catch (MalformedJwtException e) {
      logger.error("잘못된 JWT 형식: " + e.getMessage());
    } catch (SignatureException e) {
      logger.error("잘못된 서명:" + e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("JWT claims가 비어있음: " + e.getMessage());
    }
    return false;
  }

  public String getUserIdFromToken(String token) {
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();

    return claims.getSubject();
  }

  public String generateToken(String userId) {
    return Jwts.builder()
        .setSubject(userId)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
        .signWith(key)
        .compact();

  }

}
