package com.football.kick_board.web.like;

import com.football.kick_board.application.like.LikeService;
import com.football.kick_board.common.exception.ErrorResponse;
import com.football.kick_board.web.like.model.LikeStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "4. 좋아요 (Like) API", description = "게시글/댓글의 좋아요 토글 및 상태 조회 API")
public class LikeController {

  private final LikeService likeService;

  //게시글 좋아요 토글(누르기/취소)
  @PreAuthorize("isAuthenticated()")
  @PostMapping("/posts/{postId}")
  @Operation(summary = "게시글 좋아요 토글", description = "(인증 필요) 게시글의 '좋아요'를 누르거나 '좋아요 취소'를 수행합니다. (Toggle 방식)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "토글 성공 (현재 총 좋아요 수와 사용자 좋아요 여부 반환)",
          content = @Content(schema = @Schema(implementation = LikeStatusResponse.class))),
      @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "실패 (e.g., 존재하지 않는 게시글, 비활성 게시글)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<LikeStatusResponse> togglePostLike(@PathVariable Long postId) {
    log.info("게시글 좋아요 토글 요청: postId={}", postId);
    LikeStatusResponse response = likeService.togglePostLike(postId);
    return ResponseEntity.ok(response);
  }

  // 댓글 좋아요 토글 (누르기/취소)
  @PreAuthorize("isAuthenticated()")
  @PostMapping("/comments/{commentId}")
  @Operation(summary = "댓글 좋아요 토글", description = "(인증 필요) 댓글의 '좋아요'를 누르거나 '좋아요 취소'를 수행합니다. (Toggle 방식)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "토글 성공 (현재 총 좋아요 수와 사용자 좋아요 여부 반환)",
          content = @Content(schema = @Schema(implementation = LikeStatusResponse.class))),
      @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "실패 (e.g., 존재하지 않는 댓글, 비활성 댓글)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<LikeStatusResponse> toggleCommentLike(@PathVariable Long commentId) {
    log.info("댓글 좋아요 토글 요청: commentId={}", commentId);
    LikeStatusResponse response = likeService.toggleCommentLike(commentId);
    return ResponseEntity.ok(response);
  }

  // 게시글 좋아요 상태 조회
  @GetMapping("/posts/{postId}")
  @Operation(summary = "게시글 좋아요 상태 조회", description = "(비로그인 가능) 특정 게시글의 현재 좋아요 총 수와 (로그인 시) 나의 좋아요 여부를 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(schema = @Schema(implementation = LikeStatusResponse.class))),
      @ApiResponse(responseCode = "409", description = "조회 실패 (e.g., 존재하지 않는 게시글, 비활성 게시글)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<LikeStatusResponse> getPostLikeStatus(@PathVariable Long postId) {
    log.info("게시글 좋아요 상태 조회: postId={}", postId);
    LikeStatusResponse response = likeService.getPostLikeStatus(postId);
    return ResponseEntity.ok(response);
  }

  // 댓글 좋아요 상태 조회
  @GetMapping("/comments/{commentId}")
  @Operation(summary = "댓글 좋아요 상태 조회", description = "(비로그인 가능) 특정 댓글의 현재 좋아요 총 수와 (로그인 시) 나의 좋아요 여부를 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(schema = @Schema(implementation = LikeStatusResponse.class))),
      @ApiResponse(responseCode = "409", description = "조회 실패 (e.g., 존재하지 않는 댓글, 비활성 댓글)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<LikeStatusResponse> getCommentLikeStatus(@PathVariable Long commentId) {
    log.info("댓글 좋아요 상태 조회: commentId={}", commentId);
    LikeStatusResponse response = likeService.getCommentLikeStatus(commentId);
    return ResponseEntity.ok(response);
  }
}
