package com.football.kick_board.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.football.kick_board.common.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException, ServletException {
    log.error(">>> JwtAccessDeniedHandler - handle 메소드 실행 확인 <<<");
    log.warn("접근 거부 (Access Denied): {}", accessDeniedException.getMessage());

    response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8"); // ✨ 한글 깨짐 방지를 위해 추가 ✨

    // ✨ ErrorResponse DTO 객체 생성 ✨
    ErrorResponse errorResponse = ErrorResponse.builder()
        .errorCode("ACCESS_DENIED_ERROR")
        .message("해당 리소스에 접근할 권한이 없습니다.") // 글로벌 핸들러의 메시지와 동일하게 유지
        .path(request.getRequestURI())
        // .errors(null)
        .build();

    // ✨ ObjectMapper를 사용하여 ErrorResponse 객체를 JSON으로 직렬화하여 응답 ✨
    objectMapper.writeValue(response.getWriter(), errorResponse);
  }
}