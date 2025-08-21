package com.football.KickBoard.web.member.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PasswordChangeRequest {
  @NotBlank(message = "현재 비밀번호는 필수입니다.")
  private String currentPassword;

  @NotBlank(message = "새 비밀번호는 필수입니다.")
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
      message = "비밀번호는 최소 8자 이상이며, 문자, 숫자, 특수문자를 포함해야 합니다")
  private String newPassword;

  @NotBlank(message = "새 바말번호 확인은 필수입니다.")
  private String confirmPassword;



}
