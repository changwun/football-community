package com.football.KickBoard.web.member.dto;

import com.football.KickBoard.domain.member.Member;
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

  //중복되어 사용하는 정보 toDto 처리
  public static MemberResponseDto fromEntity(Member m) {
    return new MemberResponseDto(
        m.getId(),
        m.getUserId(),
        m.getNickname(),
        m.getEmail(),
        m.getFavoriteTeam(),
        m.getRole() != null ? m.getRole().name() : "USER"
    );
  }
}
