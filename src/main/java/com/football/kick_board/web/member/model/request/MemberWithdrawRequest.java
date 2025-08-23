package com.football.kick_board.web.member.model.request;

import com.football.kick_board.common.constants.Constants;
import com.football.kick_board.common.validation.ExactValue;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberWithdrawRequest {

  @NotBlank(message = "비밀번호는 필수입니다.")
  private String password;


}
