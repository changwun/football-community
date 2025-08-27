package com.football.kick_board.web.like;

import com.football.kick_board.application.like.LikeService;
import com.football.kick_board.web.like.model.LikeStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
@Slf4j
public class LikeController {

  private final LikeService likeService;

  //게시글 좋아요 토글(누르기/취소)
  @PreAuthorize("isAuthenticated()")
  @PostMapping("/posts/{postId}")
  public ResponseEntity<LikeStatusResponse> togglePostLike(@PathVariable Long postId) {
    log.info("게시글 좋아요 토글 요청: postId={}", postId);
    LikeStatusResponse response = likeService.togglePostLike(postId);
    return ResponseEntity.ok(response);
  }

  // 댓글 좋아요 토글 (누르기/취소)
  @PreAuthorize("isAuthenticated()")
  @PostMapping("/comments/{commentId}")
  public ResponseEntity<LikeStatusResponse> toggleCommentLike(@PathVariable Long commentId) {
    log.info("댓글 좋아요 토글 요청: commentId={}", commentId);
    LikeStatusResponse response = likeService.toggleCommentLike(commentId);
    return ResponseEntity.ok(response);
  }

  // 게시글 좋아요 상태 조회
  @GetMapping("/posts/{postId}")
  public ResponseEntity<LikeStatusResponse> getPostLikeStatus(@PathVariable Long postId) {
    log.info("게시글 좋아요 상태 조회: postId={}", postId);
    LikeStatusResponse response = likeService.getPostLikeStatus(postId);
    return ResponseEntity.ok(response);
  }

  // 댓글 좋아요 상태 조회
  @GetMapping("/comments/{commentId}")
  public ResponseEntity<LikeStatusResponse> getCommentLikeStatus(@PathVariable Long commentId) {
    log.info("댓글 좋아요 상태 조회: commentId={}", commentId);
    LikeStatusResponse response = likeService.getCommentLikeStatus(commentId);
    return ResponseEntity.ok(response);
  }
}
