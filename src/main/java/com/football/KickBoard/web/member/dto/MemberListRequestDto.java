package com.football.KickBoard.web.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberListRequestDto {
  private String searchKeyword; //userId,nickname,email 등
  private Boolean activeStatus; //null:전체,true:활성만,false:비활성만

  private int page = 0;
  private int size = 10;
  private String sortBy = "createdAt";
  private String sortDirection = "desc";


}
