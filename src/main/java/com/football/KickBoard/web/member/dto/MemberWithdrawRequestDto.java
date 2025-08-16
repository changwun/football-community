package com.football.KickBoard.web.member.dto;

import com.football.KickBoard.common.constants.Constants;
import com.football.KickBoard.common.validation.ExactValue;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberWithdrawRequestDto {

  @NotBlank(message = "비밀번호는 필수입니다.")
  private String password;

  @NotBlank(message = "탈퇴 확인을 위해 '회원탈퇴'를 정확히 입력해주세요.")
  @ExactValue(value = Constants.WITHDRAW_CONFIRM_TEXT, message = "탈퇴 확인 문구가 올바르지 않습니다.'"
      + Constants.WITHDRAW_CONFIRM_TEXT + "'를 정확히 입력해주세.")
  private String confirmText;
}
