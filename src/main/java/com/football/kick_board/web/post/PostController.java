package com.football.kick_board.web.post;

import com.football.kick_board.application.like.LikeService;
import com.football.kick_board.application.post.PostService;
import com.football.kick_board.common.security.SecurityUtils;
import com.football.kick_board.domain.comment.Comment;
import com.football.kick_board.domain.post.Post;
import com.football.kick_board.web.comment.model.response.CommentResponse;
import com.football.kick_board.web.post.model.request.PostCreateRequest;
import com.football.kick_board.web.post.model.request.PostListRequest;
import com.football.kick_board.web.post.model.response.PostResponse;
import com.football.kick_board.web.post.model.request.PostUpdateRequest;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;
  private final LikeService likeService;

  //게시글 목록 조회(검색, 페이징)
  @GetMapping
  public ResponseEntity<Page<PostResponse>> getPosts(
      @ModelAttribute PostListRequest requestDto) {
    Page<Post> postPage = postService.getPosts(requestDto);
    log.info("게시글 목록 조회 요청: 페이지={},크기={},키워드='{}'",
        requestDto.getPage(), requestDto.getSize(), requestDto.getKeyword());
    Page<PostResponse> responsePage = postPage.map(post -> {
      boolean userLiked = likeService.isPostLikedByCurrentUser(post.getId());
      long likeCount = likeService.countPostLikes(post.getId());
      //목록 조회 시에는 댓글은 빈 리스트로 제공
      return PostResponse.from(post,userLiked,likeCount,new ArrayList<>());
    });
    return ResponseEntity.ok(responsePage);
  }

  //게시글 상세 조회(조회수 증가)
  @GetMapping("/{postId}")
  public ResponseEntity<PostResponse> getPostById(@PathVariable Long postId) {
    Post post = postService.getPostById(postId);
    log.info("게시글 상세 조회 요청: postId={}", postId);

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
    PostResponse response = PostResponse.from(post,userLiked,likeCount,commentResponses);
    return ResponseEntity.ok(response);
  }

  //게시글 등록
  @PreAuthorize("isAuthenticated()")
  @PostMapping
  public ResponseEntity<PostResponse> createPost(@RequestBody @Valid PostCreateRequest request) {
    Post post = postService.createdPost(request);
    log.info("게시글 등록 요청: boardType={},userId{}, 제목={}", request.getBoardType(),SecurityUtils.getCurrentUserId(), request.getTitle());

    //컨트롤러에서 DTO 변환
    boolean userLiked = false;
    long likeCount = 0;
    List<CommentResponse> comments = new ArrayList<>();

    PostResponse response = PostResponse.from(post,userLiked,likeCount,comments);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  //게시글 수정
  @PutMapping("/{postId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<PostResponse> updatePost(
      @PathVariable Long postId,
      @RequestBody @Valid PostUpdateRequest requestDto) {
    Post updatedPost = postService.updatePost(postId,requestDto);
    log.info("게시글 수정 요청: postId={}, userId={}", postId, SecurityUtils.getCurrentUserId());

    boolean userLiked = likeService.isPostLikedByCurrentUser(postId);
    long likeCount = likeService.countPostLikes(postId);

    //댓글 목록 DTO반환(게시글 상세 조회와 동일 내용 포함으로 따로 빼서 사용해도 될듯 함.)
    List<CommentResponse> commentResponses = updatedPost.getComments().stream()
        .filter(Comment::isActive)
        .filter(comment -> comment.getParent() == null) // 최상위 댓글만 필터링
        .map(comment -> CommentResponse.from(comment, likeService))
        .collect(Collectors.toList());

    PostResponse response = PostResponse.from(updatedPost, userLiked,likeCount,commentResponses);
    return ResponseEntity.ok(response);
  }



  //게시글 삭제
  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/{postId}")
  public ResponseEntity<?> deletePost(@PathVariable Long postId) {
    postService.deletePost(postId);
    log.info("게시글 삭제 요청: postId={},userId={}", postId, SecurityUtils.getCurrentUserId());

    return ResponseEntity.ok().body(Map.of("success", true, "message", "게시글이 삭제 되었습니다."));
  }


}
