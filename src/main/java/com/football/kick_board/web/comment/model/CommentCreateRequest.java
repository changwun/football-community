package com.football.kick_board.web.comment.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {

  @NotBlank(message = "댓글 내용은 필수입니다.")
  @Size(max = 1000, message = "댓글 내용은 최대 1000자까지 가능합니다.")
  private String content;

  private Long postId; // 어떤 게시글에 대한 댓글인지
  private Long parentCommentId; // 대댓글인 경우, 부모 댓글의 ID (null이면 일반 댓글)
}
