package com.football.KickBoard.web.member.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberListRequest {
  private String searchKeyword; //userId,nickname,email 등
  private Boolean activeStatus; //null:전체,true:활성만,false:비활성만

  private int page = 0;
  private int size = 10;
  private String sortBy = "createdAt";
  private String sortDirection = "desc";

  // Pageable 변환 메서드 추가
  public Pageable toPageable() {
    Sort sort = "asc".equalsIgnoreCase(sortDirection) ?
        Sort.by(sortBy).ascending() :
        Sort.by(sortBy).descending();
    return PageRequest.of(page, size, sort);
  }

}
