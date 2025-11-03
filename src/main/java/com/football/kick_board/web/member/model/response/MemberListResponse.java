package com.football.kick_board.web.member.model.response;

import com.football.kick_board.domain.member.Member;
import com.football.kick_board.domain.member.Role;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberListResponse {
  private Long id;
  private String userId;
  private String email;
  private String nickname;
  private Role role;
  private LocalDateTime createdAt;
  private LocalDateTime lastLoginAt;
  private boolean active;// 활성/비활성 상태

  // 코드 가독성을 위한 MemberListResponse 클래스에 정적 팩토리 메소드 추가
  public static MemberListResponse from(Member member) {
    return new MemberListResponse(
        member.getId(),
        member.getUserId(),
        member.getEmail(),
        member.getNickname(),
        member.getRole(),
        member.getCreatedAt(),
        member.getLastLoginAt(),
        member.isActive()
    );
  }

}
