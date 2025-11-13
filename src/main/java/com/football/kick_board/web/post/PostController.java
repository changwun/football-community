package com.football.kick_board.web.post;

import com.football.kick_board.application.like.LikeService;
import com.football.kick_board.application.post.PostService;
import com.football.kick_board.common.exception.ErrorResponse;
import com.football.kick_board.common.security.SecurityUtils;
import com.football.kick_board.domain.comment.Comment;
import com.football.kick_board.domain.post.Post;
import com.football.kick_board.web.comment.model.response.CommentResponse;
import com.football.kick_board.web.post.model.request.PostCreateRequest;
import com.football.kick_board.web.post.model.request.PostListRequest;
import com.football.kick_board.web.post.model.response.PostResponse;
import com.football.kick_board.web.post.model.request.PostUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "2. 게시글 (Post) API", description = "게시글/용병모집글 작성조회, 수정, 삭제 API")
public class PostController {

  private final PostService postService;
  private final LikeService likeService;

  //게시글 목록 조회(검색, 페이징)
  @GetMapping
  @Operation(summary = "게시글 목록 조회", description = "전체 게시글(또는 용병글) 목록을 검색 조건과 함께 페이징하여 조회합니다. (비로그인 가능)")
  @ApiResponses(value = {
      // [!!] 200 OK는 Page<PostResponse>로 자동 추론되지만, 설명을 추가합니다.
      @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공")
  })
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
  @Operation(summary = "게시글 상세 조회", description = "게시글 ID(PK)로 특정 게시글을 상세 조회합니다. (비로그인 가능)\n\n- (성공 시) 해당 게시글의 **조회수가 1 증가**합니다.\n- 게시글의 좋아요 정보와 '최상위' 댓글 목록이 함께 반환됩니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "게시글 상세 조회 성공",
          content = @Content(schema = @Schema(implementation = PostResponse.class))),
      // GlobalExceptionHandler가 IllegalArgumentException을 409로 처리
      @ApiResponse(responseCode = "409", description = "조회 실패 (e.g., 존재하지 않는 게시글, 삭제된 게시글)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
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
  @Operation(summary = "게시글 생성", description = "(인증 필요) 새 게시글(일반/용병)을 생성합니다.\\n\\n- `boardType`에 따라(`GENERAL`/`MERCENARY`) `@Valid` 유효성 검사 그룹이 다르게 적용됩니다.")
  @ApiResponses(value = {
      // [!!] 201 Created는 PostResponse로 자동 추론되지만, 응답 코드를 명시합니다.
  @ApiResponse(responseCode = "201", description = "게시글 생성 성공",
      content = @Content(schema = @Schema(implementation = PostResponse.class))),
  @ApiResponse(responseCode = "400", description = "유효성 검증 실패 (e.g., 제목 누락, 용병 게시글의 위치 누락)",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
  @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료)",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
  @ApiResponse(responseCode = "409", description = "작성자 정보(Member)를 찾을 수 없음",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
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
  @Operation(summary = "게시글 수정", description = "(인증 필요) **게시글 작성자 본인**만 게시글의 제목과 내용을 수정할 수 있습니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "게시글 수정 성공",
          content = @Content(schema = @Schema(implementation = PostResponse.class))),
      @ApiResponse(responseCode = "400", description = "유효성 검증 실패 (e.g., 제목 누락)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "수정 실패 (e.g., 존재하지 않는 글, 이미 삭제된 글, **수정 권한 없음**)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
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
  @Operation(summary = "게시글 삭제", description = "(인증 필요) **게시글 작성자 본인**만 게시글을 삭제(비활성)할 수 있습니다.")
  @ApiResponses(value = {
      // [!!] ResponseEntity<?> 타입이므로, 성공 응답(200)을 수동으로 명시
      @ApiResponse(responseCode = "200", description = "게시글 삭제 성공",
          content = @Content(schema = @Schema(example = "{\"success\": true, \"message\": \"게시글이 삭제 되었습니다.\"}"))),
      @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "삭제 실패 (e.g., 존재하지 않는 글, 이미 삭제된 글, **삭제 권한 없음**)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<?> deletePost(@PathVariable Long postId) {
    postService.deletePost(postId);
    log.info("게시글 삭제 요청: postId={},userId={}", postId, SecurityUtils.getCurrentUserId());

    return ResponseEntity.ok().body(Map.of("success", true, "message", "게시글이 삭제 되었습니다."));
  }


}
