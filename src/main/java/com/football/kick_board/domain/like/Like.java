package com.football.kick_board.domain.like;

import com.football.kick_board.domain.comment.Comment;
import com.football.kick_board.domain.member.Member;
import com.football.kick_board.domain.post.Post;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_likes", uniqueConstraints = {
    // 1. "유저" + "게시글" 조합은 유니크해야 한다.
    @UniqueConstraint(
        name = "uk_member_post",
        columnNames = {"member_id", "post_id"}
    ),
    // 2. "유저" + "댓글" 조합은 유니크해야 한다.
    @UniqueConstraint(
        name = "uk_member_comment",
        columnNames = {"member_id", "comment_id"}
    )
})
public class Like {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private Post post;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "comment_id")
  private Comment comment;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  // 게시글에 대한 좋아요 생성
  public static Like ofPost(Member member, Post post) {
    if (post == null) {
      throw new IllegalArgumentException("Post는 null일 수 없습니다.");
    }
    Like like = new Like();
    like.member = member;
    like.post = post;
    return like;
  }

  // 댓글에 대한 좋아요 생성
  public static Like ofComment(Member member, Comment comment) {
    if (comment == null) {
      throw new IllegalArgumentException("Comment는 null일 수 없습니다.");
    }
    Like like = new Like();
    like.member = member;
    like.comment = comment;
    return like;
  }
}


