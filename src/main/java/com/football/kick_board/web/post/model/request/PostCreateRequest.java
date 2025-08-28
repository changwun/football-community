package com.football.kick_board.web.post.model.request;

import com.football.kick_board.common.validation.ValidationGroups;
import com.football.kick_board.domain.post.BoardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {

  @NotBlank(message = "제목은 필수 입력 사항입니다.", groups = {ValidationGroups.GeneralPost.class, ValidationGroups.MercenaryPost.class})
  @Size(max = 100, message = "제목은 최대 100자까지 가능합니다.", groups = {ValidationGroups.GeneralPost.class, ValidationGroups.MercenaryPost.class})
  private String title;

  @NotBlank(message = "내용은 필수입니다.", groups = {ValidationGroups.GeneralPost.class, ValidationGroups.MercenaryPost.class})
  @Size(max = 2000, message = "내용은 최대 2000자까지 가능합니다.", groups = {ValidationGroups.GeneralPost.class, ValidationGroups.MercenaryPost.class})
  private String content;

  @NotNull(message = "게시글 유형은 필수입니다.", groups = {ValidationGroups.GeneralPost.class, ValidationGroups.MercenaryPost.class})
  private BoardType boardType;

  //용병 모집 게시글 (MercenaryPost)일 때만 필수, 일반 게시글 (GeneralPost)일 때는 Null이어야 함
  @NotBlank(message = "경기 지역은 필수입니다.", groups = ValidationGroups.MercenaryPost.class)
  @Null(message = "경기 지역은 일반 게시글에서는 필요하지 않습니다.", groups = ValidationGroups.GeneralPost.class)
  private String location;

  @NotNull(message = "경기 시간은 필수입니다.", groups = ValidationGroups.MercenaryPost.class)
  @Null(message = "경기 시간은 일반 게시글에서는 필요하지 않습니다.", groups = ValidationGroups.GeneralPost.class)
  private LocalDateTime matchTime;

  @NotBlank(message = "필요 포지션은 필수입니다.", groups = ValidationGroups.MercenaryPost.class)
  @Null(message = "필요 포지션은 일반 게시글에서는 필요하지 않습니다.", groups = ValidationGroups.GeneralPost.class)
  private String position;

  @NotNull(message = "필요 인원수는 필수입니다.", groups = ValidationGroups.MercenaryPost.class)
  @Positive(message = "필요 인원수는 양수여야 합니다.", groups = ValidationGroups.MercenaryPost.class)
  @Null(message = "필요 인원수는 일반 게시글에서는 필요하지 않습니다.", groups = ValidationGroups.GeneralPost.class)
  private Integer neededPersonnel;

}
