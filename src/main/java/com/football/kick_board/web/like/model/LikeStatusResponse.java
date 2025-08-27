package com.football.kick_board.web.like.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LikeStatusResponse {

  private String targetType; // "POST", "COMMENT"
  private Long targetId;     // 대상 ID (게시글 ID 또는 댓글 ID)
  private long likeCount; // 해당 대상의 총 좋아요 개수
  private boolean userLiked; // 현재 사용자가 좋아요를 눌렀는지 여부
}
