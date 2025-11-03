package com.football.kick_board.application.post;


import com.football.kick_board.application.like.LikeService;
import com.football.kick_board.common.security.SecurityUtils;
import com.football.kick_board.common.validation.ValidationGroups;
import com.football.kick_board.domain.comment.Comment;
import com.football.kick_board.domain.member.Member;
import com.football.kick_board.domain.member.MemberRepository;
import com.football.kick_board.domain.post.Post;
import com.football.kick_board.domain.post.PostRepository;
import com.football.kick_board.web.comment.model.response.CommentResponse;
import com.football.kick_board.web.post.model.request.PostCreateRequest;
import com.football.kick_board.web.post.model.request.PostListRequest;
import com.football.kick_board.web.post.model.response.PostResponse;
import com.football.kick_board.web.post.model.request.PostUpdateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor// final 필드들에 대한 생성자 자동 생성 및 의존성 주입
public class PostServiceImpl implements PostService {

  private final PostRepository postRepository;
  private final MemberRepository memberRepository;
  private final LikeService likeService;
  private final Validator validator;

  //게시글 등록
  @Override
  @Transactional
  public Post createdPost(PostCreateRequest requestDto) {
    //동적 유효성 검증 수행
    Class<?> validationGroup;
    switch (requestDto.getBoardType()) {
      case GENERAL:
        validationGroup = ValidationGroups.GeneralPost.class;
        break;
      case MERCENARY:
        validationGroup = ValidationGroups.MercenaryPost.class;
        break;
      default:
        throw new IllegalArgumentException("유효하지 않은 게시글 유형입니다.");
    }
    Set<ConstraintViolation<PostCreateRequest>> violations = validator.validate(requestDto, validationGroup);

    if (!violations.isEmpty()) {
      // 유효성 검증 실패 시 예외 처리
      String errorMessage = violations.stream()
          .map(ConstraintViolation::getMessage)
          .collect(Collectors.joining(", "));
      throw new IllegalArgumentException("게시글 생성 실패: " + errorMessage);
    }

    String currentUserId = SecurityUtils.getCurrentUserId();
    Member author = memberRepository.findByUserId(currentUserId)
        .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다."));

    Post post = Post.builder()
        .title(requestDto.getTitle())
        .content(requestDto.getContent())
        .author(author)
        .boardType(requestDto.getBoardType())
        .location(requestDto.getLocation())
        .matchTime(requestDto.getMatchTime())
        .position(requestDto.getPosition())
        .neededPersonnel(requestDto.getNeededPersonnel())
        .build();

    return postRepository.save(post);
  }

  //게시글 목록 조회
  @Override
  @Transactional(readOnly = true)
  public Page<Post> getPosts(PostListRequest requestDto) {
    Pageable pageable = PageRequest.of(
        requestDto.getPage(),
        requestDto.getSize(),
        Sort.Direction.fromString(requestDto.getSortDirection()),
        requestDto.getSortBy()
    );
    return postRepository.searchPosts(
        requestDto.getKeyword(),
        requestDto.getActiveStatus(),
        requestDto.getBoardType(),
        pageable
    );
  }

  //게시글 상세 조회
  @Override
  @Transactional //TODO  조회수 증가(DB 변경)로 인해 트랜잭션 필요
  public Post getPostById(Long postId) {
    Post post = postRepository.findByIdWithComments(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    if (!post.isActive()) {
      throw new IllegalArgumentException("삭제되었거나 비활성 상태인 게시글입니다.");
    }
    //조회 수 증가(데이터베이스에서 직접 업데이트)
    postRepository.incrementViewCount(postId);//데이터베이스에서 직접 조회수 증가
    post.incrementViewCountInMemory();

    return post;
    }

  //게시글 수정
  @Override
  @Transactional
  public Post updatePost(Long postId, PostUpdateRequest requestDto) {
    String currentUserId = SecurityUtils.getCurrentUserId();
    //게시글 찾기
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    //권한 및 상태 확인
    if (!post.isActive()) {
      throw new IllegalArgumentException("삭제되었거나 비활성 상태인 게시글은 수정할 수 없습니다.");
    }
    if (!post.getAuthor().getUserId().equals(currentUserId)) {
      throw new IllegalArgumentException("게시글을 수정할 권한이 없습니다.");
    }
    //게시글 내용 업데이트
    post.update(requestDto.getTitle(), requestDto.getContent());

    // 업데이트된 최신 상태의 게시글을 다시 조회 (댓글 및 좋아요 정보 포함)
    Post updatedPostFromDb = postRepository.findByIdWithComments(postId)
        .orElseThrow(() -> new IllegalStateException("수정 후 게시글을 찾을 수 없습니다."));

    return updatedPostFromDb;
  }

  //게시글 삭제(소프트 삭제)
  @Override
  @Transactional
  public void deletePost(Long postId) {
    String currentUserId = SecurityUtils.getCurrentUserId();
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    if (!post.isActive()) {
      throw new IllegalArgumentException("이미 삭제 된 게시글입니다.");
    }
    if (!post.getAuthor().getUserId().equals(currentUserId)) {
      throw new IllegalArgumentException("게시글을 삭제할 권한이 없습니다.");
    }
    post.deactivate();
  }


}

