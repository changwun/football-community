package com.football.kick_board.web.comment;

import com.football.kick_board.application.comment.CommentService;
import com.football.kick_board.application.like.LikeService;
import com.football.kick_board.web.comment.model.CommentCreateRequest;
import com.football.kick_board.web.comment.model.CommentResponse;
import com.football.kick_board.web.comment.model.CommentUpdateRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

  private final CommentService commentService;

  //댓글 작성(게시글에 댓글 또는 대댓글)
  @PreAuthorize("isAuthenticated()")
  @PostMapping
  public ResponseEntity<CommentResponse> createComment(
      @RequestBody @Valid CommentCreateRequest request) {
    log.info("댓글 작성 요청: postId={}, parentId={}", request.getPostId(), request.getParentCommentId());
    CommentResponse comment = commentService.createComment(request);
    return ResponseEntity.ok(comment);
  }

  //특정 게시글의 댓글 목록 조회(페이징)
  @GetMapping("/posts/{postId}")
  public ResponseEntity<Page<CommentResponse>> getCommentsByPostId(@PathVariable Long postId,
      Pageable pageable) {
    log.info("게시글 댓글 목록 조회: postId={}, page={}, size={}",
        postId, pageable.getPageNumber(), pageable.getPageSize());
    Page<CommentResponse> comments = commentService.getCommentsByPostId(postId, pageable);
    return ResponseEntity.ok(comments);
  }

  //댓글 수장
  @PreAuthorize("isAuthenticated()")
  @PutMapping("/{commentId}")
  public ResponseEntity<CommentResponse> updateComment(
      @PathVariable Long commentId,
      @RequestBody @Valid CommentUpdateRequest request) {
    log.info("댓글 수정 요청: commentId={}", commentId);
    CommentResponse response = commentService.updateComment(commentId, request);
    return ResponseEntity.ok(response);
  }

  //댓글 삭제
  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/{commentId}")
  public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
    log.info("댓글 삭제 요청: commentId={}", commentId);
    commentService.deleteComment(commentId);
    return ResponseEntity.ok().body(Map.of("success", true, "message", "댓글이 삭제되었습니다."));
  }

}
