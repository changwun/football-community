package com.football.kick_board.domain.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {

  /**
   * 게시글을 동적으로 검색하여 페이징 처리된 목록을 반환합니다.
   *
   * @param keyword      검색어 (제목 또는 내용에 포함 여부)
   * @param activeStatus 활성 상태 (true: 활성, false: 비활성/삭제, null: 전체)
   * @param pageable     페이징 및 정렬 정보
   * @return 검색 조건에 맞는 Post 엔티티의 Page 객체
   */
  Page<Post> searchPosts(String keyword, Boolean activeStatus, Pageable pageable);

}
