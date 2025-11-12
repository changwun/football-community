package com.football.kick_board.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.football.kick_board.common.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  public JwtAuthenticationEntryPoint(ObjectMapper objectMapper){
    this.objectMapper = objectMapper;
  }
  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException, ServletException {
    log.warn("인증 실패 (Unauthenticated Access): {}", authException.getMessage());

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8"); // ✨ 한글 깨짐 방지를 위해 추가 ✨

    String errorMessage;
    String errorCode = "UNAUTHENTICATED_ERROR"; // 에러 코드도 통일

    // 토큰 만료 처리 (혹시 이 부분에서 발생하는 Exception이 있다면 추가)
    if (authException.getCause() instanceof io.jsonwebtoken.ExpiredJwtException) {
      errorMessage = "토큰이 만료되었습니다. 다시 로그인해주세요.";
      errorCode = "TOKEN_EXPIRED";
    }
    // 유효하지 않은 토큰 시그니처 (변조된 토큰 등)
    else if (authException.getCause() instanceof io.jsonwebtoken.security.SignatureException) {
      errorMessage = "유효하지 않은 토큰입니다. (서명 오류)";
      errorCode = "TOKEN_INVALID_SIGNATURE";
    }
    // 그 외 인증 실패 (토큰 없음, 잘못된 형식 등)
    else {
      errorMessage = "인증 정보가 없거나 유효하지 않습니다. 로그인 후 이용해주세요.";
    }

    // ✨ ErrorResponse DTO 객체 생성 ✨
    ErrorResponse errorResponse = ErrorResponse.builder()
        .errorCode(errorCode)
        .message(errorMessage)
        .path(request.getRequestURI())
        // .errors(null) // 유효성 검증 실패가 아니므로 errors 필드는 null 또는 제외
        .build();

    // ✨ ObjectMapper를 사용하여 ErrorResponse 객체를 JSON으로 직렬화하여 응답 ✨
    objectMapper.writeValue(response.getWriter(), errorResponse); // response.getWriter()가 더 안전
  }
}
