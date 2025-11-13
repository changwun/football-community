package com.football.kick_board.web.comment;

import com.football.kick_board.application.comment.CommentService;
import com.football.kick_board.common.exception.ErrorResponse;
import com.football.kick_board.web.comment.model.request.CommentCreateRequest;
import com.football.kick_board.web.comment.model.response.CommentResponse;
import com.football.kick_board.web.comment.model.request.CommentUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "3. 댓글 (Comment) API", description = "게시글의 댓글/대댓글 작성, 조회, 수정, 삭제 API")
public class CommentController {

  private final CommentService commentService;

  //댓글 작성(게시글에 댓글 또는 대댓글)
  @PreAuthorize("isAuthenticated()")
  @PostMapping
  @Operation(summary = "댓글/대댓글 생성", description = "(인증 필요) 특정 게시글에 새 댓글을 작성하거나, 부모 댓글(parentCommentId)을 지정하여 대댓글을 작성합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "댓글 작성 성공",
          content = @Content(schema = @Schema(implementation = CommentResponse.class))),
      @ApiResponse(responseCode = "400", description = "유효성 검증 실패 (e.g., 내용 누락)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "생성 실패 (e.g., 존재하지 않는 게시글, 존재하지 않는 부모 댓글)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<CommentResponse> createComment(
      @RequestBody @Valid CommentCreateRequest request) {
    log.info("댓글 작성 요청: postId={}, parentId={}", request.getPostId(), request.getParentCommentId());
    CommentResponse comment = commentService.createComment(request);
    return ResponseEntity.ok(comment);
  }

  //특정 게시글의 댓글 목록 조회(페이징)
  @GetMapping("/posts/{postId}")
  @Operation(summary = "특정 게시글의 댓글 목록 조회", description = "(비로그인 가능) 특정 게시글(postId)의 '최상위' 댓글 목록을 페이징하여 조회합니다. (대댓글은 `replies` 필드에 재귀적으로 포함됨)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공"),
      @ApiResponse(responseCode = "409", description = "조회 실패 (e.g., 존재하지 않는 게시글)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<Page<CommentResponse>> getCommentsByPostId(@PathVariable Long postId,
      Pageable pageable) {
    log.info("게시글 댓글 목록 조회: postId={}, page={}, size={}",
        postId, pageable.getPageNumber(), pageable.getPageSize());
    Page<CommentResponse> comments = commentService.getCommentsByPostId(postId, pageable);
    return ResponseEntity.ok(comments);
  }

  //댓글 수정
  @PreAuthorize("isAuthenticated()")
  @PutMapping("/{commentId}")
  @Operation(summary = "댓글 수정", description = "(인증 필요) **댓글 작성자 본인**만 댓글의 내용을 수정할 수 있습니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "댓글 수정 성공",
          content = @Content(schema = @Schema(implementation = CommentResponse.class))),
      @ApiResponse(responseCode = "400", description = "유효성 검증 실패 (e.g., 내용 누락)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "수정 실패 (e.g., 존재하지 않는 댓글, 이미 삭제된 댓글, **수정 권한 없음**)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
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
  @Operation(summary = "댓글 삭제", description = "(인증 필요) **댓글 작성자 본인**만 댓글을 삭제(비활성)할 수 있습니다.")
  @ApiResponses(value = {
      // [!!] WHY? ResponseEntity<?> 타입이므로, '성공(200)' 응답을 'example'로 수동 정의합니다.
      @ApiResponse(responseCode = "200", description = "댓글 삭제 성공",
          content = @Content(schema = @Schema(example = "{\"success\": true, \"message\": \"댓글이 삭제되었습니다.\"}"))),
      @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "삭제 실패 (e.g., 존재하지 않는 댓글, 이미 삭제된 댓글, **삭제 권한 없음**)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
    log.info("댓글 삭제 요청: commentId={}", commentId);
    commentService.deleteComment(commentId);
    return ResponseEntity.ok().body(Map.of("success", true, "message", "댓글이 삭제되었습니다."));
  }

}
