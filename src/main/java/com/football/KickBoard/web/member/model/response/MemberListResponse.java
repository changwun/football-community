package com.football.KickBoard.web.member.model.response;

import com.football.KickBoard.domain.member.Member;
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
  private String role;
  private LocalDateTime createdAt;
  private LocalDateTime lastLoginAt;
  private boolean active;// 활성/비활성 상태

  //Member 엔티티를 인자로 받는 생성자
  public MemberListResponse(Member member){
    this.id = member.getId();
    this.userId = member.getUserId();
    this.email = member.getEmail();
    this.nickname = member.getNickname();
    this.role = member.getRole().name();
    this.createdAt = member.getCreatedAt();
    this.lastLoginAt = member.getLastLoginAt();
    this.active = member.isActive();
  }
}
