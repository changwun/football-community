package com.football.kick_board.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "members")
@EntityListeners(AuditingEntityListener.class)
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;//내부 식별자(Pk)

  @Column(name = "userid", nullable = false, unique = true)
  private String userId; //사용자 ID
  @Column(nullable = false)
  private String password;
  @Column(nullable = false)
  private String email;
  @Column(nullable = false)
  private String nickname;

  private String phoneNumber;
  private LocalDate birthDate;
  @Column(nullable = false)
  private String favoriteTeam;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Builder.Default
  @Column(nullable = false)
  private boolean active = true;

  private LocalDateTime deletedAt;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;
  private LocalDateTime lastLoginAt;

  public void updateLastLoginAt() {
    this.lastLoginAt = LocalDateTime.now();
  }

  public void deactivate(){
    this.active = false;
    this.deletedAt = LocalDateTime.now();
  }

  public void updatePassword(String newPassword) {
    this.password = newPassword;
  }


}
