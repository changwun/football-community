package com.football.kick_board.application.comment;

import com.football.kick_board.application.like.LikeService;
import com.football.kick_board.domain.comment.Comment;
import com.football.kick_board.domain.comment.CommentRepository;
import com.football.kick_board.domain.member.Member;
import com.football.kick_board.domain.member.MemberRepository;
import com.football.kick_board.domain.post.Post;
import com.football.kick_board.domain.post.PostRepository;
import com.football.kick_board.web.comment.model.request.CommentCreateRequest;
import com.football.kick_board.web.comment.model.response.CommentResponse;
import com.football.kick_board.web.comment.model.request.CommentUpdateRequest;
import com.football.kick_board.common.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

  private final CommentRepository commentRepository;
  private final MemberRepository memberRepository;
  private final PostRepository postRepository;
  private final LikeService likeService;

  // 댓글/대댓글 생성
  @Override
  @Transactional
  public CommentResponse createComment(CommentCreateRequest request) {
    String currentUserId = SecurityUtils.getCurrentUserId();

    // 1. 작성자 찾기
    Member author = memberRepository.findByUserId(currentUserId)
        .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다."));

    // 2. 게시글 찾기
    Post post = postRepository.findById(request.getPostId())
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

    // 3. 부모 댓글 찾기 (대댓글인 경우)
    Comment parentComment = null;
    if (request.getParentCommentId() != null) {
      parentComment = commentRepository.findById(request.getParentCommentId())
          .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));
      if (!parentComment.getPost().getId().equals(post.getId())) {
        throw new IllegalArgumentException("부모 댓글이 해당 게시글에 속하지 않습니다.");
      }
    }

    // 4. Comment 엔티티 생성
    Comment comment = Comment.builder()
        .content(request.getContent())
        .author(author)
        .post(post)
        .parent(parentComment)
        .build();

    Comment savedComment = commentRepository.save(comment);
    //(좋아요 정보는 아직 필요 없음 -> 기본 생성자 사용)
    return CommentResponse.from(savedComment);
  }

  // 특정 게시글의 댓글 목록 조회
  @Override
  @Transactional(readOnly = true)
  public Page<CommentResponse> getCommentsByPostId(Long postId, Pageable pageable) {
    // 게시글 존재 여부 확인
    postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

    // 최상위 댓글만 조회
    Page<Comment> commentsPage = commentRepository.findByPostIdAndParentIsNullAndActiveTrue(postId,
        pageable);

    // 각 댓글에 좋아요 정보 추가
    return commentsPage.map(comment -> CommentResponse.from(comment, likeService));
  }

  // 댓글 수정
  @Override
  @Transactional
  public CommentResponse updateComment(Long commentId, CommentUpdateRequest request) {
    String currentUserId = SecurityUtils.getCurrentUserId();

    // 1. 댓글 찾기
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

    // 2. 작성자 본인 확인
    if (!comment.getAuthor().getUserId().equals(currentUserId)) {
      throw new IllegalArgumentException("댓글을 수정할 권한이 없습니다.");
    }

    // 3. 댓글이 활성 상태인지 확인
    if (!comment.isActive()) {
      throw new IllegalArgumentException("이미 삭제되었거나 비활성 상태인 댓글입니다.");
    }

    // 4. 댓글 내용 업데이트
    comment.update(request.getContent());

    return CommentResponse.from(comment, likeService);
  }

  // 댓글 삭제 (소프트 삭제)
  @Override
  @Transactional
  public void deleteComment(Long commentId) {
    String currentUserId = SecurityUtils.getCurrentUserId();

    // 1. 댓글 찾기
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

    // 2. 작성자 본인 확인
    if (!comment.getAuthor().getUserId().equals(currentUserId)) {
      throw new IllegalArgumentException("댓글을 삭제할 권한이 없습니다.");
    }

    // 3. 이미 비활성 상태인지 확인
    if (!comment.isActive()) {
      throw new IllegalArgumentException("이미 삭제된 댓글입니다.");
    }

    // 4. 소프트 삭제 처리 (Comment 엔티티의 deactivate() 메서드 호출)
    comment.deactivate();
  }
}