package com.football.KickBoard.common.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  //유효성 검증 실패 시 예외 처리
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String ,String >> handleValidationExceptions(
      MethodArgumentNotValidException ex){
    Map<String, String> errors = new HashMap<>();
    BindingResult bindingResult = ex.getBindingResult();

    bindingResult.getAllErrors().forEach(error ->{
      String filedName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(filedName,errorMessage);
    });
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
  }
  // IllegalArgumentException 예외 처리 (이미 사용 중인 아이디 등)
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
      IllegalArgumentException ex) {

    Map<String, String> error = new HashMap<>();
    error.put("message", ex.getMessage());

    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  // 그 외 모든 예외 처리
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleAllExceptions(Exception ex) {
    Map<String, String> error = new HashMap<>();
    error.put("message", "서버 내부 오류가 발생했습니다.");
    error.put("detail", ex.getMessage());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }


}
