package com.football.KickBoard.web.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class MemberLoginRequestDto {
  @NotBlank
  private String userId;
  @NotBlank
  private String password;

}
