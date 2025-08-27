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
public class CommentUpdateRequest {

  @NotBlank(message = "댓글 내용은 필수입니다.")
  @Size(max = 1000, message = "댓글 내용은 최대 1000자까지 가능합니다.")
  private String content;
}