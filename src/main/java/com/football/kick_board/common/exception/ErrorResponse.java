package com.football.kick_board.common.exception;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
  private final LocalDateTime timestamp = LocalDateTime.now();//에러 발생 시간
  private final String errorCode;//내부적인 에러코드
  private final String message;//사용자에게 보여줄 메시지
  private final String path;// 요청 경로
  private final List<FieldErrorDetail> errors;//필드 유효성 검사

  @Getter
  @Builder
  public static class FieldErrorDetail{
    private String field;//오류가 발생한 필드명
    private String code;//오류코드 명
    private String message;//오류 메시지
  }

}
