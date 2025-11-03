package com.football.kick_board.web.post.model.response;

import com.football.kick_board.domain.member.Member;
import com.football.kick_board.domain.post.Post;
import com.football.kick_board.web.comment.model.response.CommentResponse;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

  // ✨ 정적 팩토리 메소드 추가 ✨
  public static PostResponse from(Post post, boolean userLiked, long likeCount, List<CommentResponse> comments) {
    return new PostResponse(
        post.getId(),
        post.getTitle(),
        post.getContent(),
        post.getAuthor().getUserId(),
        post.getAuthor().getNickname(),
        post.getCreatedAt(),
        post.getUpdatedAt(),
        post.getViewCount(),
        post.isActive(),
        likeCount,
        userLiked,
        comments
    );
  }
}
