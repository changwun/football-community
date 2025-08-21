package com.football.KickBoard.web.member.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class MemberLoginRequest {

  @NotBlank
  private String userId;
  @NotBlank
  private String password;

}
