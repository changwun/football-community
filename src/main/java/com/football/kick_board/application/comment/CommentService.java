package com.football.kick_board.application.comment;

import com.football.kick_board.web.comment.model.CommentCreateRequest;
import com.football.kick_board.web.comment.model.CommentResponse;
import com.football.kick_board.web.comment.model.CommentUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // 페이징을 위한 Pageable 임포트

public interface CommentService {

  // 댓글/대댓글 생성
  CommentResponse createComment(CommentCreateRequest request);

  // 특정 게시글의 댓글 목록 조회 (페이징, 최상위 댓글만 조회 후 대댓글은 CommentResponse에서 재귀적으로 처리)
  Page<CommentResponse> getCommentsByPostId(Long postId, Pageable pageable);

  // 댓글 수정
  CommentResponse updateComment(Long commentId, CommentUpdateRequest request);

  // 댓글 삭제 (소프트 삭제)
  void deleteComment(Long commentId);
}
