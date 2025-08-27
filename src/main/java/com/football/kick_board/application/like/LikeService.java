package com.football.kick_board.application.like;

import com.football.kick_board.web.like.model.LikeStatusResponse; // 좋아요 상태 DTO

public interface LikeService {

  /**
   * 특정 게시글에 좋아요를 누르거나 취소합니다.
   *
   * @param postId 좋아요를 누를/취소할 게시글 ID
   * @return 해당 게시글의 총 좋아요 수와 사용자의 좋아요 여부
   */
  LikeStatusResponse togglePostLike(Long postId);

  /**
   * 특정 댓글에 좋아요를 누르거나 취소합니다.
   *
   * @param commentId 좋아요를 누를/취소할 댓글 ID
   * @return 해당 댓글의 총 좋아요 수와 사용자의 좋아요 여부
   */
  LikeStatusResponse toggleCommentLike(Long commentId);

  /**
   * 특정 게시글의 좋아요 상태를 조회합니다.
   *
   * @param postId 게시글 ID
   * @return 해당 게시글의 총 좋아요 수와 사용자의 좋아요 여부
   */
  LikeStatusResponse getPostLikeStatus(Long postId);

  /**
   * 특정 댓글의 좋아요 상태를 조회합니다.
   *
   * @param commentId 댓글 ID
   * @return 해당 댓글의 총 좋아요 수와 사용자의 좋아요 여부
   */
  LikeStatusResponse getCommentLikeStatus(Long commentId);

  // 특정 게시글의 좋아요 정보 조회 (좋아요 수와 사용자의 좋아요 여부)
  boolean isPostLikedByCurrentUser(Long postId);

  long countPostLikes(Long postId);

  boolean isCommentLikedByCurrentUser(Long commentId);

  long countCommentLikes(Long commentId);
}
