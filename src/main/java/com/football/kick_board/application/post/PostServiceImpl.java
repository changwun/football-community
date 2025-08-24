package com.football.kick_board.application.post;

import com.football.kick_board.common.security.SecurityUtils;
import com.football.kick_board.domain.member.Member;
import com.football.kick_board.domain.member.MemberRepository;
import com.football.kick_board.domain.post.Post;
import com.football.kick_board.domain.post.PostRepository;
import com.football.kick_board.web.post.model.request.PostCreateRequest;
import com.football.kick_board.web.post.model.request.PostListRequest;
import com.football.kick_board.web.post.model.response.PostResponse;
import com.football.kick_board.web.post.model.request.PostUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor// final 필드들에 대한 생성자 자동 생성 및 의존성 주입
public class PostServiceImpl implements PostService{

  private final PostRepository postRepository;
  private final MemberRepository memberRepository;

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
    return new PostResponse(savedPost);
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
    return postPage.map(PostResponse::new);
  }

  //게시글 상세 조회
  @Override
  @Transactional
  public PostResponse getPostById(Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    if (!post.isActive()) {
      throw new IllegalArgumentException("삭제되었거나 비활성 상태인 게시글입니다.");
    }
    postRepository.incrementViewCount(postId);//데이터베이스에서 직접 조회수 증가
    post.incrementViewCountInMemory();
    return new PostResponse(post);

  }

  //게시글 수정
  @Override
  @Transactional
  public PostResponse updatePost(Long postId,PostUpdateRequest requestDto) {
    String currentUserId = SecurityUtils.getCurrentUserId();
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    if (!post.isActive()) {
      throw new IllegalArgumentException("삭제되었거나 비활성 상태인 게시글은 수정할 수 없습니다.");
    }
    if (!post.getAuthor().getUserId().equals(currentUserId)) {
      throw new IllegalArgumentException("게시글을 수정할 권한이 없습니다.");
    }
    post.update(requestDto.getTitle(), requestDto.getContent());
    return new PostResponse(post);
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

