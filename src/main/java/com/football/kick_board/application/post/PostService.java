package com.football.kick_board.application.post;


import com.football.kick_board.web.post.model.request.PostCreateRequest;
import com.football.kick_board.web.post.model.request.PostListRequest;
import com.football.kick_board.web.post.model.response.PostResponse;
import com.football.kick_board.web.post.model.request.PostUpdateRequest;
import org.springframework.data.domain.Page;

public interface PostService {

  //게시글 등록
  PostResponse createdPost(PostCreateRequest requestDto);

  //게시글 목록 조회(검색 및 페이징 포함)
  Page<PostResponse> getPosts(PostListRequest requestDto);

  //게시글 수정
  PostResponse updatePost(Long postId, PostUpdateRequest requestDto);

  //게시글 상세 조회(조회 수 증가 포함)
  PostResponse getPostById(Long postId);

  //게시글 삭제(소프트 삭제)
  void deletePost(Long postId);
}
