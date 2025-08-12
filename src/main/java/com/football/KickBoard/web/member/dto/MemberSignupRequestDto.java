package com.football.KickBoard.web.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberSignupRequestDto {

  @NotBlank(message = "아이디 입력은 필수입니다.")
  private String userId;

  @NotBlank(message = "비밀번호 입력은 필수입니다.")
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
      message = "비밀번호는 최소 8자 이상이며, 문자, 숫자, 특수문자를 포함해야 합니다")
  private String password;

  @NotBlank(message = "닉네임 입력은 필수입니다.")
  private String nickname;

  @NotBlank(message = "이메일 입력은 필수입니다.")
  private String email;

  @NotBlank(message = "좋아하는 팀 입력은 필수입니다.")
  private String favoriteTeam;

}
