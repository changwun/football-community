package com.football.KickBoard.web.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberJoinRequest {

  private String email;
  private String password;
  private String name;

}
