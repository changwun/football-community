package com.football.kick_board.web.post;

import com.football.kick_board.application.post.PostService;
import com.football.kick_board.common.security.SecurityUtils;
import com.football.kick_board.web.post.model.request.PostCreateRequest;
import com.football.kick_board.web.post.model.request.PostListRequest;
import com.football.kick_board.web.post.model.response.PostResponse;
import com.football.kick_board.web.post.model.request.PostUpdateRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
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

  //게시글 목록 조회(검색, 페이징)
  @GetMapping
  public ResponseEntity<Page<PostResponse>> getPosts(
      @ModelAttribute PostListRequest requestDto) {
    log.info("게시글 목록 조회 요청: 페이지={},크기={},키워드='{}'",
        requestDto.getPage(), requestDto.getSize(), requestDto.getKeyword());
    Page<PostResponse> posts = postService.getPosts(requestDto);
    return ResponseEntity.ok(posts);
  }

  //게시글 상세 조회(조회수 증가)
  @GetMapping("/{postId}")
  public ResponseEntity<PostResponse> getPostById(@PathVariable Long postId) {
    log.info("게시글 상세 조회 요청: postId={}", postId);

    PostResponse post = postService.getPostById(postId);
    return ResponseEntity.ok(post);
  }

  //게시글 등록
  @PreAuthorize("isAuthenticated()")
  @PostMapping
  public ResponseEntity<PostResponse> createPost(@RequestBody @Valid PostCreateRequest requestDto) {
    log.info("게시글 등록 요청: userId{}, 제목={}", SecurityUtils.getCurrentUserId(), requestDto.getTitle());

    PostResponse createdPost = postService.createdPost(requestDto);
    return ResponseEntity.ok(createdPost);
  }

  //게시글 수정
  @PreAuthorize("isAuthenticated()")
  @PutMapping("/{postId}")
  public ResponseEntity<PostResponse> updatePost(
      @PathVariable Long postId,
      @RequestBody @Valid PostUpdateRequest requestDto) {

    log.info("게시글 수정 요청: postId={}, userId={}", postId, SecurityUtils.getCurrentUserId());

    PostResponse updatedPost = postService.updatePost(postId, requestDto);
    return ResponseEntity.ok(updatedPost);
  }

  //게시글 삭제
  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/{postId}")
  public ResponseEntity<?> deletePost(@PathVariable Long postId) {

    log.info("게시글 삭제 요청: postId={},userId={}", postId, SecurityUtils.getCurrentUserId());

    postService.deletePost(postId);
    return ResponseEntity.ok().body(Map.of("success", true, "message", "게시글이 삭제 되었습니다."));
  }


}
