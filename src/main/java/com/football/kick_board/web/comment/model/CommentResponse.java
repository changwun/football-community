package com.football.kick_board.web.comment.model;

import com.football.kick_board.application.like.LikeService;
import com.football.kick_board.domain.comment.Comment;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CommentResponse {

  private Long id;
  private String content;
  private String authorUserId;
  private String authorNickname;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private boolean active;
  private Long parentCommentId; // 부모 댓글 ID
  private long likeCount;
  private boolean userLiked;
  private List<CommentResponse> replies; // 자식 댓글 목록


  // Comment 엔티티를 받아서 DTO로 변환하는 생성자
  private CommentResponse(Comment comment, long likeCount, boolean userLiked,
      List<CommentResponse> replies) {
    this.id = comment.getId();
    this.content = comment.getContent();
    this.authorUserId = comment.getAuthor().getUserId();
    this.authorNickname = comment.getAuthor().getNickname();
    this.createdAt = comment.getCreatedAt();
    this.updatedAt = comment.getUpdatedAt();
    this.active = comment.isActive();
    this.parentCommentId = (comment.getParent() != null) ? comment.getParent().getId() : null;
    this.likeCount = likeCount;
    this.userLiked = userLiked;
    this.replies = replies;
  }

  // 정적 팩토리 메서드 1: 좋아요 서비스에 접근하여 좋아요 정보를 가져오는 경우 (주로 조회 시)
  public static CommentResponse from(Comment comment, LikeService likeService) {
    // 현재 댓글의 좋아요 정보 계산
    long currentCommentLikeCount = likeService.countCommentLikes(comment.getId());
    boolean currentUserLikedComment = likeService.isCommentLikedByCurrentUser(comment.getId());

    // 자식 댓글(대댓글)에 대해 재귀적으로 CommentResponse 생성
    List<CommentResponse> replies = comment.getReplies().stream()
        .filter(Comment::isActive) // 활성 상태인 대댓글만 포함
        .map(reply -> CommentResponse.from(reply, likeService)) // ✨ 재귀 호출
        .collect(Collectors.toList());

    return new CommentResponse(
        comment,
        currentCommentLikeCount,
        currentUserLikedComment,
        replies
    );
  }

  // 이 생성자는 주로 createComment API 응답처럼 초기 좋아요 정보가 0일 때 사용.
  public static CommentResponse from(Comment comment) {
    List<CommentResponse> replies = comment.getReplies().stream()
        .filter(Comment::isActive)
        .map(CommentResponse::from) // 재귀 호출: 좋아요 서비스 없이 호출
        .collect(Collectors.toList());

    return new CommentResponse(
        comment,
        0,   // 초기 좋아요 수 0
        false, // 초기 좋아요 안 누름
        replies
    );
  }
}
