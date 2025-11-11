package com.football.kick_board.application.like;

import com.football.kick_board.domain.comment.Comment;
import com.football.kick_board.domain.comment.CommentRepository;
import com.football.kick_board.domain.like.Like;
import com.football.kick_board.domain.like.LikeRepository;
import com.football.kick_board.domain.member.Member;
import com.football.kick_board.domain.member.MemberRepository;
import com.football.kick_board.domain.post.Post;
import com.football.kick_board.domain.post.PostRepository;
import com.football.kick_board.common.security.SecurityUtils;
import com.football.kick_board.web.like.model.LikeStatusResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

  private final LikeRepository likeRepository;
  private final MemberRepository memberRepository;
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;

  //현재 사용자가 특정 댓글에 좋아요 여부
  @Override
  public boolean isCommentLikedByCurrentUser(Long commentId) {
    String currentUserId = SecurityUtils.getCurrentUserId();

    // 현재 로그인한 사용자가 없는 경우
    if (currentUserId == null) {
      return false;
    }

    Member member = memberRepository.findByUserId(currentUserId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

    return likeRepository.existsByMemberAndComment(member, comment);
  }

  @Override
  public long countCommentLikes(Long commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

    return likeRepository.countByCommentAndCommentActiveTrue(comment);
  }

  // 좋아요 토글(Toggle): 이미 좋아요가 있으면 취소, 없으면 추가
  @Override
  @Transactional
  public LikeStatusResponse togglePostLike(Long postId) {
    String currentUserId = SecurityUtils.getCurrentUserId();

    Member member = memberRepository.findByUserId(currentUserId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

    // 게시글이 활성 상태인지 확인
    if (!post.isActive()) {
      throw new IllegalArgumentException("비활성 상태의 게시글에는 좋아요를 할 수 없습니다.");
    }

    Optional<Like> existingLike = likeRepository.findByMemberAndPost(member, post);
    boolean userLiked; // 사용자가 좋아요를 눌렀는지 여부

    if (existingLike.isPresent()) {
      // 이미 좋아요를 누른 경우 -> 좋아요 취소
      likeRepository.delete(existingLike.get());
      userLiked = false;
    } else {
      // 좋아요를 누르지 않은 경우 -> 좋아요 추가
      Like newLike = Like.ofPost(member, post);
      likeRepository.save(newLike);
      userLiked = true;
    }

    long likeCount = likeRepository.countByPostAndPostActiveTrue(post);
    return new LikeStatusResponse("POST", postId, likeCount, userLiked);
  }

  @Override
  @Transactional
  public LikeStatusResponse toggleCommentLike(Long commentId) {
    String currentUserId = SecurityUtils.getCurrentUserId();

    Member member = memberRepository.findByUserId(currentUserId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

    // 댓글의 게시글이 활성 상태인지 확인
    if (!comment.getPost().isActive()) {
      throw new IllegalArgumentException("비활성 상태의 게시글 댓글에는 좋아요를 할 수 없습니다.");
    }
    // 댓글 자체의 활성 상태 확인 (소프트 삭제된 댓글인지)
    if (!comment.isActive()) {
      throw new IllegalArgumentException("비활성 상태의 댓글에는 좋아요를 할 수 없습니다.");
    }

    Optional<Like> existingLike = likeRepository.findByMemberAndComment(member, comment);
    boolean userLiked;

    if (existingLike.isPresent()) {
      // 이미 좋아요를 누른 경우 -> 좋아요 취소
      likeRepository.delete(existingLike.get());
      userLiked = false;
    } else {
      // 좋아요를 누르지 않은 경우 -> 좋아요 추가
      Like newLike = Like.ofComment(member,comment);
      likeRepository.save(newLike);
      userLiked = true;
    }

    long likeCount = likeRepository.countByCommentAndCommentActiveTrue(comment);
    return new LikeStatusResponse("COMMENT", commentId, likeCount, userLiked);
  }

  @Override
  @Transactional(readOnly = true)
  public LikeStatusResponse getPostLikeStatus(Long postId) {
    String currentUserId = SecurityUtils.getCurrentUserId(); // 좋아요 여부 확인용

    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

    if (!post.isActive()) {
      throw new IllegalArgumentException("비활성 상태의 게시글은 좋아요 상태를 조회할 수 없습니다.");
    }

    long likeCount = likeRepository.countByPostAndPostActiveTrue(post);
    boolean userLiked = false; // 기본값은 false (손님)
    if (currentUserId != null) { //  로그인한 경우에만
      Member member = memberRepository.findByUserId(currentUserId).orElse(null); //  DB에서 사용자를 찾고
      if (member != null) {
        userLiked = likeRepository.existsByMemberAndPost(member, post); //  좋아요 여부 확인
      }
    }

    return new LikeStatusResponse("POST", postId, likeCount, userLiked);
  }

  @Override
  @Transactional(readOnly = true)
  public LikeStatusResponse getCommentLikeStatus(Long commentId) {
    String currentUserId = SecurityUtils.getCurrentUserId(); // 좋아요 여부 확인용

    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

    if (!comment.isActive()) {
      throw new IllegalArgumentException("비활성 상태의 댓글은 좋아요 상태를 조회할 수 없습니다.");
    }

    long likeCount = likeRepository.countByCommentAndCommentActiveTrue(comment);
    boolean userLiked = false; // 기본값 false
    if (currentUserId != null) { // 로그인한 경우에만
      Member member = memberRepository.findByUserId(currentUserId).orElse(null);
      if (member != null) {
        userLiked = likeRepository.existsByMemberAndComment(member, comment); // 좋아요 여부 확인
      }
    }

    return new LikeStatusResponse("COMMENT", commentId, likeCount, userLiked);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isPostLikedByCurrentUser(Long postId) {
    String currentUserId = SecurityUtils.getCurrentUserId();
    Member member = memberRepository.findByUserId(currentUserId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

    return likeRepository.existsByMemberAndPost(member, post);
  }

  @Override
  @Transactional(readOnly = true)
  public long countPostLikes(Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

    return likeRepository.countByPostAndPostActiveTrue(post);
  }
}