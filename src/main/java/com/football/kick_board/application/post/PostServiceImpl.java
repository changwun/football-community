package com.football.kick_board.application.post;


import com.football.kick_board.application.like.LikeService;
import com.football.kick_board.common.security.SecurityUtils;
import com.football.kick_board.domain.comment.Comment;
import com.football.kick_board.domain.member.Member;
import com.football.kick_board.domain.member.MemberRepository;
import com.football.kick_board.domain.post.Post;
import com.football.kick_board.domain.post.PostRepository;
import com.football.kick_board.web.comment.model.CommentResponse;
import com.football.kick_board.web.post.model.request.PostCreateRequest;
import com.football.kick_board.web.post.model.request.PostListRequest;
import com.football.kick_board.web.post.model.response.PostResponse;
import com.football.kick_board.web.post.model.request.PostUpdateRequest;
import java.util.ArrayList;
import java.util.List;
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


  //게시글 등록
  @Override
  @Transactional
  public PostResponse createdPost(PostCreateRequest requestDto) {
    String currentUserId = SecurityUtils.getCurrentUserId();
    Member author = memberRepository.findByUserId(currentUserId)
        .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다."));

    Post post = Post.builder()
        .title(requestDto.getTitle())
        .content(requestDto.getContent())
        .author(author)
        .build();

    Post savedPost = postRepository.save(post);
    return new PostResponse(savedPost, false, 0, new ArrayList<>());
  }

  //게시글 목록 조회
  @Override
  @Transactional(readOnly = true)
  public Page<PostResponse> getPosts(PostListRequest requestDto) {
    Pageable pageable = PageRequest.of(
        requestDto.getPage(),
        requestDto.getSize(),
        Sort.Direction.fromString(requestDto.getSortDirection()),
        requestDto.getSortBy()
    );
    Page<Post> postPage = postRepository.searchPosts(
        requestDto.getKeyword(),
        requestDto.getActiveStatus(),
        pageable
    );
    return postPage.map(post -> {
      boolean userLiked = likeService.isPostLikedByCurrentUser(post.getId());
      long likeCount = likeService.countPostLikes(post.getId());
      //목록 조회 시에는 댓글 목록은 비워둠
      return new PostResponse(post, userLiked, likeCount, new ArrayList<>());
    });
  }

  //게시글 상세 조회
  @Override
  @Transactional //TODO  조회수 증가(DB 변경)로 인해 트랜잭션 필요
  public PostResponse getPostById(Long postId) {
    Post post = postRepository.findByIdWithComments(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    if (!post.isActive()) {
      throw new IllegalArgumentException("삭제되었거나 비활성 상태인 게시글입니다.");
    }
    //조회 수 증가(데이터베이스에서 직접 업데이트)
    postRepository.incrementViewCount(postId);//데이터베이스에서 직접 조회수 증가
    post.incrementViewCountInMemory();

    //좋아요 정보 조회
    boolean userLiked = likeService.isPostLikedByCurrentUser(postId);
    long likeCount = likeService.countPostLikes(postId);
    //댓글 목록에 대한 좋아요 정보 조회
    List<CommentResponse> commentResponses = post.getComments().stream()
        .filter(Comment::isActive)
        .filter(comment -> comment.getParent() == null) // 최상위 댓글만
        .map(comment -> CommentResponse.from(comment, likeService))
        .collect(Collectors.toList());

    // 게시글 응답 객체 생성 (댓글 목록 포함)
    return new PostResponse(post, userLiked, likeCount, commentResponses);
  }

  //게시글 수정
  @Override
  @Transactional
  public PostResponse updatePost(Long postId, PostUpdateRequest requestDto) {
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

    boolean userLiked = likeService.isPostLikedByCurrentUser(postId);
    long likeCount = likeService.countPostLikes(postId);

    //댓글 목록 DTO반환(게시글 상세 조회와 동일 내용 포함으로 따로 빼서 사용해도 될듯 함.)
    List<CommentResponse> commentResponses = updatedPostFromDb.getComments().stream()
        .filter(Comment::isActive)
        .filter(comment -> comment.getParent() == null) // 최상위 댓글만 필터링
        .map(comment -> CommentResponse.from(comment, likeService))
        .collect(Collectors.toList());
    return new PostResponse(post, userLiked, likeCount, commentResponses);
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

