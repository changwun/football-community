package com.football.kick_board.domain.like;

import com.football.kick_board.domain.comment.Comment;
import com.football.kick_board.domain.post.Post;
import com.football.kick_board.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

  // 특정 회원이 특정 게시글에 좋아요를 눌렀는지 확인 (중복 방지)
  boolean existsByMemberAndPost(Member member, Post post);

  Optional<Like> findByMemberAndPost(Member member, Post post);

  // 특정 회원이 특정 댓글에 좋아요를 눌렀는지 확인 (중복 방지)
  boolean existsByMemberAndComment(Member member, Comment comment);

  Optional<Like> findByMemberAndComment(Member member, Comment comment);

  // 특정 게시글의 좋아요 개수 (활성 상태인 것만)
  long countByPostAndPostActiveTrue(Post post);

  // 특정 댓글의 좋아요 개수 (활성 상태인 것만)
  long countByCommentAndCommentActiveTrue(Comment comment);
}
