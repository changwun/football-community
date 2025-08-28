package com.football.kick_board.web.post.model.request;

import com.football.kick_board.domain.post.BoardType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostListRequest {

  private String keyword;
  private Boolean activeStatus;
  private BoardType boardType;

  private int page = 0;
  private int size = 10;
  private String sortBy = "createdAt";
  private String sortDirection = "desc"; //정렬 방향

}
