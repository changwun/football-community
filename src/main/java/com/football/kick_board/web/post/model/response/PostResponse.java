package com.football.kick_board.web.post.model.response;

import com.football.kick_board.domain.post.Post;
import com.football.kick_board.web.comment.model.CommentResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostResponse {

  private Long id;
  private String title;
  private String content;
  private String authorUserId; //작성자의 userId
  private String authorNickname; // 작성자의 닉네임
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private int viewCount;
  private boolean active; //게시글 활성 상태

  private long likeCount;
  private boolean userLiked;

  private List<CommentResponse> comments;

  public PostResponse(Post post, boolean userLiked, long likeCount,
      List<CommentResponse> comments) {
    this.id = post.getId();
    this.title = post.getTitle();
    this.content = post.getContent();
    this.authorUserId = post.getAuthor().getUserId(); // Member 엔티티의 userId 가져오기
    this.authorNickname = post.getAuthor().getNickname(); // Member 엔티티의 nickname 가져오기
    this.createdAt = post.getCreatedAt();
    this.updatedAt = post.getUpdatedAt();
    this.viewCount = post.getViewCount();
    this.active = post.isActive();
    // 좋아요 정보 설정
    this.userLiked = userLiked;
    this.likeCount = likeCount;
    //좋아요 정보가 포함된 댓글 목록
    this.comments = comments;

  }
}
