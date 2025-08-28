package com.football.kick_board.domain.post;

import com.football.kick_board.domain.comment.Comment;
import com.football.kick_board.domain.member.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "posts")
public class Post {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member author;

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Comment> comments = new ArrayList<>();

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  @Column(nullable = false)
  private int viewCount = 0;


  @Column(nullable = false)
  private boolean active = true;

  private LocalDateTime deletedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BoardType boardType = BoardType.GENERAL;//기본값 일반 게시판

  //용병 모집 게시판 전용 추가 필드
  private String location;
  private LocalDateTime matchTime;
  private String position;
  private Integer neededPersonnel;


  @Builder
  public Post(String title, String content, Member author, BoardType boardType,
      String location, LocalDateTime matchTime, String position, Integer neededPersonnel) {
    this.title = title;
    this.content = content;
    this.author = author;
    this.boardType = boardType != null ? boardType : BoardType.GENERAL;
    this.location = location;
    this.matchTime = matchTime;
    this.position = position;
    this.neededPersonnel = neededPersonnel;
  }

  public void update(String title, String content) {
    this.title = title;
    this.content = content;
  }

  // 용병 게시글 추가 정보 수정 메서드 (필요시)
  public void updateMercenaryPost(String title, String content, String location,
      LocalDateTime matchTime,
      String position, Integer neededPersonnel) {
    this.title = title;
    this.content = content;
    this.location = location;
    this.matchTime = matchTime;
    this.position = position;
    this.neededPersonnel = neededPersonnel;
  }

  public void deactivate() {
    this.active = false;
    this.deletedAt = LocalDateTime.now();
  }

  public void incrementViewCountInMemory() {
    this.viewCount++;
  }

}

