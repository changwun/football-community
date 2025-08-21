package com.football.kick_board.domain.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryCustom {

  /**
   * 관리자용 회원 검색 기능
   *  searchKeyword (userId, email, nickname 대상)
   *  activeStatus true=활성, false=비활성, null=전체
   *  pageable 페이징/정렬 정보
   */
  Page<Member> searchMembers(Boolean activeStatus, String searchKeyword, Pageable pageable);

}
