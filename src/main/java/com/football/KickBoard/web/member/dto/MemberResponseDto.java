package com.football.KickBoard.web.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberResponseDto {

  private Long id;
  private String userId;
  private String nickname;
  private String email;
  private String favoriteTeam;
  private String role;

}
