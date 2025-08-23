package com.football.kick_board.domain.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

//  //활성 상태인 게시글만 조회
//  Page<Post> findByActiveTrue(Pageable pageable);
//
//  //특정 작성자의 게시글만 조회
//  Page<Post> findByAuthor_UserIdAndActiveTrue(String userId, Pageable pageable);
//
//  //제목에 특정 키워드가 포함된 게시글 검색
//  Page<Post> findByTitleContainingAndActiveTrue(String keyword, Pageable pageable);
//
//  //내용에 특정 키워드가 포함된 게시글 검색
//  Page<Post> findByContentContainingAndActiveTrue(String keyword, Pageable pageable);
//
//  //제목 또는 내용에 특정 키워드가 포함된 게시글 검색
//  Page<Post> findByTitleContainingOrContentContainingAndActiveTrue(
//      String titleKeyword, String contentKeyword, Pageable pageable);
  @Modifying
  @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
  int incrementViewCount(@Param("postId")Long PostId);

}
