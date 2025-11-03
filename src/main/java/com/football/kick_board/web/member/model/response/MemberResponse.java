package com.football.kick_board.web.member.model.response;


import com.football.kick_board.domain.member.Member;
import com.football.kick_board.domain.member.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberResponse {

  private Long id;
  private String userId;
  private String nickname;
  private String email;
  private String favoriteTeam;
  private Role role;


  //중복되어 사용하는 정보 toDto 처리
  public static MemberResponse fromMember(Member member) {
    return new MemberResponse(
        member.getId(),
        member.getUserId(),
        member.getNickname(),
        member.getEmail(),
        member.getFavoriteTeam(),
        member.getRole() != null ? member.getRole() : Role.USER
    );


  }
}
