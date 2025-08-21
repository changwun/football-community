package com.football.KickBoard.web.post.model.response;

import com.football.KickBoard.domain.post.Post;
import java.time.LocalDateTime;
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

  public PostResponse(Post post) {
    this.id = post.getId();
    this.title = post.getTitle();
    this.content = post.getContent();
    this.authorUserId = post.getAuthor().getUserId(); // Member 엔티티의 userId 가져오기
    this.authorNickname = post.getAuthor().getNickname(); // Member 엔티티의 nickname 가져오기
    this.createdAt = post.getCreatedAt();
    this.updatedAt = post.getUpdatedAt();
    this.viewCount = post.getViewCount();
    this.active = post.isActive();
  }

}
