package com.football.kick_board.web.match.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResponse {
  private Long matchId;
  private String matchDate; // 또는 ZonedDateTime 타입으로 변환 가능
  private String status;
  private String competitionName;
  private String homeTeamName;
  private String awayTeamName;
  private Integer homeScore;
  private Integer awayScore;
  private Integer matchday;
  private String stage;
  private String group;
  private String homeCrest;
  private String awayCrest;
}
