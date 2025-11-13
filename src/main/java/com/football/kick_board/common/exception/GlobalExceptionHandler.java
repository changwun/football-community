package com.football.kick_board.common.exception;

import com.football.kick_board.common.exception.ErrorResponse.FieldErrorDetail;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

  @Slf4j
  @RestControllerAdvice
  public class GlobalExceptionHandler {

    // 유효성 검증 실패 예외 처리 (400 Bad Request)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
        MethodArgumentNotValidException ex, HttpServletRequest request) {

      log.error("유효성 검증 실패: {}", ex.getMessage());

      BindingResult bindingResult = ex.getBindingResult();
      List<FieldErrorDetail> fieldErrors = new ArrayList<>();

      for (FieldError fieldError : bindingResult.getFieldErrors()) {
        fieldErrors.add(ErrorResponse.FieldErrorDetail.builder()
            .field(fieldError.getField())
            .code(fieldError.getCode())
            .message(fieldError.getDefaultMessage())
            .build());
      }

      ErrorResponse errorResponse = ErrorResponse.builder()
          .errorCode("VALIDATION_ERROR")
          .message("입력값 검증에 실패했습니다.")
          .path(request.getRequestURI())
          .errors(fieldErrors)
          .build();

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // 비즈니스 로직 예외 처리 (409 Conflict)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
        IllegalArgumentException ex, HttpServletRequest request) {

      log.error("비즈니스 로직 예외: {}", ex.getMessage());

      ErrorResponse errorResponse = ErrorResponse.builder()
          .errorCode("BUSINESS_ERROR")
          .message(ex.getMessage())
          .path(request.getRequestURI())
          .build();

      return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

//     //권한 없음 예외 처리 (403 Forbidden)삭제 ->AccessDeniedException 따로 관리
// @ExceptionHandler(AccessDeniedException.class)
// public ResponseEntity<ErrorResponse> handleAccessDeniedException(
//     AccessDeniedException ex, HttpServletRequest request) {
//
//   log.warn("접근 권한 없음: {}", ex.getMessage());
//
//   ErrorResponse errorResponse = ErrorResponse.builder()
//       .errorCode("ACCESS_DENIED")
//       .message("접근 권한이 없습니다.")
//       .path(request.getRequestURI())
//       .build();
//
//   return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
// }

    // (@PreAuthorize("hasRole('ADMIN')") 실패 시 500 에러를 막아줍니다)
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(
        AuthorizationDeniedException ex, HttpServletRequest request) {

      log.warn("@PreAuthorize 권한 없음: {}", ex.getMessage());

      ErrorResponse errorResponse = ErrorResponse.builder()
          .errorCode("AUTHORIZATION_DENIED")
          .message("해당 기능에 접근할 권한이 없습니다.")
          .path(request.getRequestURI())
          .build();

      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    // 인증 실패 예외 처리 (401 Unauthorized)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
        AuthenticationException ex, HttpServletRequest request) {

      log.warn("인증 실패: {}", ex.getMessage());

      ErrorResponse errorResponse = ErrorResponse.builder()
          .errorCode("AUTHENTICATION_FAILED")
          .message("인증에 실패했습니다. 다시 로그인해 주세요.")
          .path(request.getRequestURI())
          .build();

      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    // JWT 관련 예외 처리 (401 Unauthorized)
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(
        JwtException ex, HttpServletRequest request) {

      log.warn("JWT 예외: {}", ex.getMessage());

      ErrorResponse errorResponse = ErrorResponse.builder()
          .errorCode("INVALID_TOKEN")
          .message("유효하지 않거나 만료된 토큰입니다.")
          .path(request.getRequestURI())
          .build();

      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    // 데이터 무결성 위반 예외 처리 (400 Bad Request)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
        DataIntegrityViolationException ex, HttpServletRequest request) {

      log.error("데이터 무결성 위반: {}", ex.getMessage());

      ErrorResponse errorResponse = ErrorResponse.builder()
          .errorCode("DATA_INTEGRITY_VIOLATION")
          .message("데이터 무결성 제약 조건을 위반했습니다. 중복된 데이터가 있는지 확인해 주세요.")
          .path(request.getRequestURI())
          .build();

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // 기타 모든 예외 처리 (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(
        Exception ex, HttpServletRequest request) {

      log.error("서버 내부 오류: {}", ex.getMessage(), ex);

      ErrorResponse errorResponse = ErrorResponse.builder()
          .errorCode("INTERNAL_SERVER_ERROR")
          .message("서버 내부 오류가 발생했습니다.")
          .path(request.getRequestURI())
          .build();

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }
