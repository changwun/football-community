package com.football.KickBoard.web.post.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {

  @NotBlank(message = "제목은 필수입니다.")
  @Size(max = 100, message = "제목은 최대 100자까지 가능합니다.")
  private String title;

  @NotBlank(message = "내용은 필수입니다.")
  @Size(max = 2000, message = "내용은 최대 2000자까지 가능합니다.")
  private String content;

}
